package com.example.vtiu.ui.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.AppointmentBooking
import com.example.vtiu.data.model.AppointmentSlot
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val bookingsApi by viewModel.bookings
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (bookingsApi.isEmpty()) {
                item {
                    Text(text = "No appointments booked yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }
            items(bookingsApi) { b ->
                AppointmentStatusCard(
                    AppointmentBooking(
                        id = b.id,
                        slot = AppointmentSlot(0, b.teacher, b.date, b.start, "", true),
                        status = b.status,
                        note = b.note,
                        createdAt = ""
                    ),
                    onCancelClick = {
                        viewModel.cancelAppointment(userId, b.id)
                    }
                )
            }
        }
    }
}

@Composable
fun AppointmentStatusCard(booking: AppointmentBooking, onCancelClick: () -> Unit) {
    val statusColor = when (booking.status.lowercase()) {
        "approved" -> Color(0xFF2E7D32)
        "completed" -> Color.Gray
        "pending" -> Color(0xFFF57F17)
        "cancelled", "declined" -> Color.Red
        else -> Color.Black
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = booking.slot.teacherName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.slot.date, fontSize = 13.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${booking.slot.startTime} - ${booking.slot.endTime}", fontSize = 13.sp, color = Color.Gray)
            }

            if (booking.note != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Note: ${booking.note}",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            if (booking.status.lowercase() == "pending") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel Booking", fontSize = 12.sp)
                }
            }
        }
    }
}
