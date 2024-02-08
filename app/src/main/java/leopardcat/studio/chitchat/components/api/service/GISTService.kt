package leopardcat.studio.chitchat.components.api.service

import io.reactivex.Single
import leopardcat.studio.chitchat.components.api.model.ImageData
import retrofit2.http.GET
import retrofit2.http.Path

interface GISTService {
    @GET("{info}")
    fun getRepos(@Path("info") info: String): Single<ImageData>
}