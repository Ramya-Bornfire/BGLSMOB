package com.example.bgls.Retrofit
import com.example.bgls.DataModels.CalendarResponse
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

    @GET("api/organizationDetails")
    suspend fun getCalendar(
        @Query("formmode") formmode: String,
        @Query("year") year: String,
        @Query("month") month: String?
    ): Response<CalendarResponse>

    @GET("api/employeeProfile")
    fun getEmployeeProfile(
        @Query("formmode") formmode: String?,
        @Query("employee_id") employeeId: String?
    ): Call<Map<String, Any>>
    @GET("api/glcode")
    fun getGLCode(
        @Query("formmode") formmode: String?,
        @Query("glcode") glcode: String?,
        @Query("glsh_Code") glshCode: String?
    ): Call<Map<String, Any>>
    @GET("api/parameters")
    fun getParameters(
        @Query("formmode") formmode: String?
    ): Call<Map<String, Any>>

    @GET("api/parameters/view")
    fun viewParameter(
        @Query("id") id: String
    ): Call<Map<String, Any>>

    @GET("api/parameters/add")
    fun addParameter(): Call<Map<String, Any>>

    @GET("api/parameters/update")
    fun updateParameter(
        @Query("id") id: String
    ): Call<Map<String, Any>>

    @GET("api/parameters/delete")
    fun deleteParameter(
        @Query("id") id: String
    ): Call<Map<String, Any>>

    @GET("api/chartOfAccounts")
    fun getChartOfAccounts(
        @Query("formmode") formmode: String?,
        @Query("acct_num") acctNum: String?
    ): Call<Map<String, Any>>
    @GET("api/accountLedger")
    fun getAccountLedger(
        @Query("formmode") formmode: String?,
        @Query("acct_num") acctNum: String?
    ): Call<Map<String, Any>>
    @GET("api/transactionsAccounts")
    fun getTransactionsAccounts(
        @Query("formmode") formmode: String?,
        @Query("id") id: Long?
    ): Call<Map<String, Any>>
    @GET("api/serviceactivities")
    fun getServiceActivities(
        @Query("formmode") formmode: String?,
        @Query("Fromdate") fromDate: String?,
        @Query("ListFlg") listFlg: String?
    ): Call<Map<String, Any>>

}