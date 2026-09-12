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
import com.example.vtiu.data.model.api.VClassMeetingApi
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassJoinMeetingScreen(
    meetingId: Int,
    onBackClick: () -> Unit,
    onJoinNowClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val meetingDetail by viewModel.meetingDetail
    
    LaunchedEffect(meetingId) {
        viewModel.loadMeetingDetail(meetingId)
    }

    if (meetingDetail == null) {
        Scaffold(containerColor = Color(0xFF0F1720)) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00C950))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Preparing session...", color = Color.White)
                }
            }
        }
        return
    }

    val meeting = meetingDetail!!
    
    Scaffold(
        containerColor = Color(0xFF0F1720),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
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
                        tint = Color.White
                    )
                }
                Text(
                    text = "Join Live Class",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Session Info Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2732))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = VClassPrimary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = VClassPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = meeting.courseName,
                            color = VClassPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = meeting.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Hosted by ${meeting.teacherName}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "You will join as a viewer. Only the teacher can broadcast audio and video.",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }
        }
    }
}
