package com.example.bgls.Retrofit

import com.example.bgls.Retrofit.ServiceApi
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
object RetrofitClient {

    // private const val BASE_URL = "http://10.0.2.2:8080/ASPIRA/"
    private const val BASE_URL = "https://twirl-bubbly-gusty.ngrok-free.dev/ASPIRA/"
    private val cookieJar = PersistentCookieJar()
    private val client: OkHttpClient by lazy {

        val username = "EMP04"
        val password = "Bornfire@123"

        val authHeader = Credentials.basic(username, password)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(logging)
            .cookieJar(cookieJar)// 👈 ADD THIS
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", authHeader)
                    .addHeader("Accept", "application/json")
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()
                chain.proceed(request)
            })
            .build()
    }

    val api: ServiceApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ServiceApi::class.java)
    }
}
class PersistentCookieJar : CookieJar {
    private val cookies = mutableSetOf<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies.filter { it.matches(url) }
    }
}