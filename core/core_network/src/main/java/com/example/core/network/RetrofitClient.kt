package com.example.core.network

import com.example.core.network.NetworkConfig.BASE_URL
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * description ： retrofit客户端
 * email : 3014386984@qq.com
 * date : 2026/7/14 17:57
 */
object RetrofitClient {
    val gson = Gson()

    /** 仅 debug 构建输出报文，release 一律 NONE，不再泄露请求/响应体与认证头。 */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    /** 附加客户端凭据；值为空（未配置 secrets.properties）时省略对应请求头，不发送空头。 */
    private val cookieInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        NetworkConfig.X_API_KEY.takeIf { it.isNotBlank() }?.let { builder.header("x-api-key", it) }
        NetworkConfig.X_THEFAIR_APPID.takeIf { it.isNotBlank() }?.let { builder.header("X-THEFAIR-APPID", it) }
        NetworkConfig.X_THEFAIR_AUTH.takeIf { it.isNotBlank() }?.let { builder.header("X-THEFAIR-AUTH", it) }
        NetworkConfig.X_THEFAIR_CID.takeIf { it.isNotBlank() }?.let { builder.header("X-THEFAIR-CID", it) }
        NetworkConfig.CLIENT_UA.takeIf { it.isNotBlank() }?.let { builder.header("User-Agent", it) }
        chain.proceed(builder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(cookieInterceptor)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> create(): T = retrofit.create(T::class.java)

    fun resolvePlayUrl(redirectUrl: String): String {
        return try {
            val client = okHttpClient.newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
            val request = okhttp3.Request.Builder().url(redirectUrl).build()
            val response = client.newCall(request).execute()
            response.request.url.toString()
        } catch (e: Exception) {
            redirectUrl
        }
    }
}
