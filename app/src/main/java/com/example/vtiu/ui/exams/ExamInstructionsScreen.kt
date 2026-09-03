package com.example.vtiu.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.api.ExamDetailApi
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamInstructionsScreen(
    examId: Int,
    onBackClick: () -> Unit,
    onStartExamClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val examApi by viewModel.examDetail
    
    LaunchedEffect(examId) {
        viewModel.loadExamDetail(examId)
    }

    if (examApi == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SchoolPrimary)
        }
        return
    }

    val exam = examApi!!
    var examPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Instructions", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = exam.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = exam.courseName,
                fontSize = 14.sp,
                color = SchoolPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Security Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = examPassword,
                        onValueChange = { 
                            examPassword = it
                            passwordError = false 
                        },
                        label = { Text("Enter Exam Password") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    if (passwordError) {
                        Text(text = "Invalid password. Please try again.", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Guidelines & Rules:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val rules = listOf(
                        "Duration: ${exam.durationMinutes} minutes.",
                        "Total Marks: ${exam.questions.sumOf { it.marks.toInt() }} points.",
                        "The exam will auto-submit when the timer hits zero.",
                        "Once started, do not exit the application or switch tabs.",
                        "Ensure you have a stable internet connection."
                    )
                    
                    rules.forEach { rule ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = "• ", fontWeight = FontWeight.Bold, color = SchoolPrimary)
                            Text(text = rule, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    if (examPassword == "1234") { // Mock check
                        onStartExamClick(examId)
                    } else {
                        passwordError = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Exam Red
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Examination", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
