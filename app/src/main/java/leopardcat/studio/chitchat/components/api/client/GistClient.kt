package leopardcat.studio.chitchat.components.api.client

import leopardcat.studio.chitchat.components.api.service.GISTService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class GistClient {
    companion object {
        private const val GIST_URL = "https://gist.githubusercontent.com/YU-HASUNG/41caffb9876dd08c406889c253e643a0/raw/90aa1047c7430642245a65e16a188742566b61e8/"

        fun getGISTApi(): GISTService = Retrofit.Builder()
            .baseUrl(GIST_URL)
            .client(OkHttpClient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GISTService::class.java)
    }
}