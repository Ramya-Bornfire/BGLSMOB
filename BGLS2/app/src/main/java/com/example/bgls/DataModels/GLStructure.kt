package com.example.bgls.DataModels

data class GLStructure(
    val glCode: String? = null,
    val glDescription: String? = null,
    val gl_type_description: String? = null,
    val branch_id: String? = null,
    val branch_desc: String? = null,
    val glsh_code: String? = null,
    val glsh_desc: String? = null,
    val crncy_code: String? = null,
    val bal_sheet_group: String? = null,
    val seq_order: String? = null,
    val total_balance: String? = null,
    val no_acct_opened: String? = null,
    val no_acct_closed: String? = null
)

data class GLResponse(
    val formmode: String? = null,
    val getvaluelist: List<GLStructure>? = null
)
