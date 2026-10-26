package com.kampplus.hava.model

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Öneri motorunun kuralları (docs/HAVAPLUS_TASARIM.md → Bölüm 3). */
class AdviceTest {

    /** Şehrin yerel saati 11:30; "şu anki saat" 11:00 satırıdır. */
    private val now = LocalDateTime.parse("2026-09-24T11:30")

    /**
     * Test tahmini kurar. [chances]: 11:00'den başlayarak saat saat yağış olasılıkları.
     * [hourCodes]: aynı saatlerin hava kodları (verilmezse hepsi açık hava).
     */
    private fun forecast(
        feelsLike: Double? = 20.0,
        temperature: Double = 20.0,
        code: Int = 0,
        windKmh: Double? = 10.0,
        chances: List<Int?> = List(12) { 0 },
        hourCodes: List<Int> = List(chances.size) { 0 }
    ) = Forecast(
        current = CurrentWeather(
            temperatureC = temperature,
            weatherCode = code,
            observedAt = now,
            apparentTemperatureC = feelsLike,
            windSpeedKmh = windKmh
        ),
        hourly = chances.indices.map { i ->
            HourlyForecast(
                time = LocalDateTime.parse("2026-09-24T11:00").plusHours(i.toLong()),
                temperatureC = temperature,
                weatherCode = hourCodes[i],
                precipitationProbability = chances[i]
            )
        },
        daily = emptyList()
    )

    // --- Giysi: aralıklar ve sınır değerleri ---

    @Test
    fun `5 derece ve alti mont atki bere`() {
        val advice = buildAdvice(forecast(feelsLike = 5.0))
        assertEquals("Mont, atkı ve bere zamanı.", advice.text)
        assertEquals(listOf("Mont"), advice.chips)
    }

    @Test
    fun `6 ile 12 arasi mont`() {
        assertEquals("Mont giy.", buildAdvice(forecast(feelsLike = 6.0)).text)
        assertEquals("Mont giy.", buildAdvice(forecast(feelsLike = 12.0)).text)
    }

    @Test
    fun `13 ile 18 arasi ceket`() {
        assertEquals("Ceket yeterli.", buildAdvice(forecast(feelsLike = 13.0)).text)
        assertEquals("Ceket yeterli.", buildAdvice(forecast(feelsLike = 18.0)).text)
        assertEquals(listOf("Ceket"), buildAdvice(forecast(feelsLike = 15.0)).chips)
    }

    @Test
    fun `19 ile 24 arasi ince ust`() {
        assertEquals("İnce bir üst yeterli.", buildAdvice(forecast(feelsLike = 19.0)).text)
        assertEquals("İnce bir üst yeterli.", buildAdvice(forecast(feelsLike = 24.0)).text)
    }

    @Test
    fun `25 ve ustu tisort ve su`() {
        val advice = buildAdvice(forecast(feelsLike = 25.0))
        assertEquals("Tişört havası, bol su iç.", advice.text)
        assertEquals(listOf("Su"), advice.chips)
    }

    @Test
    fun `hissedilen yoksa gercek sicaklik kullanilir`() {
        val advice = buildAdvice(forecast(feelsLike = null, temperature = 3.0))
        assertEquals("Mont, atkı ve bere zamanı.", advice.text)
    }

    @Test
    fun `kesirli hissedilen yuvarlanarak araliga yerlestirilir`() {
        // 12.4 → 12 (mont), 12.6 → 13 (ceket)
        assertEquals("Mont giy.", buildAdvice(forecast(feelsLike = 12.4)).text)
        assertEquals("Ceket yeterli.", buildAdvice(forecast(feelsLike = 12.6)).text)
    }

    // --- Yağış ---

    @Test
    fun `su anki saat yuzde 50 ise semsiyesiz cikma`() {
        val advice = buildAdvice(forecast(feelsLike = 15.0, chances = listOf(50, 10, 10)))
        assertEquals("Ceket yeterli. Şemsiyesiz çıkma.", advice.text)
        assertEquals(listOf("Ceket", "Şemsiye"), advice.chips)
    }

    @Test
    fun `su an yagmur yagiyorsa olasilik dusuk olsa da semsiyesiz cikma`() {
        val advice = buildAdvice(forecast(feelsLike = 15.0, code = 61, chances = listOf(20, 10)))
        assertEquals("Ceket yeterli. Şemsiyesiz çıkma.", advice.text)
    }

