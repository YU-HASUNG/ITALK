package leopardcat.studio.chitchat.components.util

import android.content.Context
import android.content.SharedPreferences


class Preference(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)
    private val prefContext = context

    /***
     * Heart 개수
     ***/
    fun setHeart(key: String, heart: Int) {
        prefs.edit().putInt(key, heart).apply()
    }
    fun getHeart(key: String): Int {
        return prefs.getInt(key, 8)
    }

    /***
     * vip 시간
     ***/
    fun setVipTime(key: String, time: Long) {
        prefs.edit().putLong(key, time).apply()
    }
    fun getVipTime(key: String): Long {
        return prefs.getLong(key, 0L)
    }

    /***
     * 구독 여부
     ***/
    fun setSubscribeMonthly(key: String, isMonthly: Boolean) {
        prefs.edit().putBoolean(key, isMonthly).apply()
    }
    fun getSubscribeMonthly(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }
    fun setSubscribeYearly(key: String, isYearly: Boolean) {
        prefs.edit().putBoolean(key, isYearly).apply()
    }
    fun getSubscribeYearly(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }
    fun setSubscribeYearly24(key: String, isYearly24: Boolean) {
        prefs.edit().putBoolean(key, isYearly24).apply()
    }
    fun getSubscribeYearly24(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }
}