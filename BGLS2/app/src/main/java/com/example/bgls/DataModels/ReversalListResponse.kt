package com.example.bgls.DataModels

data class ReversalListResponse(
    val data: List<JournalEntryItem>,
    val totalPages: Int,
    val currentPage: Int,
    val totalItems: Int
)
