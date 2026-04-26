package io.github.jqssun.gpssetter.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import io.github.jqssun.gpssetter.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val START = "start"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
        private const val HOOKED_SYSTEM = "system_hooked"
        private const val RANDOM_POSITION = "random_position"
        private const val ACCURACY_SETTING = "accuracy_level"
        private const val MAP_TYPE = "map_type"
        private const val DARK_THEME = "dark_theme"
        private const val DISABLE_UPDATE = "update_disabled"
        private const val ENABLE_JOYSTICK = "joystick_enabled"
    }

    private val pref: SharedPreferences by lazy {
        val prefsFile = "${BuildConfig.APPLICATION_ID}_prefs"
        context.getSharedPreferences(prefsFile, Context.MODE_PRIVATE)
    }

    private val _isStarted = MutableStateFlow(pref.getBoolean(START, false))
    val isStarted: StateFlow<Boolean> = _isStarted

    private val _getLat = MutableStateFlow(pref.getFloat(LATITUDE, 40.7128F).toDouble())
    val getLat: StateFlow<Double> = _getLat

    private val _getLng = MutableStateFlow(pref.getFloat(LONGITUDE, -74.0060F).toDouble())
    val getLng: StateFlow<Double> = _getLng

    private val _mapType = MutableStateFlow(pref.getInt(MAP_TYPE, 1))
    val mapType: StateFlow<Int> = _mapType

    var isSystemHooked: Boolean
        get() = pref.getBoolean(HOOKED_SYSTEM, false)
        set(value) { pref.edit().putBoolean(HOOKED_SYSTEM, value).apply() }

    var isRandomPosition: Boolean
        get() = pref.getBoolean(RANDOM_POSITION, false)
        set(value) { pref.edit().putBoolean(RANDOM_POSITION, value).apply() }

    var accuracy: String?
        get() = pref.getString(ACCURACY_SETTING, "10")
        set(value) { pref.edit().putString(ACCURACY_SETTING, value).apply() }

    var darkTheme: Int
        get() = pref.getInt(DARK_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = pref.edit().putInt(DARK_THEME, value).apply()

    var isUpdateDisabled: Boolean
        get() = pref.getBoolean(DISABLE_UPDATE, false)
        set(value) = pref.edit().putBoolean(DISABLE_UPDATE, value).apply()

    var isJoystickEnabled: Boolean
        get() = pref.getBoolean(ENABLE_JOYSTICK, false)
        set(value) = pref.edit().putBoolean(ENABLE_JOYSTICK, value).apply()

    fun update(start: Boolean, la: Double, ln: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            pref.edit().apply {
                putFloat(LATITUDE, la.toFloat())
                putFloat(LONGITUDE, ln.toFloat())
                putBoolean(START, start)
                apply()
            }
            _isStarted.value = start
            _getLat.value = la
            _getLng.value = ln
        }
    }
}
