package leopardcat.studio.chitchat.components.api.client

import leopardcat.studio.chitchat.components.api.service.GPTService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class GPTClient {
    companion object {
        private const val GPT_URL = "https://api.openai.com/v1/chat/"

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        fun getGPTApi(): GPTService = Retrofit.Builder()
            .baseUrl(GPT_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GPTService::class.java)
    }
}