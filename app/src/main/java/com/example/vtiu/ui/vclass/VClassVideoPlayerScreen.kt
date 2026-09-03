package com.example.vtiu.ui.vclass

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.VClassRecording
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun VClassVideoPlayerScreen(
    recordingId: Int,
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recording = VClassRecording(recordingId, "Title", "Course", "Teacher", "", "0:00", null, "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    val otherRecordings = emptyList<VClassRecording>()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(recording.url))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson Player", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VClassPrimary, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F4F4))
        ) {
            // Functional Video Player Area
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Recording Info
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = recording.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${recording.courseName} • ${recording.teacherName}", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "Recorded on ${recording.recordedAt}", fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AssistChip(
                            onClick = { /* Download Simulation */ },
                            label = { Text("Download") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        AssistChip(
                            onClick = { /* Share Simulation */ },
                            label = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Divider
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
            }

            // Related Recordings
            item {
                Text(
                    text = "Related Sessions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(otherRecordings) { item ->
                RecordingItem(title = item.title)
            }
        }
    }
}
