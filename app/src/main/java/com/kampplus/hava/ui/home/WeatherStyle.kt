package com.kampplus.hava.ui.home

import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.component.WeatherGroup
import com.kampplus.hava.ui.component.weatherGroup
import com.kampplus.hava.ui.theme.HavaInk
import com.kampplus.hava.ui.theme.HavaYellow
import com.kampplus.hava.ui.theme.SkyStyle
import com.kampplus.hava.ui.theme.WeatherSky

/*
 * Ana ekranın havaya göre değişen görünüm kuralları (docs/HAVAPLUS_TASARIM.md → 2.1 tablosu).
 * Gökyüzü renklerinin kendisi ui/theme/WeatherSky.kt'de; hava → grup eşlemesi ui/component/WeatherIcons.kt'de.
 * Compose çizimi yok, yalnızca karar veren küçük fonksiyonlar var; bu yüzden unit test edilebilir.
 */

/** Üst alanın gökyüzü stili (geçiş renkleri, yazı renkleri, derece halkası). İkonla aynı [weatherGroup]'tan gelir. */
fun skyStyle(condition: WeatherCondition): SkyStyle = when (weatherGroup(condition)) {
    WeatherGroup.CLEAR -> WeatherSky.Clear
    WeatherGroup.PARTLY -> WeatherSky.PartlyCloudy
    WeatherGroup.CLOUDY -> WeatherSky.Overcast
    WeatherGroup.RAIN -> WeatherSky.Rain
    WeatherGroup.SNOW -> WeatherSky.Snow
    WeatherGroup.STORM -> WeatherSky.Storm
}

/** Konum izni bandının renkleri: bandın zemini ve yazısı, "Konumumu kullan" butonunun zemini ve yazısı. */
data class LocationBannerColors(val background: Color, val text: Color, val buttonBackground: Color, val buttonText: Color)

/**
 * Renk koduna değil zeminin açık/koyu olmasına ([SkyStyle.isDark]) bakar.
 * Açık zeminde koyu buton + beyaz yazı; koyu zeminde marka sarısı buton + koyu yazı.
 */
fun locationBannerColors(sky: SkyStyle): LocationBannerColors = if (sky.isDark) {
    LocationBannerColors(
        background = Color.White.copy(alpha = 0.14f),
        text = sky.sub,
        buttonBackground = HavaYellow,
        buttonText = HavaInk
    )
} else {
    LocationBannerColors(
        background = HavaInk.copy(alpha = 0.08f),
        text = sky.sub,
        buttonBackground = HavaInk,
        buttonText = Color.White
    )
}

/** 7 günlük satırdaki mavi çubuğun yeri: [start] soldaki boşluk, [width] çubuğun boyu (0..1 arası oran). */
data class RangeBarPosition(val start: Float, val width: Float)

/**
 * Günün en düşük–en yüksek aralığını haftanın en düşük–en yüksek aralığına göre konumlar.
 * Örnek: hafta 9°–24°, gün 14°–23° → start = (14-9)/15 ≈ 0.33, width = (23-14)/15 = 0.6.
 */
fun rangeBarPosition(min: Double, max: Double, weekMin: Double, weekMax: Double): RangeBarPosition {
    val span = weekMax - weekMin
    // Bütün hafta aynı sıcaklıksa bölme sıfıra düşmesin: çubuk tam dolu çizilir.
    if (span <= 0.0) return RangeBarPosition(start = 0f, width = 1f)
    val start = ((min - weekMin) / span).toFloat().coerceIn(0f, 1f)
    val width = ((max - min) / span).toFloat().coerceIn(0f, 1f - start)
    return RangeBarPosition(start = start, width = width)
}
