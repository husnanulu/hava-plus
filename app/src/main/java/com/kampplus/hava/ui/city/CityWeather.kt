package com.kampplus.hava.ui.city

import com.kampplus.hava.model.City
import com.kampplus.hava.model.CurrentWeather

/**
 * Şehir seç ekranındaki bir satır: şehir + anlık hava.
 * Anlık hava henüz gelmediyse ya da istek başarısız olduysa [current] null'dur; satır yine görünür.
 * Kamp konusu: data class (Uygulama 2).
 */
data class CityWeather(val city: City, val current: CurrentWeather? = null)
