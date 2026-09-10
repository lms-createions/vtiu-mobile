package com.example.vtiu.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vtiu.ui.theme.SchoolPrimary
import com.example.vtiu.ui.theme.VClassPrimary
import com.example.vtiu.R

data class PortalTile(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val colors: List<Color>,
    val onClick: () -> Unit
)

@Composable
fun PortalSelectionScreen(
    onStudentClick: () -> Unit,
    onTeacherClick: () -> Unit,
    onVClassClick: () -> Unit,
    onExamClick: () -> Unit
) {
    val portals = listOf(
        PortalTile("Students", "Access materials and view grades", Icons.Default.School, listOf(Color(0xFF3B82F6), Color(0xFF1E40AF)), onStudentClick),
        PortalTile("Teachers", "Manage classes and grade tasks", Icons.Default.Laptop, listOf(Color(0xFF10B981), Color(0xFF047857)), onTeacherClick),
        PortalTile("VClass", "Join live lessons and recordings", Icons.Default.VideoCall, listOf(Color(0xFF06B6D4), Color(0xFF0EA5A4)), onVClassClick),
        PortalTile("Exams", "Secure timed tests and results", Icons.Default.Assignment, listOf(Color(0xFF7C3AED), Color(0xFF2563EB)), onExamClick)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with Dark Overlay
        AsyncImage(
            model = "https://i.postimg.cc/wvy0XwYx/studying.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.school_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "VTIU", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(text = "Unified Learning Portal", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Choose your portal",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Select the portal that matches your role to continue.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(portals) { portal ->
                    PortalCard(portal)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Need help? admissions@vtiu.edu.gh",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PortalCard(portal: PortalTile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { portal.onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(portal.colors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = portal.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = portal.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = portal.description, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}
