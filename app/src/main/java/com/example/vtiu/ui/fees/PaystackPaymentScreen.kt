package com.example.vtiu.ui.fees

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.dashboard.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaystackPaymentScreen(
    url: String,
    reference: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color.White)
        ) {
            // Standard Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                    }
                    Text(
                        text = "Secure Checkout",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, urlStr: String?) {
                                    isLoading = false
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, urlStr: String?): Boolean {
                                    // Detect Paystack success or callback
                                    if (urlStr?.contains("paystack/callback") == true || 
                                        urlStr?.contains("verify") == true || 
                                        urlStr?.contains("success") == true) {
                                        viewModel.verifyPayment(reference) {
                                            onSuccess()
                                        }
                                        return true
                                    }
                                    return false
                                }
                            }
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00C950))
                    }
                }
            }
        }
    }
}
