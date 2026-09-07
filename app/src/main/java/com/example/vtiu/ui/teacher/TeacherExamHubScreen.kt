package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vtiu.ui.theme.TeacherPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExamHubScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreateExamClick: () -> Unit,
    onManageExamsClick: () -> Unit,
    onViewSubmissionsClick: () -> Unit
) {
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
                        text = "Manage Examinations",
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
                    text = "Exam Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                HubActionCard(
                    title = "Create New Exam",
                    description = "Design and set up a final or mid-term exam",
                    icon = Icons.Default.Add,
                    color = Color(0xFFD32F2F), // Exam Red
                    onClick = onCreateExamClick
                )

                HubActionCard(
                    title = "Manage Existing Exams",
                    description = "View, edit, or delete exams you've created",
                    icon = Icons.Default.Description,
                    color = Color(0xFFFB8C00),
                    onClick = onManageExamsClick
                )

                HubActionCard(
                    title = "Exam Submissions",
                    description = "Review and grade student examination attempts",
                    icon = Icons.Default.Description,
                    color = TeacherPrimary,
                    onClick = onViewSubmissionsClick
                )
            }
        }
    }
}
