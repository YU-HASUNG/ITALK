package leopardcat.studio.chitchat.components.data


import android.os.Parcelable
import androidx.annotation.DrawableRes
import leopardcat.studio.chitchat.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person (
    val id: Int = 0,
    val name: String = "", //캐릭터 이름
    val explain: String ="", //캐릭터 설명
    val starId: String ="", //인스타 ID
    val follower: String ="", //팔로워
    var lastChat: String ="", //마지막 대화시간
    var lock: Boolean = false,
    @DrawableRes val icon: Int = R.drawable.default_image
) : Parcelable