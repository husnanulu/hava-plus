package com.kampplus.hava.data

import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.Forecast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WeatherRepository {
    /** Verilen koordinat için anlık durum + saatlik + günlük tahmini getirir. */
    suspend fun getForecast(latitude: Double, longitude: Double): Forecast

    /** Yalnızca anlık durum (saatlik/günlük yok); liste satırlarındaki sıcaklık için. */
    suspend fun getCurrent(latitude: Double, longitude: Double): CurrentWeather
}

class OpenMeteoWeatherRepository(
    private val api: ForecastApi = ApiClient.forecastApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast = withContext(ioDispatcher) {
        // Sütun biçimindeki yanıtı ekranın beklediği satır biçimine çevirir.
        api.getForecast(latitude = latitude, longitude = longitude).toForecast()
    }

    override suspend fun getCurrent(latitude: Double, longitude: Double): CurrentWeather = withContext(ioDispatcher) {
        api.getCurrent(latitude = latitude, longitude = longitude).toForecast().current
    }
}
