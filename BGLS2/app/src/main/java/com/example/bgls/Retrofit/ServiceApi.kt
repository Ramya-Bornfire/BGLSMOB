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
        @Query("formmode") formmode: String? = "add",
        @Query("branch_code") branchCode: String? = null,
        @Query("record_srl") recordSrl: Long? = null,
        @Query("month") month: String? = null,
        @Query("year") year: String? = null
    ): Response<OrganizationResponse>

}