package com.kampplus.hava.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.buildAdvice
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.BigTemperature
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.HavaIcons
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.component.StatusBarIcons
import com.kampplus.hava.ui.component.headerIcon
import com.kampplus.hava.ui.degrees
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.HavaYellow
import com.kampplus.hava.ui.theme.Navy
import com.kampplus.hava.ui.theme.SkyStyle
import com.kampplus.hava.ui.theme.weatherSky
import kotlin.math.roundToInt

/** Bağlayıcı: ViewModel'i edinir, state'i HomeScreen'e aktarır, izin penceresini yönetir. */
@Composable
fun HomeRoute(onPlaceClick: () -> Unit, onReady: () -> Unit, modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel()) {
    val place by viewModel.place.collectAsStateWithLifecycle()
    val isLocated by viewModel.isLocated.collectAsStateWithLifecycle()
    val showLocationBanner by viewModel.showLocationBanner.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Paylaş ve fizy, Intent başlatmak için Context ister.
    val context = LocalContext.current

    // Sistemin "Konumuna erişilsin mi?" penceresi. Sonuç ViewModel'e iletilir.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onLocationPermissionResult
    )
    // Ekran ilk açıldığında (kullanıcı şehir seçmediyse) izin bir kez istenir.
    LaunchedEffect(Unit) {
        if (viewModel.shouldAskLocationPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
    // İlk tahmin geldiğinde (ya da hata olduğunda) açılış ekranı kapanabilir.
    LaunchedEffect(uiState is LoadState.Loading) {
        if (uiState !is LoadState.Loading) onReady()
    }

    HomeScreen(
        place = place,
        isLocated = isLocated,
        showLocationBanner = showLocationBanner,
        isFavorite = isFavorite,
        uiState = uiState,
        onPlaceClick = onPlaceClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onShareClick = {
            val forecast = (uiState as? LoadState.Success)?.data
            val shownPlace = place
            if (forecast != null && shownPlace != null) shareForecast(context, shownPlace, forecast)
        },
        onFizyClick = { url -> openLink(context, url) },
        onUseLocationClick = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onRetry = viewModel::onRetry,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    place: City?,
    isLocated: Boolean,
    showLocationBanner: Boolean,
    isFavorite: Boolean,
    uiState: LoadState<Forecast>,
    onPlaceClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onFizyClick: (String) -> Unit,
    onUseLocationClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tahmin gelince üst alan havaya göre gökyüzü olur. Koyu gökyüzünde (kar dışındaki her hava) durum çubuğu
    // ikonları açık, açık gökyüzünde koyu olur. Yüklenirken / hata durumunda zemin zaten açık.
    val sky = (uiState as? LoadState.Success)?.let { skyStyle(WeatherCondition.fromWeatherCode(it.data.current.weatherCode)) }
    StatusBarIcons(onDarkBackground = sky?.isDark == true)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            LoadState.Loading -> ShimmerList(modifier = Modifier.statusBarsPadding())
            LoadState.Empty -> HomeMessage(place?.name.orEmpty(), onPlaceClick) {
                ErrorView(message = stringResource(R.string.forecast_empty))
            }
            is LoadState.Error -> HomeMessage(place?.name.orEmpty(), onPlaceClick) {
                ErrorView(message = uiState.message, onRetry = onRetry)
            }
            is LoadState.Success -> {
                HomeContent(
                    place = place ?: HomeViewModel.DEFAULT_CITY,
                    isLocated = isLocated,
                    showLocationBanner = showLocationBanner,
                    isFavorite = isFavorite,
                    forecast = uiState.data,
                    onPlaceClick = onPlaceClick,
                    onFavoriteClick = onFavoriteClick,
                    onShareClick = onShareClick,
                    onFizyClick = onFizyClick,
                    onUseLocationClick = onUseLocationClick
                )
                // Durum çubuğunun arkasına gökyüzünün en üst renginde bir şerit: aşağı kaydırınca
                // içerik saat/pil ikonlarının altından görünmesin.
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(sky?.top ?: Color.Transparent)
                )
            }
        }
    }
}

/** Hata / boş durumda bile yer adı görünsün ve başka şehir seçilebilsin. */
@Composable
private fun HomeMessage(placeName: String, onPlaceClick: () -> Unit, message: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        PlaceButton(name = placeName, isLocated = false, color = Navy, onClick = onPlaceClick)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { message() }
    }
}

/** Öneri kartının üst alana ne kadar bineceği (taslakta margin-top: -52px). */
private val AdviceOverlap = 52.dp

