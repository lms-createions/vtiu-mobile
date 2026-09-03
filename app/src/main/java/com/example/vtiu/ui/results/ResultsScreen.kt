package com.example.vtiu.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.CourseResult
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onBackClick: () -> Unit,
    onViewTranscriptClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val resultsApi by viewModel.fullResults
    val profileApi by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Semester Results", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Generating Results PDF...")
                            kotlinx.coroutines.delay(1500)
                            snackbarHostState.showSnackbar("PDF Ready. Opening Print Service...")
                        }
                    }) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header Info
            item {
                ResultsHeader(profileApi?.academicYear ?: "N/A", profileApi?.semester ?: "N/A")
            }

            // Summary Cards
            item {
                val mappedResults = resultsApi.map {
                    CourseResult(it.courseCode, it.courseName, it.score, it.grade, it.credits, it.gp, it.quizWeight, it.assignmentWeight, it.examWeight, it.remark)
                }
                ResultsSummary(mappedResults)
            }

            item {
                OutlinedButton(
                    onClick = onViewTranscriptClick,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full Academic History")
                }
            }

            item {
                Text(
                    text = "Course Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            if (resultsApi.isEmpty()) {
                item {
                    Text(text = "No results found in Flask database.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            }

            // Results List
            items(resultsApi) { result ->
                ResultCard(
                    CourseResult(
                        courseCode = result.courseCode,
                        courseName = result.courseName,
                        score = result.score,
                        grade = result.grade,
                        creditHours = result.credits,
                        points = result.gp,
                        quizWeight = result.quizWeight,
                        assignmentWeight = result.assignmentWeight,
                        examWeight = result.examWeight,
                        remark = result.remark
                    )
                )
            }

            // Footer Info
            item {
                InfoAlert()
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ResultsHeader(year: String, semester: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color(0xFF00ACC1).copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = year.split("/")[0],
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00ACC1)
            )
        }
        Surface(
            color = Color.LightGray.copy(alpha = 0.2f),
            shape = CircleShape
        ) {
            Text(
                text = "$semester Semester",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ResultsSummary(results: List<CourseResult>) {
    val totalCredits = results.sumOf { it.creditHours }
    val totalPoints = results.sumOf { it.points.toDouble() }
    val gpa = if (totalCredits > 0) (totalPoints / totalCredits) else 0.0
    val avgScore = if (results.isNotEmpty()) results.map { it.score }.average() else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(Modifier.weight(1f), "GPA", "%.2f".format(gpa), Color.Red)
        SummaryCard(Modifier.weight(1f), "Avg Score", "%.1f%%".format(avgScore), Color(0xFFFBC02D))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(Modifier.weight(1f), "Total Courses", results.size.toString(), SchoolPrimary)
        SummaryCard(Modifier.weight(1f), "Total Credits", totalCredits.toString(), Color(0xFF2E7D32))
    }
}

@Composable
fun SummaryCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier.height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun ResultCard(result: CourseResult) {
    val statusColor = if (result.score >= 70) Color(0xFF2E7D32) else if (result.score >= 50) Color(0xFFFBC02D) else Color.Red
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = result.courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = result.courseCode, fontSize = 14.sp, color = Color.Gray)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = result.grade,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ResultDetailItem("Score", "%.1f%%".format(result.score), statusColor)
                ResultDetailItem("Credits", result.creditHours.toString(), Color.Black)
                ResultDetailItem("GP", "%.2f".format(result.points), SchoolPrimary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${result.quizWeight.toInt()}% A${result.assignmentWeight.toInt()}% E${result.examWeight.toInt()}%",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Surface(
                    color = if(result.remark == "Passed") Color(0xFF2E7D32).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = result.remark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if(result.remark == "Passed") Color(0xFF2E7D32) else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun ResultDetailItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun InfoAlert() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        color = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Results are calculated according to each course's assessment scheme. Contact Academic Affairs for queries.",
                fontSize = 12.sp,
                color = Color(0xFF1976D2),
                lineHeight = 16.sp
            )
        }
    }
}
