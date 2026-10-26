package com.kampplus.hava.model

import com.google.gson.annotations.SerializedName

/** Şehir arama yanıtı. Sonuç yoksa servis "results" alanını hiç göndermez. */
data class CitySearchResponse(
    val results: List<City>?
)

data class City(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    // JSON'da alt çizgili (country_code), Kotlin'de camelCase (countryCode).
    @SerializedName("country_code")
    val countryCode: String?,
    val country: String?,
    // Bazı kayıtlarda bölge bilgisi gelmez; soru işareti çökmeyi önler.
    val admin1: String?
) {
    /** Kartta gösterilen alt yazı: "Ankara, Türkiye". */
    val subtitle: String
        get() = listOfNotNull(admin1, country).distinct().joinToString(", ")
}
