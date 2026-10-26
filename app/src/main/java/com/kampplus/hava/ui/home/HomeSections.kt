package com.kampplus.hava.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.R
import com.kampplus.hava.model.Advice
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.DailyForecast
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.HourlyForecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.component.HavaIcons
import com.kampplus.hava.ui.component.cardIcon
import com.kampplus.hava.ui.degrees
import com.kampplus.hava.ui.percent
import com.kampplus.hava.ui.theme.Blue
import com.kampplus.hava.ui.theme.BorderColor
import com.kampplus.hava.ui.theme.CardWhite
import com.kampplus.hava.ui.theme.DividerColor
import com.kampplus.hava.ui.theme.HavaYellow
import com.kampplus.hava.ui.theme.Navy
import com.kampplus.hava.ui.theme.OnDarkTextMuted
import com.kampplus.hava.ui.theme.RecommendationCardColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

/*
 * Ana ekranın üst alan altındaki bölümleri: öneri kartı, üç küçük kart, saatlik ve 7 günlük.
 * Kamptaki ForecastScreen'in parçalarıdır; yalnızca görünümleri Hava+ taslağına göre değişti.
 */

/** "BUGÜN İÇİN ÖNERİ" kartı. Görsel kimlik v2: her havada beyaz zemin, ince çerçeve, gri haplar. */
@Composable
fun AdviceCard(advice: Advice, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = RecommendationCardColors.Background,
        contentColor = RecommendationCardColors.Text,
        border = BorderStroke(1.dp, RecommendationCardColors.Border)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(HavaIcons.Umbrella, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.advice_title),
                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.4.sp)
                )
            }
            Text(text = advice.text, style = MaterialTheme.typography.titleLarge)
            // FlowRow: haplar sığmazsa alt satıra geçer.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                advice.chips.forEach { chip ->
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(RecommendationCardColors.ChipBackground)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

/** Hissedilen · Nem · Rüzgâr: eşit genişlikte üç küçük kart. */
@Composable
fun MetricsRow(current: CurrentWeather, modifier: Modifier = Modifier) {
    val none = stringResource(R.string.no_value)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard(
            icon = HavaIcons.Thermometer,
            value = current.apparentTemperatureC?.let { degrees(it) } ?: none,
            label = stringResource(R.string.detail_feels_like),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = HavaIcons.Drop,
            value = current.humidityPercent?.let { "%$it" } ?: none,
            label = stringResource(R.string.detail_humidity),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            icon = HavaIcons.Wind,
            value = current.windSpeedKmh?.let { stringResource(R.string.wind_value, it.roundToInt()) } ?: none,
            label = stringResource(R.string.detail_wind),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(18.dp), color = CardWhite) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.size(20.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), maxLines = 1)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

/** Önümüzdeki 24 saat, yatay kaydırılabilir kartlar. Geçmiş saatler elenir. */
@Composable
fun HourlyRow(hours: List<HourlyForecast>, now: LocalDateTime, modifier: Modifier = Modifier) {
    // "Şimdi" telefonun değil, şehrin yerel saati (sunucudan gelir).
    val start = now.truncatedTo(ChronoUnit.HOURS)
    val upcoming = hours.filter { !it.time.isBefore(start) }.take(HOURLY_COUNT)
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = upcoming, key = { it.time.toString() }) { hour ->
            HourCard(hour)
        }
    }
}

@Composable
private fun HourCard(hour: HourlyForecast) {
    Surface(modifier = Modifier.width(64.dp), shape = RoundedCornerShape(18.dp), color = CardWhite) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = hour.time.format(HOUR_FORMAT),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Image(
                painter = painterResource(cardIcon(WeatherCondition.fromWeatherCode(hour.weatherCode))),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(text = degrees(hour.temperatureC), style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
            Text(
                // Boş yerine boşluk: kartların yüksekliği eşit kalsın.
                text = percent(hour.precipitationProbability) ?: " ",
                style = MaterialTheme.typography.labelSmall,
                color = Blue,
                modifier = Modifier.heightIn(min = 14.dp)
            )
        }
    }
}

