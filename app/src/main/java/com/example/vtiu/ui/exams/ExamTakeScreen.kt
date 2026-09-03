package com.example.vtiu.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.api.ExamDetailApi
import com.example.vtiu.data.model.api.ExamQuestionApi
import com.example.vtiu.ui.theme.SchoolPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTakeScreen(
    examId: Int,
    onBackClick: () -> Unit,
    onSubmitSuccess: (String, Float, Float) -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val examApi by viewModel.examDetail
    
    LaunchedEffect(examId) {
        viewModel.loadExamDetail(examId)
    }

    if (examApi == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFD32F2F))
        }
        return
    }

    val exam = examApi!!
    val questions = exam.questions
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    
    var timeLeftSeconds by remember { mutableIntStateOf(exam.durationMinutes * 60) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // Timer Effect
    LaunchedEffect(exam.id) {
        while (timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        }
        onSubmitSuccess(exam.title, calculateScore(questions, userAnswers), calculateTotal(questions))
    }

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timerText = "%02d:%02d".format(minutes, seconds)
    val progress = if (questions.isNotEmpty()) userAnswers.size.toFloat() / questions.size else 0f

    Scaffold(
        containerColor = Color(0xFFF4F6F8),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Examination", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = if (timeLeftSeconds < 300) Color.Red else Color.Gray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timerText, color = if (timeLeftSeconds < 300) Color.Red else Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                .navigationBarsPadding() // Smartly sits on top of system navigation
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFFD32F2F),
                trackColor = Color.White
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(questions) { index, question ->
                    ExamQuestionCard(
                        index = index,
                        question = question,
                        selectedOptionId = userAnswers[question.id],
                        onOptionSelected = { userAnswers[question.id] = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish Examination", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        Dialog(onDismissRequest = { showSubmitDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Submit Examination?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You have answered ${userAnswers.size} of ${questions.size} questions. This action cannot be undone.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showSubmitDialog = false
                            onSubmitSuccess(exam.title, calculateScore(questions, userAnswers), calculateTotal(questions))
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Confirm Submission", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showSubmitDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Review Answers", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamQuestionCard(
    index: Int,
    question: ExamQuestionApi,
    selectedOptionId: Int?,
    onOptionSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Question ${index + 1}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), fontSize = 12.sp)
                Text(text = "${question.marks} Marks", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = question.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEach { option ->
                val isSelected = selectedOptionId == option.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = isSelected, onClick = { onOptionSelected(option.id) })
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onOptionSelected(option.id) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = option.text, fontSize = 15.sp)
                }
            }
        }
    }
}

private fun calculateScore(questions: List<ExamQuestionApi>, answers: Map<Int, Int>): Float {
    return answers.size * 5f 
}

private fun calculateTotal(questions: List<ExamQuestionApi>): Float {
    return questions.sumOf { it.marks.toDouble() }.toFloat()
}
