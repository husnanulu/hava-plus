package com.kampplus.hava.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.theme.BadgeBlue
import com.kampplus.hava.ui.theme.CardWhite
import com.kampplus.hava.ui.theme.HavaTheme

/*
 * Şehir seç ekranındaki tek bir şehir satırı (kamptaki CityCard'ın Hava+ görünümü).
 * Hem favoriler hem arama sonuçları bu satırı kullanır; sağ taraf (sıcaklık, yıldız) dışarıdan verilir.
 */

/**
 * @param showBadge Arama sonuçlarında soldaki ülke kodu rozeti ("TR") gösterilir.
 * @param trailing Satırın sağındaki içerik (sıcaklık, ikon, yıldız butonu).
 */
@Composable
fun CityRow(
    city: City,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showBadge) CountryBadge(code = city.countryCode)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = city.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (city.subtitle.isNotEmpty()) {
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
    }
}

/** Ülke kodu rozeti ("TR"). Kod gelmezse "?" gösterilir. */
@Composable
private fun CountryBadge(code: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BadgeBlue),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = code ?: "?",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CityRowPreview() {
    HavaTheme {
        Surface(color = CardWhite) {
            CityRow(
                city = City(
                    id = 323786,
                    name = "Ankara",
                    latitude = 39.91987,
                    longitude = 32.85427,
                    countryCode = "TR",
                    country = "Türkiye",
                    admin1 = "Ankara"
                ),
                onClick = {},
                showBadge = true
            )
        }
    }
}
