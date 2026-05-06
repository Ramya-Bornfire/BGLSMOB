package com.example.bgls.DataModels

data class MassEntryModel(
    var tran_id: String = "",
    var part_tran_id: String = "",
    var acct_num: String = "",
    var acct_name: String = "",
    var part_tran_type: String = "Credit",
    var tran_amt: Double = 0.0,
    var tran_particular: String = "",
    var tran_remarks: String = ""
)
