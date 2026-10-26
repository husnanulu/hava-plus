package com.kampplus.hava.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Hava+ paleti. Her Material rolü ([Color.kt]'deki) bir marka rengine bağlanır:
 * `primary` vurgu mavisi, `background` ekran zemini, `surface` kart beyazı,
 * `onSurface` kart üstündeki lacivert yazıdır.
 */
private val HavaColors = lightColorScheme(
    primary = Blue,
    onPrimary = CardWhite,
    primaryContainer = BadgeBlue,
    onPrimaryContainer = Navy,
    secondary = HavaYellow,
    onSecondary = Navy,
    secondaryContainer = HavaYellow,
    onSecondaryContainer = Navy,
    background = ScreenBackground,
    onBackground = Navy,
    surface = CardWhite,
    onSurface = Navy,
    surfaceVariant = BorderColor,
    onSurfaceVariant = SecondaryText,
    surfaceContainer = CardWhite,
    surfaceContainerLow = CardWhite,
    surfaceContainerHighest = CardWhite,
    outline = BorderColor,
    outlineVariant = DividerColor,
    error = ErrorRed
)

/**
 * Uygulamanın teması. Bütün ekranlar bunun içine sarmalanır.
 *
 * `dynamicColor` (Android 12'nin duvar kâğıdından renk alması) bilerek KULLANILMAZ:
 * marka renkleri her telefonda aynı görünmeli. Karanlık tema şimdilik yok.
 */
@Composable
fun HavaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HavaColors,
        typography = HavaTypography,
        content = content
    )
}
