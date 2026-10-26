package com.kampplus.hava.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.R
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.ui.theme.FizyCardColors
import com.kampplus.hava.ui.theme.HavaTheme

/*
 * Ana ekrandaki fizy kartı: nota ikonu, havaya göre çalma listesi adı ve "fizy'de dinle" butonu.
 * Hangi havada hangi listenin açılacağı WeatherCondition'da; burada yalnızca görünüm var.
 * Renkler ui/theme/FizyCardColors.kt'de tek yerde durur.
 */

/** Kartın tamamı da, sağdaki buton da aynı linki açar ([onListenClick]). */
@Composable
fun FizyCard(condition: WeatherCondition, onListenClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onListenClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = FizyCardColors.Background
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NoteCircle()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = condition.musicText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = FizyCardColors.Title
                )
                Text(
                    text = stringResource(R.string.fizy_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = FizyCardColors.Subtitle
                )
            }
            ListenButton(onClick = onListenClick)
        }
    }
}

/** 44dp yuvarlak, mor → pembe çapraz geçişli zemin, içinde beyaz nota. */
@Composable
private fun NoteCircle() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            // linearGradient: başlangıç ve bitiş verilmezse sol üstten sağ alta, yani çapraz geçiş.
            .background(Brush.linearGradient(listOf(FizyCardColors.AccentStart, FizyCardColors.AccentEnd))),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Hap şeklinde, soldan sağa mor → pembe geçişli "fizy'de dinle". Görünen hap küçük ama dokunma alanı en az 48dp. */
@Composable
private fun ListenButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.fizy_listen),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
            color = FizyCardColors.Title,
            modifier = Modifier
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(FizyCardColors.AccentStart, FizyCardColors.AccentEnd)))
                .padding(horizontal = 13.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FizyCardPreview() {
    HavaTheme {
        FizyCard(
            condition = WeatherCondition.RAIN,
            onListenClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
