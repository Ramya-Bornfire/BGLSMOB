package com.example.bgls.data.model

data class BusinessActivity(val auditDate: String,
                            val tableName: String,
                            val function: String,
                            val entryUser: String,
                            val entryTime: String,
                            val authorizer: String,
                            val authorizerTime: String,
                            val FieldName: String,
                            val OldValue: String,
                            val NewValue: String,
                            val remarks: String)
