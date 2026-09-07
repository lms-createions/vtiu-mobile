package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vtiu.ui.theme.SchoolPrimary
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.ui.theme.VClassPrimary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassDashboardScreen(
    onBackClick: () -> Unit,
    onMaterialsClick: () -> Unit,
    onAssignmentsClick: () -> Unit,
    onLiveClick: () -> Unit,
    onQuizClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val coursesApi by viewModel.courses
    val quizzesApi by viewModel.availableQuizzes
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Quizzes", "Assignments")

    Scaffold { padding ->
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
                    text = "Virtual Class",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // 1. Live Clock Section
            LiveClockHeader()

            // 2. Summary Cards Row
            SummaryCardsRow(activeQuizzes = quizzesApi.size.toString(), upcomingEvents = "0")

            // 3. Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = VClassPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = VClassPrimary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // 4. Content Area
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    item { SectionHeader("Active & Recent Quizzes") }
                    items(quizzesApi) { quiz ->
                        VClassItemCard(
                            title = quiz.title,
                            course = quiz.courseName,
                            status = "Active",
                            statusColor = Color(0xFF00C950),
                            onClick = { onQuizClick(quiz.id) }
                        )
                    }
                    if (quizzesApi.isEmpty()) {
                        item { Text(text = "No active quizzes.", color = Color.Gray, modifier = Modifier.padding(16.dp)) }
                    }
                } else {
                    item { SectionHeader("Course Materials") }
                    items(coursesApi) { course ->
                        VClassItemCard(
                            title = "Handout: ${course.name}",
                            course = course.code,
                            status = "PDF",
                            statusColor = VClassPrimary,
                            onClick = onMaterialsClick
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onMaterialsClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VClassPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Materials Hub")
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onLiveClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VClassPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Classes & Recordings")
                    }
                }
            }
        }
    }
}

@Composable
fun LiveClockHeader() {
    var currentTime by remember { mutableStateOf(Calendar.getInstance(TimeZone.getTimeZone("Africa/Accra"))) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance(TimeZone.getTimeZone("Africa/Accra"))
            delay(1000)
        }
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("Africa/Accra")
    }
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("Africa/Accra")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = dateFormatter.format(currentTime.time), fontSize = 12.sp, color = Color.Gray)
            Text(
                text = timeFormatter.format(currentTime.time),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF17BED4) // matching web clock color
            )
        }
        Text(text = "Africa/Accra", fontSize = 10.sp, color = Color(0xFF2B9B3A), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SummaryCardsRow(activeQuizzes: String, upcomingEvents: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VClassSummaryCard(
            modifier = Modifier.weight(1f),
            title = "Active Quizzes",
            value = activeQuizzes,
            icon = Icons.Default.Bolt,
            color = SchoolPrimary
        )
        VClassSummaryCard(
            modifier = Modifier.weight(1f),
            title = "Upcoming Events",
            value = upcomingEvents,
            icon = Icons.Default.CalendarMonth,
            color = Color(0xFFF57F17)
        )
    }
}

@Composable
fun VClassSummaryCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp),
        color = Color.DarkGray
    )
}

@Composable
fun VClassItemCard(title: String, course: String, status: String, statusColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = course, fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}
