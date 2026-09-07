package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.theme.TeacherPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val profile by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    val scrollState = rememberScrollState()

    Scaffold { padding ->
        val teacher = profile
        if (teacher == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TeacherPrimary)
            }
        } else {
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
                            text = "My Profile",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    IconButton(onClick = { /* Edit Profile Simulation */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Header with picture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!teacher.profilePictureUrl.isNullOrBlank()) {
                                val staticUrl = com.example.vtiu.di.NetworkModule.STATIC_URL
                                coil.compose.AsyncImage(
                                    model = "$staticUrl${teacher.profilePictureUrl}",
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(TeacherPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = teacher.name.take(1),
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TeacherPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = teacher.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = teacher.department ?: "N/A", fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Information Sections
                    ProfileInfoSection(
                        title = "Personal Information",
                        items = listOf(
                            ProfileItem("Full Name", teacher.name, Icons.Default.Person),
                            ProfileItem("Username", teacher.username, Icons.Default.Badge),
                            ProfileItem("Email", teacher.email ?: "N/A", Icons.Default.Email),
                            ProfileItem("Phone", teacher.phone ?: "N/A", Icons.Default.Phone)
                        )
                    )

                    ProfileInfoSection(
                        title = "Professional Details",
                        items = listOf(
                            ProfileItem("Department", teacher.department ?: "N/A", Icons.Default.School),
                            ProfileItem("Qualification", teacher.qualification ?: "N/A", Icons.Default.WorkspacePremium),
                            ProfileItem("Specialization", teacher.specialization ?: "N/A", Icons.Default.History),
                            ProfileItem("Office", teacher.officeLocation ?: "N/A", Icons.Default.LocationOn)
                        )
                    )

                    ProfileInfoSection(
                        title = "Account Settings",
                        items = listOf(
                            ProfileItem("Role", "Teacher", Icons.Default.AdminPanelSettings),
                            ProfileItem("User ID", teacher.userId, Icons.Default.Fingerprint)
                        )
                    )

                    // Logout Action
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        TextButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Logout from Portal", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileInfoSection(title: String, items: List<ProfileItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TeacherPrimary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = item.label, fontSize = 12.sp, color = Color.Gray)
                            Text(text = item.value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

data class ProfileItem(val label: String, val value: String, val icon: ImageVector)
