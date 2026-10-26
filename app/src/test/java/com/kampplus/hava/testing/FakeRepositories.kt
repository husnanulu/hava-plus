package com.kampplus.hava.testing

import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.FavoritesRepository
import com.kampplus.hava.data.LastPlaceRepository
import com.kampplus.hava.data.LocationRepository
import com.kampplus.hava.data.RecentSearchRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Coordinates
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.Forecast
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Testlerde gerçek repository yerine kullanılan sahte.
 * İnternete çıkmadan istenen sonucu (başarı / boş / hata) üretir.
 */
class FakeCityRepository(
    var result: List<City> = emptyList(),
    var error: Throwable? = null
) : CityRepository {

    /** Son aranan metin. */
    var lastQuery: String? = null
        private set

    /** Kaç kez arama yapıldığı. */
    var callCount: Int = 0
        private set

    override suspend fun search(query: String): List<City> {
        lastQuery = query
        callCount++
        error?.let { throw it }
        return result
    }
}

/** Tahmin için sahte repository. error doluysa onu fırlatır. */
class FakeWeatherRepository(
    var result: Forecast = sampleForecast(),
    var error: Throwable? = null
) : WeatherRepository {

    var callCount: Int = 0
        private set

    /** Son istenen koordinat. */
    var lastLatitude: Double = 0.0
        private set

    /** Hafif "anlık hava" isteklerinin enlemleri (hangi şehirler için istendi?). */
    val currentRequests = mutableListOf<Double>()

    /** Bu enlemlerde getCurrent hata fırlatır (tek satırın hata vermesi senaryosu). */
    val failingLatitudes = mutableSetOf<Double>()

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        callCount++
        lastLatitude = latitude
        error?.let { throw it }
        return result
    }

    override suspend fun getCurrent(latitude: Double, longitude: Double): CurrentWeather {
        currentRequests += latitude
        error?.let { throw it }
        if (latitude in failingLatitudes) throw java.io.IOException("no network")
        return result.current
    }
}

/** Konum için sahte repository: izin, konum ve yer adı elle ayarlanır. */
class FakeLocationRepository(
    var permission: Boolean = false,
    var coordinates: Coordinates? = null,
    var name: String? = null
) : LocationRepository {

    var locationRequests: Int = 0
        private set

    override fun hasPermission(): Boolean = permission

    override suspend fun currentLocation(): Coordinates? {
        locationRequests++
        return if (permission) coordinates else null
    }

    override suspend fun placeName(coordinates: Coordinates): String? = name
}

/** "Son bakılan yer" için bellekte duran sahte depolama. */
class FakeLastPlaceRepository(var saved: City? = null) : LastPlaceRepository {
    override suspend fun lastPlace(): City? = saved

    override suspend fun saveLastPlace(city: City) {
        saved = city
    }
}

/** Favoriler için bellekte duran sahte depo; gerçeği gibi bir akış (Flow) yayınlar. */
class FakeFavoritesRepository(initial: List<City> = emptyList()) : FavoritesRepository {
    private val state = MutableStateFlow(initial)

    override val favorites: Flow<List<City>> = state

    override suspend fun toggle(city: City) {
        val current = state.value
        state.value = if (current.any { it.id == city.id }) current.filterNot { it.id == city.id } else current + city
    }
}

class FakeRecentSearchRepository(initial: List<City> = emptyList()) : RecentSearchRepository {
    private val state = MutableStateFlow(initial)

    override val recentSearches: Flow<List<City>> = state

    override suspend fun add(city: City) {
        state.value = (listOf(city) + state.value.filterNot { it.id == city.id }).take(RecentSearchRepository.MAX_RECENT)
    }

    override suspend fun remove(city: City) {
        state.value = state.value.filterNot { it.id == city.id }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}

/** Testlerde tekrar tekrar yazmamak için hazır bir şehir. */
fun sampleCity(id: Long = 323786L, name: String = "Ankara", latitude: Double = 39.91987) = City(
    id = id,
    name = name,
    latitude = latitude,
    longitude = 32.85427,
    countryCode = "TR",
    country = "Türkiye",
    admin1 = "Ankara"
)

/** Testlerde tekrar tekrar yazmamak için hazır bir tahmin. */
fun sampleForecast() = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = 0,
        observedAt = LocalDateTime.parse("2026-09-24T11:30")
    ),
    hourly = emptyList(),
    daily = emptyList()
)
