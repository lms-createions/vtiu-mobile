package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
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
import com.example.vtiu.data.model.VClassAssignment
import com.example.vtiu.ui.theme.VClassPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassAssignmentsScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val assignmentsApi by viewModel.vclassAssignments
    val userId = sessionManager.getUserId() ?: ""
    
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val assignments = assignmentsApi.map { 
        VClassAssignment(it.id, it.title, it.courseName ?: "Course", it.description, it.dueDate, null)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color(0xFFF4F4F4))
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
                    text = "My Assignments",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AssignmentStats(assignments)
                }

                items(assignments) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        onDownloadClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Downloading: ${assignment.filename}...")
                            }
                        },
                        onSubmitClick = { onSubmitClick(assignment.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentStats(assignments: List<VClassAssignment>) {
    val today = "2024-08-27" // Mock today
    val upcoming = assignments.count { it.dueDate > today }
    val dueToday = assignments.count { it.dueDate == today }
    val overdue = assignments.count { it.dueDate < today }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBox(Modifier.weight(1f), "Upcoming", upcoming.toString(), Color(0xFF2E7D32))
        StatBox(Modifier.weight(1f), "Due Today", dueToday.toString(), Color(0xFFFBC02D))
        StatBox(Modifier.weight(1f), "Overdue", overdue.toString(), Color.Red)
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: VClassAssignment,
    onDownloadClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val today = "2024-08-27"
    val status = when {
        assignment.dueDate > today -> "Upcoming" to Color(0xFF2E7D32)
        assignment.dueDate == today -> "Due Today" to Color(0xFFFBC02D)
        else -> "Overdue" to Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = assignment.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = assignment.courseName, fontSize = 13.sp, color = Color.Gray)
                }
                Surface(
                    color = status.second.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status.first,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = status.second
                    )
                }
            }

            if (assignment.instructions != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = assignment.instructions,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "DUE DATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = assignment.dueDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (assignment.filename != null) {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(18.dp), tint = Color.Gray)
                        }
                    }

                    if (status.first != "Overdue") {
                        Button(
                            onClick = onSubmitClick,
                            colors = ButtonDefaults.buttonColors(containerColor = VClassPrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Submit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
