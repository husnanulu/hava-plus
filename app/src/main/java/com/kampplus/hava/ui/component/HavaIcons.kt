package com.kampplus.hava.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import com.kampplus.hava.ui.theme.DarkYellow
import com.kampplus.hava.ui.theme.HavaYellow

/*
 * Hava+'nın çizgi ikonları. Tasarım taslaklarındaki (docs/tasarim) SVG yolları
 * ("M12 3.5l2.6 5.3…") aynen kopyalanıp Compose'un ImageVector'üne çevrildi.
 * Hepsi 24x24'lük bir tuvalde çizilir; boyutu ekranda Modifier.size belirler.
 * Arayüz ikonları Icon(tint = …) ile gösterilir. Hava ikonları ayrı: hazır dosyalar, bkz. WeatherIcons.kt.
 */

/** Tek renkli ikonlar siyah çizilir; ekrandaki rengi Icon'un `tint` parametresi verir. */
private val Ink = Color.Black

/** 24x24 tuval üzerinde, verilen SVG yollarını çizgi olarak çizen kısa yardımcı. */
private fun lineIcon(name: String, strokeWidth: Float, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    paths.forEach { builder.strokePath(it, Ink, strokeWidth) }
    return builder.build()
}

private fun ImageVector.Builder.strokePath(path: String, color: Color, width: Float) = addPath(
    pathData = addPathNodes(path),
    stroke = SolidColor(color),
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round
)

private fun ImageVector.Builder.fillPath(path: String, fill: Color, stroke: Color, width: Float) = addPath(
    pathData = addPathNodes(path),
    fill = SolidColor(fill),
    stroke = SolidColor(stroke),
    strokeLineWidth = width,
    strokeLineJoin = StrokeJoin.Round
)

/** SVG'deki <circle> etiketinin yol karşılığı: iki yarım yay. */
private fun circle(cx: Float, cy: Float, r: Float) = "M${cx - r},${cy}a$r,$r 0 1,0 ${2 * r},0a$r,$r 0 1,0 ${-2 * r},0"

private const val STAR = "M12 3.5l2.6 5.3 5.9.9-4.3 4.1 1 5.8L12 16.9l-5.2 2.7 1-5.8-4.3-4.1 5.9-.9z"
private const val PIN = "M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21z"

/** Arayüzde kullanılan tek renkli çizgi ikonlar. */
object HavaIcons {
    val Star = lineIcon("star", 2f, STAR)
    val Share = lineIcon(
        "share",
        2f,
        circle(18f, 5f, 2.5f),
        circle(6f, 12f, 2.5f),
        circle(18f, 19f, 2.5f),
        "M8.2 10.8l7.6-4.4M8.2 13.2l7.6 4.4"
    )
    val Pin = lineIcon("pin", 2.2f, PIN, circle(12f, 9.5f, 2.5f))
    val ChevronDown = lineIcon("chevronDown", 2.2f, "M6 9l6 6 6-6")
    val Back = lineIcon("back", 2.2f, "M15 5l-7 7 7 7")
    val Search = lineIcon("search", 2.2f, circle(11f, 11f, 6.5f), "M20 20l-4.2-4.2")
    val Umbrella = lineIcon("umbrella", 2f, "M12 3a9 9 0 0 1 9 9H3a9 9 0 0 1 9-9zM12 12v6a2 2 0 0 0 4 0")
    val Thermometer = lineIcon("thermometer", 2f, "M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z")
    val Drop = lineIcon("drop", 2f, "M12 3s6 6.5 6 11a6 6 0 0 1-12 0c0-4.5 6-11 6-11z")
    val Wind = lineIcon("wind", 2f, "M3 8h11a3 3 0 1 0-3-3M3 12h15a3 3 0 1 1-3 3M3 16h8")

    /** Favorideki şehir: sarı dolgulu, koyu sarı çerçeveli yıldız (Image ile gösterilir). */
    val StarFilled: ImageVector = ImageVector.Builder("starFilled", 24.dp, 24.dp, 24f, 24f)
        .fillPath(STAR, fill = HavaYellow, stroke = DarkYellow, width = 1.6f)
        .build()
}
