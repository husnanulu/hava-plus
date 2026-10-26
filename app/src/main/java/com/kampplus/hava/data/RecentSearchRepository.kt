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
 * "Son aramalar": arama sonucundan seçilen son 5 şehir, en yenisi başta.
 * Favorilerle aynı kalıp (FavoritesRepository): liste DataStore'da Gson ile JSON olarak durur,
 * `recentSearches` bir Flow'dur; Şehir seç ekranı değişiklikleri kendiliğinden görür.
 */
interface RecentSearchRepository {
    /** En yenisi başta, en fazla [MAX_RECENT] şehir. */
    val recentSearches: Flow<List<City>>

    /** Şehri en üste koyar. Listede zaten varsa kopyası oluşmaz, yalnızca en üste taşınır. */
    suspend fun add(city: City)

    /** Tek bir şehri listeden çıkarır (satırdaki X). */
    suspend fun remove(city: City)

    /** Bütün listeyi siler ("Temizle"). */
    suspend fun clear()

    companion object {
        const val MAX_RECENT = 5
    }
}

class DataStoreRecentSearchRepository(
    private val dataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RecentSearchRepository {

    private val gson = Gson()

    override val recentSearches: Flow<List<City>> = dataStore.data.map { preferences -> decode(preferences[RECENT_KEY]) }

    override suspend fun add(city: City) {
        // Önce aynı şehir varsa çıkarılır, sonra başa eklenir; fazlası sondan kesilir.
        update { current -> (listOf(city) + current.filterNot { it.id == city.id }).take(RecentSearchRepository.MAX_RECENT) }
    }

    override suspend fun remove(city: City) {
        update { current -> current.filterNot { it.id == city.id } }
    }

    override suspend fun clear() {
        update { emptyList() }
    }

    /** Kayıtlı listeyi okur, [change] ile yenisini üretir ve geri yazar. */
    private suspend fun update(change: (List<City>) -> List<City>) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                preferences[RECENT_KEY] = gson.toJson(change(decode(preferences[RECENT_KEY])))
            }
        }
    }

    /** JSON → liste. Kayıt yoksa ya da bozuksa boş liste. */
    private fun decode(json: String?): List<City> {
        if (json == null) return emptyList()
        return try {
            gson.fromJson(json, Array<City>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Son aramalar çözümlenemedi", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "RecentSearchRepository"
        val RECENT_KEY = stringPreferencesKey("recent_searches")
    }
}
