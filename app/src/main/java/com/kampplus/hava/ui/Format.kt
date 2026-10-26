package com.kampplus.hava.ui

import kotlin.math.roundToInt

/*
 * Ekranlarda ortak kullanılan küçük biçimlendirme fonksiyonları.
 * Kamptaki ForecastScreen'de dosyaya özeldiler; artık iki ekran kullandığı için buraya taşındı.
 */

/** 18.4 → "18°". */
fun degrees(celsius: Double) = "${celsius.roundToInt()}°"

/** Yağış olasılığı yalnızca sıfırdan büyükse gösterilir: 40 → "%40", 0 → null. */
fun percent(value: Int?) = value?.takeIf { it > 0 }?.let { "%$it" }
