package com.example.bgls.DataModels

data class UserProfile(
    val userId: String,
    val userName: String,
    val status: String  // "Verified" or "Pending" etc.
)