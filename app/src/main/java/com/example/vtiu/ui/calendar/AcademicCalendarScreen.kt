package com.example.vtiu.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vtiu.ui.teacher.TeacherViewModel
import com.example.vtiu.data.model.AcademicEvent
import com.example.vtiu.ui.theme.SchoolPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicCalendarScreen(
    onBackClick: () -> Unit,
    viewModel: TeacherViewModel = hiltViewModel()
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val eventsApi by viewModel.calendarEvents
    val selectedDateStr = dateFormat.format(selectedDate.time)
    
    LaunchedEffect(Unit) {
        viewModel.loadCalendar()
    }

    val dayEvents = eventsApi.filter { it.date == selectedDateStr }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .background(Color(0xFFF8FAFC))
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
                        text = "Academic Calendar",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                
                IconButton(onClick = { 
                    calendar = Calendar.getInstance()
                    selectedDate = Calendar.getInstance()
                }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Today", tint = SchoolPrimary)
                }
            }

            // Legend
            CalendarLegend()

            // Calendar Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newCal = calendar.clone() as Calendar
                                newCal.add(Calendar.MONTH, -1)
                                calendar = newCal
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev", modifier = Modifier.size(20.dp))
                        }
                        
                        Text(
                            text = monthYearFormat.format(calendar.time),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        
                        IconButton(
                            onClick = {
                                val newCal = calendar.clone() as Calendar
                                newCal.add(Calendar.MONTH, 1)
                                calendar = newCal
                            },
                            modifier = Modifier.size(32.dp).background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Days of Week
                    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid
                    CalendarGrid(
                        calendar = calendar,
                        selectedDate = selectedDate,
                        events = eventsApi.map { AcademicEvent(it.id, it.title, it.date, it.type, it.isWorkday, "") },
                        onDateSelected = { newDate ->
                            selectedDate = newDate
                            // If the selected date is in a different month than currently displayed, switch the view
                            if (newDate.get(Calendar.MONTH) != calendar.get(Calendar.MONTH) || 
                                newDate.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) {
                                val newCal = newDate.clone() as Calendar
                                newCal.set(Calendar.DAY_OF_MONTH, 1)
                                calendar = newCal
                            }
                        }
                    )
                }
            }

            // Events List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(selectedDate.time),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                if (dayEvents.isNotEmpty()) {
                    Surface(
                        color = SchoolPrimary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${dayEvents.size} Events",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolPrimary
                        )
                    }
                }
            }

            // Events List
            if (dayEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            color = Color(0xFFF8FAFC),
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Event, 
                                contentDescription = null, 
                                tint = Color(0xFFCBD5E1), 
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Free day", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text("No academic activities listed", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dayEvents) { event ->
                        AcademicEventCard(
                            AcademicEvent(
                                id = event.id,
                                title = event.title,
                                date = event.date,
                                type = event.type,
                                isWorkday = event.isWorkday,
                                description = ""
                            )
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(Color(0xFF2E7D32), "Academic")
        LegendItem(Color(0xFFD32F2F), "Exams")
        LegendItem(Color(0xFFF57F17), "Holiday")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
    }
}

@Composable
fun CalendarGrid(
    calendar: Calendar,
    selectedDate: Calendar,
    events: List<AcademicEvent>,
    onDateSelected: (Calendar) -> Unit
) {
    val tempCal = calendar.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val prevMonthCal = tempCal.clone() as Calendar
    prevMonthCal.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(260.dp),
        userScrollEnabled = false
    ) {
        // Previous Month Padding
        items((0 until firstDayOfWeek).toList()) { i ->
            val day = daysInPrevMonth - firstDayOfWeek + i + 1
            val prevMonthDayCal = calendar.clone() as Calendar
            prevMonthDayCal.add(Calendar.MONTH, -1)
            prevMonthDayCal.set(Calendar.DAY_OF_MONTH, day)
            
            val hasEvent = events.any { it.date == dateFormat.format(prevMonthDayCal.time) }
            
            DayItem(
                day = day, 
                isCurrentMonth = false,
                hasEvent = hasEvent,
                onClick = { onDateSelected(prevMonthDayCal) }
            )
        }

        // Current Month Days
        items((1..daysInMonth).toList()) { day ->
            val currentDayCal = calendar.clone() as Calendar
            currentDayCal.set(Calendar.DAY_OF_MONTH, day)
            
            val isSelected = dateFormat.format(currentDayCal.time) == dateFormat.format(selectedDate.time)
            val hasEvent = events.any { it.date == dateFormat.format(currentDayCal.time) }
            val isToday = dateFormat.format(currentDayCal.time) == dateFormat.format(Calendar.getInstance().time)

            DayItem(
                day = day,
                isCurrentMonth = true,
                isSelected = isSelected,
                isToday = isToday,
                hasEvent = hasEvent,
                onClick = { onDateSelected(currentDayCal) }
            )
        }

        // Next Month Padding to fill the grid (usually 42 cells total for 6 rows)
        val totalCellsShown = firstDayOfWeek + daysInMonth
        val nextMonthPadding = 42 - totalCellsShown
        items((1..nextMonthPadding).toList()) { day ->
            val nextMonthDayCal = calendar.clone() as Calendar
            nextMonthDayCal.add(Calendar.MONTH, 1)
            nextMonthDayCal.set(Calendar.DAY_OF_MONTH, day)

            val hasEvent = events.any { it.date == dateFormat.format(nextMonthDayCal.time) }

            DayItem(
                day = day,
                isCurrentMonth = false,
                hasEvent = hasEvent,
                onClick = { onDateSelected(nextMonthDayCal) }
            )
        }
    }
}

@Composable
fun DayItem(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean = false,
    isToday: Boolean = false,
    hasEvent: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp)) // Modern squared-round shape
            .background(
                when {
                    isSelected -> SchoolPrimary
                    isToday -> SchoolPrimary.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
            )
            .then(
                if (isToday && !isSelected) Modifier.border(1.dp, SchoolPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = when {
                    isSelected -> Color.White
                    !isCurrentMonth -> Color(0xFFCBD5E1)
                    isToday -> SchoolPrimary
                    else -> Color(0xFF334155)
                }
            )
            if (hasEvent && isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else SchoolPrimary)
                )
            }
        }
    }
}

@Composable
fun AcademicEventCard(event: AcademicEvent) {
    val color = when (event.type) {
        "Lecture" -> Color(0xFF10B981) // Modern Emerald
        "Exam" -> Color(0xFFEF4444)    // Modern Red
        "Holiday" -> Color(0xFFF59E0B) // Modern Amber
        else -> SchoolPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(event.type) {
                        "Exam" -> Icons.Default.Info
                        else -> Icons.Default.Event
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.type.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                    if (!event.isWorkday) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• Campus Closed", 
                            fontSize = 11.sp, 
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
