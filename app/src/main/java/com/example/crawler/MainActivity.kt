package com.example.crawler

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.crawler.databinding.ActivityMainBinding
import org.jsoup.Connection
import org.jsoup.Jsoup
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        CoroutineScope(Dispatchers.Main).launch {
            val loginInfoArray = withContext(Dispatchers.IO) {
                getLoginInfoArray()
            }
            // 작업 배열에 있는 각 작업을 코루틴으로 실행
            loginInfoArray.forEach {
                withContext(Dispatchers.IO) {
                    loginAndNavigate(it)
                }
            }
        }

    }

    suspend fun getLoginInfoArray(): Array<LoginInfo> {
        // 외부 api를 호출하여 작업 배열을 받아온다는 가정
        val loginInfoArray = arrayOf(
            LoginInfo(0, "0dong@tpxlab.com", "0dong", "https://dropbase.api.battle.zodium.io/auth/signin"),
            LoginInfo(1, "1dong@tpxlab.com", "1dong", "https://dropbase.api.battle.zodium.io/auth/signin"),
            LoginInfo(2, "2dong@tpxlab.com", "2dong", "https://dropbase.api.battle.zodium.io/auth/signin"),
        )
        return loginInfoArray
    }

    private suspend fun loginAndNavigate(loginInfo: LoginInfo) {
        val url = loginInfo.uri
        val formData = hashMapOf("email" to loginInfo.id, "password" to loginInfo.pw)
        val response = Jsoup.connect(url)
            .data(formData)
            .method(Connection.Method.POST)
            .ignoreContentType(true)
            .execute()


        val gson = Gson()
        val json = response.body()
        val resultAccessToken = gson.fromJson(json, AccessToken::class.java)

        val cookies = response.cookies()
        val refreshToken = response.cookie("refreshToken")
        Log.d("body",resultAccessToken.accessToken)
        Log.d("cookies",cookies.toString())

        val url2 = "https://dropbase.api.battle.zodium.io/users/myinfo"
        val response2 = Jsoup.connect(url2)
            .header("Authorization", "Bearer ${resultAccessToken.accessToken}")
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .method(Connection.Method.GET)
            .ignoreContentType(true)
            .execute()

        Log.d("body2",response2.body())



        withContext(Dispatchers.Main) {
            if (loginInfo.index == 0) {
                binding.top.text = resultAccessToken.accessToken
            }
            if (loginInfo.index == 1) {
                binding.middle.text =  resultAccessToken.accessToken
            }
            if (loginInfo.index == 2) {
                binding.bottom.text =  resultAccessToken.accessToken
            }
            
        }
    }


}
data class AccessToken(
    @SerializedName("accessToken")
    val accessToken: String
)

data class LoginInfo(
    val index : Int,
    val id: String,
    val pw: String,
    val uri: String
)