package com.example.bgls.data.model


data class UserActivity(
    val auditDate: String,
    val tableName: String,
    val function: String,
    val entryUser: String,
    val entryTime: String,
    val authorizer: String,
    val authorizerTime: String,
    val remarks: String
)