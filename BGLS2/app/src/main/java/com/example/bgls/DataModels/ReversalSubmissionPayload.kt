package com.example.bgls.DataModels

data class ReversalSubmissionPayload(
    val originalTransactions: List<JournalEntryItem>,
    val reversalTransactions: List<JournalEntryItem>,
    val newTransactions: List<JournalEntryItem>
)
