package com.kampplus.hava.ui.component

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Durum çubuğundaki saat/pil ikonlarının rengini ayarlar.
 * Ana ekranın mavi/koyu üst alanında açık (beyaz) ikon, Şehir seç'in açık zemininde koyu ikon gerekir.
 * SideEffect: her başarılı çizimden sonra çalışan, Compose dışındaki dünyaya dokunan blok.
 */
@Composable
fun StatusBarIcons(onDarkBackground: Boolean) {
    val view = LocalView.current
    // @Preview içinde gerçek bir pencere yoktur.
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as Activity).window
        // "Light status bar" = açık zemine uygun, yani KOYU ikonlar.
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !onDarkBackground
    }
}
