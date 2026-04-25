package com.example.bgls.DataModels


data class OrganizationResponse(
    val formmode: String?,
    val OrgBranch: List<Branch>?
)

data class Organization(
    val orgName: String?,
    val address: String?
)
