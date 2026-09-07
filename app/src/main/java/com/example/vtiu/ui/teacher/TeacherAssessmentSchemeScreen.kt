package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.data.model.CourseAssessmentScheme
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssessmentSchemeScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val schemesApi by viewModel.assessmentSchemes
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    var selectedScheme by remember { mutableStateOf<CourseAssessmentScheme?>(null) }
    
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
                        text = "Assessment Schemes",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                }
            }

            InfoBanner()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Manage Course Weights",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (schemesApi.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "No assessment schemes found.", color = Color.Gray)
                            Text(text = "Ensure you have courses assigned in Flask.", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }

                items(schemesApi) { s ->
                    val scheme = CourseAssessmentScheme(
                        id = s.id,
                        courseId = s.courseId,
                        courseName = s.courseName,
                        courseCode = s.courseCode,
                        quizWeight = s.quizWeight,
                        assignmentWeight = s.assignmentWeight,
                        examWeight = s.examWeight
                    )
                    SchemeCard(
                        scheme = scheme,
                        onEditClick = { selectedScheme = scheme }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    if (selectedScheme != null) {
        EditSchemeBottomSheet(
            scheme = selectedScheme!!,
            onDismiss = { selectedScheme = null },
            onSave = { updatedScheme ->
                viewModel.updateAssessmentScheme(
                    com.example.vtiu.data.model.api.CourseAssessmentSchemeApi(
                        id = updatedScheme.id,
                        courseId = updatedScheme.courseId,
                        courseName = updatedScheme.courseName,
                        courseCode = updatedScheme.courseCode,
                        quizWeight = updatedScheme.quizWeight,
                        assignmentWeight = updatedScheme.assignmentWeight,
                        examWeight = updatedScheme.examWeight
                    ),
                    userId
                )
                selectedScheme = null
                scope.launch {
                    snackbarHostState.showSnackbar("Grading scheme updated for ${updatedScheme.courseName}")
                }
            }
        )
    }
}

@Composable
fun InfoBanner() {
    Surface(
        color = Color(0xFFE3F2FD),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Total weight for Quizzes, Assignments, and Exams must equal 100%.",
                fontSize = 13.sp,
                color = Color(0xFF1976D2),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SchemeCard(scheme: CourseAssessmentScheme, onEditClick: () -> Unit) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = scheme.courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = scheme.courseCode, fontSize = 13.sp, color = Color.Gray)
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TeacherPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeightIndicator(Modifier.weight(1f), "Quiz", "${scheme.quizWeight.toInt()}%", Color(0xFFFB8C00))
                WeightIndicator(Modifier.weight(1f), "Assign.", "${scheme.assignmentWeight.toInt()}%", Color(0xFF1E88E5))
                WeightIndicator(Modifier.weight(1f), "Exam", "${scheme.examWeight.toInt()}%", Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun WeightIndicator(modifier: Modifier, label: String, value: String, color: Color) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSchemeBottomSheet(
    scheme: CourseAssessmentScheme,
    onDismiss: () -> Unit,
    onSave: (CourseAssessmentScheme) -> Unit
) {
    var quizW by remember { mutableStateOf(scheme.quizWeight.toInt().toString()) }
    var assignW by remember { mutableStateOf(scheme.assignmentWeight.toInt().toString()) }
    var examW by remember { mutableStateOf(scheme.examWeight.toInt().toString()) }
    
    val total = (quizW.toIntOrNull() ?: 0) + (assignW.toIntOrNull() ?: 0) + (examW.toIntOrNull() ?: 0)
    val isValid = total == 100

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Edit Grading Weights", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = scheme.courseName, fontSize = 14.sp, color = Color.Gray)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Balance, contentDescription = null, tint = if (isValid) Color(0xFF43A047) else Color.Red)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Total Weight: $total%",
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) Color(0xFF43A047) else Color.Red
                )
                if (isValid) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF43A047), modifier = Modifier.size(16.dp))
                }
            }

            OutlinedTextField(
                value = quizW,
                onValueChange = { quizW = it },
                label = { Text("Quiz Weight (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = assignW,
                onValueChange = { assignW = it },
                label = { Text("Assignment Weight (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = examW,
                onValueChange = { examW = it },
                label = { Text("Exam Weight (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    onSave(scheme.copy(
                        quizWeight = quizW.toFloat(),
                        assignmentWeight = assignW.toFloat(),
                        examWeight = examW.toFloat()
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeacherPrimary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
