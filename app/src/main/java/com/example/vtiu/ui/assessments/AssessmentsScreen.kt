package com.example.vtiu.ui.assessments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpCenter
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
import com.example.vtiu.data.model.Assessment
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentsScreen(
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val assessmentsApi by viewModel.studentAssessments
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    Scaffold { padding ->
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Assessment Feedback",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "My Assessment Results",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Raw scores and feedback from quizzes, assignments, and exams",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )
                }

                items(assessmentsApi) { res ->
                    AssessmentCard(
                        Assessment(
                            id = res.id,
                            type = res.type,
                            course = res.course,
                            title = res.title,
                            rawScore = res.rawScore,
                            maxScore = res.maxScore,
                            date = res.date,
                            feedback = res.feedback ?: "No feedback available"
                        )
                    )
                }

                item {
                    InfoAlert()
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun AssessmentCard(assessment: Assessment) {
    val (typeIcon, typeColor) = when (assessment.type) {
        "Quiz" -> Icons.AutoMirrored.Filled.HelpCenter to SchoolPrimary
        "Assignment" -> Icons.Default.Description to Color(0xFF00ACC1)
        else -> Icons.Default.Edit to Color(0xFFFBC02D)
    }

    val percentageColor = when {
        assessment.percentage >= 70 -> Color(0xFF2E7D32)
        assessment.percentage >= 50 -> Color(0xFFFBC02D)
        else -> Color.Red
    }

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
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(typeColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = assessment.course, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = assessment.title, fontSize = 14.sp, color = Color.Gray)
                    }
                }
                
                Surface(
                    color = percentageColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${assessment.percentage.toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = percentageColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "RAW SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = "${assessment.rawScore} / ${assessment.maxScore}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text(text = assessment.date ?: "", fontSize = 12.sp, color = Color.Gray)
            }

            if (assessment.feedback != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "FEEDBACK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = assessment.feedback, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun InfoAlert() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        color = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "About These Results", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1976D2))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These are your raw scores and feedback. Final weighted grades will be released after the semester is finalized.",
                fontSize = 12.sp,
                color = Color(0xFF1976D2),
                lineHeight = 18.sp
            )
        }
    }
}
