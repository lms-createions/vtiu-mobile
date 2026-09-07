package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
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
import com.example.vtiu.data.model.VClassAssignment
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassSubmitAssignmentScreen(
    assignmentId: Int,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val assignmentsApi by viewModel.vclassAssignments
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }
    
    val assignmentItem = assignmentsApi.find { it.id == assignmentId }
    
    val assignment = if (assignmentItem != null) {
        VClassAssignment(assignmentItem.id, assignmentItem.title, "Course", assignmentItem.description, assignmentItem.dueDate)
    } else {
        VClassAssignment(assignmentId, "Loading...", "Course", "", "")
    }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color(0xFFF4F4F4))
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
                        text = "Submit Assignment",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = assignment.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Course: ${assignment.courseName}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Upload your work",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8F9FA))
                                .border(
                                    width = 1.dp,
                                    color = if (selectedFileName != null) VClassPrimary else Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    selectedFileName = "assignment_solution_${assignment.courseName.lowercase()}.pdf"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedFileName == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tap to select file", color = Color.Gray, fontSize = 14.sp)
                                    Text(
                                        "Allowed: .pdf, .doc, .docx, .txt",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = VClassPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedFileName!!,
                                        fontWeight = FontWeight.Medium,
                                        color = VClassPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    TextButton(onClick = { selectedFileName = null }) {
                                        Text("Change File", color = Color.Red, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isUploading = true
                                viewModel.submitAssignment(userId, assignmentId, selectedFileName!!) {
                                    isUploading = false
                                    onSubmitSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = selectedFileName != null && !isUploading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VClassPrimary)
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Submit Assignment", fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Make sure your file is named correctly and does not exceed 10MB.",
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
