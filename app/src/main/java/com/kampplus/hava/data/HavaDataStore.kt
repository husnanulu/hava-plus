package com.kampplus.hava.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/*
 * Uygulamanın küçük kalıcı ayar dosyası (DataStore Preferences): son bakılan yer, favoriler ve son aramalar.
 * `by preferencesDataStore` sayesinde bütün uygulamada bu dosyanın TEK bir kopyası olur.
 */
val Context.havaDataStore: DataStore<Preferences> by preferencesDataStore(name = "hava_ayarlar")
