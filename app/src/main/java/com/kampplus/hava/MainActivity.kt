package com.kampplus.hava

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.kampplus.hava.data.AppContainer
import com.kampplus.hava.ui.HavaNavHost
import com.kampplus.hava.ui.theme.HavaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Uygulamanın tek Activity'si; kullanıcının girdiği kapı.
 *
 * Activity bir **pencereyi** yönetir. Pencerenin içine ne çizileceğini ise
 * Compose söyler: sahne Activity'dir, dekoru Compose kurar.
 */
class MainActivity : ComponentActivity() {

    /** true olduğu sürece açılış ekranı ekranda kalır. */
    private var keepSplash = true

    /**
     * Ekran oluşturulurken Android bu fonksiyonu **kendisi** çağırır; biz çağırmayız.
     * Yaşam döngüsünün ilk adımıdır.
     *
     * Logcat'i açıp uygulamayı arka plana alıp geri dönerek `onCreate` / `onStart` /
     * `onResume` sırasını gözlemleyebilirsin.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Açılış ekranı (lacivert zemin + ikon). super.onCreate'ten ÖNCE çağrılmalı.
        // İlk tahmin gelene kadar açık kalır; ama yapay bekletme yok: en fazla 2 saniye.
        installSplashScreen().setKeepOnScreenCondition { keepSplash }
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        // Konum ve DataStore repository'leri uygulama Context'ine ihtiyaç duyar.
        AppContainer.init(this)
        lifecycleScope.launch {
            delay(MAX_SPLASH_MILLIS)
            keepSplash = false
        }

        // İçeriği durum ve navigasyon çubuklarının altına kadar yayar.
        enableEdgeToEdge()

        // Süslü parantezin içi Compose dünyasıdır.
        setContent {
            HavaTheme {
                HavaNavHost(onHomeReady = { keepSplash = false })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    private companion object {
        /** Logcat'te filtrelemek için: arama kutusuna `Lifecycle` yaz. */
        const val TAG = "Lifecycle"

        /** Açılış ekranı en fazla bu kadar bekler (milisaniye). */
        const val MAX_SPLASH_MILLIS = 2_000L
    }
}
