package leopardcat.studio.chitchat.components.api.model

import com.google.gson.annotations.SerializedName

data class ImageData(
    @SerializedName("json") val imageList: List<ImageItem>
)

data class ImageItem(
    @SerializedName("image") val image: List<ImageDetail>
)

data class ImageDetail(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String
)