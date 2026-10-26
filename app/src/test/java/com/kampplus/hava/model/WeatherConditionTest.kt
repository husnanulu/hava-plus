package com.kampplus.hava.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `acik hava kodu CLEAR dondurur`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWeatherCode(0))
    }

    @Test
    fun `ayni gorunume dusen kodlar gruplanir`() {
        // 61, 63, 65 hafif/orta/şiddetli yağmurdur; üçü de "Yağmurlu".
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWeatherCode(61))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWeatherCode(63))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWeatherCode(65))
    }

    @Test
    fun `firtina kodlari THUNDERSTORM dondurur`() {
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWeatherCode(95))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWeatherCode(99))
    }

    @Test
    fun `taninmayan kod UNKNOWN dondurur ve uygulama cokmez`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWeatherCode(12345))
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWeatherCode(-1))
    }

    @Test
    fun `her durumun bir etiketi ve muzik turu vardir`() {
        WeatherCondition.entries.forEach { condition ->
            assert(condition.label.isNotBlank()) { "${condition.name} için etiket boş" }
            assert(condition.musicLabel.isNotBlank()) { "${condition.name} için müzik türü boş" }
        }
    }

    // --- fizy (Bölüm 6) ---

    @Test
    fun `muzik turu tasarim tablosuna uyar`() {
        assertEquals("Pop", WeatherCondition.CLEAR.musicLabel)
        assertEquals("Pop", WeatherCondition.MAINLY_CLEAR.musicLabel)
        assertEquals("Akustik", WeatherCondition.PARTLY_CLOUDY.musicLabel)
        assertEquals("Lo-fi", WeatherCondition.OVERCAST.musicLabel)
        assertEquals("Lo-fi", WeatherCondition.FOG.musicLabel)
        assertEquals("Sakin", WeatherCondition.RAIN.musicLabel)
        assertEquals("Sakin", WeatherCondition.DRIZZLE.musicLabel)
        assertEquals("Kış", WeatherCondition.SNOW_SHOWERS.musicLabel)
        assertEquals("Rock", WeatherCondition.THUNDERSTORM.musicLabel)
    }

    @Test
    fun `fizy kart metni baslik ve turu birlestirir`() {
        assertEquals("Güneşli günün müziği: Pop", WeatherCondition.CLEAR.musicText)
        assertEquals("Yağmurlu günün müziği: Sakin", WeatherCondition.RAIN_SHOWERS.musicText)
    }

    @Test
    fun `her durumun bir fizy linki vardir`() {
        WeatherCondition.entries.forEach { condition ->
            assert(condition.fizyUrl.startsWith("https://")) { "${condition.name} için link geçersiz" }
        }
    }
}
