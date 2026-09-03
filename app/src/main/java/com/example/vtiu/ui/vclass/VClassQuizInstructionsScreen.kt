package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.vtiu.data.model.api.QuizDetailApi
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassQuizInstructionsScreen(
    quizId: Int,
    onBackClick: () -> Unit,
    onStartQuizClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val quizApi by viewModel.quizDetail
    
    LaunchedEffect(quizId) {
        viewModel.loadQuizDetail(quizId)
    }

    if (quizApi == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VClassPrimary)
        }
        return
    }

    val quiz = quizApi!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Instructions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VClassPrimary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F4F4))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Quiz Instructions: ${quiz.title}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    QuizInfoRow("Course", quiz.courseName)
                    QuizInfoRow("Duration", "${quiz.durationMinutes} minutes")
                    QuizInfoRow("Max Score", "${quiz.maxScore} points")
                    QuizInfoRow("Start Time", quiz.startDatetime)
                    QuizInfoRow("End Time", quiz.endDatetime)
                    QuizInfoRow("Attempts Allowed", quiz.attemptsAllowed.toString())

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Text(text = "Before You Begin:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    val guidelines = listOf(
                        "Find a quiet place free from distractions.",
                        "The quiz timer starts immediately and cannot be paused.",
                        "Avoid switching apps or closing the app during the quiz.",
                        "Submit all answers before time runs out."
                    )
                    guidelines.forEach { guideline ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "• ", fontWeight = FontWeight.Bold)
                            Text(text = guideline, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onStartQuizClick(quizId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VClassPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Quiz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuizInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
    }
}
