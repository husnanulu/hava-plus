package com.kampplus.hava.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.kampplus.hava.model.Coordinates
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/*
 * Telefonun konumu ve konumun adı ("Kadıköy, İstanbul").
 * Google Play Services KULLANILMAZ: Android'in kendi LocationManager ve Geocoder sınıfları yeterli.
 * Kamptaki Repository deseni: interface (testte sahtesi) + gerçek uygulama.
 */
interface LocationRepository {
    /** Kullanıcı yaklaşık konum iznini verdi mi? */
    fun hasPermission(): Boolean

    /** Konum bulunamazsa (izin yok, konum kapalı, 5 sn'de cevap yok) null. */
    suspend fun currentLocation(): Coordinates?

    /** "Kadıköy, İstanbul" gibi bir ad; bulunamazsa null. */
    suspend fun placeName(coordinates: Coordinates): String?
}

class AndroidLocationRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocationRepository {

    private val locationManager = context.getSystemService(LocationManager::class.java)

    override fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

    override suspend fun currentLocation(): Coordinates? = withContext(ioDispatcher) {
        if (!hasPermission()) return@withContext null
        try {
            // 1) Telefonun bildiği son konum yeterince yeniyse onu kullan (anında gelir).
            val last = lastKnownLocation()
            if (last != null && isFresh(last)) return@withContext last.toCoordinates()
            // 2) Değilse tek seferlik yeni konum iste; en fazla 5 saniye bekle.
            (requestCurrentLocation() ?: last)?.toCoordinates()
        } catch (e: SecurityException) {
            Log.e(TAG, "Konum izni reddedildi", e)
            null
        }
    }

    /** Bu uygulamanın kullanabildiği açık sağlayıcılar (ağ, gps…) arasındaki en yeni konum. */
    // İzni currentLocation() başında kendimiz kontrol ettiğimiz için Lint uyarısını susturuyoruz.
    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? = locationManager.getProviders(true)
        .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
        .maxByOrNull { it.time }

    private fun isFresh(location: Location) = System.currentTimeMillis() - location.time < FRESH_MILLIS

    /**
     * Tek seferlik konum isteği. Sonuç bir "callback" ile gelir; CompletableDeferred ise
     * "ileride dolacak bir kutu" gibidir: await() kutu dolana kadar bekler.
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): Location? {
        val provider = bestProvider() ?: return null
        val result = CompletableDeferred<Location?>()
        // 5 saniyede cevap gelmezse isteği iptal etmek için.
        val cancellation = CancellationSignal()
        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            cancellation,
            ContextCompat.getMainExecutor(context)
        ) { location: Location? -> result.complete(location) }
        val location = withTimeoutOrNull(TIMEOUT_MILLIS) { result.await() }
        if (location == null) cancellation.cancel()
        return location
    }

    /** Yaklaşık konum için önce ağ, sonra diğerleri. */
    private fun bestProvider(): String? {
        val providers = locationManager.getProviders(true)
        return PROVIDER_ORDER.firstOrNull { it in providers } ?: providers.firstOrNull()
    }

    override suspend fun placeName(coordinates: Coordinates): String? = withContext(ioDispatcher) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            // Bu (eski) sürüm sonucu doğrudan döndürür; IO iş parçacığında olduğumuz için beklemesi sorun değil.
            @Suppress("DEPRECATION")
            val address = Geocoder(context, Locale.forLanguageTag("tr"))
                .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                ?.firstOrNull() ?: return@withContext null
            // İlçe + il: "Kadıköy, İstanbul". Aynı ad iki kez gelirse bir kez yazılır.
            val district = address.subAdminArea ?: address.locality ?: address.subLocality
            listOfNotNull(district, address.adminArea).distinct().joinToString(", ").ifEmpty { null }
        } catch (e: IOException) {
            Log.e(TAG, "Konumun adı bulunamadı", e)
            null
        }
    }

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    private companion object {
        const val TAG = "LocationRepository"
        const val TIMEOUT_MILLIS = 5_000L

        /** 10 dakikadan eski "son konum" kullanılmaz. */
        const val FRESH_MILLIS = 10 * 60 * 1_000L
        val PROVIDER_ORDER = listOf(LocationManager.NETWORK_PROVIDER, "fused", LocationManager.GPS_PROVIDER)
    }
}
