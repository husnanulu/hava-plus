package com.kampplus.hava.model

/**
 * Hava durumunun ekrandaki karşılığı: Türkçe etiket ve havaya göre müzik önerisi.
 * (Kamptaki emojiler yerine artık çizgi ikonlar kullanılıyor: ui/component/HavaIcons.kt.)
 *
 * @param musicTitle fizy kartındaki başlığın ilk kısmı ("Güneşli günün müziği").
 * @param musicLabel Önerilen tür ("Pop"). Kartta "Güneşli günün müziği: Pop" olarak birleşir.
 * @param fizyUrl Açılacak çalma listesi; linkler [FizyPlaylists] içinde tek yerde durur.
 */
enum class WeatherCondition(
    val label: String,
    val musicTitle: String,
    val musicLabel: String,
    val fizyUrl: String
) {
    CLEAR("Açık", "Güneşli günün müziği", "Pop", FizyPlaylists.SUNNY),
    MAINLY_CLEAR("Az bulutlu", "Güneşli günün müziği", "Pop", FizyPlaylists.SUNNY),
    PARTLY_CLOUDY("Parçalı bulutlu", "Bulutlu günün müziği", "Akustik", FizyPlaylists.PARTLY_CLOUDY),
    OVERCAST("Kapalı", "Kapalı günün müziği", "Lo-fi", FizyPlaylists.OVERCAST),
    FOG("Sisli", "Kapalı günün müziği", "Lo-fi", FizyPlaylists.OVERCAST),
    DRIZZLE("Çisenti", "Yağmurlu günün müziği", "Sakin", FizyPlaylists.RAINY),
    RAIN("Yağmurlu", "Yağmurlu günün müziği", "Sakin", FizyPlaylists.RAINY),
    SNOW("Karlı", "Karlı günün müziği", "Kış", FizyPlaylists.SNOWY),
    RAIN_SHOWERS("Sağanak yağış", "Yağmurlu günün müziği", "Sakin", FizyPlaylists.RAINY),
    SNOW_SHOWERS("Kar sağanağı", "Karlı günün müziği", "Kış", FizyPlaylists.SNOWY),
    THUNDERSTORM("Gök gürültülü fırtına", "Fırtınalı günün müziği", "Rock", FizyPlaylists.STORMY),
    UNKNOWN("Bilinmiyor", "Günün müziği", "Pop", FizyPlaylists.SUNNY);

    /** fizy kartındaki başlık: "Güneşli günün müziği: Pop". */
    val musicText: String
        get() = "$musicTitle: $musicLabel"

    /** Yağmur, çisenti ya da sağanak mı? (Öneri motoru "şemsiyesiz çıkma" der.) */
    val isRain: Boolean
        get() = this == DRIZZLE || this == RAIN || this == RAIN_SHOWERS

    /** Kar ya da kar sağanağı mı? */
    val isSnow: Boolean
        get() = this == SNOW || this == SNOW_SHOWERS

    companion object {
        /** WMO 4677 kod tablosu. Tanınmayan kod gelirse UNKNOWN döner. */
        fun fromWeatherCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1 -> MAINLY_CLEAR
            2 -> PARTLY_CLOUDY
            3 -> OVERCAST
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67 -> RAIN
            71, 73, 75, 77 -> SNOW
            80, 81, 82 -> RAIN_SHOWERS
            85, 86 -> SNOW_SHOWERS
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}
