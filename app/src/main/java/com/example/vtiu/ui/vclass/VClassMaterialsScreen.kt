package com.example.vtiu.ui.vclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.VClassMaterial
import com.example.vtiu.ui.theme.SchoolPrimary
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassMaterialsScreen(
    onBackClick: () -> Unit,
    onPreviewClick: (Int) -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }
    val types = listOf("All", "PDF", "Video", "Doc", "Image")

    val materialsApi by viewModel.materials
    val userId = sessionManager.getUserId() ?: ""
    
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val filteredMaterials = materialsApi.filter {
        (selectedType == "All" || it.fileType.equals(selectedType, ignoreCase = true)) &&
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materials Hub", fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .background(Color(0xFFF4F4F4))
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search files...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Type Filter
            ScrollableTabRow(
                selectedTabIndex = types.indexOf(selectedType),
                containerColor = Color.Transparent,
                contentColor = VClassPrimary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                types.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = { Text(text = type) }
                    )
                }
            }

            if (filteredMaterials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No materials found.", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMaterials) { material ->
                        MaterialFileCard(
                            material = VClassMaterial(
                                id = material.id,
                                title = material.title,
                                courseName = material.courseName ?: "Course",
                                filename = material.filename,
                                fileType = material.fileType,
                                uploadedAt = material.date
                            ),
                            onPreviewClick = { onPreviewClick(material.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialFileCard(
    material: VClassMaterial,
    onPreviewClick: () -> Unit
) {
    val (icon, iconColor) = when (material.fileType.lowercase()) {
        "pdf" -> Icons.Default.PictureAsPdf to Color.Red
        "video" -> Icons.Default.VideoLibrary to Color.Black
        "doc" -> Icons.Default.Description to SchoolPrimary
        "image" -> Icons.Default.Image to Color.Blue
        else -> Icons.AutoMirrored.Filled.InsertDriveFile to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Uniform height
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = material.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${material.courseName} • ${material.fileType.uppercase()}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviewClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Preview", fontSize = 10.sp)
                }
                Button(
                    onClick = { /* Download */ },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VClassPrimary)
                ) {
                    Text("Get", fontSize = 10.sp)
                }
            }
        }
    }
}
