package com.kampplus.hava.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Hava+ marka renkleri (docs/HAVAPLUS_TASARIM.md → Bölüm 1).
 *
 * Renkler burada **isimlendirilir**, [Theme] dosyasında Material rollerine dağıtılır.
 * Bileşenler çoğunlukla `MaterialTheme.colorScheme` üzerinden okur; havaya göre değişen
 * üst alan ve derece halkası gibi "markaya özel" yerler buradaki sabitleri doğrudan kullanır.
 */
// Görsel kimlik v2: üç marka rengi. Uygulama ikonu ve açılış ekranında da (havaplus_colors.xml) aynılarıdır.
/** Ana marka rengi: ikon zemini, büyük sıcaklıktaki derece halkası. */
val HavaSky = Color(0xFF4FB0EE)

/** Logodaki artı işaretinin sarısı; birincil eylem butonları. */
val HavaYellow = Color(0xFFFFD21F)

/** Koyu metin (açık zeminli üst alan, öneri kartı). */
val HavaInk = Color(0xFF1F2430)

val Navy = Color(0xFF1A2A6C)
val Blue = Color(0xFF2D55D6)
val DarkYellow = Color(0xFFE0A800)
val ScreenBackground = Color(0xFFF4F6FC)
val CardWhite = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFF4A5788)
val DividerColor = Color(0xFFE8ECF6)
val BorderColor = Color(0xFFE3E7F4)
val BadgeBlue = Color(0xFFE7ECFB)

/** Lacivert Konumum kartı üzerindeki ikincil yazılar. */
val OnDarkTextMuted = Color(0xFFC9D3F5)

/** Hata rengi (Material varsayılanı). */
val ErrorRed = Color(0xFFBA1A1A)
