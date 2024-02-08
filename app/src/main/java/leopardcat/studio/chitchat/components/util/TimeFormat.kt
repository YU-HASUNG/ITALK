package leopardcat.studio.chitchat.components.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertToAmPmFormat(context: Context, inputDateTime: String): String {
    val inputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val userLocale: Locale = context.resources.configuration.locale
    val outputFormat = SimpleDateFormat("a hh:mm", userLocale)
    val dateTime = inputFormat.parse(inputDateTime)
    return if(dateTime == null){
        inputDateTime
    } else {
        outputFormat.format(dateTime)
    }
}

fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val currentTime = Date()
    return dateFormat.format(currentTime)
}

fun isToday(dateString: String): Boolean {
    val currentTime = Date()

    // SimpleDateFormat을 사용하여 날짜 문자열을 Date 객체로 변환합니다
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val targetDate = dateFormat.parse(dateString)

    // 현재 날짜와 목표 날짜를 비교하여 같은지 확인합니다
    return dateFormat.format(currentTime) == targetDate?.let { dateFormat.format(it) }
}

fun convertMillisToTime(millis: Long): String {
    val seconds = millis / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secondsRemaining = seconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, secondsRemaining)
}

