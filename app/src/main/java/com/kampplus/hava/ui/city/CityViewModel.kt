package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.AppContainer
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.FavoritesRepository
import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.RecentSearchRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.toCity
import com.kampplus.hava.ui.LoadState
import com.kampplus.hava.ui.home.HomeViewModel
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository(),
    private val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(),
    private val locationRepository: LocationRepository = AppContainer.locationRepository,
    private val favoritesRepository: FavoritesRepository = AppContainer.favoritesRepository,
    private val recentSearchRepository: RecentSearchRepository = AppContainer.recentSearchRepository
) : ViewModel() {

    // Arama metni her girişte boş başlar; hiçbir yere (DataStore, route) yazılmaz.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Yazma sırasında bekleyen arama; yeni harf gelince iptal edilip yeniden kurulur. */
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(locationRepository.hasPermission())

    /** İzin yoksa "Konumum" kartı "Konumumu kullan" eylemine dönüşür. */
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _myLocation = MutableStateFlow<CityWeather?>(null)

    /** "Konumum" kartındaki yer ve anlık sıcaklığı; bulunana kadar null. */
    val myLocation: StateFlow<CityWeather?> = _myLocation.asStateFlow()

    private val _favorites = MutableStateFlow<List<CityWeather>>(emptyList())

    /** "FAVORİLERİM" bölümü: favori şehirler ve (geldiyse) anlık sıcaklıkları. */
    val favorites: StateFlow<List<CityWeather>> = _favorites.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<City>>(emptyList())

    /** "SON ARAMALAR" bölümü: arama sonucundan seçilen son şehirler, en yenisi başta. */
    val recentSearches: StateFlow<List<City>> = _recentSearches.asStateFlow()

    init {
        // Açılışta arama yapılmaz: arama kutusu boştur, ekranda Konumum ve favoriler görünür.
        loadMyLocation()
        observeFavorites()
        observeRecentSearches()
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            recentSearchRepository.recentSearches.collect { _recentSearches.value = it }
        }
    }

    /**
     * Arama sonucundan şehir seçildi: önce son aramalara yazılır, sonra [onSaved] ile Ana ekrana geçilir.
     * Sıra önemli: ekran kapanınca viewModelScope iptal olur; kayıt bitmeden geçseydik yazma yarıda kalabilirdi.
     */
    fun onSearchResultClick(city: City, onSaved: () -> Unit) {
        viewModelScope.launch {
            recentSearchRepository.add(city)
            onSaved()
        }
    }

    /** Son aramalar satırındaki X. */
    fun onRemoveRecent(city: City) {
        viewModelScope.launch { recentSearchRepository.remove(city) }
    }

    /** Son aramalar başlığındaki "Temizle". */
    fun onClearRecent() {
        viewModelScope.launch { recentSearchRepository.clear() }
    }

    /** Yıldıza dokunuldu: favorideyse çıkar, değilse ekle. */
    fun onToggleFavorite(city: City) {
        viewModelScope.launch { favoritesRepository.toggle(city) }
    }

    /**
     * Favori listesi her değiştiğinde: liste hemen gösterilir, sıcaklığı henüz bilinmeyen
     * favoriler için anlık hava istenir. Daha önce gelen sıcaklıklar korunur (tekrar istenmez).
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favorites.collect { cities ->
                val known = _favorites.value.associateBy { it.city.id }
                _favorites.value = cities.map { city -> CityWeather(city, known[city.id]?.current) }
                loadFavoriteWeather(cities.filter { known[it.id]?.current == null })
            }
        }
    }

    /**
     * Her favori için istek AYNI ANDA başlar (async = "bunu başlat, sonucunu sonra alırım"),
     * awaitAll hepsinin bitmesini bekler. 5 favori 5 kat değil, yaklaşık 1 istek süresinde gelir.
     */
    private suspend fun loadFavoriteWeather(cities: List<City>) {
        if (cities.isEmpty()) return
        val results = coroutineScope {
            cities.map { city -> async { city.id to currentWeatherOrNull(city) } }.awaitAll()
        }.toMap()
        _favorites.value = _favorites.value.map { item ->
            val current = results[item.city.id] ?: return@map item
            item.copy(current = current)
        }
    }

    /**
     * Kullanıcı arama kutusuna her harf yazdığında çağrılır. Her harfte istek atılmaz:
     * yazma [TYPING_DELAY_MS] kadar durunca tek bir arama yapılır. Metin silinirse arama yapılmaz.
     */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) return
        // Sonuç gelene kadar yükleniyor görünümü.
        _uiState.value = LoadState.Loading
        searchJob = viewModelScope.launch {
            delay(TYPING_DELAY_MS)
            runSearch()
        }
    }

    /** Klavyedeki "Ara" ya da "Tekrar dene": beklemeden hemen arar. */
    fun search() {
        searchJob?.cancel()
        if (_query.value.isBlank()) return
        searchJob = viewModelScope.launch { runSearch() }
    }

    /** Aramayı çalıştırır ve sonucu dört durumdan birine çevirir. */
    private suspend fun runSearch() {
        _uiState.value = LoadState.Loading
        _uiState.value = try {
            val cities = repository.search(_query.value)
            if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
        } catch (e: IOException) {
            // İnternet yok, DNS çözülemedi veya zaman aşımı.
            Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
            LoadState.Error("İnternet bağlantını kontrol et.")
        } catch (e: HttpException) {
            // Sunucu yanıt verdi ama 4xx/5xx döndü.
            Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
            LoadState.Error("Sunucu şu an cevap vermiyor.")
        } catch (e: JsonSyntaxException) {
            // Beklenmeyen JSON: model ile yanıt uyuşmuyor.
            Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
            LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
        }
    }

    /** "Konumumu kullan" ile istenen iznin sonucu. */
    fun onLocationPermissionResult(granted: Boolean) {
        _hasLocationPermission.value = granted
        if (granted) loadMyLocation()
    }

    /** Konumu, adını ve anlık sıcaklığını bulur. Herhangi biri olmazsa kart yine "Konumum" olarak görünür. */
    private fun loadMyLocation() {
        if (!locationRepository.hasPermission()) return
        viewModelScope.launch {
            val coordinates = locationRepository.currentLocation() ?: return@launch
            val name = locationRepository.placeName(coordinates) ?: HomeViewModel.LOCATION_FALLBACK_NAME
            val city = coordinates.toCity(name)
            _myLocation.value = CityWeather(city)
            _myLocation.value = CityWeather(city, currentWeatherOrNull(city))
        }
    }

    /** Liste satırı için anlık hava; hata olursa yalnızca o satırda sıcaklık gösterilmez. */
    private suspend fun currentWeatherOrNull(city: City): CurrentWeather? = try {
        weatherRepository.getCurrent(city.latitude, city.longitude)
    } catch (e: IOException) {
        Log.e(TAG, "Anlık hava alınamadı: ${city.name}", e)
        null
    } catch (e: HttpException) {
        Log.e(TAG, "Anlık hava alınamadı: HTTP ${e.code()}", e)
        null
    } catch (e: JsonSyntaxException) {
        Log.e(TAG, "Anlık hava çözümlenemedi: ${city.name}", e)
        null
    } catch (e: IllegalStateException) {
        Log.e(TAG, "Anlık hava eksik: ${city.name}", e)
        null
    }

    private companion object {
        const val TAG = "CityViewModel"
        const val TYPING_DELAY_MS = 400L
    }
}
