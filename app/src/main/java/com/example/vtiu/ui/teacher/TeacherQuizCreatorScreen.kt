package com.example.vtiu.ui.teacher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.courses.DropdownSelector
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherQuizCreatorScreen(
    quizId: Int? = null,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onQuizPublished: () -> Unit,
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
    
    // Quiz State
    var title by remember { mutableStateOf("") }
    var selectedProgramme by remember { mutableStateOf("Midwifery") }
    var selectedLevel by remember { mutableStateOf("100") }
    var selectedCourse by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("15") }
    
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var startHour by remember { mutableIntStateOf(10) }
    var startMinute by remember { mutableIntStateOf(0) }
    
    val questions = remember { mutableStateListOf<DraftQuestion>() }

    val existingQuizDetail by viewModel.quizDetail

    LaunchedEffect(quizId) {
        if (quizId != null) {
            viewModel.loadQuizDetail(quizId)
        }
    }

    LaunchedEffect(existingQuizDetail) {
        if (quizId != null && existingQuizDetail != null) {
            val existing = existingQuizDetail!!
            title = existing.title
            selectedCourse = existing.courseName
            duration = existing.durationMinutes.toString()
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(existing.startDatetime)
                if (date != null) {
                    selectedDate = date
                    val cal = Calendar.getInstance()
                    cal.time = date
                    startHour = cal.get(Calendar.HOUR_OF_DAY)
                    startMinute = cal.get(Calendar.MINUTE)
                }
            } catch (e: Exception) {}
            
            questions.clear()
            existing.questions.forEach { q ->
                val correctIndex = q.options.indexOfFirst { it.isCorrect }.coerceAtLeast(0)
                questions.add(DraftQuestion(
                    text = q.questionText,
                    type = if (q.questionType.lowercase() == "mcq") "MCQ" else "Short Answer",
                    options = q.options.map { it.text }.toMutableStateList(),
                    correctOptionIndex = correctIndex
                ))
            }
        }
    }
    
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
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = if (quizId == null) "Create New Quiz" else "Edit Quiz",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = onMenuClick) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                }
            }

            StepProgressBar(currentStep = currentStep)

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
                    label = "wizard_step"
                ) { step ->
                    when (step) {
                        1 -> QuizSetupStep(
                            title = title, onTitleChange = { title = it },
                            programme = selectedProgramme, onProgrammeChange = { selectedProgramme = it },
                            level = selectedLevel, onLevelChange = { selectedLevel = it },
                            course = selectedCourse, onCourseChange = { selectedCourse = it },
                            duration = duration, onDurationChange = { duration = it },
                            selectedDate = selectedDate, onDateClick = { /* dialog */ },
                            onDateChange = { selectedDate = it },
                            startHour = startHour, startMinute = startMinute,
                            onTimeChange = { h, m -> startHour = h; startMinute = m },
                            teacherClasses = teacherClasses,
                            onNext = {
                                if (title.isBlank() || selectedCourse.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Please fill all required fields") }
                                } else {
                                    currentStep = 2
                                }
                            }
                        )
                        2 -> QuestionBuilderStep(
                            questions = questions,
                            onNext = {
                                if (questions.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar("Add at least one question") }
                                } else {
                                    currentStep = 3
                                }
                            }
                        )
                        3 -> QuizReviewStep(
                            title = title,
                            course = selectedCourse,
                            questionCount = questions.size,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate),
                            time = "%02d:%02d".format(startHour, startMinute),
                            onPublish = {
                                val startDateTime = "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)}T${"%02d:%02d".format(startHour, startMinute)}:00"
                                val request = com.example.vtiu.data.model.api.QuizCreateRequest(
                                    title = title,
                                    courseName = selectedCourse,
                                    durationMinutes = duration.toIntOrNull() ?: 15,
                                    startDatetime = startDateTime,
                                    questions = questions.map { q ->
                                        com.example.vtiu.data.model.api.QuizQuestionCreateApi(
                                            text = q.text,
                                            type = q.type,
                                            points = 1.0f,
                                            options = q.options.toList(),
                                            correctOptionIndex = q.correctOptionIndex
                                        )
                                    }
                                )
                                scope.launch {
                                    if (quizId == null) {
                                        snackbarHostState.showSnackbar("Publishing quiz...")
                                        viewModel.createQuiz(request, userId) {
                                            onQuizPublished()
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Updating quiz...")
                                        viewModel.updateQuiz(quizId, request, userId) {
                                            onQuizPublished()
                                        }
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
fun StepProgressBar(currentStep: Int) {
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
                    .background(if (isActive) TeacherPrimary else Color.LightGray.copy(alpha = 0.5f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetupStep(
    title: String, onTitleChange: (String) -> Unit,
    programme: String, onProgrammeChange: (String) -> Unit,
    level: String, onLevelChange: (String) -> Unit,
    course: String, onCourseChange: (String) -> Unit,
    duration: String, onDurationChange: (String) -> Unit,
    selectedDate: Date, onDateClick: () -> Unit,
    onDateChange: (Date) -> Unit,
    startHour: Int, startMinute: Int,
    onTimeChange: (Int, Int) -> Unit,
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
                    Text("Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Quiz Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dateFormatter.format(selectedDate),
                            onValueChange = {},
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = "%02d:%02d".format(startHour, startMinute),
                            onValueChange = {},
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
                            )
                        )
                    }
                    
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
                colors = ButtonDefaults.buttonColors(containerColor = TeacherPrimary)
            ) {
                Text("Continue to Questions", fontWeight = FontWeight.Bold)
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateChange(Date(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                onTimeChange(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) { TimePicker(state = timePickerState) }
    }
}

data class DraftQuestion(
    var text: String = "",
    var type: String = "MCQ", // MCQ, Short Answer
    val options: MutableList<String> = mutableStateListOf("", ""),
    var correctOptionIndex: Int = 0
)

@Composable
fun QuestionBuilderStep(
    questions: MutableList<DraftQuestion>,
    onNext: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quiz Questions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = TeacherPrimary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = questions.size.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = TeacherPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            itemsIndexed(questions) { index, question ->
                QuestionEditorCard(
                    index = index,
                    question = question,
                    onDelete = { questions.removeAt(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Question", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TeacherPrimary)
            ) {
                Text("Preview & Publish", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Question Type") },
            text = { Text("What type of question would you like to add?") },
            confirmButton = {
                TextButton(onClick = {
                    questions.add(DraftQuestion(type = "MCQ"))
                    showAddDialog = false
                }) { Text("Multiple Choice") }
            },
            dismissButton = {
                TextButton(onClick = {
                    questions.add(DraftQuestion(type = "Short Answer"))
                    showAddDialog = false
                }) { Text("Short Answer") }
            }
        )
    }
}

@Composable
fun QuestionEditorCard(index: Int, question: DraftQuestion, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${index + 1}", fontWeight = FontWeight.Bold, color = TeacherPrimary)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = question.text,
                onValueChange = { question.text = it },
                placeholder = { Text("Enter question text...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            if (question.type == "MCQ") {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Options (Check the correct one)", fontSize = 12.sp, color = Color.Gray)
                
                question.options.forEachIndexed { optIndex, optText ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = question.correctOptionIndex == optIndex,
                            onClick = { question.correctOptionIndex = optIndex },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF43A047))
                        )
                        OutlinedTextField(
                            value = optText,
                            onValueChange = { question.options[optIndex] = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        if (question.options.size > 2) {
                            IconButton(onClick = { question.options.removeAt(optIndex) }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
                
                TextButton(onClick = { question.options.add("") }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Option")
                }
            }
        }
    }
}

@Composable
fun QuizReviewStep(
    title: String,
    course: String,
    questionCount: Int,
    date: String,
    time: String,
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
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF43A047),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "Final Review", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "Verify the details before publishing", color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReviewRow("Title", title)
                ReviewRow("Course", course)
                ReviewRow("Questions", "$questionCount items")
                ReviewRow("Date", date)
                ReviewRow("Start Time", time)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onPublish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TeacherPrimary)
        ) {
            Text("Publish Quiz Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
