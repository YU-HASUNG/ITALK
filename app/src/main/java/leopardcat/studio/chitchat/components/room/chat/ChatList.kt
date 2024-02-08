package leopardcat.studio.chitchat.components.room.chat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatList(

    @PrimaryKey(autoGenerate = false)
    val name: String,

    @ColumnInfo
    val message: List<String>,

    @ColumnInfo
    val time: List<String>,

    @ColumnInfo
    val direction: List<Boolean>
)