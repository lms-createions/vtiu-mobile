package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.data.model.ClassPerformanceRow
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassPerformanceScreen(
    onBackClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val teacherClasses by viewModel.teacherClasses
    val performanceApi by viewModel.classPerformance
    val userId = sessionManager.getUserId() ?: ""

    var selectedCourseId by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }

    LaunchedEffect(selectedCourseId) {
        if (selectedCourseId != 0) {
            viewModel.loadPerformance(selectedCourseId)
        }
    }
    
    // Auto-select first course
    if (selectedCourseId == 0 && teacherClasses.isNotEmpty()) {
        selectedCourseId = teacherClasses.first().id
    }
    var isLocked by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Class Performance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLocked) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("LOCKED", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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
            if (teacherClasses.isNotEmpty()) {
                ScrollableTabRow(
                    selectedTabIndex = teacherClasses.indexOfFirst { it.id == selectedCourseId }.coerceAtLeast(0),
                    containerColor = Color.White,
                    contentColor = TeacherPrimary,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    teacherClasses.forEach { cls ->
                        Tab(
                            selected = selectedCourseId == cls.id,
                            onClick = { if (!isLocked) selectedCourseId = cls.id },
                            text = { Text(text = cls.courseName, fontSize = 13.sp) }
                        )
                    }
                }
            }

            // Legend / Vetting Action
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Submission for Vetting", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = if (isLocked) "Results submitted to Admin" else "Lock and send results to Academic Affairs",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Button(
                            onClick = {
                                isSubmitting = true
                                scope.launch {
                                    delay(2000)
                                    isSubmitting = false
                                    isLocked = true
                                    snackbarHostState.showSnackbar("Results locked and submitted for vetting!")
                                }
                            },
                            enabled = !isLocked && !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Excel-style Table
            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(Color.DarkGray, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeader("Student Name", 180.dp)
                        TableHeader("Quiz", 70.dp)
                        TableHeader("Assign.", 70.dp)
                        TableHeader("Exam", 70.dp)
                        TableHeader("Total", 80.dp)
                        TableHeader("Grade", 70.dp)
                        TableHeader("Status", 100.dp)
                    }

                    // Data Rows
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    ) {
                        if (performanceApi.isEmpty()) {
                            item {
                                Text(text = "No performance data found.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                            }
                        }
                        items(performanceApi) { row ->
                            PerformanceDataRow(
                                ClassPerformanceRow(
                                    studentId = row.studentId,
                                    studentName = row.studentName,
                                    quizScore = row.quizScore.toFloat(),
                                    assignmentScore = row.assignmentScore.toFloat(),
                                    examScore = row.examScore.toFloat(),
                                    totalScore = row.totalScore.toFloat(),
                                    grade = row.grade,
                                    status = row.status
                                )
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TableHeader(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun PerformanceDataRow(row: ClassPerformanceRow) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name
        Column(modifier = Modifier.width(180.dp).padding(horizontal = 4.dp)) {
            Text(text = row.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = row.studentId, fontSize = 11.sp, color = Color.Gray)
        }
        
        // Scores
        TableData("%.1f".format(row.quizScore), 70.dp)
        TableData("%.1f".format(row.assignmentScore), 70.dp)
        TableData("%.1f".format(row.examScore), 70.dp)
        
        // Total
        Text(
            text = "%.1f".format(row.totalScore),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = TeacherPrimary,
            modifier = Modifier.width(80.dp).padding(horizontal = 4.dp),
            textAlign = TextAlign.Start
        )
        
        // Grade
        Surface(
            color = if (row.grade.startsWith("A")) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(70.dp).padding(horizontal = 4.dp)
        ) {
            Text(
                text = row.grade,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (row.grade.startsWith("A")) Color(0xFF2E7D32) else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        // Status
        Text(
            text = row.status,
            color = if (row.status == "Passed") Color(0xFF2E7D32) else Color.Red,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.width(100.dp).padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun TableData(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color.DarkGray,
        modifier = Modifier.width(width).padding(horizontal = 4.dp),
        textAlign = TextAlign.Start
    )
}
