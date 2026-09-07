package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import com.example.vtiu.data.model.api.AttendanceAnalyticsApi
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceAnalyticsScreen(
    onBackClick: () -> Unit,
    courseId: Int,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val analyticsApi by viewModel.attendanceAnalytics
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(courseId) {
        viewModel.loadAttendanceAnalytics(courseId)
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color(0xFFF4F6F8))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = "Attendance Reports",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Generating CSV Report...")
                    }
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Export", tint = Color.Black)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AnalyticsSummary(analyticsApi)
                }

                item {
                    Text(text = "Student Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                }

                if (analyticsApi.isEmpty()) {
                    item {
                        Text(text = "No analytics data available yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(analyticsApi) { row ->
                    StudentAnalyticsCard(row)
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun AnalyticsSummary(data: List<AttendanceAnalyticsApi>) {
    val avgPercentage = if (data.isNotEmpty()) data.map { it.percentage }.average() else 0.0
    val lowAttendanceCount = data.count { it.percentage < 75f }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Avg. Attendance", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = "%.1f%%".format(avgPercentage), fontSize = 24.sp, fontWeight = FontWeight.Black, color = TeacherPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Below 75%", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = lowAttendanceCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Red)
            }
        }
    }
}

@Composable
fun StudentAnalyticsCard(row: AttendanceAnalyticsApi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF4F6F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = row.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "Present: ${row.present} | Absent: ${row.absent}", fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.0f%%".format(row.percentage),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = if (row.percentage >= 75f) Color(0xFF43A047) else Color.Red
                )
                LinearProgressIndicator(
                    progress = { row.percentage / 100f },
                    modifier = Modifier.width(60.dp).height(4.dp).clip(CircleShape),
                    color = if (row.percentage >= 75f) Color(0xFF43A047) else Color.Red,
                    trackColor = Color.LightGray.copy(alpha = 0.2f)
                )
            }
        }
    }
}
