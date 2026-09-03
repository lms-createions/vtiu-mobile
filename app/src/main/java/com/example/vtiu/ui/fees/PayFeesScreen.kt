package com.example.vtiu.ui.fees

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.ui.courses.DropdownSelector
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayFeesScreen(
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val feeApi by viewModel.feeBalance
    val profileApi by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    var selectedYear by remember { mutableStateOf("2024/2025") }
    var selectedSemester by remember { mutableStateOf("First") }
    
    val totalFee = feeApi?.amountDue?.toFloat() ?: 0f
    val currentPaid = feeApi?.amountPaid?.toFloat() ?: 0f
    val outstanding = totalFee - currentPaid
    val progress = if (totalFee > 0) currentPaid / totalFee else 0f
    
    var amountToPay by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Mobile Money") }
    var description by remember { mutableStateOf("") }
    var selectedProofFile by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val studentLevel = profileApi?.level ?: 100

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Submit Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6F8)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Session Filter
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownSelector(options = listOf("2023/2024", "2024/2025"), selected = selectedYear, onSelect = { selectedYear = it })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownSelector(options = listOf("First", "Second"), selected = selectedSemester, onSelect = { selectedSemester = it })
                        }
                    }
                }
            }

            // 2. Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FeeRow("Total Fee", "GHS %.2f".format(totalFee), Color.Black)
                        FeeRow("Approved Payments", "GHS %.2f".format(currentPaid), Color(0xFF2E7D32))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        FeeRow("Outstanding", "GHS %.2f".format(outstanding), if (outstanding > 0) Color.Red else Color(0xFF2E7D32), isBold = true)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = Color(0xFF2E7D32),
                            trackColor = Color(0xFFE8F5E9)
                        )
                        Text(
                            text = "%.1f%% Paid".format(progress * 100),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }

            // 3. Level 100 Notice
            item {
                val noticeColor = if (studentLevel == 100) Color(0xFFFFF3E0) else Color(0xFFE3F2FD)
                val iconColor = if (studentLevel == 100) Color(0xFFE65100) else Color(0xFF1976D2)
                Surface(
                    color = noticeColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = iconColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (studentLevel == 100) 
                                "Level 100: Full payment is required. Installments are not allowed."
                                else "Continuing Student: You can pay in full or in installments.",
                            fontSize = 12.sp,
                            color = iconColor,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 4. Payment Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Payment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        
                        OutlinedTextField(
                            value = amountToPay,
                            onValueChange = { amountToPay = it },
                            label = { Text("Amount (GHS)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            prefix = { Text("GHS ") }
                        )

                        DropdownSelector(
                            options = listOf("Mobile Money", "Bank Transfer", "Cash"),
                            selected = selectedMethod,
                            onSelect = { selectedMethod = it }
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            placeholder = { Text("e.g. Full payment or Installment 1") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Proof Upload
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8F9FA))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                .clickable { selectedProofFile = "payment_receipt_${System.currentTimeMillis() / 1000}.jpg" },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedProofFile == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Gray)
                                    Text("Upload Proof of Payment", fontSize = 13.sp, color = Color.Gray)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedProofFile!!, fontSize = 13.sp, color = SchoolPrimary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                isSubmitting = true
                                val amount = amountToPay.toDoubleOrNull() ?: 0.0
                                viewModel.payFees(userId, amount, description) {
                                    isSubmitting = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Payment submitted for approval")
                                    }
                                    onBackClick()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = amountToPay.isNotBlank() && selectedProofFile != null && !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Submit Payment", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeeRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = value, 
            color = valueColor, 
            fontSize = 14.sp, 
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}
