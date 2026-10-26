package com.kampplus.hava.data

import android.content.Context

/*
 * Context isteyen repository'lerin tek adresi. ApiClient nesnesiyle aynı fikir: `object` + `by lazy`.
 * MainActivity açılırken init() bir kez çağrılır; ViewModel'ler repository'leri buradan alır.
 * (Hilt gibi bir kütüphane yerine elle yazılmış, en basit "bağımlılık kutusu".)
 */
object AppContainer {

    private lateinit var appContext: Context

    fun init(context: Context) {
        // Activity değil uygulama Context'i saklanır; Activity kapanınca bellekte asılı kalmaz.
        appContext = context.applicationContext
    }

    val locationRepository: LocationRepository by lazy { AndroidLocationRepository(appContext) }

    val lastPlaceRepository: LastPlaceRepository by lazy { DataStoreLastPlaceRepository(appContext.havaDataStore) }

    // Ana ekran ve Şehir seç aynı nesneyi kullanır; ikisi de aynı listeyi dinler.
    val favoritesRepository: FavoritesRepository by lazy { DataStoreFavoritesRepository(appContext.havaDataStore) }

    val recentSearchRepository: RecentSearchRepository by lazy { DataStoreRecentSearchRepository(appContext.havaDataStore) }
}
