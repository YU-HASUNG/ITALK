package leopardcat.studio.chitchat.components.api.service

import io.reactivex.Single
import leopardcat.studio.chitchat.components.api.model.GPTRepo
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GPTService {
    @POST("completions")
    fun getGPTRepos(
        @Header("Content-Type") contentType: String,
        @Header("Authorization") authorization: String,
        @Body body: RequestBody
    ) : Single<GPTRepo>
}