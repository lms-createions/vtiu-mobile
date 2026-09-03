package com.example.vtiu.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.theme.TeacherPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceScreen(
    onBackClick: () -> Unit,
    courseId: Int = 1, // Mock course ID for now
    viewModel: TeacherViewModel = hiltViewModel(),
    sessionManager: com.example.vtiu.data.local.SessionManager
) {
    val studentsApi by viewModel.courseStudents
    val existingAttendance by viewModel.attendanceByDate
    val userId = sessionManager.getUserId() ?: ""

    val calendar = remember { Calendar.getInstance() }
    val apiDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault()) }
    
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var showDatePicker by remember { mutableStateOf(false) }

    val attendanceStates = remember { mutableStateMapOf<String, Boolean>() }
    
    LaunchedEffect(courseId) {
        viewModel.loadPerformance(courseId)
    }

    LaunchedEffect(courseId, selectedDate) {
        viewModel.loadAttendanceByDate(courseId, apiDateFormat.format(selectedDate))
    }

    // When existing attendance is loaded, update the map
    LaunchedEffect(existingAttendance) {
        if (existingAttendance.isNotEmpty()) {
            existingAttendance.forEach { record ->
                attendanceStates[record.studentId] = record.isPresent
            }
        } else {
            // Default all to present if no record exists yet
            studentsApi.forEach { student ->
                if (!attendanceStates.containsKey(student.userId)) {
                    attendanceStates[student.userId] = true
                }
            }
        }
    }

    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existingAttendance.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.deleteAttendanceByDate(courseId, apiDateFormat.format(selectedDate)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Attendance deleted for this date")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                    IconButton(
                        onClick = {
                            isSaving = true
                            val records = attendanceStates.map { (id, present) ->
                                com.example.vtiu.data.model.api.AttendanceRecordApi(studentId = id, isPresent = present)
                            }
                            viewModel.markAttendance(
                                courseId = courseId,
                                teacherId = userId,
                                date = apiDateFormat.format(selectedDate),
                                records = records
                            ) {
                                isSaving = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Attendance saved successfully!")
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = TeacherPrimary)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = TeacherPrimary)
                        }
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
            // Header Info - Clickable for Date Picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                onClick = { showDatePicker = true }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TeacherPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Class Date (Tap to change)", fontSize = 12.sp, color = Color.Gray)
                        Text(text = displayDateFormat.format(selectedDate), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Student List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Student Name", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                        Text(text = "Present", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    }
                }

                items(studentsApi) { student ->
                    AttendanceRow(
                        name = student.name,
                        isPresent = attendanceStates[student.userId] ?: true,
                        onToggle = { isPresent -> attendanceStates[student.userId] = isPresent }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) { Text("OK", color = TeacherPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AttendanceRow(name: String, isPresent: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(
                    text = if (isPresent) "Present" else "Absent",
                    fontSize = 12.sp,
                    color = if (isPresent) Color(0xFF43A047) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Switch(
                checked = isPresent,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF43A047),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
        }
    }
}
