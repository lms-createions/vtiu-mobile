package com.example.vtiu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignedFee(
    val title: String,
    val description: String?,
    val amount: Float,
    @SerialName("due_date")
    val dueDate: String,
    @SerialName("fee_type")
    val feeType: String
)

@Serializable
data class FeeTransaction(
    val id: Int,
    val timestamp: String,
    val description: String,
    val amount: Float,
    @SerialName("is_approved")
    val isApproved: Boolean
)

@Serializable
data class FeeDetails(
    @SerialName("academic_year")
    val academicYear: String,
    val semester: String,
    @SerialName("total_fee")
    val totalFee: Float,
    @SerialName("current_balance")
    val currentBalance: Float,
    @SerialName("pending_balance")
    val pendingBalance: Float,
    @SerialName("assigned_fees")
    val assignedFees: List<AssignedFee>,
    val transactions: List<FeeTransaction>
)
