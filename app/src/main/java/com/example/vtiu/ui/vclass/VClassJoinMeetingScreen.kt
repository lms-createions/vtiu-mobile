package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
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
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.VClassMeeting
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassJoinMeetingScreen(
    meetingId: Int,
    onBackClick: () -> Unit,
    onJoinNowClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val meeting = VClassMeeting(meetingId, "Session", "Course", "Teacher", "", "", null, true, false)
    var isMicOn by remember { mutableStateOf(false) }
    var isCamOn by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0F1720), // Match meeting theme
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Join Live Class", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VClassPrimary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simulated Camera Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E2732)),
                contentAlignment = Alignment.Center
            ) {
                if (isCamOn) {
                    // In real app, show actual camera preview
                    Text("Camera Preview", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Text("Camera is off", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }

                // Top Course Badge
                Surface(
                    color = VClassPrimary.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                ) {
                    Text(
                        text = meeting.courseName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Toggle
                FloatingActionButton(
                    onClick = { isMicOn = !isMicOn },
                    containerColor = if (isMicOn) Color.White.copy(alpha = 0.1f) else Color.Red,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff, contentDescription = "Mic")
                }

                // Camera Toggle
                FloatingActionButton(
                    onClick = { isCamOn = !isCamOn },
                    containerColor = if (isCamOn) Color.White.copy(alpha = 0.1f) else Color.Red,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(if (isCamOn) Icons.Default.Videocam else Icons.Default.VideocamOff, contentDescription = "Camera")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Meeting Details
            Text(
                text = meeting.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = "Host: ${meeting.teacherName}",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Join Button
            Button(
                onClick = { onJoinNowClick(meetingId) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C950)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Join Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Not Now", color = Color.Gray)
            }
        }
    }
}
