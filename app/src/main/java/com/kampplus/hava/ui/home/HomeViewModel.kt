package com.kampplus.hava.ui.home

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.AppContainer
import com.kampplus.hava.data.FavoritesRepository
import com.kampplus.hava.data.LastPlaceRepository
import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.toCity
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Ana ekranın ("Bugün") ViewModel'i. Kamptaki ForecastViewModel'in yeni adı ve genişlemiş hâli:
 * hangi yerin gösterileceğine karar verir (konum kuralı), tahmini yükler, ekran durumunu StateFlow ile yayınlar.
 */
class HomeViewModel(
    savedStateHandle: SavedStateHandle,
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
    private val lastPlaceRepository: LastPlaceRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    // viewModel() tam olarak (SavedStateHandle) imzalı bir kurucu arar.
    constructor(savedStateHandle: SavedStateHandle) : this(
        savedStateHandle,
        OpenMeteoWeatherRepository(),
        AppContainer.locationRepository,
        AppContainer.lastPlaceRepository,
        AppContainer.favoritesRepository
    )

    /** Kullanıcı Şehir seç'ten bir yer seçtiyse rotadan gelir; parametresiz açılışta null. */
    private val routeCity: City? = cityFromRoute(savedStateHandle)

    private val _place = MutableStateFlow(routeCity)

    /** Ekranda gösterilen yer. Karar verilene kadar (konum aranırken) null. */
    val place: StateFlow<City?> = _place.asStateFlow()

    private val _isLocated = MutableStateFlow(false)

    /** Gösterilen yer telefonun konumu mu? (Yer adının yanında sarı iğne çıkar.) */
    val isLocated: StateFlow<Boolean> = _isLocated.asStateFlow()

    private val _showLocationBanner = MutableStateFlow(false)

    /** "Konum izni kapalı. Son baktığın şehir gösteriliyor." bandı. */
    val showLocationBanner: StateFlow<Boolean> = _showLocationBanner.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)

    /** Gösterilen yer favorilerde mi? (☆ boş / ★ dolu) */
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    /** Favorilerin son hâli; yer değişince yıldızı yeniden hesaplamak için saklanır. */
    private var favoriteCities: List<City> = emptyList()

    private val _uiState = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<Forecast>> = _uiState.asStateFlow()

    /** İzin penceresi bu ekranda bir kez gösterilsin diye. */
    private var permissionAsked = false

    init {
        viewModelScope.launch {
            showPlace(routeCity ?: choosePlace())
        }
        // Favori listesi değiştikçe (bu ekranda ya da Şehir seç'te) yıldız güncellenir.
        viewModelScope.launch {
            favoritesRepository.favorites.collect { cities ->
                favoriteCities = cities
                updateIsFavorite()
            }
        }
    }

    /** ☆ / ★ butonu: gösterilen yeri favorilere ekler ya da çıkarır. */
    fun onFavoriteClick() {
        val city = _place.value ?: return
        viewModelScope.launch { favoritesRepository.toggle(city) }
    }

    private fun updateIsFavorite() {
        val id = _place.value?.id
        _isFavorite.value = favoriteCities.any { it.id == id }
    }

    /**
     * Konum kuralı (docs/HAVAPLUS_TASARIM.md → Bölüm 4), öncelik sırasıyla:
     * 1. rotadan gelen şehir (init'te zaten seçildi) → 2. izin var ve konum bulundu → konum
     * → 3. son bakılan yer (izin yoksa bant) → 4. İstanbul.
     */
    private suspend fun choosePlace(): City {
        if (locationRepository.hasPermission()) {
            locationCity()?.let { return it }
        } else {
            _showLocationBanner.value = true
        }
        return lastPlaceRepository.lastPlace() ?: DEFAULT_CITY
    }

    /** Konumu bulup adlandırır; bulunamazsa null. */
    private suspend fun locationCity(): City? {
        val coordinates = locationRepository.currentLocation() ?: return null
        val name = locationRepository.placeName(coordinates) ?: LOCATION_FALLBACK_NAME
        _isLocated.value = true
        return coordinates.toCity(name)
    }

    /** Yeri ekrana koyar, "son bakılan yer" olarak kaydeder ve tahminini yükler. */
    private suspend fun showPlace(city: City) {
        _place.value = city
        updateIsFavorite()
        lastPlaceRepository.saveLastPlace(city)
        loadForecast(city)
    }

    /**
     * Ekran açılınca izin penceresi gösterilsin mi? Yalnızca kullanıcı bir şehir seçmediyse,
     * izin henüz verilmediyse ve bu ekranda daha önce sorulmadıysa.
     */
    fun shouldAskLocationPermission(): Boolean {
        val ask = routeCity == null && !permissionAsked && !locationRepository.hasPermission()
        permissionAsked = true
        return ask
    }

    /** İzin penceresinin sonucu. İzin verildiyse konumdaki yere geçilir. */
    fun onLocationPermissionResult(granted: Boolean) {
        if (!granted) return
        viewModelScope.launch {
            _showLocationBanner.value = false
            // İzin var ama konum bulunamadıysa mevcut yer ekranda kalır.
            val here = locationCity() ?: return@launch
            showPlace(here)
        }
    }

    /** Hata ekranındaki "Tekrar dene" düğmesi buraya bağlanır. */
    fun onRetry() {
        val city = _place.value ?: return
        viewModelScope.launch { loadForecast(city) }
    }

    private suspend fun loadForecast(city: City) {
        _uiState.value = LoadState.Loading
        _uiState.value = try {
            LoadState.Success(weatherRepository.getForecast(city.latitude, city.longitude))
        } catch (e: IOException) {
            Log.e(TAG, "Tahmin alınamadı: bağlantı hatası", e)
            LoadState.Error("İnternet bağlantını kontrol et.")
        } catch (e: HttpException) {
            Log.e(TAG, "Tahmin alınamadı: HTTP ${e.code()}", e)
            LoadState.Error("Sunucu şu an cevap vermiyor.")
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Tahmin alınamadı: yanıt çözümlenemedi", e)
            LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
        } catch (e: IllegalStateException) {
            // toForecast(), "current" bloğu yoksa bunu fırlatır.
            Log.e(TAG, "Tahmin alınamadı: eksik veri", e)
            LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"

        /** Konumun adı bulunamazsa gösterilen ad. */
        const val LOCATION_FALLBACK_NAME = "Konumum"

        /** Rota parametrelerinin adları; HavaNavHost ile ortak. */
        const val ARG_ID = "id"
        const val ARG_NAME = "name"
        const val ARG_SUBTITLE = "subtitle"
        const val ARG_LATITUDE = "lat"
        const val ARG_LONGITUDE = "lon"

        /** Hiçbir bilgi yokken gösterilen yer. */
        val DEFAULT_CITY = City(
            id = 745044L,
            name = "İstanbul",
            latitude = 41.0082,
            longitude = 28.9784,
            countryCode = "TR",
            country = "Türkiye",
            admin1 = "İstanbul"
        )

        /**
         * Rota parametrelerinden bir City kurar. Koordinatlar yoksa ya da sayıya çevrilemiyorsa null döner.
         * Rotada yalnızca "Ankara, Türkiye" gibi hazır alt yazı taşınır; onu `country` alanına koyarız
         * ki `City.subtitle` aynı metni geri versin.
         */
        fun cityFromRoute(savedStateHandle: SavedStateHandle): City? {
            val latitude = savedStateHandle.get<String>(ARG_LATITUDE)?.toDoubleOrNull() ?: return null
            val longitude = savedStateHandle.get<String>(ARG_LONGITUDE)?.toDoubleOrNull() ?: return null
            return City(
                id = savedStateHandle.get<String>(ARG_ID)?.toLongOrNull() ?: 0L,
                name = savedStateHandle.get<String>(ARG_NAME).orEmpty(),
                latitude = latitude,
                longitude = longitude,
                countryCode = null,
                country = savedStateHandle.get<String>(ARG_SUBTITLE)?.takeIf { it.isNotEmpty() },
                admin1 = null
            )
        }
    }
}
