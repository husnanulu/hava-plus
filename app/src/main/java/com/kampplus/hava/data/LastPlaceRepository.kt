package com.kampplus.hava.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.model.City
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/*
 * "Son bakılan yer": konum izni yoksa uygulama bu yerle açılır (docs/HAVAPLUS_TASARIM.md → Bölüm 4).
 * Kamptaki Repository deseni: interface + gerçek uygulama. Şehir, projede zaten olan Gson ile JSON'a çevrilip saklanır.
 */
interface LastPlaceRepository {
    /** Daha önce hiç yer gösterilmediyse null. */
    suspend fun lastPlace(): City?

    suspend fun saveLastPlace(city: City)
}

class DataStoreLastPlaceRepository(
    private val dataStore: DataStore<Preferences>,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LastPlaceRepository {

    private val gson = Gson()

    override suspend fun lastPlace(): City? = withContext(ioDispatcher) {
        try {
            // data bir Flow'dur; first() o anki değeri bir kez okur.
            val json = dataStore.data.first()[LAST_PLACE_KEY] ?: return@withContext null
            gson.fromJson(json, City::class.java)
        } catch (e: IOException) {
            Log.e(TAG, "Son yer okunamadı", e)
            null
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Son yer çözümlenemedi", e)
            null
        }
    }

    override suspend fun saveLastPlace(city: City) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences -> preferences[LAST_PLACE_KEY] = gson.toJson(city) }
        }
    }

    private companion object {
        const val TAG = "LastPlaceRepository"
        val LAST_PLACE_KEY = stringPreferencesKey("last_place")
    }
}
