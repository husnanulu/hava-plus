package com.kampplus.hava.ui.component

import com.kampplus.hava.R
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.home.skyStyle
import com.kampplus.hava.ui.theme.WeatherSky
import org.junit.Assert.assertEquals
import org.junit.Test

/** Hava kodu → altı grup eşlemesi; ikon ve üst alan gökyüzü aynı gruptan gelmeli. */
class WeatherIconsTest {

    private fun groupOf(code: Int) = weatherGroup(WeatherCondition.fromWeatherCode(code))

    @Test
    fun `kodlar tasarimdaki gruplara duser`() {
        assertEquals(WeatherGroup.CLEAR, groupOf(0))
        listOf(1, 2).forEach { assertEquals(WeatherGroup.PARTLY, groupOf(it)) }
        listOf(3, 45, 48).forEach { assertEquals(WeatherGroup.CLOUDY, groupOf(it)) }
        listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82).forEach { assertEquals(WeatherGroup.RAIN, groupOf(it)) }
        listOf(71, 73, 75, 77, 85, 86).forEach { assertEquals(WeatherGroup.SNOW, groupOf(it)) }
        listOf(95, 96, 99).forEach { assertEquals(WeatherGroup.STORM, groupOf(it)) }
    }

    @Test
    fun `ust alan ikonu ve kart ikonu gruba gore secilir`() {
        assertEquals(R.drawable.ic_wx_clear, headerIcon(WeatherCondition.CLEAR))
        assertEquals(R.drawable.ic_wx_partly, headerIcon(WeatherCondition.MAINLY_CLEAR))
        assertEquals(R.drawable.ic_wx_cloudy, headerIcon(WeatherCondition.FOG))
        assertEquals(R.drawable.ic_wx_rain_card, cardIcon(WeatherCondition.DRIZZLE))
        assertEquals(R.drawable.ic_wx_snow_card, cardIcon(WeatherCondition.SNOW_SHOWERS))
        assertEquals(R.drawable.ic_wx_storm_card, cardIcon(WeatherCondition.THUNDERSTORM))
    }

    @Test
    fun `ikon ve ust alan gokyuzu hicbir havada ayrismaz`() {
        val paletteOf = mapOf(
            WeatherGroup.CLEAR to WeatherSky.Clear,
            WeatherGroup.PARTLY to WeatherSky.PartlyCloudy,
            WeatherGroup.CLOUDY to WeatherSky.Overcast,
            WeatherGroup.RAIN to WeatherSky.Rain,
            WeatherGroup.SNOW to WeatherSky.Snow,
            WeatherGroup.STORM to WeatherSky.Storm
        )
        WeatherCondition.entries.forEach {
            assertEquals(it.name, paletteOf[weatherGroup(it)], skyStyle(it))
        }
    }
}
