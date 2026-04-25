package com.example.bgls.Retrofit
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.DataModels.ReferenceCode
import retrofit2.Call
import retrofit2.http.*

interface ServiceApi {
    @GET("api/refCodeMain")
    fun getRefList(
        @Query("formmode") formmode: String = "list"
    ): Call<RefResponse>
}