package com.example.vtiu.ui.teacher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.courses.DropdownSelector
import com.example.vtiu.ui.theme.TeacherPrimary
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherExamCreatorScreen(
    onBackClick: () -> Unit,
    onExamPublished: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val teacherClasses by viewModel.teacherClasses
    val userId = sessionManager.getUserId() ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadTeacherClasses(userId)
        }
    }
    
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Exam State
    var title by remember { mutableStateOf("") }
    var selectedProgramme by remember { mutableStateOf("Midwifery") }
    var selectedLevel by remember { mutableStateOf("100") }
    var selectedCourse by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }
    var examPassword by remember { mutableStateOf("") }
    var assignmentMode by remember { mutableStateOf("Random") } // Random, Choice, Hash
    
    val questions = remember { mutableStateListOf<DraftQuestion>() }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create New Exam", fontWeight = FontWeight.Bold) },
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
            // Progress Indicator (4 steps for Exam)
            ExamStepProgressBar(currentStep = currentStep)

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
                    label = "exam_wizard_step"
                ) { step ->
                    when (step) {
                        1 -> ExamSetupStep(
                            title = title, onTitleChange = { title = it },
                            programme = selectedProgramme, onProgrammeChange = { selectedProgramme = it },
                            level = selectedLevel, onLevelChange = { selectedLevel = it },
                            course = selectedCourse, onCourseChange = { selectedCourse = it },
                            duration = duration, onDurationChange = { duration = it },
                            teacherClasses = teacherClasses,
                            onNext = {
                                if (title.isBlank() || selectedCourse.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                } else {
                                    currentStep = 2
                                }
                            }
                        )
                        2 -> ExamSecurityStep(
                            password = examPassword, onPasswordChange = { examPassword = it },
                            mode = assignmentMode, onModeChange = { assignmentMode = it },
                            onNext = {
                                if (examPassword.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Set an exam password for security") }
                                } else {
                                    currentStep = 3
                                }
                            }
                        )
                        3 -> QuestionBuilderStep(
                            questions = questions,
                            onNext = {
                                if (questions.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("Add at least one question") }
                                } else {
                                    currentStep = 4
                                }
                            }
                        )
                        4 -> ExamReviewStep(
                            title = title,
                            course = selectedCourse,
                            questionCount = questions.size,
                            mode = assignmentMode,
                            onPublish = {
                                val startDateTime = "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())}T10:00:00" // Hardcoded time for demo or need time picker
                                val request = com.example.vtiu.data.model.api.ExamCreateRequest(
                                    title = title,
                                    courseName = selectedCourse,
                                    durationMinutes = duration.toIntOrNull() ?: 60,
                                    startDatetime = startDateTime,
                                    assignmentMode = assignmentMode,
                                    questions = questions.map { q ->
                                        com.example.vtiu.data.model.api.ExamQuestionCreateApi(
                                            text = q.text,
                                            type = q.type,
                                            marks = 1,
                                            options = q.options.toList(),
                                            correctOptionIndex = q.correctOptionIndex
                                        )
                                    }
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Configuring exam sets and publishing...")
                                    viewModel.createExam(request, userId) {
                                        onExamPublished()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExamStepProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val stepNum = index + 1
            val isActive = stepNum <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFFD32F2F) else Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun ExamSetupStep(
    title: String, onTitleChange: (String) -> Unit,
    programme: String, onProgrammeChange: (String) -> Unit,
    level: String, onLevelChange: (String) -> Unit,
    course: String, onCourseChange: (String) -> Unit,
    duration: String, onDurationChange: (String) -> Unit,
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
                    Text("Target Program", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
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
                    Text("Exam Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Exam Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = duration,
                        onValueChange = onDurationChange,
                        label = { Text("Duration (Minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Next: Security Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExamSecurityStep(
    password: String, onPasswordChange: (String) -> Unit,
    mode: String, onModeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val modes = listOf("Random", "Choice", "Hash")

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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Access Security", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Exam Access Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("e.g. 1234") }
                )
                
                Text(
                    text = "Students will be required to enter this password before they can see the exam instructions.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Assignment Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                modes.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == item,
                            onClick = { onModeChange(item) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD32F2F))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = item, fontWeight = FontWeight.Medium)
                            Text(
                                text = when(item) {
                                    "Random" -> "Assigns a random question set to students"
                                    "Choice" -> "Students choose their preferred question set"
                                    else -> "Uses a hash to ensure stable set assignment"
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Continue to Questions", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExamReviewStep(
    title: String,
    course: String,
    questionCount: Int,
    mode: String,
    onPublish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GppGood,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Final Exam Review", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Please verify all settings before final publication", color = Color.Gray, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewRow("Exam Title", title)
                ReviewRow("Subject", course)
                ReviewRow("Questions", "$questionCount Items")
                ReviewRow("Mode", mode)
                ReviewRow("Security", "Password Set")
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Publish Final Exam", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
