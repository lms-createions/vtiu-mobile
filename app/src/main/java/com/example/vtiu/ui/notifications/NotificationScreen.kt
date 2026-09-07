package com.example.vtiu.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.api.NotificationApi
import com.example.vtiu.data.model.Notification
import com.example.vtiu.ui.theme.SchoolPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val notificationsApi by viewModel.notifications
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
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
                        text = "Notifications",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { /* Mark all as read */ }) {
                        Text("Mark all read", color = SchoolPrimary, fontSize = 12.sp)
                    }
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notificationsApi.isEmpty()) {
                    item {
                        Text(text = "No notifications yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                }
                items(notificationsApi) { notification ->
                    NotificationItem(
                        Notification(
                            id = notification.id,
                            title = notification.title,
                            message = notification.message,
                            type = notification.type,
                            priority = notification.priority,
                            senderName = notification.sender,
                            senderType = "admin", // Placeholder
                            createdAt = notification.date,
                            isRead = notification.isRead
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val (icon, color) = when (notification.type) {
        "quiz" -> Icons.AutoMirrored.Filled.Assignment to Color(0xFFFBC02D)
        "assignment" -> Icons.Default.Description to Color(0xFF00ACC1)
        "exam" -> Icons.Default.Event to Color(0xFFD32F2F)
        "grade" -> Icons.Default.Star to Color(0xFF2E7D32)
        "fee" -> Icons.Default.Payments to Color(0xFF388E3C)
        else -> Icons.Default.Notifications to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* View details */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 1.dp else 2.dp),
        border = if (!notification.isRead) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SchoolPrimary.copy(alpha = 0.5f))) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    if (!notification.isRead) {
                        Surface(
                            color = SchoolPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notification.createdAt.split(" ")[0],
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    if (notification.senderName != null) {
                        Text(text = " • ", fontSize = 11.sp, color = Color.LightGray)
                        Text(
                            text = "From: ${notification.senderName}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
