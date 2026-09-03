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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentFileManagerScreen(
    onBackClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    val assignmentsApi by viewModel.vclassAssignments
    val submissionsApi by viewModel.assignmentSubmissions

    var selectedAssignmentId by remember { mutableIntStateOf(0) }
    
    // Auto-select first assignment
    LaunchedEffect(assignmentsApi) {
        if (selectedAssignmentId == 0 && assignmentsApi.isNotEmpty()) {
            selectedAssignmentId = assignmentsApi.first().id
        }
    }

    val selectedAssignment = assignmentsApi.find { it.id == selectedAssignmentId }
    
    val filteredSubmissions = submissionsApi.filter { it.assignmentTitle == selectedAssignment?.title }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Assignment Files", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Downloading all submissions as ZIP...")
                        }
                    }) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = "Download All", tint = TeacherPrimary)
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
            // Assignment Selector
            if (assignmentsApi.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = assignmentsApi.indexOfFirst { it.id == selectedAssignmentId }.coerceAtLeast(0),
                    containerColor = Color.White,
                    contentColor = TeacherPrimary,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    assignmentsApi.forEach { assignment ->
                        Tab(
                            selected = selectedAssignmentId == assignment.id,
                            onClick = { selectedAssignmentId = assignment.id },
                            text = { Text(text = assignment.title, fontSize = 13.sp) }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Card
                item {
                    selectedAssignment?.let {
                        AssignmentSummaryCard(
                            com.example.vtiu.data.model.VClassAssignment(
                                id = it.id,
                                title = it.title,
                                courseName = it.courseName ?: "Course",
                                instructions = it.description,
                                dueDate = it.dueDate
                            )
                        )
                    }
                }

                item {
                    Text(
                        text = "Student Submissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (filteredSubmissions.isEmpty()) {
                    item {
                        Text(text = "No submissions found for this assignment.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(filteredSubmissions) { s ->
                    val submission = AssignmentSubmissionPreview(
                        id = s.id,
                        studentName = s.studentName,
                        studentId = s.studentId,
                        assignmentTitle = s.assignmentTitle,
                        submittedAt = s.submittedAt,
                        status = s.status
                    )
                    FileSubmissionItem(
                        submission = submission,
                        onDownload = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Downloading ${submission.studentName}_work.pdf")
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
}

@Composable
fun AssignmentSummaryCard(assignment: com.example.vtiu.data.model.VClassAssignment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TeacherPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = assignment.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = assignment.courseName, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "DUE DATE", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = assignment.dueDate, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "SUBMISSIONS", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "42 / 45", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun FileSubmissionItem(
    submission: AssignmentSubmissionPreview,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF4F6F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description, 
                    contentDescription = null, 
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = submission.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = "PDF • 1.2 MB", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Uploaded: ${submission.submittedAt.split(" ")[0]}", fontSize = 11.sp, color = Color.LightGray)
            }
            
            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Download", tint = TeacherPrimary)
            }
        }
    }
}
