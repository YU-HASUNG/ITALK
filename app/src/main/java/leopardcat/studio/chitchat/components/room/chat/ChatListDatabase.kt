package leopardcat.studio.chitchat.components.room.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(StringListConverter::class, BooleanListConverter::class)
@Database(
    entities = [ChatList::class],
    version = 3
)

abstract class ChatListDatabase: RoomDatabase() {

    abstract val chatInfoDao: ChatListDao
}