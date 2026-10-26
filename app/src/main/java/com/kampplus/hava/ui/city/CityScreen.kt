package com.kampplus.hava.ui.city

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.component.EmptyView
import com.kampplus.hava.ui.component.ErrorView
import com.kampplus.hava.ui.component.HavaIcons
import com.kampplus.hava.ui.component.ShimmerList
import com.kampplus.hava.ui.component.StatusBarIcons
import com.kampplus.hava.ui.component.cardIcon
import com.kampplus.hava.ui.degrees
import com.kampplus.hava.ui.theme.Blue
import com.kampplus.hava.ui.theme.CardWhite
import com.kampplus.hava.ui.theme.DividerColor
import com.kampplus.hava.ui.theme.HavaTheme
import com.kampplus.hava.ui.theme.HavaYellow
import com.kampplus.hava.ui.theme.Navy
import com.kampplus.hava.ui.theme.OnDarkTextMuted

@Composable
fun CityRoute(
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CityViewModel = viewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val myLocation by viewModel.myLocation.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onLocationPermissionResult
    )

    CityScreen(
        query = query,
        uiState = uiState,
        hasLocationPermission = hasLocationPermission,
        myLocation = myLocation,
        favorites = favorites,
        recentSearches = recentSearches,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::search,
        onBack = onBack,
        onCityClick = onCityClick,
        // Yalnızca arama sonucundan seçilen şehir son aramalara eklenir.
        onSearchResultClick = { city -> viewModel.onSearchResultClick(city) { onCityClick(city) } },
        onRemoveRecent = viewModel::onRemoveRecent,
        onClearRecent = viewModel::onClearRecent,
        onToggleFavorite = viewModel::onToggleFavorite,
        onMyLocationClick = {
            // İzin varsa Ana ekrana konumla dön; yoksa önce izni iste.
            if (hasLocationPermission) {
                onMyLocationClick()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CityScreen(
    query: String,
    uiState: LoadState<List<City>>,
    hasLocationPermission: Boolean,
    myLocation: CityWeather?,
    favorites: List<CityWeather>,
    recentSearches: List<City>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onCityClick: (City) -> Unit,
    onSearchResultClick: (City) -> Unit,
    onRemoveRecent: (City) -> Unit,
    onClearRecent: () -> Unit,
    onToggleFavorite: (City) -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Arama sonuçlarındaki yıldızların dolu mu boş mu olacağı bu kimliklerden anlaşılır.
    val favoriteIds = favorites.map { it.city.id }
    StatusBarIcons(onDarkBackground = false)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopBar(onBack = onBack)
        SearchBox(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Arama kutusu boşken Konumum, son aramalar ve favoriler; yazmaya başlayınca yalnızca arama sonuçları.
            if (query.isBlank()) {
                item {
                    MyLocationCard(
                        myLocation = myLocation,
                        hasPermission = hasLocationPermission,
                        onClick = onMyLocationClick
                    )
                }
                // Hiç son arama yoksa bölüm hiç görünmez.
                if (recentSearches.isNotEmpty()) {
                    item { RecentHeader(onClearClick = onClearRecent) }
                    item {
                        RowsCard {
                            recentSearches.forEachIndexed { index, city ->
                                if (index > 0) RowDivider()
                                RecentRow(city = city, onClick = { onCityClick(city) }, onRemove = { onRemoveRecent(city) })
                            }
                        }
                    }
                }
                item { SectionLabel(stringResource(R.string.favorites_header)) }
                item {
                    if (favorites.isEmpty()) {
                        Text(
                            text = stringResource(R.string.favorites_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        RowsCard {
                            favorites.forEachIndexed { index, favorite ->
                                if (index > 0) RowDivider()
                                FavoriteRow(
                                    favorite = favorite,
                                    onClick = { onCityClick(favorite.city) },
                                    onToggleFavorite = { onToggleFavorite(favorite.city) }
                                )
                            }
                        }
                    }
                }
            } else {
                item { SectionLabel(stringResource(R.string.results_header, query)) }
                // Dört durumun tamamı burada karşılanır.
                item {
                    when (uiState) {
                        LoadState.Loading -> ShimmerList(itemCount = 3)
                        is LoadState.Success -> RowsCard {
                            uiState.data.forEachIndexed { index, city ->
                                if (index > 0) RowDivider()
                                CityRow(city = city, onClick = { onSearchResultClick(city) }, showBadge = true) {
                                    StarButton(isFavorite = city.id in favoriteIds, onClick = { onToggleFavorite(city) })
                                }
                            }
                        }
                        LoadState.Empty -> EmptyView(
                            title = stringResource(R.string.city_not_found_title),
                            message = stringResource(R.string.city_not_found_message),
                            modifier = Modifier.fillMaxWidth()
                        )
                        is LoadState.Error -> ErrorView(
                            message = uiState.message,
                            onRetry = onSearch,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lacivert "Konumum" kartı: sarı daire içinde iğne, yer adı, anlık sıcaklık.
 * İzin yoksa ikinci satır "Konumumu kullan" olur; dokununca izin istenir.
 */
@Composable
private fun MyLocationCard(myLocation: CityWeather?, hasPermission: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Navy,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HavaYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(HavaIcons.Pin, contentDescription = null, tint = Navy, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.my_location),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = OnDarkTextMuted
                )
                Text(
                    text = when {
                        !hasPermission -> stringResource(R.string.action_use_location)
                        myLocation == null -> stringResource(R.string.location_searching)
                        else -> myLocation.city.name
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            myLocation?.current?.let { current ->
                Text(text = degrees(current.temperatureC), style = MaterialTheme.typography.titleLarge)
                // Kart ikonları açık zemin için çizildi; lacivert kartta küçük beyaz bir zeminin üstünde gösterilir.
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(cardIcon(WeatherCondition.fromWeatherCode(current.weatherCode))),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/** Geri oku + "Şehir seç" başlığı. */
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(HavaIcons.Back, stringResource(R.string.action_back), Modifier.size(22.dp))
        }
        Text(text = stringResource(R.string.cities_title), style = MaterialTheme.typography.headlineSmall)
    }
}

/** Mavi çerçeveli arama kutusu. Boşken "Şehir ara" yazar; metin varken sağda temizle (X) butonu çıkar. */
@Composable
private fun SearchBox(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(HavaIcons.Search, contentDescription = null, tint = Blue, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                }
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue,
            unfocusedBorderColor = Blue,
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite
        ),
        // Klavyedeki Enter tuşu "Ara" olur.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        )
    )
}

/** "FAVORİLERİM", "ARAMA SONUÇLARI" gibi küçük büyük harfli bölüm başlığı. */
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp)
    )
}

/** "SON ARAMALAR" başlığı ve sağında "Temizle". */
@Composable
private fun RecentHeader(onClearClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) { SectionLabel(stringResource(R.string.recent_header)) }
        TextButton(onClick = onClearClick) {
            Text(text = stringResource(R.string.recent_clear), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Son arama satırı: ad + "il, ülke" ve silmek için küçük X. */
@Composable
private fun RecentRow(city: City, onClick: () -> Unit, onRemove: () -> Unit) {
    CityRow(city = city, onClick = onClick) {
        IconButton(onClick = onRemove, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = stringResource(R.string.action_remove_recent),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Satırları taşıyan beyaz, köşeleri yuvarlak kart. */
@Composable
private fun RowsCard(content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = CardWhite, modifier = Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

/** İki satır arasındaki ince çizgi. */
@Composable
private fun RowDivider() {
    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(start = 16.dp))
}

/** Favori satırı: ad + "il, ülke", anlık sıcaklık ve ikon (geldiyse), dolu sarı yıldız. */
@Composable
private fun FavoriteRow(favorite: CityWeather, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    CityRow(city = favorite.city, onClick = onClick) {
        // Sıcaklık gelmediyse (istek başarısız) satır yine görünür, yalnızca sıcaklık olmaz.
        favorite.current?.let { current ->
            Text(text = degrees(current.temperatureC), style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp))
            Image(
                painter = painterResource(cardIcon(WeatherCondition.fromWeatherCode(current.weatherCode))),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        StarButton(isFavorite = true, onClick = onToggleFavorite)
    }
}

/** ★ favorideyse dolu sarı, ☆ değilse boş gri yıldız. 44dp dokunma alanı. */
@Composable
private fun StarButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        if (isFavorite) {
            Image(
                imageVector = HavaIcons.StarFilled,
                contentDescription = stringResource(R.string.action_remove_favorite),
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = HavaIcons.Star,
                contentDescription = stringResource(R.string.action_add_favorite),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    HavaTheme {
        CityScreen(
            query = "Ankara",
            hasLocationPermission = true,
            myLocation = null,
            recentSearches = emptyList(),
            favorites = listOf(
                CityWeather(City(311046, "İzmir", 38.41, 27.13, "TR", "Türkiye", "İzmir"))
            ),
            uiState = LoadState.Success(
                listOf(
                    City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara"),
                    City(738648, "Ankara", 41.0050, 39.7269, "TR", "Türkiye", "Trabzon")
                )
            ),
            onQueryChange = {},
            onSearch = {},
            onBack = {},
            onCityClick = {},
            onSearchResultClick = {},
            onRemoveRecent = {},
            onClearRecent = {},
            onToggleFavorite = {},
            onMyLocationClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenStartPreview() {
    HavaTheme {
        CityScreen(
            query = "",
            uiState = LoadState.Loading,
            hasLocationPermission = true,
            myLocation = null,
            favorites = listOf(
                CityWeather(City(311046, "İzmir", 38.41, 27.13, "TR", "Türkiye", "İzmir"))
            ),
            recentSearches = listOf(
                City(741100, "Rize", 41.02, 40.52, "TR", "Türkiye", "Rize"),
                City(323786, "Ankara", 39.91987, 32.85427, "TR", "Türkiye", "Ankara")
            ),
            onQueryChange = {},
            onSearch = {},
            onBack = {},
            onCityClick = {},
            onSearchResultClick = {},
            onRemoveRecent = {},
            onClearRecent = {},
            onToggleFavorite = {},
            onMyLocationClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenEmptyPreview() {
    HavaTheme {
        CityScreen(
            query = "xyz",
            uiState = LoadState.Empty,
            hasLocationPermission = false,
            myLocation = null,
            recentSearches = emptyList(),
            favorites = emptyList(),
            onQueryChange = {},
            onSearch = {},
            onBack = {},
            onCityClick = {},
            onSearchResultClick = {},
            onRemoveRecent = {},
            onClearRecent = {},
            onToggleFavorite = {},
            onMyLocationClick = {}
        )
    }
}
