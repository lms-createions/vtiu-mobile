package com.example.vtiu.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.SemesterTranscript
import com.example.vtiu.data.model.TranscriptCourse
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriptScreen(
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val transcriptApi by viewModel.transcript
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }
    
    var showGpaInfo by remember { mutableStateOf(false) }

    Scaffold { padding ->
        val transcript = transcriptApi
        if (transcript == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SchoolPrimary)
            }
        } else {
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
                            text = "Academic Transcript",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    Row {
                        IconButton(onClick = { showGpaInfo = true }) {
                            Icon(Icons.Default.Info, contentDescription = "GPA Info", tint = SchoolPrimary)
                        }
                        IconButton(onClick = { /* Download Full Transcript */ }) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.Black)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Student Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SchoolPrimary),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = transcript.studentName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Student ID: ${transcript.studentId}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            }
                        }
                    }

                    // Cumulative Stats
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TranscriptStatCard(Modifier.weight(1f), "Cum. GPA", "%.2f".format(transcript.cumulativeGpa ?: 0.0f), SchoolPrimary)
                            TranscriptStatCard(Modifier.weight(1f), "Weighted GPA", "%.2f".format(transcript.weightedGpa ?: 0.0f), Color(0xFF2E7D32))
                        }
                    }

                    item {
                        Text(text = "Academic History", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }

                    if (transcript.semesters.isEmpty()) {
                        item {
                            Text(text = "No history available yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                        }
                    }

                    // Semester breakdown
                    items(transcript.semesters) { semester ->
                        SemesterBreakdownCard(
                            SemesterTranscript(
                                academicYear = semester.academicYear,
                                semester = semester.semester,
                                coursesCount = semester.courses.size,
                                gpa = semester.gpa,
                                weightedGpa = semester.gpa,
                                creditHours = semester.courses.sumOf { it.credits },
                                isReleased = semester.isReleased,
                                courses = semester.courses.map { 
                                    TranscriptCourse(it.code, it.name, it.credits, it.score ?: 0f, it.grade, 20f, 30f, 50f)
                                }
                            )
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    if (showGpaInfo) {
        AlertDialog(
            onDismissRequest = { showGpaInfo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SchoolPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GPA Explanation", fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Cumulative GPA", fontWeight = FontWeight.Bold, color = SchoolPrimary)
                        Text("The standard average of all your courses. It treats Level 100 courses and Level 400 courses with equal weight.", fontSize = 14.sp)
                    }
                    Column {
                        Text("Weighted GPA", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Text("A prioritized average where higher-level courses (300/400) have more impact. This is often used to determine your final honors or graduation class.", fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGpaInfo = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun TranscriptStatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun SemesterBreakdownCard(semester: SemesterTranscript) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (semester.isReleased) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${semester.academicYear} • Semester ${semester.semester}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (semester.isReleased) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                    Text(
                        text = "GPA: %.2f".format(semester.gpa),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                semester.courses.forEach { course ->
                    TranscriptCourseRow(course)
                    if (course != semester.courses.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptCourseRow(course: TranscriptCourse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = course.courseName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "${course.courseCode} • ${course.creditHours} Credits", fontSize = 12.sp, color = Color.Gray)
        }
        Surface(
            color = if (course.gradeLetter?.startsWith("A") == true) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = course.gradeLetter ?: "N/A",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (course.gradeLetter?.startsWith("A") == true) Color(0xFF2E7D32) else Color.Black
            )
        }
    }
}
