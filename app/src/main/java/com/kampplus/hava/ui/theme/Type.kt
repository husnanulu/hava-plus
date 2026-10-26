package com.kampplus.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.kampplus.hava.R

/**
 * Yazı tipi ölçeği. Tema üç dosyadan oluşur: Color, Theme, Type.
 *
 * Plus Jakarta Sans'ın dört ağırlığı `res/font` altındadır. Bileşenler boyutu elle yazmaz;
 * `MaterialTheme.typography.titleMedium` gibi **rol** isimleriyle okur.
 */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

/**
 * Kısaltma: verilen boyut ve ağırlıkta Plus Jakarta Sans stili.
 * "liga 0": bitişik harfler (ligatür) kapalı. Açıkken "fi" tek harf gibi çizilip i'nin noktası
 * kayboluyor; Türkçede "fizy" "fızy" okunuyordu.
 */
private fun jakarta(size: TextUnit, weight: FontWeight, letterSpacing: TextUnit = 0.sp) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = size,
    fontWeight = weight,
    letterSpacing = letterSpacing,
    fontFeatureSettings = "liga 0"
)

internal val HavaTypography = Typography(
    // Ana ekrandaki büyük sıcaklık: 88sp, en kalın, harfler sıkışık.
    displayLarge = jakarta(88.sp, FontWeight.ExtraBold, (-3).sp).copy(lineHeight = 88.sp),
    displayMedium = jakarta(44.sp, FontWeight.ExtraBold),
    displaySmall = jakarta(36.sp, FontWeight.ExtraBold),
    headlineLarge = jakarta(28.sp, FontWeight.ExtraBold),
    headlineMedium = jakarta(24.sp, FontWeight.ExtraBold),
    // Ekran başlığı ("Şehir seç").
    headlineSmall = jakarta(22.sp, FontWeight.ExtraBold),
    // Öneri cümlesi.
    titleLarge = jakarta(20.sp, FontWeight.ExtraBold).copy(lineHeight = 26.sp),
    // Bölüm başlıkları ("Saatlik", "7 Günlük").
    titleMedium = jakarta(17.sp, FontWeight.ExtraBold),
    titleSmall = jakarta(15.sp, FontWeight.Bold),
    bodyLarge = jakarta(16.sp, FontWeight.Normal),
    bodyMedium = jakarta(15.sp, FontWeight.Normal),
    bodySmall = jakarta(13.sp, FontWeight.Normal),
    // Butonlar ve haplar.
    labelLarge = jakarta(13.sp, FontWeight.Bold),
    labelMedium = jakarta(12.sp, FontWeight.SemiBold),
    labelSmall = jakarta(11.sp, FontWeight.Bold)
)
