package leopardcat.studio.chitchat.components.room.chat

import androidx.room.TypeConverter

class BooleanListConverter {
    @TypeConverter
    fun fromBooleanList(value: List<Boolean>?): String? {
        return value?.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toBooleanList(value: String?): List<Boolean>? {
        return value?.split(",")?.map { it.toBoolean() }
    }
}