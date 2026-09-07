package com.example.vtiu.ui.vclass

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.api.QuizDetailApi
import com.example.vtiu.data.model.api.QuizQuestionApi
import com.example.vtiu.ui.theme.VClassPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassTakeQuizScreen(
    quizId: Int,
    onBackClick: () -> Unit,
    onSubmitSuccess: (String, Float, Float) -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val quizApi by viewModel.quizDetail
    val userId = sessionManager.getUserId() ?: ""
    
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
    val questions = quiz.questions
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    
    var timeLeftSeconds by remember { mutableIntStateOf(quiz.durationMinutes * 60) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // Timer Effect
    LaunchedEffect(quiz.id) {
        while (timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        }
        val answers = userAnswers.mapKeys { it.key.toString() }
        viewModel.submitQuiz(userId, quiz.id, answers) { score, total ->
            onSubmitSuccess(quiz.title, score, total)
        }
    }

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val timerText = "%02d:%02d".format(minutes, seconds)
    val progress = if (questions.isNotEmpty()) userAnswers.size.toFloat() / questions.size else 0f

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color(0xFFF4F4F4))
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VClassPrimary)
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = quiz.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(timerText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF00C950),
                trackColor = Color.White
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(questions) { index, question ->
                    QuestionCard(
                        index = index,
                        question = question,
                        selectedAnswer = userAnswers[question.id],
                        onAnswerSelected = { userAnswers[question.id] = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C950)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Quiz", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showSubmitDialog) {
        Dialog(onDismissRequest = { showSubmitDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C950).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00C950),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Ready to Submit?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You've answered ${userAnswers.size} out of ${questions.size} questions. Once submitted, you cannot edit your responses.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            showSubmitDialog = false
                            val answers = userAnswers.mapKeys { it.key.toString() }
                            viewModel.submitQuiz(userId, quiz.id, answers) { score, total ->
                                onSubmitSuccess(quiz.title, score, total)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C950))
                    ) {
                        Text("Yes, Submit Now", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = { showSubmitDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Go Back & Review", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    index: Int,
    question: QuizQuestionApi,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Question ${index + 1}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VClassPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (question.questionType == "mcq") {
                question.options.forEach { option ->
                    val isSelected = selectedAnswer == option.id.toString()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                onClick = { onAnswerSelected(option.id.toString()) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onAnswerSelected(option.id.toString()) },
                            colors = RadioButtonDefaults.colors(selectedColor = VClassPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option.text, fontSize = 14.sp)
                    }
                }
            } else {
                OutlinedTextField(
                    value = selectedAnswer ?: "",
                    onValueChange = onAnswerSelected,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type your answer here...") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
