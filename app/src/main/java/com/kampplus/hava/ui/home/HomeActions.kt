package com.kampplus.hava.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.kampplus.hava.R
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.WeatherCondition
import com.kampplus.hava.model.buildAdvice
import com.kampplus.hava.ui.degrees
import java.util.Locale

/*
 * Ana ekranın uygulama dışına çıkan iki eylemi: tahmini paylaşmak ve fizy'yi açmak.
 * İkisi de Intent kullanır: Android'e "bu işi yapabilecek bir uygulama aç" deriz.
 * docs/HAVAPLUS_TASARIM.md → Bölüm 6.
 */

private const val TAG = "HomeActions"

/**
 * Paylaş menüsünü açar. ACTION_SEND + createChooser: telefonda metin paylaşabilen her uygulama
 * (BiP yüklüyse o da) listede çıkar; belirli bir uygulamayı hedeflemiyoruz.
 * Metin: "Hava+ · İstanbul bugün 21°, parçalı bulutlu. Ceket yeterli."
 */
fun shareForecast(context: Context, place: City, forecast: Forecast) {
    val condition = WeatherCondition.fromWeatherCode(forecast.current.weatherCode)
    val text = context.getString(
        R.string.share_text,
        place.name,
        degrees(forecast.current.temperatureC),
        condition.label.lowercase(Locale.forLanguageTag("tr")),
        buildAdvice(forecast).text
    )
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.action_share)))
}

/** fizy linkini açar (uygulama yüklüyse uygulama, değilse tarayıcı). Açacak bir şey yoksa kısa uyarı gösterir. */
fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "Bağlantı açılamadı: $url", e)
        Toast.makeText(context, R.string.link_open_failed, Toast.LENGTH_SHORT).show()
    }
}
