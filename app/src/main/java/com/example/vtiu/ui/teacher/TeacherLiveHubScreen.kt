package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.theme.TeacherPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherLiveHubScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreateLiveClick: () -> Unit,
    onHostClick: (Int) -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val userId = sessionManager.getUserId() ?: ""
    val meetingsApi by viewModel.teacherMeetings
    
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }

    val meetings = meetingsApi.map {
        com.example.vtiu.data.model.VClassMeeting(it.id, it.title, it.courseName, "", it.start, it.end, null, it.isLive, false)
    }
    Scaffold { padding ->
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
                        text = "Live Class Hub",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Lecture Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                HubActionCard(
                    title = "Schedule New Lecture",
                    description = "Create a new live session link for your courses",
                    icon = Icons.Default.Add,
                    color = Color(0xFF2D8CFF), // Zoom Blue
                    onClick = onCreateLiveClick
                )

                Text(
                    text = "Active & Upcoming Sessions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(meetings) { meeting ->
                        TeacherMeetingCard(
                            meeting = meeting,
                            onHostClick = { onHostClick(meeting.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMeetingCard(
    meeting: com.example.vtiu.data.model.VClassMeeting,
    onHostClick: () -> Unit
) {
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
                    Text(text = meeting.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = meeting.courseName, fontSize = 13.sp, color = Color.Gray)
                }
                if (meeting.isLive) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoCall, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${meeting.scheduledStart} - ${meeting.scheduledEnd}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onHostClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D8CFF))
            ) {
                Text(text = if (meeting.isLive) "Enter Host Room" else "Start Early", fontWeight = FontWeight.Bold)
            }
        }
    }
}
