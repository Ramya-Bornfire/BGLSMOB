package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class Branch(
    val srlNo: Int = 0,

    @SerializedName("branchCode")
    val branchCode: String?,

    @SerializedName("branchName")
    val branchName: String?,

    @SerializedName("swiftCode")
    val swiftCode: String?,

    @SerializedName("branchHead")
    val branchHead: String?
)

data class BranchDto(

    @SerializedName("branch_code")
    val branchCode: String?,

    @SerializedName("branch_name")
    val branchName: String?,

    @SerializedName("swift_code")
    val swiftCode: String?,

    @SerializedName("branch_head")
    val branchHead: String?
)
