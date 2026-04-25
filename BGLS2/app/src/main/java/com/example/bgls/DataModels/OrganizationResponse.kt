package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName


data class Organization(
    val orgName: String?,
    val address: String?
)
data class OrganizationResponse(

    @SerializedName("OrgBranch")
    val OrgBranch: List<BranchDto> = emptyList()
)
