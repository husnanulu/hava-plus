package com.kampplus.hava.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.model.City
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/*
 * Favori şehirler (docs/HAVAPLUS_TASARIM.md → Bölüm 5). Liste DataStore'da Gson ile JSON olarak durur.
 * `favorites` bir Flow'dur: Ana ekrandaki ☆ ve Şehir seç'teki yıldızlar aynı akışı dinler,
 * birinde değişen liste ötekine kendiliğinden yansır.
 */
interface FavoritesRepository {
    /** Eklenme sırasıyla favoriler; her değişiklikte yeni liste yayınlanır. */
    val favorites: Flow<List<City>>

    /** Favorideyse çıkarır, değilse ekler (en fazla [MAX_FAVORITES]). */
    suspend fun toggle(city: City)

    companion object {
        const val MAX_FAVORITES = 10
    }
}

class DataStoreFavoritesRepository(
    private val dataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoritesRepository {

    private val gson = Gson()

    // map: DataStore'daki her yeni kaydı (JSON metni) şehir listesine çevirir.
    override val favorites: Flow<List<City>> = dataStore.data.map { preferences -> decode(preferences[FAVORITES_KEY]) }

    override suspend fun toggle(city: City) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                val current = decode(preferences[FAVORITES_KEY])
                val updated = when {
                    current.any { it.id == city.id } -> current.filterNot { it.id == city.id }
                    current.size >= FavoritesRepository.MAX_FAVORITES -> current // Liste dolu: eklenmez.
                    else -> current + city // Sona eklenir: sıra = eklenme sırası.
                }
                preferences[FAVORITES_KEY] = gson.toJson(updated)
            }
        }
    }

    /** JSON → liste. Kayıt yoksa ya da bozuksa boş liste. */
    private fun decode(json: String?): List<City> {
        if (json == null) return emptyList()
        return try {
            // Dizi olarak çözüp listeye çeviriyoruz (List<City> için ek tip bilgisi gerekmesin diye).
            gson.fromJson(json, Array<City>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Favoriler çözümlenemedi", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "FavoritesRepository"
        val FAVORITES_KEY = stringPreferencesKey("favorites")
    }
}