    @Test
    fun `sonraki bir saatte ilk kez yuzde 50 olursa saat soylenir`() {
        // 11:00 %10 … 18:00 %60 (index 7)
        val chances = listOf(10, 10, 10, 20, 20, 30, 40, 60, 70, 80, 0, 0)
        val advice = buildAdvice(forecast(feelsLike = 15.0, chances = chances))
        assertEquals("Ceket yeterli. 18:00'den sonra şemsiyeni al.", advice.text)
        assertEquals(listOf("Ceket", "Şemsiye 18:00+"), advice.chips)
    }

    @Test
    fun `en yuksek yuzde 30 ile 49 arasi ise semsiye almakta fayda var`() {
        val advice = buildAdvice(forecast(feelsLike = 15.0, chances = listOf(10, 49, 30)))
        assertEquals("Ceket yeterli. Yanına şemsiye almakta fayda var.", advice.text)
        assertEquals(listOf("Ceket", "Şemsiye"), advice.chips)
    }

    @Test
    fun `yuzde 49 sinirinda saat soylenmez yuzde 50 de soylenir`() {
        val at49 = buildAdvice(forecast(chances = listOf(0, 0, 49))).text
        val at50 = buildAdvice(forecast(chances = listOf(0, 0, 50))).text
        assertFalse(at49.contains("13:00"))
        assertTrue(at50.contains("13:00'ten sonra şemsiyeni al."))
    }

    @Test
    fun `yagis ihtimali dusukse yagis cumlesi yok`() {
        val advice = buildAdvice(forecast(feelsLike = 15.0, chances = listOf(0, 10, 29)))
        assertEquals("Ceket yeterli.", advice.text)
    }

    @Test
    fun `12 saatten sonraki yagis dikkate alinmaz`() {
        // index 12 = 23:00, pencerenin (11:00–22:59) dışında.
        val chances = List(12) { 0 } + listOf(90)
        assertEquals("Ceket yeterli.", buildAdvice(forecast(feelsLike = 15.0, chances = chances)).text)
    }

    // --- Ekler ---

    @Test
    fun `firtina bekleniyorsa uyari cumlesi eklenir`() {
        val advice = buildAdvice(forecast(feelsLike = 15.0, chances = listOf(0, 0), hourCodes = listOf(0, 95)))
        assertEquals("Ceket yeterli. Fırtına bekleniyor, dışarıda uzun kalma.", advice.text)
    }

    @Test
    fun `kar varsa yollar kaygan ve bot hapi`() {
        val advice = buildAdvice(forecast(feelsLike = 0.0, code = 71))
        assertEquals("Mont, atkı ve bere zamanı. Yollar kaygan olabilir.", advice.text)
        assertEquals(listOf("Mont", "Bot"), advice.chips)
    }

    @Test
    fun `ruzgar 40 ve ustundeyse ruzgarlik hapi`() {
        assertEquals(listOf("Ceket", "Rüzgârlık"), buildAdvice(forecast(feelsLike = 15.0, windKmh = 40.0)).chips)
        assertEquals(listOf("Ceket"), buildAdvice(forecast(feelsLike = 15.0, windKmh = 39.0)).chips)
    }

    @Test
    fun `hissedilen 30 ve ustundeyse gunes kremi hapi`() {
        assertEquals(listOf("Su", "Güneş kremi"), buildAdvice(forecast(feelsLike = 30.0)).chips)
    }

    @Test
    fun `ekler oncelik sirasiyla yalnizca bir tane`() {
        // Hem rüzgârlı hem sıcak: yalnızca öncelikli olan (rüzgârlık) eklenir.
        val advice = buildAdvice(forecast(feelsLike = 32.0, windKmh = 50.0, chances = listOf(60)))
        assertEquals(listOf("Su", "Şemsiye", "Rüzgârlık"), advice.chips)
    }

    // --- Türkçe ek ---

    @Test
    fun `saat ekleri ses uyumuna uyar`() {
        assertEquals("den", ablativeSuffix(18)) // on sekiz
        assertEquals("dan", ablativeSuffix(19)) // on dokuz
        assertEquals("den", ablativeSuffix(20)) // yirmi
        assertEquals("ten", ablativeSuffix(15)) // on beş
        assertEquals("dan", ablativeSuffix(16)) // on altı
        assertEquals("dan", ablativeSuffix(10)) // on
        assertEquals("dan", ablativeSuffix(0)) // sıfır
        assertEquals("ten", ablativeSuffix(23)) // yirmi üç
    }
}
