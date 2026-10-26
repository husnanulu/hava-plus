package com.kampplus.hava.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/*
 * Öneri motoru: tahmini "ne giysem, şemsiye alayım mı?" cümlesine çevirir.
 * Android'e hiç dokunmayan saf Kotlin'dir; bu yüzden unit test ile her kuralı tek tek deneyebiliriz.
 * Kurallar: docs/HAVAPLUS_TASARIM.md → Bölüm 3. Kamp konusu: data class + when + liste işlemleri.
 */

/** Ana ekrandaki öneri kartının içeriği: bir cümle ve altındaki haplar (en fazla 3). */
data class Advice(val text: String, val chips: List<String>)

/** Tek bir kuralın sonucu: cümleye eklenecek parça ve (varsa) hap. */
private data class AdvicePart(val sentence: String?, val chip: String?)

/** Yağış kuralları için bakılan süre: şimdiden itibaren 12 saat. */
private const val LOOKAHEAD_HOURS = 12L
private const val RAIN_LIKELY = 50
private const val RAIN_POSSIBLE = 30
private const val WINDY_KMH = 40.0
private const val HOT_FEELS_LIKE = 30

fun buildAdvice(forecast: Forecast): Advice {
    val current = forecast.current
    // Hissedilen sıcaklık yoksa gerçek sıcaklık kullanılır.
    val feelsLike = (current.apparentTemperatureC ?: current.temperatureC).roundToInt()
    val nextHours = upcomingHours(forecast.hourly, current.observedAt)

    val clothing = clothingAdvice(feelsLike)
    val rain = rainAdvice(current, nextHours)
    val extra = extraAdvice(current, nextHours, feelsLike)

    val parts = listOf(clothing, rain, extra)
    val text = parts.mapNotNull { it.sentence }.joinToString(" ")
    val chips = parts.mapNotNull { it.chip }.distinct().take(3)
    return Advice(text = text, chips = chips)
}

/** "Şimdi" şehrin yerel saatidir (observedAt); o saatten başlayıp 12 saatlik dilim alınır. */
private fun upcomingHours(hours: List<HourlyForecast>, now: LocalDateTime): List<HourlyForecast> {
    val start = now.truncatedTo(ChronoUnit.HOURS)
    val end = start.plusHours(LOOKAHEAD_HOURS)
    return hours.filter { !it.time.isBefore(start) && it.time.isBefore(end) }
}

/** Giysi: hissedilen sıcaklığa göre beş aralık. */
private fun clothingAdvice(feelsLike: Int): AdvicePart = when {
    feelsLike <= 5 -> AdvicePart("Mont, atkı ve bere zamanı.", "Mont")
    feelsLike <= 12 -> AdvicePart("Mont giy.", "Mont")
    feelsLike <= 18 -> AdvicePart("Ceket yeterli.", "Ceket")
    feelsLike <= 24 -> AdvicePart("İnce bir üst yeterli.", "İnce üst")
    else -> AdvicePart("Tişört havası, bol su iç.", "Su")
}

/** Yağış: şu an mı yağıyor, sonra mı başlayacak, yoksa sadece ihtimal mi var? */
private fun rainAdvice(current: CurrentWeather, nextHours: List<HourlyForecast>): AdvicePart {
    val thisHour = current.observedAt.truncatedTo(ChronoUnit.HOURS)
    val nowChance = nextHours.firstOrNull { it.time == thisHour }?.precipitationProbability ?: 0
    val rainingNow = WeatherCondition.fromWeatherCode(current.weatherCode).isRain
    if (nowChance >= RAIN_LIKELY || rainingNow) {
        return AdvicePart("Şemsiyesiz çıkma.", "Şemsiye")
    }
    val laterHours = nextHours.filter { it.time.isAfter(thisHour) }
    val firstRainyHour = laterHours.firstOrNull { (it.precipitationProbability ?: 0) >= RAIN_LIKELY }
    if (firstRainyHour != null) {
        val hour = firstRainyHour.time.hour
        val clock = "%02d:00".format(hour)
        return AdvicePart("$clock'${ablativeSuffix(hour)} sonra şemsiyeni al.", "Şemsiye $clock+")
    }
    val maxChance = nextHours.maxOfOrNull { it.precipitationProbability ?: 0 } ?: 0
    if (maxChance >= RAIN_POSSIBLE) {
        return AdvicePart("Yanına şemsiye almakta fayda var.", "Şemsiye")
    }
    return AdvicePart(null, null)
}

/** Ekler: en fazla bir tane, öncelik sırasıyla fırtına → kar → rüzgâr → sıcak. */
private fun extraAdvice(current: CurrentWeather, nextHours: List<HourlyForecast>, feelsLike: Int): AdvicePart {
    val conditions = (listOf(current.weatherCode) + nextHours.map { it.weatherCode })
        .map { WeatherCondition.fromWeatherCode(it) }
    return when {
        WeatherCondition.THUNDERSTORM in conditions ->
            AdvicePart("Fırtına bekleniyor, dışarıda uzun kalma.", null)
        conditions.any { it.isSnow } -> AdvicePart("Yollar kaygan olabilir.", "Bot")
        (current.windSpeedKmh ?: 0.0) >= WINDY_KMH -> AdvicePart(null, "Rüzgârlık")
        feelsLike >= HOT_FEELS_LIKE -> AdvicePart(null, "Güneş kremi")
        else -> AdvicePart(null, null)
    }
}

/**
 * Saatin sesli okunuşuna göre "-den / -dan / -ten / -tan" eki (Türkçe ses uyumu).
 * Örnek: 18:00 ("on sekiz") → 'den, 19:00 ("on dokuz") → 'dan, 15:00 ("on beş") → 'ten.
 */
fun ablativeSuffix(hour: Int): String {
    val ones = hour % 10
    return when {
        ones == 0 && hour == 20 -> "den" // yirmi
        ones == 0 -> "dan" // sıfır, on
        ones == 3 || ones == 4 || ones == 5 -> "ten" // üç, dört, beş
        ones == 6 || ones == 9 -> "dan" // altı, dokuz
        else -> "den" // bir, iki, yedi, sekiz
    }
}
