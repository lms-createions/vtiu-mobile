package com.example.vtiu.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vtiu.data.model.Course
import com.example.vtiu.ui.theme.SchoolPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRegistrationScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: com.example.vtiu.ui.dashboard.StudentViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedSemester by remember { mutableStateOf("First") }
    
    val userId = sessionManager.getUserId() ?: ""
    val availableCourses by viewModel.availableRegistration
    val profile by viewModel.profile

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadStudentData(userId)
            viewModel.loadAvailableRegistration(userId)
        }
    }
    
    val selectedYear = profile?.academicYear ?: ""
    val mandatoryCourses = availableCourses.filter { it.isMandatory && it.semester == selectedSemester }
    val optionalCourses = availableCourses.filter { !it.isMandatory && it.semester == selectedSemester }
    
    val selectedOptionalIds = remember { mutableStateListOf<Int>() }
    var isRegistered by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
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
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                    }
                    Text(
                        text = "Course Registration",
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
            
            if (step == 1) {
                SemesterSelectionStep(
                    selectedSemester = selectedSemester,
                    onSemesterChange = { selectedSemester = it },
                    selectedYear = selectedYear,
                    onProceed = { step = 2 }
                )
            } else {
                CourseSelectionStep(
                    mandatoryCourses = mandatoryCourses,
                    optionalCourses = optionalCourses,
                    selectedOptionalIds = selectedOptionalIds,
                    isRegistered = isRegistered,
                    isPrinting = isPrinting,
                    onRegister = {
                        val ids = mandatoryCourses.map { it.id } + selectedOptionalIds
                        viewModel.registerCourses(userId, ids) {
                            isRegistered = true
                            scope.launch {
                                snackbarHostState.showSnackbar("Registration Successful!")
                            }
                        }
                    },
                    onPrint = {
                        isPrinting = true
                        scope.launch {
                            delay(2000)
                            isPrinting = false
                            snackbarHostState.showSnackbar("Registration PDF generated successfully")
                        }
                    },
                    onReset = {
                        isRegistered = false
                        selectedOptionalIds.clear()
                    }
                )
            }
        }
    }
}

@Composable
fun SemesterSelectionStep(
    selectedSemester: String,
    onSemesterChange: (String) -> Unit,
    selectedYear: String,
    onProceed: () -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(text = "Select Academic Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Semester", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        DropdownSelector(options = listOf("First", "Second"), selected = selectedSemester, onSelect = onSemesterChange)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Academic Year", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = selectedYear,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            placeholder = { Text("Loading...") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onProceed,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary),
            enabled = selectedYear.isNotEmpty()
        ) {
            Text("Proceed to Course Selection")
        }
    }
}

@Composable
fun CourseSelectionStep(
    mandatoryCourses: List<com.example.vtiu.data.model.api.CourseRegistrationApi>,
    optionalCourses: List<com.example.vtiu.data.model.api.CourseRegistrationApi>,
    selectedOptionalIds: MutableList<Int>,
    isRegistered: Boolean,
    isPrinting: Boolean,
    onRegister: () -> Unit,
    onPrint: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader("Mandatory Courses")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    mandatoryCourses.forEach { course ->
                        CourseRow(course = course, isMandatory = true)
                        if (course != mandatoryCourses.lastOrNull()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        if (optionalCourses.isNotEmpty()) {
            item {
                SectionHeader("Select Optional Courses")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        optionalCourses.forEach { course ->
                            val isChecked = selectedOptionalIds.contains(course.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { 
                                        if (!isRegistered) {
                                            if (it) selectedOptionalIds.add(course.id) else selectedOptionalIds.remove(course.id)
                                        }
                                    },
                                    enabled = !isRegistered
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = course.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${course.code} • ${course.credits} Credits", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            if (course != optionalCourses.lastOrNull()) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        item {
            if (!isRegistered) {
                Button(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolPrimary)
                ) {
                    Text("Register Courses", fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onPrint,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Registration PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                    ) {
                        Text("Reset Registration")
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun CourseRow(course: com.example.vtiu.data.model.api.CourseRegistrationApi, isMandatory: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = course.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "${course.code} • ${course.credits} Credits", fontSize = 12.sp, color = Color.Gray)
        }
        if (isMandatory) {
            Surface(
                color = Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Mandatory",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onSelect(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