@Composable
private fun HomeContent(
    place: City,
    isLocated: Boolean,
    showLocationBanner: Boolean,
    isFavorite: Boolean,
    forecast: Forecast,
    onPlaceClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onFizyClick: (String) -> Unit,
    onUseLocationClick: () -> Unit
) {
    val condition = WeatherCondition.fromWeatherCode(forecast.current.weatherCode)
    // Öneri yalnızca tahmin değişince yeniden hesaplanır.
    val advice = remember(forecast) { buildAdvice(forecast) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            // İçerik ekrana sığmaz; dikey kaydırma ekliyoruz.
            .verticalScroll(rememberScrollState())
    ) {
        TopArea(
            place = place,
            isLocated = isLocated,
            showLocationBanner = showLocationBanner,
            isFavorite = isFavorite,
            forecast = forecast,
            condition = condition,
            onPlaceClick = onPlaceClick,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick,
            onUseLocationClick = onUseLocationClick
        )
        // Öneri kartı ve altındaki her şey 52dp yukarı kayar; böylece kart üst alanın üstüne biner.
        Column(
            modifier = Modifier
                .offset(y = -AdviceOverlap)
                .navigationBarsPadding()
        ) {
            AdviceCard(
                advice = advice,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            MetricsRow(current = forecast.current, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp))
            SectionTitle(stringResource(R.string.detail_hourly))
            HourlyRow(hours = forecast.hourly, now = forecast.current.observedAt)
            SectionTitle(stringResource(R.string.detail_daily))
            DailyCard(days = forecast.daily, modifier = Modifier.padding(horizontal = 16.dp))
            FizyCard(
                condition = condition,
                onListenClick = { onFizyClick(condition.fizyUrl) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp)
            )
            ShareButton(onClick = onShareClick, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp))
            Footer()
        }
    }
}

/** Havaya göre gökyüzü zeminli üst alan: yer adı, butonlar, büyük sıcaklık, ikon. */
@Composable
private fun TopArea(
    place: City,
    isLocated: Boolean,
    showLocationBanner: Boolean,
    isFavorite: Boolean,
    forecast: Forecast,
    condition: WeatherCondition,
    onPlaceClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onUseLocationClick: () -> Unit
) {
    val sky = skyStyle(condition)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Düz renk yerine dikey geçiş + ışık parlamaları (ui/theme/WeatherSky.kt).
            .weatherSky(sky)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 76.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlaceButton(
                name = place.name,
                isLocated = isLocated,
                color = sky.ink,
                onClick = onPlaceClick,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            FavoriteButton(isFavorite = isFavorite, color = sky.ink, onClick = onFavoriteClick)
            Spacer(modifier = Modifier.width(8.dp))
            RoundButton(color = sky.ink, onClick = onShareClick) {
                Icon(HavaIcons.Share, stringResource(R.string.action_share), Modifier.size(20.dp))
            }
        }
        if (showLocationBanner) {
            LocationBanner(sky = sky, onUseLocationClick = onUseLocationClick)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BigTemperature(value = forecast.current.temperatureC.roundToInt(), textColor = sky.ink, ringColor = sky.ring)
                Text(
                    text = condition.label,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                    color = sky.ink
                )
            }
            // Çok renkli ikon: Icon değil Image, tint yok (Icon hepsini tek renge boyardı).
            Image(
                painter = painterResource(headerIcon(condition)),
                contentDescription = null,
                modifier = Modifier.size(76.dp)
            )
        }
        // Bugünün en yüksek / en düşük değerleri günlük listenin ilk satırındadır.
        forecast.daily.firstOrNull()?.let { today ->
            Text(
                text = stringResource(R.string.home_range, degrees(today.maxTemperatureC), degrees(today.minTemperatureC)),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                color = sky.sub
            )
        }
    }
}

/** "📍 Kadıköy, İstanbul ⌄" — dokununca Şehir seç açılır. Dokunma alanı en az 44dp. */
@Composable
private fun PlaceButton(name: String, isLocated: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val changeCity = stringResource(R.string.action_change_city)
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = changeCity, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLocated) {
            Icon(HavaIcons.Pin, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Icon(HavaIcons.ChevronDown, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
    }
}

/** Üst alandaki 44dp'lik yarı saydam yuvarlak buton; ikon rengi = üst alanın yazı rengi. */
@Composable
private fun RoundButton(color: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        ),
        content = content
    )
}

/** ☆ favoride değilse boş, ★ favorideyse dolu yıldız (sarı zeminde de görünsün diye yazı renginde). */
@Composable
private fun FavoriteButton(isFavorite: Boolean, color: Color, onClick: () -> Unit) {
    RoundButton(color = color, onClick = onClick) {
        if (isFavorite) {
            Icon(
                imageVector = HavaIcons.StarFilled,
                contentDescription = stringResource(R.string.action_remove_favorite),
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(HavaIcons.Star, stringResource(R.string.action_add_favorite), Modifier.size(20.dp))
        }
    }
}

/** "Konum izni kapalı…" bandı ve sarı "Konumumu kullan" butonu. */
@Composable
private fun LocationBanner(sky: SkyStyle, onUseLocationClick: () -> Unit) {
    val colors = locationBannerColors(sky)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.background)
            .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.location_off_banner),
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = colors.text,
            modifier = Modifier.weight(1f)
        )
        // Açık zeminde koyu, koyu zeminde sarı buton (bkz. locationBannerColors).
        YellowButton(
            text = stringResource(R.string.action_use_location),
            onClick = onUseLocationClick,
            containerColor = colors.buttonBackground,
            contentColor = colors.buttonText
        )
    }
}

/** Birincil eylem butonu: sarı zemin, lacivert yazı, tam yuvarlak. */
@Composable
fun YellowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = HavaYellow,
    contentColor: Color = Navy
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeScreenPreview() {
    HavaTheme {
        HomeScreen(
            place = HomeViewModel.DEFAULT_CITY,
            isLocated = true,
            showLocationBanner = false,
            isFavorite = true,
            uiState = LoadState.Success(previewForecast()),
            onPlaceClick = {},
            onFavoriteClick = {},
            onShareClick = {},
            onFizyClick = {},
            onUseLocationClick = {},
            onRetry = {}
        )
    }
}
