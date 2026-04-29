package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName



data class OrganizationResponse(

    @SerializedName("organization")
    val organization: Organization?,

    @SerializedName("OrgBranch")
    val OrgBranch: List<BranchDto> = emptyList()
)

data class Organization(

    val org_name: String?,
    val org_type: String?,
    val date_of_regn: String?,
    val reg_no: String?,
    val pan_card: String?,
    val tan_card: String?,
    val gst_ref: String?,
    val pf_ref: String?,
    val no_of_emp: String?,
    val as_on: String?,
    val reg_addr_1: String?,
    val reg_addr_2: String?,
    val corp_addr_1: String?,
    val cor_addr_2: String?,
    val web_site: String?,
    val email: String?
)

data class OrganizationViewResponse(
    @SerializedName("formmode") val formmode: String?,
    @SerializedName("OrgBranch") val branch: BranchDto?   // single object, not a list
)