package com.example.bgls.DataModels

data class WalletMaintenanceResponse(
    val formmode: String?,
    val walletMaintenanceList: List<WalletAccountEntity>?,
    val loanaccountno: String?,
    val wallet: WalletAccountEntity?
)

data class WalletInquiryResponse(
    val formmode: String?,
    val walletMaintenanceList: List<Any?>?,
    val wallet: Any?,
    val dataList: List<Any?>?
)

data class WalletAccountEntity(
    val wallet_category: String?,
    val customer_id: String?,
    val branch_id: String?,
    val branch_name: String?,
    val wallet_type: String?,
    val debit_limit: String?,
    val wallet_acct_num: String?,
    val wallet_acct_name: String?,
    val wallet_crncy: String?,
    val acct_open_date: String?,
    val act_cls_flg: String?,
    val act_cls_date: String?,
    val acct_bal: String?,
    val last_acct_bal_date: String?,
    val customer_limit: String?,
    val wallet_limit: String?,
    val sms_flg: String?,
    val mobile_no: String?,
    val email_flg: String?,
    val email_id: String?,
    val entity_flg: String?,
    val bips_acct_num: String?
)
