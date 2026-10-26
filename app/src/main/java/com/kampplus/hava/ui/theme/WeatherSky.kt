package com.kampplus.hava.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.max

/*
 * Ana ekran üst alanının "gökyüzü" zemini: her hava grubu için hazır renkler (SkyStyle) ve onları çizen Modifier.
 * Kamptaki data class + object kalıbı; çizim Brush.verticalGradient / radialGradient + drawWithCache ile.
 * Hangi havanın hangi stile düştüğüne ui/home/WeatherStyle.kt → skyStyle() karar verir.
 */

/** Gökyüzündeki ışık parlaması: (x, y) 0..1 arası konum, reach = en uzak köşeye göre yarıçap oranı. */
data class SkyGlow(val x: Float, val y: Float, val color: Color, val reach: Float)

/** Ana ekran üst alanının hava durumuna göre görünümü. */
data class SkyStyle(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val glows: List<SkyGlow> = emptyList(),
    val ink: Color,   // büyük sıcaklık, şehir adı, durum yazısı, butonlar
    val sub: Color,   // en yüksek / en düşük satırı
    val ring: Color   // derece halkası
) {
    /** Yazılar beyazsa zemin koyu sayılır (status bar ikonları, izin bandı butonu). */
    val isDark: Boolean get() = ink == Color.White
}

object WeatherSky {
    val Clear = SkyStyle(
        top = Color(0xFF1E78D6), middle = Color(0xFF4EA6EC), bottom = Color(0xFFF6D9A0),
        glows = listOf(
            SkyGlow(0.82f, 0.08f, Color(0xFFFFF4C8), 0.14f),
            SkyGlow(0.82f, 0.08f, Color(0x8CFFD678), 0.42f)
        ),
        ink = Color.White, sub = Color(0xFFF2F8FF), ring = Color.White
    )
    val PartlyCloudy = SkyStyle(
        top = Color(0xFF4F86BF), middle = Color(0xFF8FB5DA), bottom = Color(0xFFD6E3EF),
        glows = listOf(
            SkyGlow(0.80f, 0.10f, Color(0x8CFFF0BE), 0.28f),
            SkyGlow(0.20f, 0.70f, Color(0x73FFFFFF), 0.45f)
        ),
        ink = Color.White, sub = Color(0xFFEEF4FA), ring = Color.White
    )
    val Overcast = SkyStyle( // sis dahil
        top = Color(0xFF6F7A89), middle = Color(0xFF9AA3B0), bottom = Color(0xFFC9CED6),
        glows = listOf(SkyGlow(0.5f, 0f, Color(0x40FFFFFF), 0.60f)),
        ink = Color.White, sub = Color(0xFFF0F2F5), ring = HavaSky
    )
    val Rain = SkyStyle( // çisenti dahil
        top = Color(0xFF1F2A3D), middle = Color(0xFF3E4D66), bottom = Color(0xFF7C8BA3),
        glows = listOf(SkyGlow(0.5f, 1f, Color(0x59AABED7), 0.55f)),
        ink = Color.White, sub = Color(0xFFDCE3EE), ring = HavaSky
    )
    val Snow = SkyStyle(
        top = Color(0xFF8FA7C2), middle = Color(0xFFC4D3E3), bottom = Color(0xFFF2F6FA),
        glows = listOf(SkyGlow(0.5f, 1f, Color(0xE6FFFFFF), 0.50f)),
        ink = Color(0xFF1F2430), sub = Color(0xFF34495F), ring = HavaSky
    )
    val Storm = SkyStyle(
        top = Color(0xFF120F24), middle = Color(0xFF2C2848), bottom = Color(0xFF55557A),
        glows = listOf(SkyGlow(0.28f, 0.18f, Color(0x59BEAAFF), 0.30f)),
        ink = Color.White, sub = Color(0xFFCFCDE3), ring = HavaSky
    )
}

/** Üst alanın arka planını çizer: önce 3 renkli dikey geçiş, üstüne ışık parlamaları. */
fun Modifier.weatherSky(style: SkyStyle): Modifier = drawWithCache {
    val linear = Brush.verticalGradient(
        0f to style.top,
        0.55f to style.middle,
        1f to style.bottom
    )
    val glows = style.glows.map { g ->
        val c = Offset(size.width * g.x, size.height * g.y)
        val farthest = max(
            max(hypot(c.x, c.y), hypot(size.width - c.x, c.y)),
            max(hypot(c.x, size.height - c.y), hypot(size.width - c.x, size.height - c.y))
        )
        Brush.radialGradient(
            colors = listOf(g.color, g.color.copy(alpha = 0f)),
            center = c,
            radius = (farthest * g.reach).coerceAtLeast(1f)
        )
    }
    onDrawBehind {
        drawRect(linear)
        glows.asReversed().forEach { drawRect(it) } // listedeki ilk parlama en üstte
    }
}

@Preview(widthDp = 390, heightDp = 300)
@Composable
private fun WeatherSkyClearPreview() {
    Box(Modifier.fillMaxWidth().height(300.dp).weatherSky(WeatherSky.Clear))
}

@Preview(widthDp = 390, heightDp = 300)
@Composable
private fun WeatherSkyStormPreview() {
    Box(Modifier.fillMaxWidth().height(300.dp).weatherSky(WeatherSky.Storm))
}
