package com.example.vtiu.ui.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.api.FeeBalanceApi
import com.example.vtiu.data.model.AssignedFee
import com.example.vtiu.data.model.FeeTransaction
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    onBackClick: () -> Unit,
    onPayNowClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val feeApi by viewModel.feeBalance
    val txnsApi by viewModel.feeTransactions
    val profileApi by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val totalFee = feeApi?.amountDue?.toFloat() ?: 0f
    val currentPaid = feeApi?.amountPaid?.toFloat() ?: 0f
    val remaining = totalFee - currentPaid
    val paidPercentage = if (totalFee > 0) (currentPaid / totalFee) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fees & Payments", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPayNowClick,
                containerColor = Color(0xFF00C950),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                text = { Text("Pay Now") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Summary Card
            item {
                FeeSummaryCard(totalFee, profileApi?.academicYear ?: "N/A", remaining, paidPercentage)
            }

            // Level Notice
            item {
                LevelNotice(profileApi?.level ?: 100)
            }

            // Assigned Fees Section
            item {
                Text(
                    text = "Assigned Fees",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            // For now, if feeApi is available, show a generic assigned fee
            item {
                if (feeApi != null) {
                    AssignedFeeItem(AssignedFee("Total Semester Fees", "Academic", totalFee, "", ""))
                } else {
                    Text(text = "No fee data found.", color = Color.Gray)
                }
            }

            // Payment History Section
            item {
                Text(
                    text = "Payment History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )
            }
            
            if (txnsApi.isEmpty()) {
                item {
                    Text(text = "No payment history found.", color = Color.Gray)
                }
            } else {
                items(txnsApi) { txn ->
                    TransactionItem(FeeTransaction(txn.id, txn.date.split("T")[0], txn.description, txn.amount.toFloat(), txn.isApproved))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FeeSummaryCard(totalFee: Float, academicYear: String, remaining: Float, paidPercentage: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SchoolPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Fee", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(text = "GHS %.2f".format(totalFee), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = academicYear,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { paidPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color(0xFF00C950),
                trackColor = Color.White.copy(alpha = 0.2f),
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Paid: %.0f%%".format(paidPercentage * 100), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(text = "Outstanding: GHS %.2f".format(remaining), color = if(remaining > 0) Color(0xFFFFCDD2) else Color(0xFFC8E6C9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LevelNotice(level: Int) {
    val isLevel100 = level == 100
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        color = if (isLevel100) Color(0xFFFFF9C4) else Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLevel100) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (isLevel100) Color(0xFFF57F17) else Color(0xFF0288D1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isLevel100) "Level 100 Student: Full payment required. No installments." else "Flexible payments: You can pay in full or in installments.",
                fontSize = 13.sp,
                color = if (isLevel100) Color(0xFFF57F17) else Color(0xFF0288D1),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AssignedFeeItem(fee: AssignedFee) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fee.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = fee.feeType, fontSize = 12.sp, color = Color.Gray)
            }
            Text(text = "GHS %.2f".format(fee.amount), fontWeight = FontWeight.Bold, color = SchoolPrimary)
        }
    }
}

@Composable
fun TransactionItem(txn: FeeTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (txn.isApproved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (txn.isApproved) Icons.Default.CheckCircle else Icons.Default.Schedule,
                contentDescription = null,
                tint = if (txn.isApproved) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = txn.description, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(text = txn.timestamp, fontSize = 12.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "GHS %.2f".format(txn.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = if (txn.isApproved) "Approved" else "Pending",
                fontSize = 11.sp,
                color = if (txn.isApproved) Color(0xFF2E7D32) else Color(0xFFEF6C00)
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
}
