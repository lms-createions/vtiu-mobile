package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.data.model.AssignmentSubmissionPreview
import com.example.vtiu.ui.theme.TeacherPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradeSubmissionsScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val submissionsApi by viewModel.assignmentSubmissions
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    var selectedSubmission by remember { mutableStateOf<AssignmentSubmissionPreview?>(null) }
    var score by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
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
                        text = "Assignment Submissions",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Recent Submissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (submissionsApi.isEmpty()) {
                    item {
                        Text(text = "No submissions found.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(submissionsApi) { s ->
                    val submission = AssignmentSubmissionPreview(
                        id = s.id,
                        studentName = s.studentName,
                        studentId = s.studentId,
                        assignmentTitle = s.assignmentTitle,
                        submittedAt = s.submittedAt,
                        status = s.status
                    )
                    SubmissionGradeCard(
                        submission = submission,
                        onViewClick = { selectedSubmission = submission },
                        onDownloadClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Downloading: ${s.assignmentTitle}.pdf")
                            }
                        }
                    )
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
                Text(text = "Grade Submission", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "${selectedSubmission!!.studentName} • ${selectedSubmission!!.assignmentTitle}", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(text = "Assignment File Content", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("This is a simulated preview of the student's uploaded work for the ${selectedSubmission!!.assignmentTitle} task.", 
                            fontSize = 13.sp, color = Color.DarkGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Score (out of 100)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Teacher's Feedback") },
                    placeholder = { Text("Good job! Next time...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        isSaving = true
                        val finalScore = score.toFloatOrNull() ?: 0f
                        viewModel.gradeAssignmentSubmission(selectedSubmission!!.id, finalScore, feedback, userId) {
                            isSaving = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Grade saved for ${selectedSubmission!!.studentName}")
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
                        Text("Save Grade", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SubmissionGradeCard(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF4F6F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = TeacherPrimary)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = submission.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = submission.assignmentTitle, fontSize = 13.sp, color = Color.Gray)
                    Text(text = "Submitted: ${submission.submittedAt.split(" ")[0]}", fontSize = 11.sp, color = Color.LightGray)
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

            Spacer(modifier = Modifier.height(16.dp))

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
