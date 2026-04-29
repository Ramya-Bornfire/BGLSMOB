package com.example.bgls.Retrofit
import com.example.bgls.DataModels.AccessRoleRequest
import com.example.bgls.DataModels.BusinessActivityResponse
import com.example.bgls.DataModels.CalendarResponse
import com.example.bgls.DataModels.EmployeeListResponse
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.DataModels.OrganizationResponse
import com.example.bgls.DataModels.OrganizationViewResponse
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.DataModels.SingleEmployeeResponse
import com.example.bgls.DataModels.SingleUserResponse
import com.example.bgls.DataModels.UserProfile
import com.example.bgls.DataModels.UserProfileResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    suspend fun getBranchDetailsView(
        @Query("formmode") formmode: String = "view",
        @Query("branch_code") branchCode: String
    ): Response<OrganizationViewResponse>
    @POST("tab2modify")
    suspend fun updateBranch(@Body body: RequestBody): Response<ResponseBody>
    @FormUrlEncoded
    @POST("tab2Del")
    suspend fun deleteBranch(@Field("branch_code") branchCode: String): Response<ResponseBody>
    @FormUrlEncoded
    @POST("OrgBranchAdd")
    suspend fun addBranch(@FieldMap params: Map<String, String>): Response<ResponseBody>
    @GET("api/userProfile")
    fun getUserProfiles(@Query("formmode") formmode: String = "list"): Call<UserProfileResponse>

    @GET("api/userProfile")
    fun getUserDetail(
        @Query("formmode") formmode: String,
        @Query("userid") userId: String
    ): Call<SingleUserResponse>

    @POST("api/userProfile")
    fun createUser(@Body user: UserProfile): Call<Void>

    @PUT("api/userProfile")
    fun updateUser(@Body user: UserProfile): Call<Void>

    @DELETE("api/userProfile")
    fun deleteUser(@Query("userid") userId: String): Call<Void>

    @POST("api/verifyUser")
    fun verifyUser(@Query("userId") userId: String): Call<Void>

    @POST("api/resetPassword")
    fun resetPassword(@Query("userid") userId: String): Call<Void>

    @POST("api/accessRoleSubmit")
    fun submitAccessRole(@Body accessRole: AccessRoleRequest): Call<Void>


    @GET("api/employeeProfile")
    fun getEmployeeProfiles(@Query("formmode") formmode: String = "list"): Call<EmployeeListResponse>

    @GET("api/employeeProfile")
    fun getEmployeeDetail(
        @Query("formmode") formmode: String,
        @Query("employee_id") employeeId: String
    ): Call<SingleEmployeeResponse>

    @POST("api/employeeProfile")
    fun createEmployee(@Body employee: EmployeeProfile): Call<Void>

    @PUT("api/employeeProfile")
    fun updateEmployee(@Body employee: EmployeeProfile): Call<Void>

    @DELETE("api/employeeProfile")
    fun deleteEmployee(@Query("employee_id") employeeId: String): Call<Void>

    @GET("api/organizationDetails")
    suspend fun getCalendar(
        @Query("formmode") formmode: String,
        @Query("year") year: String,
        @Query("month") month: String?
    ): Response<CalendarResponse>

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

    @GET("api/useractivities")
    fun getUserActivities(
        @Query("formmode") formmode: String?,
        @Query("Fromdate") fromDate: String?,
        @Query("ListFlg") listFlg: String?
    ): Call<Map<String, Any>>

    @GET("api/serviceactivities")
    fun getServiceActivities(
        @Query("formmode") formmode: String = "list",
        @Query("Fromdate") fromDate: String,
        @Query("ListFlg") listFlg: String = "Y"
    ): Call<BusinessActivityResponse>
}