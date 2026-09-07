package com.example.vtiu.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.theme.SchoolPrimary
import androidx.compose.runtime.*
import android.widget.Toast
import android.app.Activity

data class DashboardTile(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val color: Color
)

@Composable
fun DashboardScreen(
    onTileClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    sessionManager: com.example.vtiu.data.local.SessionManager,
    viewModel: StudentViewModel = hiltViewModel()
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

    val userName = sessionManager.getUserName() ?: "Student"
    val profile by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }
    val tiles = listOf(
        DashboardTile("My Profile", Icons.Default.AccountCircle, "View and update personal info", Color.Gray),
        DashboardTile("My Courses", Icons.Default.Book, "Access enrolled courses", Color(0xFF2E7D32)),
        DashboardTile("Registration", Icons.Default.AppRegistration, "Register for courses", Color(0xFF673AB7)),
        DashboardTile("Results", Icons.Default.ShowChart, "Check semester grades", Color(0xFFFBC02D)),
        DashboardTile("Transcript", Icons.Default.History, "Full academic history", Color(0xFF795548)),
        DashboardTile("Assessments", Icons.Default.Assignment, "View feedback on quizzes", Color(0xFF00ACC1)),
        DashboardTile("Exams", Icons.Default.Description, "View exam instructions", Color(0xFFD32F2F)),
        DashboardTile("Appointments", Icons.Default.CalendarMonth, "Book time with staff", Color(0xFF009688)),
        DashboardTile("Academic Calendar", Icons.Default.Event, "View school events", Color(0xFF607D8B)),
        DashboardTile("Timetable", Icons.Default.CalendarToday, "Daily class schedule", Color(0xFFF57C00)),
        DashboardTile("Fees", Icons.Default.Payments, "View and pay school fees", Color(0xFF388E3C)),
        DashboardTile("Virtual Class", Icons.Default.Laptop, "Quizzes and materials", SchoolPrimary)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Changed to 3 columns for a cleaner icon-based look
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Section
            item(span = { GridItemSpan(3) }) {
                HeaderSection(
                    userName = userName, 
                    profilePicUrl = profile?.profilePictureUrl, 
                    onNotificationClick = onNotificationClick,
                    onMenuClick = onMenuClick
                )
            }

            // 2. Banner Card
            item(span = { GridItemSpan(3) }) {
                BannerCard()
            }

            // 3. Section Title
            item(span = { GridItemSpan(3) }) {
                SectionTitle(academicYear = profile?.academicYear ?: "2024/2025")
            }

            // 4. Grid Tiles
            items(tiles) { tile ->
                DashboardCard(tile, onClick = { onTileClick(tile.title) })
            }
            
            // Padding at the bottom
            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, profilePicUrl: String?, onNotificationClick: () -> Unit, onMenuClick: () -> Unit) {
    val staticUrl = com.example.vtiu.di.NetworkModule.STATIC_URL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            }
            Spacer(modifier = Modifier.width(4.dp))
            if (!profilePicUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = "$staticUrl$profilePicUrl",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00C950)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Welcome,", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        IconButton(onClick = onNotificationClick) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
        }
    }
}

@Composable
fun BannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00C950))
    ) {
        Column(modifier = Modifier.padding(24.dp)) { // Increased padding from 20.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student portal", // Updated text slightly
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Track your academic progress in real-time", // More descriptive text
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Access your courses, results, digital ID and virtual classrooms anywhere, anytime.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun SectionTitle(academicYear: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = "Everything you need", fontSize = 14.sp, color = Color.Gray)
            Text(text = "Quick access", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            color = Color(0xFFF4F4F5),
            shape = CircleShape
        ) {
            Text(
                text = academicYear,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardCard(tile: DashboardTile, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp), // Significantly reduced height
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tile.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = null,
                        tint = tile.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tile.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp, // Maintained font size
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
    }
}
