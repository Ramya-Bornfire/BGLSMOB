package com.example.bgls.Retrofit
import com.example.bgls.DataModels.OrganizationResponse
import com.example.bgls.DataModels.RefResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ServiceApi {
    @GET("api/refCodeMain")
    fun getRefList(
        @Query("formmode") formmode: String = "list"
    ): Call<RefResponse>

    @GET("api/organizationDetails")
    suspend fun getOrganizationDetails(
        @Query("formmode") formmode: String? = "add"
    ): Response<OrganizationResponse>

}