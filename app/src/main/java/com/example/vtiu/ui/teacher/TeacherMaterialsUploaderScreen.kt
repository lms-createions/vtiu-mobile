package com.example.vtiu.ui.teacher

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.courses.DropdownSelector
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.launch

data class SelectedFile(val name: String, val uri: Uri)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMaterialsUploaderScreen(
    onBackClick: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val context = LocalContext.current
    val teacherClasses by viewModel.teacherClasses
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Material State
    var title by remember { mutableStateOf("") }
    var selectedProgramme by remember { mutableStateOf("Midwifery") }
    var selectedLevel by remember { mutableStateOf("100") }
    var selectedCourse by remember { mutableStateOf("") }
    val selectedFiles = remember { mutableStateListOf<SelectedFile>() }
    
    var isUploading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val name = getFileName(context, uri) ?: "unknown_file"
            if (selectedFiles.none { it.uri == uri }) {
                selectedFiles.add(SelectedFile(name, uri))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upload Materials", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBackClick()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F6F8))
        ) {
            // Progress Indicator
            UploaderProgressBar(currentStep = currentStep)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    },
                    label = "uploader_wizard_step"
                ) { step ->
                    when (step) {
                        1 -> MaterialDetailsStep(
                            title = title, onTitleChange = { title = it },
                            programme = selectedProgramme, onProgrammeChange = { selectedProgramme = it },
                            level = selectedLevel, onLevelChange = { selectedLevel = it },
                            course = selectedCourse, onCourseChange = { selectedCourse = it },
                            teacherClasses = teacherClasses,
                            onNext = {
                                if (title.isBlank() || selectedCourse.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                } else {
                                    currentStep = 2
                                }
                            }
                        )
                        2 -> FileSelectionStep(
                            selectedFiles = selectedFiles,
                            onPickFiles = { launcher.launch("*/*") },
                            onNext = {
                                if (selectedFiles.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("Select at least one file") }
                                } else {
                                    currentStep = 3
                                }
                            }
                        )
                        3 -> MaterialReviewStep(
                            title = title,
                            course = selectedCourse,
                            fileCount = selectedFiles.size,
                            onUpload = {
                                isUploading = true
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val filesData = selectedFiles.map { sf ->
                                        val bytes = context.contentResolver.openInputStream(sf.uri)?.use { it.readBytes() } ?: byteArrayOf()
                                        Pair(sf.name, bytes)
                                    }
                                    
                                    viewModel.uploadMaterialWithFiles(
                                        title = title,
                                        programme = selectedProgramme,
                                        level = selectedLevel,
                                        courseName = selectedCourse,
                                        files = filesData
                                    ) {
                                        isUploading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Materials uploaded successfully!")
                                            onUploadSuccess()
                                        }
                                    }
                                }
                            },
                            isUploading = isUploading
                        )
                    }
                }
            }
        }
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

@Composable
fun UploaderProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val stepNum = index + 1
            val isActive = stepNum <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF36B9CC) else Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun MaterialDetailsStep(
    title: String, onTitleChange: (String) -> Unit,
    programme: String, onProgrammeChange: (String) -> Unit,
    level: String, onLevelChange: (String) -> Unit,
    course: String, onCourseChange: (String) -> Unit,
    teacherClasses: List<com.example.vtiu.data.model.api.TeacherClassApi>,
    onNext: () -> Unit
) {
    val availableProgrammes = teacherClasses.map { it.programme }.distinct()
    val availableLevels = teacherClasses
        .filter { it.programme == programme }
        .map { it.level }
        .distinct()

    val filteredCourses = teacherClasses
        .filter { it.programme == programme && it.level == level }
        .map { it.courseName }

    // Auto-select first available if current selection is invalid
    LaunchedEffect(availableProgrammes) {
        if (programme.isEmpty() || !availableProgrammes.contains(programme)) {
            if (availableProgrammes.isNotEmpty()) onProgrammeChange(availableProgrammes.first())
        }
    }
    
    LaunchedEffect(availableLevels) {
        if (level.isEmpty() || !availableLevels.contains(level)) {
            if (availableLevels.isNotEmpty()) onLevelChange(availableLevels.first())
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Classification", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    DropdownSelector(
                        options = availableProgrammes,
                        selected = programme,
                        onSelect = onProgrammeChange
                    )
                    
                    DropdownSelector(
                        options = availableLevels,
                        selected = level,
                        onSelect = onLevelChange
                    )
                    
                    DropdownSelector(
                        options = filteredCourses,
                        selected = if (filteredCourses.contains(course)) course else "",
                        onSelect = onCourseChange
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("General Info", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Material Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF36B9CC))
            ) {
                Text("Next: Select Files", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FileSelectionStep(
    selectedFiles: MutableList<SelectedFile>,
    onPickFiles: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F9FA))
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPickFiles() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Tap to select files", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            "Multiple files allowed",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        if (selectedFiles.isNotEmpty()) {
            Text(text = "Selected Files", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    selectedFiles.forEachIndexed { index, file ->
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF36B9CC))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = file.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            IconButton(onClick = { selectedFiles.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (index < selectedFiles.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF36B9CC))
        ) {
            Text("Next: Final Review", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MaterialReviewStep(
    title: String,
    course: String,
    fileCount: Int,
    onUpload: () -> Unit,
    isUploading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = Color(0xFF1CC88A),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Confirm Submission", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Review your material details before uploading", color = Color.Gray, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MaterialReviewRow("Title", title)
                MaterialReviewRow("Course", course)
                MaterialReviewRow("Files", "$fileCount item(s)")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onUpload,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CC88A)),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Start Upload", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MaterialReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}
