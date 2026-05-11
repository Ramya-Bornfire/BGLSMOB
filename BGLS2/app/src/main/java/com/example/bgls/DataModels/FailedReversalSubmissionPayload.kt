package com.example.bgls.DataModels

data class FailedReversalSubmissionPayload(
    val originalTransactions: List<JournalEntryItem>,
    val excelTransactions: List<ExcelTransactionModel>
)
