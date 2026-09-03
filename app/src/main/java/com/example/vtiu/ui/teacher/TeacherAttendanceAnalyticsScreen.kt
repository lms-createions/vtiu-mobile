package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.vtiu.data.model.api.ClassPerformanceApi
import com.example.vtiu.data.model.AttendanceStat
import com.example.vtiu.ui.theme.TeacherPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceAnalyticsScreen(
    onBackClick: () -> Unit,
    courseId: Int = 1, // Mock course ID
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val analytics by viewModel.attendanceAnalytics
    
    LaunchedEffect(courseId) {
        viewModel.loadAttendanceAnalytics(courseId)
    }

    val stats = analytics.map { 
        AttendanceStat(it.studentName, it.total, it.present, it.absent, it.percentage.toInt())
    }

    val averageAttendance = if (stats.isNotEmpty()) stats.map { it.percentage }.average() else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6F8))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Overall Class Stat
                item {
                    OverallClassPerformance(averageAttendance)
                }

                item {
                    Text(
                        text = "Student Performance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // 2. Student List with Percentages
                items(stats) { stat ->
                    StudentAttendanceStatCard(stat)
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun OverallClassPerformance(averageAttendance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TeacherPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Average Attendance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text(text = "%.1f%%".format(averageAttendance), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                    Text(text = " +2.4% from last week", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 0.825f },
                    modifier = Modifier.size(60.dp),
                    color = Color.White,
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun StudentAttendanceStatCard(stat: AttendanceStat) {
    val statusColor = when {
        stat.percentage >= 80 -> Color(0xFF2E7D32)
        stat.percentage >= 65 -> Color(0xFFFBC02D)
        else -> Color(0xFFD32F2F)
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
                Column {
                    Text(text = stat.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Sessions: ${stat.totalSessions}", fontSize = 12.sp, color = Color.Gray)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${stat.percentage}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = statusColor
                    )
                    Text(text = "Presence", fontSize = 10.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { stat.percentage.toFloat() / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatLabel(label = "Present", value = stat.presentCount.toString(), color = Color(0xFF2E7D32))
                StatLabel(label = "Absent", value = stat.absentCount.toString(), color = Color(0xFFD32F2F))
                if (stat.percentage < 70) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = "Low Attendance", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatLabel(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: ", fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