/** 7 günlük kart: her satırda gün, yağış, ikon, en düşük, aralık çubuğu, en yüksek. */
@Composable
fun DailyCard(days: List<DailyForecast>, modifier: Modifier = Modifier) {
    if (days.isEmpty()) return
    // Aralık çubukları haftanın en düşüğü ile en yükseğine göre ölçeklenir.
    val weekMin = days.minOf { it.minTemperatureC }
    val weekMax = days.maxOf { it.maxTemperatureC }
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = CardWhite) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            days.forEachIndexed { index, day ->
                // İlk satır hariç her satırın üstüne ayırıcı çizgi.
                if (index > 0) HorizontalDivider(color = DividerColor)
                DayRow(
                    day = day,
                    isToday = index == 0,
                    bar = rangeBarPosition(day.minTemperatureC, day.maxTemperatureC, weekMin, weekMax)
                )
            }
        }
    }
}

@Composable
private fun DayRow(day: DailyForecast, isToday: Boolean, bar: RangeBarPosition) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isToday) stringResource(R.string.today) else day.date.format(DAY_FORMAT),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = percent(day.precipitationProbability).orEmpty(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Blue,
            textAlign = TextAlign.End,
            modifier = Modifier.width(34.dp)
        )
        Image(
            painter = painterResource(cardIcon(WeatherCondition.fromWeatherCode(day.weatherCode))),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = degrees(day.minTemperatureC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(30.dp)
        )
        RangeBar(bar = bar, modifier = Modifier.weight(1f))
        Text(
            text = degrees(day.maxTemperatureC),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            textAlign = TextAlign.End,
            modifier = Modifier.width(30.dp)
        )
    }
}

/**
 * 6dp yüksekliğinde gri zemin, içinde mavi parça. Soldaki boşluk, mavi parça ve sağdaki boşluk
 * `weight` ile paylaştırılır (weight sıfır olamayacağı için sıfır olanlar hiç çizilmez).
 */
@Composable
private fun RangeBar(bar: RangeBarPosition, modifier: Modifier = Modifier) {
    val rest = 1f - bar.start - bar.width
    Row(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(BorderColor)
    ) {
        if (bar.start > 0f) Spacer(modifier = Modifier.weight(bar.start))
        Box(
            modifier = Modifier
                .weight(bar.width.coerceAtLeast(MIN_BAR))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(Blue)
        )
        if (rest > 0f) Spacer(modifier = Modifier.weight(rest))
    }
}

/** Tam genişlik, beyaz zemin, lacivert çerçeveli paylaş butonu. */
@Composable
fun ShareButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Navy),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = Navy)
    ) {
        Icon(HavaIcons.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(R.string.share_button), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun Footer() {
    Text(
        text = stringResource(R.string.data_source),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    )
}

private const val HOURLY_COUNT = 24

/** Gün aynı sıcaklıkta sabitse mavi parça yine de nokta kadar görünsün. */
private const val MIN_BAR = 0.04f
private val HOUR_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Kısa gün adı: "Pzt", "Sal", "Çar"… */
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("tr"))

/** @Preview'lar için örnek tahmin. */
internal fun previewForecast() = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = 2,
        observedAt = LocalDateTime.of(2026, 1, 15, 14, 0),
        apparentTemperatureC = 17.0,
        humidityPercent = 45,
        windSpeedKmh = 12.0
    ),
    hourly = List(8) {
        HourlyForecast(
            time = LocalDateTime.of(2026, 1, 15, 14, 0).plusHours(it.toLong()),
            temperatureC = 21.0 - it,
            weatherCode = if (it < 4) 2 else 61,
            precipitationProbability = it * 12
        )
    },
    daily = List(7) {
        DailyForecast(
            date = LocalDate.of(2026, 1, 15).plusDays(it.toLong()),
            minTemperatureC = 9.0 + it,
            maxTemperatureC = 19.0 + it % 3 * 2,
            weatherCode = listOf(2, 0, 61, 3, 0, 2, 71)[it],
            precipitationProbability = it * 10
        )
    }
)
