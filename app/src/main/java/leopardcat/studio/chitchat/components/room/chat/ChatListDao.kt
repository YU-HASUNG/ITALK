package leopardcat.studio.chitchat.components.room.chat

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ChatListDao {

    @Upsert
    suspend fun upsertChatList(chatList: ChatList)

    @Delete
    suspend fun deleteChatList(chatList: ChatList)

    @Query("SELECT * FROM chatList WHERE name = :name")
    suspend fun getChatListByName(name: String): ChatList

    @Query("SELECT time FROM chatList WHERE name = :name")
    suspend fun getLastChatTimeByName(name: String): String?

    @Query("DELETE FROM chatList WHERE name = :name")
    suspend fun deleteChatListByName(name: String)
}