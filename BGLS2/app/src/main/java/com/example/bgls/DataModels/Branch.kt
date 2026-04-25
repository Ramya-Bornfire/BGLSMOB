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

