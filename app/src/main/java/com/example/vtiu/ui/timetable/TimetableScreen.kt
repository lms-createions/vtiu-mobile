package com.example.vtiu.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Schedule
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
import com.example.vtiu.data.model.TimetableEntry
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val timetableApi by viewModel.timetable
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val currentDay = days[selectedDayIndex]
    
    val dayEntries = timetableApi.filter { it.day.equals(currentDay, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Timetable", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Download handle */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Download PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Day Selector
            ScrollableTabRow(
                selectedTabIndex = selectedDayIndex,
                containerColor = Color.White,
                contentColor = SchoolPrimary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedDayIndex == index,
                        onClick = { selectedDayIndex = index },
                        text = {
                            Text(
                                text = day,
                                fontWeight = if (selectedDayIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "$currentDay's Schedule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                if (dayEntries.isEmpty()) {
                    item {
                        Text(text = "No classes scheduled for today.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }

                items(dayEntries) { entry ->
                    TimetableCard(
                        TimetableEntry(
                            title = entry.courseName,
                            startTime = entry.start,
                            endTime = entry.end,
                            isBreak = false,
                            room = entry.venue
                        )
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
fun TimetableCard(entry: TimetableEntry) {
    val cardColor = if (entry.isBreak) Color(0xFFFFD966).copy(alpha = 0.1f) else Color.White
    val accentColor = if (entry.isBreak) Color(0xFFF57F17) else SchoolPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (entry.isBreak) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.3f))) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Column
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = entry.startTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.LightGray))
                Text(text = entry.endTime, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (entry.isBreak) accentColor else Color.Black
                )
                if (entry.room != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = entry.room, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            if (entry.isBreak) {
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "BREAK",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
