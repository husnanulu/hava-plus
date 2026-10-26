package com.kampplus.hava.model

import kotlin.math.roundToLong

/*
 * Telefonun konumu: yalnızca enlem ve boylam. LocationRepository bunu döndürür.
 * Kamp konusu: data class (Uygulama 2).
 */
data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * Konumu bir City'ye çevirir; böylece konum da diğer şehirler gibi gösterilir, kaydedilir, favorilere eklenir.
 * Konumun Open-Meteo kimliği yoktur. Koordinat 2 basamağa (~1 km) yuvarlanıp NEGATİF bir sayı
 * üretilir: gerçek şehir kimlikleri hep pozitif olduğu için ikisi asla karışmaz.
 */
fun Coordinates.toCity(name: String): City = City(
    id = -(((latitude + 90) * 100).roundToLong() * 100_000 + ((longitude + 180) * 100).roundToLong()),
    name = name,
    latitude = latitude,
    longitude = longitude,
    countryCode = null,
    country = null,
    admin1 = null
)
