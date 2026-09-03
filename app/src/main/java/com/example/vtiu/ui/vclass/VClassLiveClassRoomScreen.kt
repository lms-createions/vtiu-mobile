package com.example.vtiu.ui.vclass

import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.VClassMeeting
import com.example.vtiu.data.remote.AgoraManager
import com.example.vtiu.ui.theme.VClassPrimary
import io.agora.rtc2.Constants
import kotlinx.coroutines.delay

@Composable
fun VClassLiveClassRoomScreen(
    meetingId: Int,
    onLeaveClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val meeting = VClassMeeting(meetingId, "Session", "Course", "Teacher", "", "", null, true, false)
    var isApproved by remember { mutableStateOf(!meeting.requiresApproval) }
    
    // Agora Setup
    val agoraManager = remember { AgoraManager(context) }
    var hostUid by remember { mutableIntStateOf(0) }
    var hasPermissions by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms.values.all { it }
        if (hasPermissions) {
            agoraManager.joinChannel(meeting.courseName, role = Constants.CLIENT_ROLE_AUDIENCE)
        }
    }

    // In a real app, you'd get the hostUid from the RTC event handler
    // For development, we'll simulate the host joining
    LaunchedEffect(isApproved) {
        if (isApproved) {
            permissionLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
            delay(2000)
            hostUid = 123 // Simulated teacher UID
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            agoraManager.leaveChannel()
            agoraManager.release()
        }
    }

    // Simulation: Auto-approve after 5 seconds if waiting
    if (meeting.requiresApproval && !isApproved) {
        LaunchedEffect(Unit) {
            delay(5000)
            isApproved = true
        }
    }

    Crossfade(targetState = isApproved, label = "room_state") { approved ->
        if (!approved) {
            WaitingRoom(meeting, onLeaveClick)
        } else {
            ActiveTeachingRoom(meeting, hostUid, agoraManager, onLeaveClick)
        }
    }
}

@Composable
fun WaitingRoom(meeting: com.example.vtiu.data.model.VClassMeeting, onLeaveClick: () -> Unit) {
    Scaffold(
        containerColor = Color(0xFF0F1720),
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Force truly full screen
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00C950), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Waiting for Host...",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Please stay on this screen. ${meeting.teacherName} will let you into the class shortly.",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(64.dp))
            OutlinedButton(
                onClick = onLeaveClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.3f)))
            ) {
                Text("Leave Meeting")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTeachingRoom(
    meeting: com.example.vtiu.data.model.VClassMeeting, 
    hostUid: Int,
    agoraManager: AgoraManager,
    onLeaveClick: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Hello everyone!", "Welcome to today's session on ${meeting.courseName}.") }
    var isFullScreen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Use same logic as Quiz screen
        topBar = {
            if (!isFullScreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(meeting.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Live • ${meeting.teacherName}", fontSize = 12.sp, color = Color(0xFF00C950))
                        }
                    },
                    actions = {
                        Button(
                            onClick = onLeaveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp).padding(end = 8.dp)
                        ) {
                            Text("Leave", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Teacher Video / Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isFullScreen) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (hostUid != 0) {
                        AndroidView(
                            factory = { ctx ->
                                SurfaceView(ctx).apply {
                                    agoraManager.setupRemoteVideo(this, hostUid)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Simulation of Teacher Teaching
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Text(
                                text = "Connecting to ${meeting.teacherName}...",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Floating Course Code
                    Surface(
                        color = Color.Red.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        Text(
                            text = "REC",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Full Screen Toggle
                    IconButton(
                        onClick = { isFullScreen = !isFullScreen },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = Color.White
                        )
                    }
                }

                if (!isFullScreen) {
                    // Interactive Tabs (Chat, Participants, etc.)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                    ) {
                        Text(
                            text = "Live Chat",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { msg ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(VClassPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = VClassPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Student", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = msg, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        // Chat Input - Modern Floating Style
                        Surface(
                            shadowElevation = 12.dp,
                            tonalElevation = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .navigationBarsPadding() // Smartly sits on top of system navigation
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFFF1F3F4)),
                                    placeholder = { Text("Ask a question...", color = Color.Gray) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { 
                                        if (messageText.isNotBlank()) {
                                            messages.add(messageText)
                                            messageText = ""
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = VClassPrimary,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // Full Screen Overlay for Student
            if (isFullScreen) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
                    IconButton(
                        onClick = { isFullScreen = false },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                }
            }
        }
    }
}
