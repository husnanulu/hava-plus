package com.kampplus.hava.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.ui.theme.HavaSky

/*
 * Görsel kimlik v2 hazır bileşeni: logodaki derece halkası (Canvas + drawCircle, içi boş Stroke).
 * Yalnızca ana ekrandaki büyük sıcaklık BigTemperature ile çizilir; küçük sıcaklıklar normal "°" kullanır.
 */

/** Hava+ marka derece işareti: içi boş mavi halka. */
@Composable
fun DegreeRing(size: Dp, modifier: Modifier = Modifier, color: Color = HavaSky) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = this.size.minDimension * 0.22f
        drawCircle(
            color = color,
            radius = (this.size.minDimension - strokeWidth) / 2f,
            style = Stroke(width = strokeWidth)
        )
    }
}

/** Büyük sıcaklık: sayı + sağ üstte halka (rengi gökyüzü stilinden gelir). Sadece ana ekrandaki büyük sıcaklıkta kullanılır. */
@Composable
fun BigTemperature(
    value: Int,
    textColor: Color,
    modifier: Modifier = Modifier,
    ringColor: Color = HavaSky,
    fontSize: TextUnit = 88.sp
) {
    val ringSize = (fontSize.value * 0.34f).dp
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Text(
            text = value.toString(),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.width(4.dp))
        DegreeRing(size = ringSize, modifier = Modifier.padding(top = (fontSize.value * 0.16f).dp), color = ringColor)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFD21F)
@Composable
private fun BigTemperaturePreview() {
    BigTemperature(value = 21, textColor = Color(0xFF1F2430))
}
