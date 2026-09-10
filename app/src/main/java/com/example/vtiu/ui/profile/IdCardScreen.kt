package com.example.vtiu.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.vtiu.ui.dashboard.StudentViewModel
import com.example.vtiu.ui.theme.SchoolPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCardScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: StudentViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val profileApi by viewModel.profile
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val profile = profileApi
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SchoolPrimary)
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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                        Text(
                            text = "Digital ID",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Tap to flip ID Card",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        FlippableIdCard(profile)
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Generating High-Res ID PDF...")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download PDF", fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        CardInfoSection(profile)
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FlippableIdCard(profile: com.example.vtiu.data.model.api.UserProfileData) {
    var rotated by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "cardFlip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.58f)
            .clickable { rotated = !rotated }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
    ) {
        if (rotation <= 90f) {
            IdCardFront(profile)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            ) {
                IdCardBack(profile)
            }
        }
    }
}

@Composable
fun IdCardFront(profile: com.example.vtiu.data.model.api.UserProfileData) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.22f)
                    .background(SchoolPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "VTIU STUDENT ID", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.78f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) {
                    if (!profile.profilePictureUrl.isNullOrBlank()) {
                        val staticUrl = com.example.vtiu.di.NetworkModule.STATIC_URL
                        AsyncImage(
                            model = "$staticUrl${profile.profilePictureUrl}",
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Text(text = profile.name.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = profile.programme ?: "N/A", fontSize = 12.sp, color = SchoolPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    VisualIdRow("ID NO", profile.userId)
                    VisualIdRow("LEVEL", profile.level?.toString() ?: "N/A")
                    VisualIdRow("YEAR", profile.academicYear ?: "2026")
                }
            }
        }
    }
}

@Composable
fun IdCardBack(profile: com.example.vtiu.data.model.api.UserProfileData) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAF5)), // Slight off-white background
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Logo Watermark (Mock)
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.Center)
                    .alpha(0.05f),
                tint = SchoolPrimary
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Top Row: Serial, Chip, Flag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "21500086", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Contactless, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    
                    // Ghana Flag Mock
                    Box(modifier = Modifier.width(36.dp).height(24.dp).border(0.5.dp, Color.LightGray)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFF0000)))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFCD116)))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF006B3F)))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Middle Section: Terms and National Logo
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = "THIS CARD IS THE PROPERTY OF VTIU AND THE GOVERNMENT OF THE REPUBLIC OF GHANA. IF FOUND, PLEASE RETURN TO THE NEAREST POLICE STATION OR VTIU OFFICE.",
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "GHA-${profile.userId}-8",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Signature Area
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp).border(0.5.dp, Color.Gray.copy(alpha = 0.5f))) {
                            Text(
                                text = "Signature / Signature",
                                fontSize = 7.sp,
                                modifier = Modifier.padding(4.dp),
                                color = Color.Gray
                            )
                            // Mock Signature
                            Text(
                                text = profile.name.split(" ").firstOrNull() ?: "Student",
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Cursive,
                                color = Color.DarkGray
                            )
                        }
                        
                        Text(
                            text = "Authority of Issuance / Autorité de délivrance",
                            fontSize = 7.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "VTIU ADMINISTRATION AUTHORITY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }

                    // Right Side: Eagle Graphic (Mock)
                    Box(modifier = Modifier.weight(0.3f), contentAlignment = Alignment.CenterEnd) {
                        Icon(
                            imageVector = Icons.Default.Public, // Placeholder for Eagle/Coat of Arms
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFB8860B)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom: MRZ Code
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val mrzName = profile.name.replace(" ", "<<").uppercase()
                    Text(
                        text = "I<GHA${profile.userId}<<<<<<<<<<<<<<<<<<",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "9306238M2905165GHA<<<<<<<<<<<<<<2",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "$mrzName<<<<<<<<<<<<<<<<<<<<<<<",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun VisualIdRow(label: String, value: String) {
    Row {
        Text(text = "$label: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun CardInfoSection(profile: com.example.vtiu.data.model.api.UserProfileData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Identity Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("Ghana Card ID Equivalent", "GHA-${profile.userId}-8")
            InfoRow("Status", "Valid & Active")
            InfoRow("Expiry Date", "31st August 2027")
            InfoRow("Issuing Authority", "VTIU National Registry")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
