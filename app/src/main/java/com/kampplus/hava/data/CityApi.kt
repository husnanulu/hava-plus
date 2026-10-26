package com.kampplus.hava.data

import com.kampplus.hava.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {

    // Örnek: GET .../v1/search?name=Ankara&count=10&language=tr
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr"
    ): CitySearchResponse
}
