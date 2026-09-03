package com.example.vtiu.ui.vclass

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.data.model.VClassMaterial
import com.example.vtiu.ui.theme.VClassPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VClassPdfViewerScreen(
    materialId: Int,
    onBackClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val materialsApi by viewModel.materials
    val materialItem = materialsApi.find { it.id == materialId }
    
    val material = if (materialItem != null) {
        VClassMaterial(materialItem.id, materialItem.title, materialItem.courseName ?: "Course", materialItem.filename, materialItem.fileType, materialItem.date)
    } else {
        VClassMaterial(materialId, "Loading...", "Course", "", "pdf", "")
    }
    var isLoading by remember { mutableStateOf(true) }
    
    // Mock PDF Viewer using Google Docs Viewer URL
    val pdfUrl = "https://docs.google.com/viewer?embedded=true&url=https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(material.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("PDF Preview", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Download */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VClassPrimary)
            )
        },
        bottomBar = {
            // Simulated Zoom Controls
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* Zoom Out */ }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = null, tint = Color.White)
                    }
                    Text("100%", color = Color.White, modifier = Modifier.padding(horizontal = 24.dp))
                    IconButton(onClick = { /* Zoom In */ }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        loadUrl(pdfUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                CircularProgressIndicator(color = VClassPrimary)
            }
        }
    }
}
