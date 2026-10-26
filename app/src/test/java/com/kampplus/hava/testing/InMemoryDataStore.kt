package com.kampplus.hava.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Testler için dosyasız DataStore: değerler yalnızca bellekte durur.
 * Gerçek DataStore aynı arayüzü uygular; repository'ler farkı anlamaz.
 * (Gerçek dosyalı DataStore, Windows'ta JVM testinde dosyayı yeniden adlandıramadığı için bunu kullanıyoruz.)
 */
class InMemoryDataStore : DataStore<Preferences> {

    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
