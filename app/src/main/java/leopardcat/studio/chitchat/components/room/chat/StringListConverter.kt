package leopardcat.studio.chitchat.components.room.chat

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun toList(string: String?): List<String>? {
        return string?.split(",")?.toList()
    }
}