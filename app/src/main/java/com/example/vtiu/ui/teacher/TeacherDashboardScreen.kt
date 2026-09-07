package com.example.vtiu.ui.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.data.model.AssignmentSubmissionPreview
import com.example.vtiu.ui.navigation.Screen
import com.example.vtiu.ui.theme.TeacherPrimary
import androidx.compose.runtime.*
import android.widget.Toast
import android.app.Activity

data class TeacherActionTile(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun TeacherDashboardScreen(
    onActionClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager // We should probably inject this via DI but for now we'll pass it if possible
) {
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    val teacherName = sessionManager.getUserName() ?: "Teacher"
    val userId = sessionManager.getUserId() ?: ""
    val teacherClasses by viewModel.teacherClasses
    val assignmentSubmissions by viewModel.assignmentSubmissions

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    val actions = listOf(
        TeacherActionTile("My Profile", Icons.Default.AccountCircle, Color.Gray, Screen.TeacherProfile.route),
        TeacherActionTile("My Classes", Icons.Default.Groups, Color(0xFF1E88E5), Screen.TeacherClasses.route),
        TeacherActionTile("Attendance", Icons.Default.HowToReg, Color(0xFF43A047), "attendance"), // Needs special handling in MainActivity likely
        TeacherActionTile("Live Classes", Icons.Default.VideoCall, Color(0xFF2D8CFF), Screen.TeacherLiveHub.route),
        TeacherActionTile("Appointments", Icons.Default.CalendarMonth, Color(0xFF009688), Screen.TeacherAppointmentHub.route),
        TeacherActionTile("Quizzes", Icons.Default.Quiz, Color(0xFFFB8C00), Screen.TeacherQuizHub.route),
        TeacherActionTile("Exams", Icons.Default.Description, Color(0xFFE53935), Screen.TeacherExamHub.route),
        TeacherActionTile("Assignments", Icons.Default.FolderZip, Color(0xFF795548), Screen.TeacherAssignmentFileManager.route),
        TeacherActionTile("Materials", Icons.Default.FolderOpen, Color(0xFF8E24AA), Screen.TeacherMaterialsHub.route),
        TeacherActionTile("Calendar", Icons.Default.CalendarMonth, Color(0xFF607D8B), Screen.AcademicCalendar.route),
        TeacherActionTile("Class Results", Icons.Default.Analytics, Color(0xFF4CAF50), Screen.TeacherClassPerformance.route),
        TeacherActionTile("Grading Schemes", Icons.Default.Balance, Color(0xFF00ACC1), Screen.TeacherAssessmentScheme.route)
    )

    Scaffold(
        topBar = {
            TeacherDashboardHeader(
                name = teacherName, 
                dept = viewModel.profile.value?.department ?: "Teacher", 
                profilePicUrl = viewModel.profile.value?.profilePictureUrl, 
                onLogoutClick = onLogoutClick,
                onMenuClick = onMenuClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6F8)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Stats Row
            item {
                TeacherStatsRow(
                    studentCount = teacherClasses.sumOf { it.studentCount }.toString(),
                    ungradedCount = assignmentSubmissions.count { it.status == "Pending" }.toString(),
                    classCount = teacherClasses.size.toString()
                )
            }

            // 2. Action Grid
            item {
                Text(text = "Quick Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                
                // We use a fixed height grid or just simple columns for actions
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val chunks = actions.chunked(2)
                    chunks.forEach { rowActions ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowActions.forEach { action ->
                                ActionCard(
                                    action = action,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onActionClick(action.route) }
                                )
                            }
                            if (rowActions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3. Pending Submissions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recent Submissions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    TextButton(onClick = { onActionClick("submissions") }) {
                        Text("View All", color = TeacherPrimary)
                    }
                }
            }

            items(assignmentSubmissions.take(3)) { submissionApi ->
                val submission = AssignmentSubmissionPreview(
                    id = submissionApi.id,
                    studentName = submissionApi.studentName,
                    studentId = submissionApi.studentId,
                    assignmentTitle = submissionApi.assignmentTitle,
                    submittedAt = submissionApi.submittedAt,
                    status = submissionApi.status
                )
                SubmissionPreviewCard(submission)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TeacherDashboardHeader(name: String, dept: String, profilePicUrl: String?, onLogoutClick: () -> Unit, onMenuClick: () -> Unit) {
    val staticUrl = com.example.vtiu.di.NetworkModule.STATIC_URL
    val firstName = name.split(" ").firstOrNull() ?: name
    Surface(
        color = TeacherPrimary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(44.dp) // Significantly reduced height
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (!profilePicUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = "$staticUrl$profilePicUrl",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = firstName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Welcome, $firstName", 
                    fontSize = 16.sp, // Restored large font size
                    fontWeight = FontWeight.Bold, 
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = dept, 
                    fontSize = 11.sp, // Restored standard font size
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            IconButton(onClick = onLogoutClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TeacherStatsRow(studentCount: String, ungradedCount: String, classCount: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(Modifier.weight(1f), "Students", studentCount, Icons.Default.People, Color(0xFF1E88E5))
        StatCard(Modifier.weight(1f), "Ungraded", ungradedCount, Icons.Default.PendingActions, Color(0xFFE53935))
        StatCard(Modifier.weight(1f), "Classes", classCount, Icons.Default.School, Color(0xFF43A047))
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ActionCard(action: TeacherActionTile, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(action.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = action.icon, contentDescription = null, tint = action.color, modifier = Modifier.size(24.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = action.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SubmissionPreviewCard(submission: AssignmentSubmissionPreview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TeacherPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = submission.studentName.take(1), color = TeacherPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = submission.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = submission.assignmentTitle, fontSize = 12.sp, color = Color.Gray)
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
    }
}
