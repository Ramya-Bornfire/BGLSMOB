package com.example.bgls.Retrofit
import com.example.bgls.DataModels.AccessRoleRequest
import com.example.bgls.DataModels.BusinessActivityResponse
import com.example.bgls.DataModels.CalendarResponse
import com.example.bgls.DataModels.ChartAccountApiItem
import com.example.bgls.DataModels.ChartAccountItem
import com.example.bgls.DataModels.ChartOfAccountsAddResponse
import com.example.bgls.DataModels.ChartOfAccountsDetailResponse
import com.example.bgls.DataModels.ChartOfAccountsListResponse
import com.example.bgls.DataModels.CreditFacilityResponse
import com.example.bgls.DataModels.EmployeeListResponse
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.DataModels.JournalEntryAddScreenResponse
import com.example.bgls.DataModels.JournalEntryListResponse
import com.example.bgls.DataModels.JournalEntryViewResponse
import com.example.bgls.DataModels.MassEntryRequest
import com.example.bgls.DataModels.OrganizationResponse
import com.example.bgls.DataModels.OrganizationViewResponse
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.DataModels.SingleEmployeeResponse
import com.example.bgls.DataModels.SingleUserResponse
import com.example.bgls.DataModels.TransactionAccountsResponse
import com.example.bgls.DataModels.TransactionDetailsResponse
import com.example.bgls.DataModels.TransactionDto
import com.example.bgls.DataModels.TransactionMigrationResponse
import com.example.bgls.DataModels.TransactionRequest
import com.example.bgls.DataModels.UserProfile
import com.example.bgls.DataModels.UserProfileResponse
import com.example.bgls.TransactionMaintenance.JournalEntriesActivity
import com.example.bgls.DataModels.LoanClosureDataResponse
import com.example.bgls.DataModels.LoanFlowDetail
import com.example.bgls.DataModels.LoanFlowTransactionRequest
import com.example.bgls.DataModels.LoanOperationResponse
import com.example.bgls.DataModels.MultipleTransactionRequest
import com.example.bgls.DataModels.SettlementRecord
import com.example.bgls.DataModels.AccountBalancesResponse
import com.example.bgls.DataModels.AssetLiabilityResponse
import com.example.bgls.DataModels.BalanceSheetResponse
import com.example.bgls.DataModels.BalancingReportResponse
import com.example.bgls.DataModels.InterestSummaryResponse
import com.example.bgls.DataModels.JournalBookResponse
import com.example.bgls.DataModels.LoanMaintenanceViewResponse
import com.example.bgls.DataModels.ProfitLossResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ServiceApi {
    @GET("api/organizationDetails")
    suspend fun getOrganizationDetails(
        @Query("formmode") formmode: String? = "add"
    ): Response<OrganizationResponse>

    @GET("api/organizationDetails")
    suspend fun getBranchDetailsView(
        @Query("formmode") formmode: String = "view",
        @Query("branch_code") branchCode: String
    ): Response<OrganizationViewResponse>

    @POST("api/tab2modify")
    suspend fun updateBranch(@Body body: RequestBody): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/tab2Del")
    suspend fun deleteBranch(@Field("branch_code") branchCode: String): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/OrgBranchAdd")
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

    @GET("api/chartOfAccounts")
    fun getChartOfAccountsList(
        @Query("formmode") formmode: String = "list"
    ): Call<ChartOfAccountsListResponse>

    @GET("api/chartOfAccounts")
    fun getChartOfAccountsDetail(
        @Query("formmode") formmode: String,
        @Query("acct_num") acctNum: String
    ): Call<ChartOfAccountsDetailResponse>

    @FormUrlEncoded
    @POST("api/AddScreens")
    fun addChartOfAccount(
        @FieldMap fields: Map<String, String>
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/ModifyScreens")
    fun modifyChartOfAccount(
        @Query("acct_num") acctNum: String,
        @FieldMap fields: Map<String, String>
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/VerifyScreens")
    fun verifyChartOfAccount(
        @FieldMap fields: Map<String, String>
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/DeleteScreens")
    fun deleteChartOfAccount(
        @Query("acct_num") acctNum: String
    ): Call<ResponseBody>

    @GET("api/chartaccounts/filter")
    fun filterChartOfAccounts(
        @Query("type") type: String
    ): Call<List<ChartAccountItem>>

    @GET("api/chartOfAccounts")
    fun getChartOfAccountsReferences(
        @Query("formmode") formmode: String = "add"
    ): Call<ChartOfAccountsAddResponse>
    // ─────────────────────────────────────────────────────────────────────────
    // REFERENCE CODE MAINTENANCE APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/refCodeMain")
    fun getRefList(
        @Query("formmode") formmode: String = "list"
    ): Call<com.example.bgls.DataModels.RefResponse>

    @FormUrlEncoded
    @POST("api/refAdd")
    fun addReferenceCode(
        @Field("ref_type") ref_type: String,
        @Field("ref_type_desc") ref_type_desc: String,
        @Field("ref_id") ref_id: String,
        @Field("ref_id_desc") ref_id_desc: String,
        @Field("module_id") module_id: String,
        @Field("remarks") remarks: String?
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/customer/refUpdate")
    fun updateReferenceCode(
        @Field("ref_type") ref_type: String,
        @Field("ref_type_desc") ref_type_desc: String,
        @Field("ref_id") ref_id: String,
        @Field("ref_id_desc") ref_id_desc: String,
        @Field("module_id") module_id: String,
        @Field("remarks") remarks: String?
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/customer/refDelete")
    fun deleteReferenceCode(
        @Field("ref_id") ref_id: String
    ): Call<ResponseBody>
// Inside ServiceApi interface


    @FormUrlEncoded
    @POST("api/verifyUserById")
    fun verifyCustomerById(@Field("UserId") userId: String): Call<ResponseBody>
    // ─────────────────────────────────────────────────────────────────────────
    // GL STRUCTURE MAINTENANCE APIs
    // ─────────────────────────────────────────────────────────────────────────
    @FormUrlEncoded
    @POST("api/modifySubmit")
    suspend fun modifyCustomer(@FieldMap fields: Map<String, String>): Response<ResponseBody>
    @GET("api/glcode")
    fun getGLCode(
        @Query("formmode") formmode: String?,
        @Query("glcode") glcode: String?,
        @Query("glsh_Code") glshCode: String?
    ): Call<com.example.bgls.DataModels.GLResponse>

    @FormUrlEncoded
    @POST("api/GeneralLedgerAdd")
    fun manageGLStructure(
        @Query("formmode") formmode: String,
        @Query("glcode") glcode: String?,
        @Query("glsh_code") glshCode: String?,
        @FieldMap fields: Map<String, String>
    ): Call<ResponseBody>

    // ─────────────────────────────────────────────────────────────────────────
    // SCHEME CODE MAINTENANCE APIs
    // ─────────────────────────────────────────────────────────────────────────
    @GET("api/transactionsAccounts")
    fun getTransactionAccountsList(
        @Query("formmode") formmode: String = "list"
    ): Call<TransactionAccountsResponse>

    @GET("api/parameters")
    fun getParameters(
        @Query("formmode") formmode: String?
    ): Call<com.example.bgls.DataModels.SchemeResponse>

    @GET("api/parameters/update")
    fun getParameterDetail(
        @Query("id") id: String
    ): Call<ResponseBody>

    @POST("api/parameters")
    fun addParameter(
        @Body schemeCode: com.example.bgls.DataModels.SchemeCode,
        @Query("formmode") formmode: String = "add"
    ): Call<ResponseBody>

    @POST("api/parameters")
    fun updateParameter(
        @Body schemeCode: com.example.bgls.DataModels.SchemeCode,
        @Query("formmode") formmode: String = "edit"
    ): Call<ResponseBody>

    @POST("api/parameters")
    fun deleteParameter(
        @Body schemeCode: com.example.bgls.DataModels.SchemeCode,
        @Query("formmode") formmode: String = "delete"
    ): Call<ResponseBody>

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

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER MASTER APIs
    // ─────────────────────────────────────────────────────────────────────────

    /** General customer master endpoint (formmode = list / view / modify / verify) */
    @GET("api/customerMaster")
    suspend fun getCustomerMaster(
        @Query("formmode") formmode: String,
        @Query("id") id: String? = null,
        @Query("branch_key") branchKey: String? = null,
        @Query("module") module: String? = null
    ): retrofit2.Response<com.example.bgls.DataModels.CustomerMasterViewResponse>

    /** All customers (verified + unverified) – server-side pagination */
    @GET("api/AllApprovedCust")
    suspend fun getAllApprovedCust(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200
    ): retrofit2.Response<com.example.bgls.DataModels.CustomerMasterPagedResponse>

    /** Only verified / approved customers – server-side pagination */
    @GET("api/ApprovedCust")
    suspend fun getApprovedCust(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200
    ): retrofit2.Response<com.example.bgls.DataModels.CustomerMasterPagedResponse>

    /** Only unverified / not-approved customers – server-side pagination */
    @GET("api/NotApprovedCust")
    suspend fun getNotApprovedCust(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200
    ): retrofit2.Response<com.example.bgls.DataModels.CustomerMasterPagedResponse>

    /** Search by customer ID (partial match), optionally filter by status */
    @GET("api/customers/search")
    suspend fun searchCustomersById(
        @Query("customerId") customerId: String,
        @Query("status") status: String? = null
    ): retrofit2.Response<List<com.example.bgls.DataModels.CustomerMaster>>

    /** Search by mobile number (partial match), optionally filter by status */
    @GET("api/customers/mobilesearch")
    suspend fun searchCustomersByMobile(
        @Query("mobile") mobile: String,
        @Query("status") status: String? = null
    ): retrofit2.Response<List<com.example.bgls.DataModels.CustomerMaster>>

    /** Search by email address (partial match), optionally filter by status */
    @GET("api/customers/emailsearch")
    suspend fun searchCustomersByEmail(
        @Query("email") email: String,
        @Query("status") status: String? = null
    ): retrofit2.Response<List<com.example.bgls.DataModels.CustomerMaster>>

    /** Filter the full list by status (ACTIVE / INACTIVE / PENDING) */
    @GET("api/customers/statusSearch")
    suspend fun searchCustomersByStatus(
        @Query("status") status: String
    ): retrofit2.Response<List<com.example.bgls.DataModels.CustomerMaster>>

    /** Account / loan details for a given customer ID – returns raw rows */
    @GET("api/getAccDet")
    suspend fun getAccDet(
        @Query("id") id: String
    ): retrofit2.Response<List<List<Any?>>>

    /** Fetch branch name by branch key */
    @FormUrlEncoded
    @POST("api/getBranchNameByKey")
    suspend fun getBranchNameByKey(
        @Field("branch_key") branchKey: String
    ): retrofit2.Response<okhttp3.ResponseBody>

    // ─────────────────────────────────────────────────────────────────────────
    // LOAN MASTER APIs
    // ─────────────────────────────────────────────────────────────────────────

    /** Paginated loan list – api/loans */
    @GET("api/loans")
    suspend fun getLoans(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 200
    ): retrofit2.Response<com.example.bgls.DataModels.LoanMasterPagedResponse>

    /** Loan detail view – api/loanMaster?formmode=viewloan */
    @GET("api/loanMaster")
    suspend fun getLoanMasterView(
        @Query("formmode") formmode: String = "viewloan",
        @Query("id") id: String,
        @Query("holder_key") holderKey: String,
        @Query("branch_key") branchKey: String
    ): retrofit2.Response<com.example.bgls.DataModels.LoanMasterViewResponse>

    @GET("api/Loan_Maintenance")
    suspend fun getLoanMaintenanceView(
        @Query("formmode") formmode: String = "view",
        @Query("id") id: String,
        @Query("holder_key") holderKey: String = "",
        @Query("branch_key") branchKey: String = ""
    ): retrofit2.Response<com.example.bgls.DataModels.LoanMasterViewResponse>

    /** Search by Loan ID (partial match) */
    @GET("api/loan/search")
    suspend fun searchLoanById(
        @Query("loanId") loanId: String
    ): retrofit2.Response<List<com.example.bgls.DataModels.LoanMaster>>

    /** Search by Loan Type (partial match) */
    @GET("api/loan_type/search")
    suspend fun searchLoanByType(
        @Query("loanType") loanType: String
    ): retrofit2.Response<List<com.example.bgls.DataModels.LoanMaster>>

    /** Search by Mobile Number (partial match) */
    @GET("api/loan_mobile_number/search")
    suspend fun searchLoanByMobile(
        @Query("MobileNumber") mobileNumber: String
    ): retrofit2.Response<List<com.example.bgls.DataModels.LoanMaster>>

    /** Filter loans by status */
    @GET("api/loan/statusSearch")
    suspend fun searchLoanByStatus(
        @Query("status") status: String
    ): retrofit2.Response<List<com.example.bgls.DataModels.LoanMaster>>

    // ─────────────────────────────────────────────────────────────────────────
    // LOAN SCHEDULE APIs
    // ─────────────────────────────────────────────────────────────────────────

    /** Loan schedule list – api/loanSchedule?formmode=listschedule */
    @GET("api/loanSchedule")
    suspend fun getLoanScheduleList(
        @Query("formmode") formmode: String = "listschedule"
    ): retrofit2.Response<com.example.bgls.DataModels.LoanScheduleListResponse>

    /** Loan schedule view – api/loanSchedule?formmode=viewloanschedule1 */
    @GET("api/loanSchedule")
    suspend fun getLoanScheduleView(
        @Query("formmode") formmode: String = "viewloanschedule1",
        @Query("id") id: String,
        @Query("holder_key") holderKey: String,
        @Query("encodedKey") encodedKey: String
    ): retrofit2.Response<com.example.bgls.DataModels.LoanScheduleViewResponse>

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT LEDGER APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/accountLedger2")
    suspend fun getAccountLedger2(
        @Query("formmode") formmode: String = "view",
        @Query("acct_num") acctNum: String
    ): retrofit2.Response<com.example.bgls.DataModels.AccountLedgerViewResponse>

    @GET("api/getTypeDescription")
    suspend fun getTypeDescription(@Query("refType") refType: String): Response<TypeDescriptionResponse>

    data class TypeDescriptionResponse(
        val typeDesc: String,
        val moduleId: String
    )

//    @GET("api/journalEntries")   // ✅ added "/api/"
//    suspend fun getJournalEntry(
//        @Query("formmode") formmode: String,
//        @Query("tran_id") tran_id: String,
//        @Query("part_tran_id") part_tran_id: String,
//        @Query("acct_num") acct_num: String
//    ): Response<JournalEntriesActivity.JournalEntryResponse>
    @GET("api/journalEntries")
    suspend fun getJournalEntryView(
        @Query("formmode") formmode: String = "view",
        @Query("tran_id") tranId: String,
        @Query("part_tran_id") partTranId: String,
        @Query("acct_num") acctNum: String
    ): Response<JournalEntryViewResponse>

    @GET("api/transactionValues")
    suspend fun getTransactionDetails(
        @Query("tran_id") tranId: String,
        @Query("part_tran_id") partTranId: String
    ): Response<TransactionDetailsResponse>

    // Inside ServiceApi interface

    @GET("api/TransactionMigration")
    suspend fun getTransactionMigration(@Query("formmode") formmode: String = "add"): Response<TransactionMigrationResponse>

    @GET("api/transactions/search")
    suspend fun searchTransactions(
        @Query("keyword") keyword: String,
        @Query("flowCode") flowCode: String,
        @Query("filterType") filterType: String
    ): Response<List<TransactionDto>>

    @GET("api/DisplayExcel")
    @Streaming
    suspend fun downloadExcel(@Query("type") type: String): Response<ResponseBody>

    // Add these to ServiceApi.kt

    @GET("api/journalEntries")
    suspend fun getJournalEntryAddScreen(
        @Query("formmode") formmode: String = "add"
    ): Response<JournalEntryAddScreenResponse>

    @GET("api/journalEntries")
    suspend fun getJournalEntriesList(
        @Query("formmode") formmode: String = "list1"
    ): Response<JournalEntryViewResponse>

    @GET("api/journalEntries")
    suspend fun getJournalEntriesListForTran(
        @Query("formmode") formmode: String = "modify1",
        @Query("tran_id") tranId: String
    ): Response<JournalEntryViewResponse>  // returns jour and tableparttran


    @POST("api/addtransactiondata")
    suspend fun addTransaction(
        @Body transactions: List<TransactionRequest>
    ): Response<ResponseBody>

    @POST("api/addtransactiondatamodiy")
    suspend fun modifyTransaction(
        @Body transactions: List<TransactionRequest>,
        @Query("tran_id") tranId: String,
        @Query("part_tran_id") partTranId: String
    ): Response<ResponseBody>

    @POST("api/multilinejournalentries")
    suspend fun addMassEntries(
        @Body entries: List<MassEntryRequest>,
        @Query("tran_date") tranDate: String,
        @Query("tran_type") tranType: String
    ): Response<ResponseBody>

    @POST("api/deletescreen")
    @FormUrlEncoded
    suspend fun deleteJournalEntry(
        @Field("tran_id") tranId: String,
        @Field("part_tran_id") partTranId: String,
        @Field("acct_num") acctNum: String
    ): Response<ResponseBody>

    @Multipart
    @POST("api/uploadxml")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("api/getPartitionFlag")
    suspend fun getPartitionFlag(
        @Query("accountNum") accountNum: String
    ): Response<String>

    @GET("api/getPointingDetail")
    suspend fun getPointingDetail(
        @Query("accountNum") accountNum: String
    ): Response<String>

    // ─────────────────────────────────────────────────────────────────────────
    // ACCOUNT LEDGER POSTING APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/accountLedgerPost")
    suspend fun getAccountLedgerPostingList(
        @Query("formmode") formmode: String = "list"
    ): Response<JournalEntryViewResponse>

    @GET("api/accountLedgerPost")
    suspend fun getAccountLedgerPostingDetail(
        @Query("formmode") formmode: String = "verify",
        @Query("tran_id") tranId: String,
        @Query("part_tran_id") partTranId: String,
        @Query("acct_num") acctNum: String
    ): Response<JournalEntryViewResponse>

    @GET("api/validateAccountStatus")
    suspend fun validateAccountStatus(
        @Query("tran_id") tranId: String
    ): Response<ResponseBody>

    @GET("api/postedTrmRecords")
    suspend fun postLedgerRecords(
        @Query("tran_id") tranId: String,
        @Query("part_tran_id") partTranId: String,
        @Query("acct_num") acctNum: String,
        @Query("entry_user") entryUser: String
    ): Response<ResponseBody>

    @GET("api/transactionaccountdetails")
    suspend fun getGLAccountDetails(
        @Query("acct_num") acctNum: String
    ): Response<ChartAccountItem>

    @GET("api/trialBalance")
    suspend fun getTrialBalanceList(
        @Query("formmode") formmode: String = "list"
    ): Response<com.example.bgls.DataModels.TrialBalanceResponse>

    @GET("api/trialBalanceReports1")
    suspend fun getTrialBalanceReports(
        @Query("balancedate") balancedate: String
    ): Response<List<List<Any>>>

    @GET("api/BGLS/ghlslistdata")
    suspend fun getGLSHListData(
        @Query("glshCode") glshCode: String
    ): Response<List<ChartAccountApiItem>>

    @GET("api/profitAndLossAccount")
    suspend fun getProfitAndLossAccount(
        @Query("formmode") formmode: String = "list"
    ): Response<com.example.bgls.DataModels.ProfitAndLossAccountResponse>

    @GET("api/incomexpenditure")
    suspend fun getIncomeExpenditure(
        @Query("balancedate") balancedate: String
    ): Response<com.example.bgls.DataModels.IncomeExpenditureResponse>

    @POST("api/Verifyloanmain")
    suspend fun verifyLoanMain(
        @Query("id") id: String
    ): Response<ResponseBody>

    @GET("api/loanOperation")
    suspend fun loanOperation(
        @Query("formmode") formmode: String?
    ): Response<LoanOperationResponse>

    @GET("api/leasecollection")
    suspend fun leaseCollection(
        @Query("formmode") formmode: String?
    ): Response<LoanOperationResponse>

    @Multipart
    @POST("api/leaseuploadexcel")
    suspend fun leaseUploadExcel(
        @Part file: MultipartBody.Part,
        @Query("screenId") screenId: String,
        @Query("userid") userid: String
    ): Response<ResponseBody>

    @POST("api/saveMultipleTransactions1")
    suspend fun saveMultipleTransactions1(
        @Body transactions: List<MultipleTransactionRequest>
    ): Response<Map<String, Any>>

    @Multipart
    @POST("api/UploadFileData")
    suspend fun uploadFileData(
        @Part file: MultipartBody.Part,
        @Query("fileInput") fileInput: String,
        @Query("overwrite") overwrite: Boolean
    ): Response<Map<String, Any>>

    @POST("api/settlementCollection")
    suspend fun settlementCollection(
        @Body request: List<SettlementRecord>
    ): Response<String>

    @GET("api/loanClosure")
    suspend fun loanClosure(
        @Query("formmode") formmode: String?
    ): Response<LoanOperationResponse>




    @POST("api/transactionInterest")
    suspend fun transactionInterest(
        @Query("flow_code") flowCode: String,
        @Query("flow_date") flowDate: String,
        @Query("flow_amount") flowAmount: String,
        @Query("flow_id") flowId: String,
        @Query("account_no") accountNo: String,
        @Query("accountName") accountName: String,
        @Query("operation") operation: String
    ): Response<Map<String, Any>>

    @GET("api/fetchacctbalance")
    suspend fun fetchAccountBalance(
        @Query("acctnum") acctnum: String
    ): Response<String>

    @GET("api/getloanclosetdatas5214")
    suspend fun getLoanClosureDatas(
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    @GET("api/search")
    suspend fun search(
        @Query("value") value: String
    ): Response<List<List<Any>>>

    @GET("api/loanflowDetails11")
    suspend fun loanFlowDetails11(
        @Query("todate") toDate: String,
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    @GET("api/loanflowDetails")
    suspend fun loanFlowDetails(
        @Query("fromDate") fromDate: String,
        @Query("todate") toDate: String,
        @Query("accountNumber") accountNumber: String
    ): Response<List<LoanFlowDetail>>

    @GET("api/loanflowDetailsfees")
    suspend fun loanFlowDetailsFees(
        @Query("fromDate") fromDate: String,
        @Query("todate") toDate: String,
        @Query("accountNumber") accountNumber: String
    ): Response<List<LoanFlowDetail>>

    @GET("api/loanflowDetailspenalty")
    suspend fun loanFlowDetailsPenalty(
        @Query("fromDate") fromDate: String,
        @Query("todate") toDate: String,
        @Query("accountNumber") accountNumber: String
    ): Response<List<LoanFlowDetail>>

    @GET("api/FlowForDateloan")
    suspend fun loanFlowDetailsBooking(
        @Query("actno1") actno1: String,
        @Query("dateFrom") dateFrom: String,
        @Query("todate") toDate: String
    ): Response<Map<String, Any>>

    @POST("api/updateFlowAllocation")
    suspend fun updateFlowAllocation(
        @Query("remainingBalance") remainingBalance: Double,
        @Query("todate") toDate: String,
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    // ─────────────────────────────────────────────────────────────────────────
    // LOAN CLOSURE / PRE-CLOSURE APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/preclosure")
    suspend fun searchPreclosureAccounts(
        @Query("value") value: String = ""
    ): Response<List<List<Any>>>

    @GET("api/closure")
    suspend fun searchClosureAccounts(
        @Query("value") value: String = ""
    ): Response<List<List<Any>>>

    @GET("api/fetchacctbalancedisbursement")
    suspend fun fetchDisbursementBalance(
        @Query("acctnum") acctnum: String
    ): Response<String>

    @GET("api/getloanclosetdatas5211")
    suspend fun getPreclosureFlowData(
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    @GET("api/getloanclosetdatas521")
    suspend fun getClosureFlowData(
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    @GET("api/getloanclosetdatas511")
    suspend fun getClosureAddRowData(
        @Query("accountNumber") accountNumber: String
    ): Response<LoanClosureDataResponse>

    @GET("api/fetchLoanDetails")
    suspend fun fetchLoanDetails(
        @Query("id") id: String
    ): Response<Map<String, Any>>

    @POST("api/saveLoanpreClosureDetails")
    suspend fun saveLoanpreClosureDetails(
        @Body request: Map<String, Any>
    ): Response<String>

    @POST("api/saveLoanClosureDetails")
    suspend fun saveLoanClosureDetails(
        @Body request: Map<String, Any>
    ): Response<String>

    // Add these two endpoints to your existing ServiceApi interface

    @GET("api/credit_facility_report")
    suspend fun getCreditFacilityReport(
        @Query("formmode") formmode: String = "list"
    ): Response<CreditFacilityResponse>

    @GET("api/downloadDetailsPdf")
    @Streaming
    suspend fun downloadDetailsPdf(
        @Query("filetype") filetype: String = "pdf",
        @Query("acctNo") acctNo: String
    ): Response<ResponseBody>

    @GET("api/downloadShedulePdf")
    @Streaming
    suspend fun downloadSchedulePdf(
        @Query("filetype") filetype: String = "pdf",
        @Query("acctNo") acctNo: String
    ): Response<ResponseBody>

    @GET("api/EndOfMonthLoanReportDownload")
    @Streaming
    suspend fun downloadEndOfMonthReport(
        @Query("dueDate") dueDate: String
    ): Response<ResponseBody>

    @GET("api/DABReportDownload")
    @Streaming
    suspend fun downloadDABReport(
        @Query("tranDate") tranDate: String
    ): Response<ResponseBody>

    @GET("api/ConsolidatedLoanReportDownload")
    @Streaming
    suspend fun downloadConsolidatedLoanReport(
        @Query("dueDate") dueDate: String
    ): Response<ResponseBody>

    @GET("api/TransactionPDFReportDownload")
    @Streaming
    suspend fun downloadTransactionReport(
        @Query("dueDate") dueDate: String
    ): Response<ResponseBody>

    @GET("api/TransactionPDFReport2Download")
    @Streaming
    suspend fun downloadRecoveryReport(
        @Query("dueDate") dueDate: String,
        @Query("reportType") reportType: String,
        @Query("DType") dType: String
    ): Response<ResponseBody>

    @GET("api/TransactionPDFReport3Download")
    @Streaming
    suspend fun downloadDemandGenerationReport(
        @Query("dueDate") dueDate: String
    ): Response<ResponseBody>

    @GET("api/LoanAccrualReportDownload")
    @Streaming
    suspend fun downloadInterestAccrualReport(
        @Query("accrualDate") accrualDate: String
    ): Response<ResponseBody>

    @GET("api/LoanDailyPenaltyReportDownload")
    @Streaming
    suspend fun downloadPenaltyAccrualReport(
        @Query("tranDate") tranDate: String
    ): Response<ResponseBody>
    @FormUrlEncoded
    @POST("login")   // or whatever your login endpoint is
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSACTION INQUIRIES APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/Account_Balances")
    suspend fun getAccountBalances(
        @Query("formmode") formmode: String? = null
    ): Response<AccountBalancesResponse>

    @GET("api/LeaseBalance")
    suspend fun getLeaseBalance(
        @Query("date_of_loan") dateOfLoan: String
    ): Response<List<List<Any>>>

    @GET("api/DepositBalance")
    suspend fun getDepositBalance(
        @Query("deposit_period") depositPeriod: String
    ): Response<List<List<Any>>>

    @GET("api/interest_summary")
    suspend fun getInterestSummary(
        @Query("formmode") formmode: String? = null
    ): Response<InterestSummaryResponse>

    @GET("api/drawDownLoanMaintanace")
    suspend fun getDrawDownLoanMaintenance(
        @Query("formmode") formmode: String?,
        @Query("loan_accountno") loanAccountNo: String? = null
    ): Response<LoanMaintenanceViewResponse>

    @GET("api/getInterestDetailsView")
    suspend fun getInterestDetailsView(
        @Query("accountNo") accountNo: String
    ): Response<List<Map<String, Any>>>

    @GET("api/getdemandflow")
    suspend fun getDemandFlow(
        @Query("accountNo") accountNo: String
    ): Response<List<List<Any>>>

    @GET("api/getLoanPosition")
    suspend fun getLoanPosition(
        @Query("accountNum") accountNum: String
    ): Response<List<List<Any>>>

    @GET("api/journalbook")
    suspend fun getJournalBook(
        @Query("formmode") formmode: String? = null
    ): Response<JournalBookResponse>

    @GET("api/journalbook2")
    suspend fun getJournalBook2(
        @Query("formmode") formmode: String?,
        @Query("selectedDate") selectedDate: String?
    ): Response<List<List<Any>>>

    @GET("api/profitAndLossAccountReports")
    suspend fun getProfitAndLossReports(
        @Query("formmode") formmode: String?,
        @Query("tran") tran: String? = null
    ): Response<ProfitLossResponse>

    @GET("api/balanceSheet")
    suspend fun getBalanceSheet(
        @Query("formmode") formmode: String?,
        @Query("emp_id") empId: String? = null
    ): Response<BalanceSheetResponse>

    @GET("api/assetliability")
    suspend fun getAssetLiability(
        @Query("formmode") formmode: String?,
        @Query("balancedate") balancedate: String?,
        @Query("tran") tran: String? = null
    ): Response<AssetLiabilityResponse>

    @GET("api/Balancing_report")
    suspend fun getBalancingReport(
        @Query("formmode") formmode: String?,
        @Query("acct_num") acctNum: String? = null,
        @Query("keyword") keyword: String? = null
    ): Response<BalancingReportResponse>

    // ─────────────────────────────────────────────────────────────────────────
    // WALLET APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/walletMaintenance")
    suspend fun getWalletMaintenance(
        @Query("formmode") formmode: String,
        @Query("acct_num") acctNum: String? = null
    ): Response<com.example.bgls.DataModels.WalletMaintenanceResponse>

    @GET("api/walletinquries")
    suspend fun getWalletInquiries(
        @Query("formmode") formmode: String,
        @Query("acctId") acctId: String? = null
    ): Response<com.example.bgls.DataModels.WalletInquiryResponse>

    @FormUrlEncoded
    @POST("api/AddScreensdata")
    suspend fun addWalletScreenData(
        @FieldMap fields: Map<String, String>
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/ModifyScreensdata")
    suspend fun modifyWalletScreenData(
        @Query("acct_num") acctNum: String,
        @FieldMap fields: Map<String, String>
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/VerifyScreensdata")
    suspend fun verifyWalletScreenData(
        @Field("wallet_acct_num") walletAcctNum: String
    ): Response<ResponseBody>

    // ─────────────────────────────────────────────────────────────────────────
    // DEPOSIT MAINTENANCE APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/deposits")
    suspend fun getDepositMaintenance(
        @Query("formmode") formmode: String,
        @Query("actno") actno: String? = null
    ): Response<com.example.bgls.DataModels.DepositMaintenanceResponse>

    @FormUrlEncoded
    @POST("api/ModifyScreensdata1")
    suspend fun modifyDepositMaintenance(
        @Query("acct_num") acctNum: String,
        @FieldMap fields: Map<String, String>
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/VerifyScreensdata1")
    suspend fun verifyDepositMaintenance(
        @Field("deposit_acct_num") depositAcctNum: String
    ): Response<ResponseBody>

    // ─────────────────────────────────────────────────────────────────────────
    // REVERSAL TRANSACTION APIs
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/getJournalEntries")
    suspend fun getReversalList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<com.example.bgls.DataModels.ReversalListResponse>

    @GET("api/ReversalTransactions")
    suspend fun getReversalTransactions(
        @Query("formmode") formmode: String,
        @Query("tran_id") tranId: String? = null,
        @Query("part_tran_id") partTranId: String? = null,
        @Query("acct_num") acctNum: String? = null,
        @Query("account_no") accountNo: String? = null,
        @Query("currency") currency: String? = null,
        @Query("accountName") accountName: String? = null
    ): Response<Map<String, Any>>

    @POST("api/submitreversaldata")
    suspend fun submitReversalData(
        @Body payload: com.example.bgls.DataModels.ReversalSubmissionPayload
    ): Response<Map<String, Any>>

    @GET("api/RecoveryReversal")
    suspend fun getRecoveryReversal(
        @Query("formmode") formmode: String,
        @Query("tran_id") tranId: String? = null,
        @Query("part_tran_id") partTranId: String? = null,
        @Query("acct_num") acctNum: String? = null
    ): Response<Map<String, Any>>

    @POST("api/submitrecoveryreversaldata")
    suspend fun submitRecoveryReversalData(
        @Body payload: com.example.bgls.DataModels.ReversalSubmissionPayload
    ): Response<Map<String, Any>>

    @GET("api/getFailedTransactions")
    suspend fun getFailedTransactions(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<com.example.bgls.DataModels.ReversalListResponse>

    @GET("api/FailedTransactions")
    suspend fun getFailedTransactionsDetails(
        @Query("formmode") formmode: String,
        @Query("tran_id") tranId: String? = null,
        @Query("part_tran_id") partTranId: String? = null,
        @Query("acct_num") acctNum: String? = null
    ): Response<Map<String, Any>>

    @POST("api/failedTransactionPayloadBuild")
    suspend fun submitFailedReversal(
        @Body payload: com.example.bgls.DataModels.FailedReversalSubmissionPayload
    ): Response<Map<String, Any>>

    // ─────────────────────────────────────────────────────────────────────────
    // BATCH JOB APIs
    // ─────────────────────────────────────────────────────────────────────────

    /** Initial list load – returns TRANDATE, booking (loan) & booking1 (deposit) */
    @GET("api/interestBatchJob_test")
    suspend fun batchJobInit(
        @Query("formmode") formmode: String = "list"
    ): Response<Map<String, Any>>

    /** Holiday / weekend check */
    @FormUrlEncoded
    @POST("api/holidayCheckBatchJob")
    suspend fun holidayCheckBatchJob(
        @Field("trndate") trndate: String
    ): Response<okhttp3.ResponseBody>

    /** DAB account list */
    @GET("api/getDabAcctList")
    suspend fun getDabAcctList(): Response<List<com.example.bgls.DataModels.DabAccountModel>>

    /** DAB run for a single account */
    @FormUrlEncoded
    @POST("api/DoatransactionpushBatchJob")
    suspend fun doaDabRun(
        @Field("acct_num")  acctNum:  String,
        @Field("from_date") fromDate: String,
        @Field("to_date")   toDate:   String
    ): Response<String>

    /** Consistency check */
    @FormUrlEncoded
    @POST("api/BatchJobconsistencyCheck")
    suspend fun consistencyCheck(
        @Field("trndate") trndate: String
    ): Response<com.example.bgls.DataModels.ConsistencyCheckResponse>

    /** Date change process */
    @FormUrlEncoded
    @POST("api/bacthJobdateChageProcess")
    suspend fun dateChangeProcess(
        @Field("nxtdate") nxtdate: String,
        @Field("trndate") trndate: String
    ): Response<okhttp3.ResponseBody>   // was Response<String>

    @GET("api/glconsolidation")
    suspend fun glConsolidation(): Response<okhttp3.ResponseBody>   // was Response<String>

    @POST("api/interest_accural_batch_job")
    suspend fun interestAccrual(
        @Body body: Map<String, String>
    ): Response<okhttp3.ResponseBody>   // was Response<String>

    @POST("api/penalty_accural_batch_job")
    suspend fun penaltyAccrual(
        @Body body: Map<String, String>
    ): Response<okhttp3.ResponseBody>
}
