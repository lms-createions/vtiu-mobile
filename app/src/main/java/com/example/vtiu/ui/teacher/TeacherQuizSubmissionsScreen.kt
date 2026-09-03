package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.data.model.AssignmentSubmissionPreview
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherQuizSubmissionsScreen(
    onBackClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val submissionsApi by viewModel.quizSubmissions
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    var selectedCourse by remember { mutableStateOf("All Courses") }
    val teacherClasses by viewModel.teacherClasses
    val courses = listOf("All Courses") + teacherClasses.map { it.courseName }.distinct()
    
    val filteredSubmissions = submissionsApi.filter { s ->
        selectedCourse == "All Courses" || s.courseName == selectedCourse
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedSubmission by remember { mutableStateOf<AssignmentSubmissionPreview?>(null) }
    var score by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quiz Submissions", fontWeight = FontWeight.Bold) },
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
            // Course Filter
            ScrollableTabRow(
                selectedTabIndex = courses.indexOf(selectedCourse),
                containerColor = Color.White,
                contentColor = TeacherPrimary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                courses.forEach { course ->
                    Tab(
                        selected = selectedCourse == course,
                        onClick = { selectedCourse = course },
                        text = { Text(text = course, fontSize = 13.sp) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Recent Quiz Attempts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (filteredSubmissions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No quiz submissions found for this course.", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredSubmissions) { s ->
                        val submission = AssignmentSubmissionPreview(
                            id = s.id,
                            studentName = s.studentName,
                            studentId = s.studentId,
                            assignmentTitle = s.quizTitle,
                            submittedAt = s.submittedAt,
                            status = if (s.score != null) "Graded" else "Pending"
                        )
                        QuizSubmissionCard(
                            submission = submission,
                            onViewClick = { selectedSubmission = submission },
                            onDownloadClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please use Flask panel to view details.")
                                }
                            }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (selectedSubmission != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSubmission = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(text = "Allocate Marks", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "${selectedSubmission!!.studentName} • ${selectedSubmission!!.assignmentTitle}", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(text = "Student Responses", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Detailed per-question responses are currently only viewable in the Flask Web Admin. You can assign the final mark below based on overall performance.", 
                            fontSize = 13.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Final Score") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Teacher's Feedback") },
                    placeholder = { Text("Enter remarks...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        isSaving = true
                        val finalScore = score.toFloatOrNull() ?: 0f
                        viewModel.gradeQuiz(selectedSubmission!!.id, finalScore, userId) {
                            isSaving = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Marks allocated for ${selectedSubmission!!.studentName}")
                            }
                            selectedSubmission = null
                            score = ""
                            feedback = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    enabled = score.isNotBlank() && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Marks", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuizSubmissionCard(
    submission: AssignmentSubmissionPreview,
    onViewClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = submission.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = submission.assignmentTitle, fontSize = 13.sp, color = Color.Gray)
                }
                Surface(
                    color = if (submission.status == "Pending") Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = submission.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (submission.status == "Pending") Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View & Grade", fontSize = 12.sp)
                }
                
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(40.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.Gray)
                }
            }
        }
    }
}
