package com.kampplus.hava.ui.home

import androidx.compose.ui.graphics.Color
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.HavaInk
import com.kampplus.hava.ui.theme.HavaYellow
import com.kampplus.hava.ui.theme.WeatherSky
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Üst alanın gökyüzü stili (WeatherSky), konum bandı ve aralık çubuğu kuralları. */
class WeatherStyleTest {

    @Test
    fun `acik havada kod 0 gokyuzu Clear`() {
        assertEquals(WeatherSky.Clear, skyStyle(WeatherCondition.CLEAR))
    }

    @Test
    fun `az bulutlu parcali bulutlu ve bilinmeyen havada gokyuzu PartlyCloudy`() {
        listOf(WeatherCondition.MAINLY_CLEAR, WeatherCondition.PARTLY_CLOUDY, WeatherCondition.UNKNOWN).forEach {
            assertEquals(WeatherSky.PartlyCloudy, skyStyle(it))
        }
    }

    @Test
    fun `kapali ve sisli havada gokyuzu Overcast`() {
        listOf(WeatherCondition.OVERCAST, WeatherCondition.FOG).forEach {
            assertEquals(WeatherSky.Overcast, skyStyle(it))
        }
    }

    @Test
    fun `yagmurlu havada cisenti dahil gokyuzu Rain`() {
        listOf(WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS).forEach {
            assertEquals(WeatherSky.Rain, skyStyle(it))
        }
    }

    @Test
    fun `karli havada gokyuzu Snow`() {
        listOf(WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS).forEach {
            assertEquals(WeatherSky.Snow, skyStyle(it))
        }
    }

    @Test
    fun `firtinada gokyuzu Storm`() {
        assertEquals(WeatherSky.Storm, skyStyle(WeatherCondition.THUNDERSTORM))
    }

    @Test
    fun `isDark yalnizca karda false`() {
        assertTrue(WeatherSky.Clear.isDark)
        assertTrue(WeatherSky.PartlyCloudy.isDark)
        assertTrue(WeatherSky.Overcast.isDark)
        assertTrue(WeatherSky.Rain.isDark)
        assertTrue(WeatherSky.Storm.isDark)
        assertFalse(WeatherSky.Snow.isDark)
    }

    @Test
    fun `durum cubugu ikonlari yalnizca karda koyu`() {
        val lightSkies = WeatherCondition.entries.filterNot { skyStyle(it).isDark }
        assertEquals(listOf(WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS), lightSkies)
    }

    @Test
    fun `derece halkasi acik ve parcali bulutluda beyaz digerlerinde mavi`() {
        assertEquals(Color.White, WeatherSky.Clear.ring)
        assertEquals(Color.White, WeatherSky.PartlyCloudy.ring)
        listOf(WeatherSky.Overcast, WeatherSky.Rain, WeatherSky.Snow, WeatherSky.Storm).forEach {
            assertEquals(Color(0xFF4FB0EE), it.ring)
        }
    }

    @Test
    fun `koyu gokyuzunde konum butonu sari ve yazisi koyu`() {
        listOf(WeatherSky.Clear, WeatherSky.PartlyCloudy, WeatherSky.Overcast, WeatherSky.Rain, WeatherSky.Storm).forEach {
            val colors = locationBannerColors(it)
            assertEquals(HavaYellow, colors.buttonBackground)
            assertEquals(HavaInk, colors.buttonText)
        }
    }

    @Test
    fun `acik gokyuzunde karda konum butonu koyu ve yazisi beyaz`() {
        val colors = locationBannerColors(WeatherSky.Snow)
        assertEquals(HavaInk, colors.buttonBackground)
        assertEquals(Color.White, colors.buttonText)
    }

    @Test
    fun `aralik cubugu haftanin araligina gore konumlanir`() {
        // Hafta 9°–24° (15 derece), gün 14°–23°.
        val bar = rangeBarPosition(min = 14.0, max = 23.0, weekMin = 9.0, weekMax = 24.0)
        assertEquals(5f / 15f, bar.start, 0.001f)
        assertEquals(9f / 15f, bar.width, 0.001f)
    }

    @Test
    fun `hafta boyunca ayni sicaklik varsa cubuk tam dolu`() {
        val bar = rangeBarPosition(min = 10.0, max = 10.0, weekMin = 10.0, weekMax = 10.0)
        assertEquals(RangeBarPosition(start = 0f, width = 1f), bar)
    }
}
