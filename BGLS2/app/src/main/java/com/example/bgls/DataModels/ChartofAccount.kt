package com.example.bgls.DataModels

data class ChartAccountItem(
    val classification: String?,
    val gl_code: String?,
    val gl_desc: String?,
    val glsh_code: String?,
    val glsh_desc: String?,
    val schm_type: String?,
    val schm_code: String?,
    val acct_num: String?,
    val acct_name: String?,
    val add_det_flg: String?,
    val acct_partition: String?,
    val ref_crncy: String?,
    val acct_crncy: String?,
    val ref_code: String?,
    val ref_desc: String?,
    val rpt_code: String?,
    val acct_status: String?,
    val own_type: String?,
    val own_remarks: String?,
    val cr_amt: String?,
    val dr_amt: String?,
    val acct_bal: String?,
    val ref_crncy_bal: String?,
    val entity_flg: String?  // "Y" = Active
)

data class RefItem(
    val ref_id: String,
    val ref_id_desc: String,
    val ref_type_desc: String
)
data class ChartOfAccountsListResponse(
    val formmode: String,
    val chartaccount: List<ChartAccountApiItem>?,
    val current_user: String?
)

data class ChartOfAccountsDetailResponse(
    val formmode: String,
    val chartaccount: ChartAccountApiItem?,
    val Chart1: List<RefCodeItem>?,
    val Chart2: List<RefCodeItem>?,
    val Chart3: List<RefCodeItem>?,
    val Chart4: List<RefCodeItem>?,
    val Chart5: List<RefCodeItem>?,
    val Chart6: List<RefCodeItem>?,
    val Chart7: List<RefCodeItem>?,
    val Chart8: List<RefCodeItem>?
)

data class ChartAccountApiItem(
    val classification: String?,
    val acct_type: String?,
    val gl_code: String?,
    val gl_desc: String?,
    val glsh_code: String?,
    val glsh_desc: String?,
    val schm_type: String?,
    val schm_code: String?,
    val acct_num: String?,
    val acct_name: String?,
    val add_det_flg: String?,
    val acct_partition: String?,
    val acct_crncy: String?,
    val ref_crncy: String?,
    val ref_code: String?,
    val ref_desc: String?,
    val rpt_code: String?,
    val acct_status: String?,
    val own_type: String?,
    val own_remarks: String?,
    val cr_amt: String?,
    val dr_amt: String?,
    val acct_bal: String?,
    val ref_crncy_bal: String?,
    val entity_flg: String?
)

data class RefCodeItem(
    val ref_id: String,
    val ref_id_desc: String,
    val ref_type_desc: String
)
data class ChartOfAccountsAddResponse(
    val formmode: String,
    val Chart1: List<RefCodeItem>?,
    val Chart2: List<RefCodeItem>?,
    val Chart3: List<RefCodeItem>?,
    val Chart4: List<RefCodeItem>?,
    val Chart5: List<RefCodeItem>?,
    val Chart6: List<RefCodeItem>?,
    val Chart7: List<RefCodeItem>?,
    val Chart8: List<RefCodeItem>?
)
