package com.kampplus.hava.ui.component

import androidx.annotation.DrawableRes
import com.kampplus.hava.R
import com.kampplus.hava.model.WeatherCondition

/*
 * Hava kodlarının altı görsel gruba ayrılması ve her grubun hazır ikon dosyası (res/drawable/ic_wx_*.xml).
 * Üst alan rengi de (ui/home/WeatherStyle.kt) aynı [weatherGroup] fonksiyonunu kullanır; böylece ikon ile renk hiç ayrışmaz.
 * İkonlar çok renkli olduğu için Icon ile değil, Image(painterResource(...)) ile ve tint vermeden gösterilir.
 */

/** Ekrandaki altı hava grubu: hem üst alan rengini hem ikonu belirler. */
enum class WeatherGroup { CLEAR, PARTLY, CLOUDY, RAIN, SNOW, STORM }

/**
 * WMO kodu → grup: 0 açık · 1, 2 parçalı · 3, 45, 48 kapalı/sisli · 51–67, 80–82 yağmur ·
 * 71–77, 85, 86 kar · 95–99 fırtına. Tanınmayan kod nötr gruba (parçalı) düşer.
 */
fun weatherGroup(condition: WeatherCondition): WeatherGroup = when (condition) {
    WeatherCondition.CLEAR -> WeatherGroup.CLEAR
    WeatherCondition.MAINLY_CLEAR, WeatherCondition.PARTLY_CLOUDY, WeatherCondition.UNKNOWN -> WeatherGroup.PARTLY
    WeatherCondition.OVERCAST, WeatherCondition.FOG -> WeatherGroup.CLOUDY
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS -> WeatherGroup.RAIN
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> WeatherGroup.SNOW
    WeatherCondition.THUNDERSTORM -> WeatherGroup.STORM
}

/** Ana ekranın renkli üst alanındaki ikon. */
@DrawableRes
fun headerIcon(condition: WeatherCondition): Int = when (weatherGroup(condition)) {
    WeatherGroup.CLEAR -> R.drawable.ic_wx_clear
    WeatherGroup.PARTLY -> R.drawable.ic_wx_partly
    WeatherGroup.CLOUDY -> R.drawable.ic_wx_cloudy
    WeatherGroup.RAIN -> R.drawable.ic_wx_rain
    WeatherGroup.SNOW -> R.drawable.ic_wx_snow
    WeatherGroup.STORM -> R.drawable.ic_wx_storm
}

/** Beyaz kartlardaki ikon (saatlik, 7 günlük, favoriler, Konumum). */
@DrawableRes
fun cardIcon(condition: WeatherCondition): Int = when (weatherGroup(condition)) {
    WeatherGroup.CLEAR -> R.drawable.ic_wx_clear_card
    WeatherGroup.PARTLY -> R.drawable.ic_wx_partly_card
    WeatherGroup.CLOUDY -> R.drawable.ic_wx_cloudy_card
    WeatherGroup.RAIN -> R.drawable.ic_wx_rain_card
    WeatherGroup.SNOW -> R.drawable.ic_wx_snow_card
    WeatherGroup.STORM -> R.drawable.ic_wx_storm_card
}
