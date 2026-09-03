package com.example.vtiu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vtiu.data.local.SessionManager
import com.example.vtiu.ui.auth.ExamLoginScreen
import com.example.vtiu.ui.auth.LoginScreen
import com.example.vtiu.ui.auth.PortalSelectionScreen
import com.example.vtiu.ui.auth.TeacherLoginScreen
import com.example.vtiu.ui.assessments.AssessmentsScreen
import com.example.vtiu.ui.calendar.AcademicCalendarScreen
import com.example.vtiu.ui.appointments.AppointmentBookingScreen
import com.example.vtiu.ui.appointments.MyAppointmentsScreen
import com.example.vtiu.ui.courses.CourseListScreen
import com.example.vtiu.ui.courses.CourseRegistrationScreen
import com.example.vtiu.ui.dashboard.DashboardScreen
import com.example.vtiu.ui.exams.ExamInstructionsScreen
import com.example.vtiu.ui.exams.ExamListScreen
import com.example.vtiu.ui.exams.ExamTakeScreen
import com.example.vtiu.ui.fees.FeesScreen
import com.example.vtiu.ui.fees.PayFeesScreen
import com.example.vtiu.ui.notifications.NotificationScreen
import com.example.vtiu.ui.navigation.Screen
import com.example.vtiu.ui.profile.IdCardScreen
import com.example.vtiu.ui.profile.ProfileScreen
import com.example.vtiu.ui.results.ResultsScreen
import com.example.vtiu.ui.results.TranscriptScreen
import com.example.vtiu.ui.timetable.TimetableScreen
import com.example.vtiu.ui.teacher.TeacherAssessmentSchemeScreen
import com.example.vtiu.ui.teacher.TeacherClassPerformanceScreen
import com.example.vtiu.ui.teacher.TeacherAttendanceAnalyticsScreen
import com.example.vtiu.ui.teacher.TeacherAttendanceHubScreen
import com.example.vtiu.ui.teacher.TeacherAttendanceScreen
import com.example.vtiu.ui.teacher.TeacherAppointmentHubScreen
import com.example.vtiu.ui.teacher.TeacherAppointmentRequestsScreen
import com.example.vtiu.ui.teacher.TeacherAssignmentFileManagerScreen
import com.example.vtiu.ui.teacher.TeacherClassesScreen
import com.example.vtiu.ui.teacher.TeacherDashboardScreen
import com.example.vtiu.ui.teacher.TeacherProfileScreen
import com.example.vtiu.ui.teacher.TeacherExamCreatorScreen
import com.example.vtiu.ui.teacher.TeacherExamHubScreen
import com.example.vtiu.ui.teacher.TeacherExamSubmissionsScreen
import com.example.vtiu.ui.teacher.TeacherGradeSubmissionsScreen
import com.example.vtiu.ui.teacher.TeacherManageExamsScreen
import com.example.vtiu.ui.teacher.TeacherManageQuizzesScreen
import com.example.vtiu.ui.teacher.TeacherLiveHubScreen
import com.example.vtiu.ui.teacher.TeacherLiveRoomScreen
import com.example.vtiu.ui.teacher.TeacherCreateLiveScreen
import com.example.vtiu.ui.teacher.TeacherManageSlotsScreen
import com.example.vtiu.ui.teacher.TeacherMaterialsHubScreen
import com.example.vtiu.ui.teacher.TeacherManageMaterialsScreen
import com.example.vtiu.ui.teacher.TeacherMaterialsUploaderScreen
import com.example.vtiu.ui.teacher.TeacherQuizCreatorScreen
import com.example.vtiu.ui.teacher.TeacherQuizHubScreen
import com.example.vtiu.ui.teacher.TeacherQuizSubmissionsScreen
import com.example.vtiu.ui.vclass.VClassAssignmentsScreen
import com.example.vtiu.ui.vclass.VClassDashboardScreen
import com.example.vtiu.ui.vclass.VClassJoinMeetingScreen
import com.example.vtiu.ui.vclass.VClassLiveClassRoomScreen
import com.example.vtiu.ui.vclass.VClassLiveScreen
import com.example.vtiu.ui.vclass.VClassMaterialsScreen
import com.example.vtiu.ui.vclass.VClassPdfViewerScreen
import com.example.vtiu.ui.vclass.VClassVideoPlayerScreen
import com.example.vtiu.ui.vclass.VClassQuizInstructionsScreen
import com.example.vtiu.ui.vclass.VClassQuizResultScreen
import com.example.vtiu.ui.vclass.VClassSubmitAssignmentScreen
import com.example.vtiu.ui.vclass.VClassTakeQuizScreen
import com.example.vtiu.ui.theme.SchoolPrimary
import com.example.vtiu.ui.theme.VTIUTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VTIUTheme {
                VtiuApp(sessionManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VtiuApp(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val startDestination = if (sessionManager.isLoggedIn()) {
        if (sessionManager.getUserRole() == "teacher") Screen.TeacherDashboard.route 
        else Screen.Dashboard.route
    } else {
        Screen.PortalSelection.route
    }

    val showBottomBar = when {
        currentDestination?.route == null -> false
        currentDestination.route == Screen.PortalSelection.route -> false
        currentDestination.route!!.contains("login") -> false
        currentDestination.route!!.startsWith("teacher") -> false
        currentDestination.route == Screen.TeacherManageQuizzes.route -> false
        currentDestination.route == Screen.TeacherAssignmentFileManager.route -> false
        currentDestination.route == Screen.AcademicCalendar.route -> false
        currentDestination.route!!.startsWith("live_class_room") -> false
        currentDestination.route!!.startsWith("vclass_pdf_viewer") -> false
        currentDestination.route!!.startsWith("take_quiz") -> false
        currentDestination.route!!.startsWith("take_exam") -> false
        currentDestination.route!!.startsWith("join_meeting") -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentDestination?.route != Screen.Login.route) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = SchoolPrimary
                ) {
                    val items = listOf(
                        Triple(Screen.Dashboard, "Home", Icons.Default.Home),
                        Triple(Screen.Courses, "Courses", Icons.Default.Book),
                        Triple(Screen.Profile, "Profile", Icons.Default.Person)
                    )
                    items.forEach { (screen, label, icon) ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SchoolPrimary,
                                selectedTextColor = SchoolPrimary,
                                indicatorColor = Color(0xFFE8F5E9) // matching light green theme
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val hostPadding = if (showBottomBar) {
            innerPadding
        } else {
            PaddingValues(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = 0.dp
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(hostPadding)
        ) {
            composable(Screen.PortalSelection.route) {
                PortalSelectionScreen(
                    onStudentClick = { navController.navigate(Screen.StudentLogin.route) },
                    onTeacherClick = { navController.navigate(Screen.TeacherLogin.route) },
                    onVClassClick = { navController.navigate(Screen.StudentLogin.route) }, // VClass usually requires student login
                    onExamClick = { navController.navigate(Screen.ExamLogin.route) }
                )
            }
            composable(Screen.StudentLogin.route) {
                LoginScreen(
                    onLoginSuccess = { userId, userName, profilePic ->
                        sessionManager.saveSession(userId, "student", userName, profilePic)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.PortalSelection.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.TeacherLogin.route) {
                TeacherLoginScreen(
                    onLoginSuccess = { userId, userName, profilePic ->
                        sessionManager.saveSession(userId, "teacher", userName, profilePic)
                        navController.navigate(Screen.TeacherDashboard.route) {
                            popUpTo(Screen.PortalSelection.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.ExamLogin.route) {
                ExamLoginScreen(
                    onLoginSuccess = { userId, userName, profilePic ->
                        sessionManager.saveSession(userId, "student", userName, profilePic)
                        navController.navigate(Screen.Exams.route) {
                            popUpTo(Screen.PortalSelection.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onTileClick = { title ->
                        when (title) {
                            "My Courses" -> navController.navigate(Screen.Courses.route)
                            "My Profile" -> navController.navigate(Screen.Profile.route)
                            "Registration" -> navController.navigate(Screen.CourseRegistration.route)
                            "Results" -> navController.navigate(Screen.Results.route)
                            "Assessments" -> navController.navigate(Screen.Assessments.route)
                            "Appointments" -> navController.navigate(Screen.BookAppointment.route)
                            "Academic Calendar" -> navController.navigate(Screen.AcademicCalendar.route)
                            "Transcript" -> navController.navigate(Screen.Transcript.route)
                            "Exams" -> navController.navigate(Screen.Exams.route)
                            "Timetable" -> navController.navigate(Screen.Timetable.route)
                            "Fees" -> navController.navigate(Screen.Fees.route)
                            "Virtual Class" -> navController.navigate(Screen.VClassDashboard.route)
                        }
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherDashboard.route) {
                TeacherDashboardScreen(
                    onActionClick = { route ->
                        when (route) {
                            "attendance" -> navController.navigate(Screen.TeacherClasses.route)
                            "submissions" -> navController.navigate(Screen.TeacherGrading.route)
                            else -> navController.navigate(route)
                        }
                    },
                    onLogoutClick = {
                        sessionManager.logout()
                        navController.navigate(Screen.PortalSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherClasses.route) {
                TeacherClassesScreen(
                    onBackClick = { navController.popBackStack() },
                    onClassClick = { courseId -> 
                        navController.navigate(Screen.TeacherAttendanceHub.createRoute(courseId))
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherProfile.route) {
                TeacherProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        sessionManager.logout()
                        navController.navigate(Screen.PortalSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    sessionManager = sessionManager
                )
            }
            composable(
                Screen.TeacherAttendanceHub.route,
                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                TeacherAttendanceHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onMarkAttendanceClick = { navController.navigate(Screen.TeacherAttendance.createRoute(courseId)) },
                    onViewAnalyticsClick = { navController.navigate(Screen.TeacherAttendanceAnalytics.createRoute(courseId)) }
                )
            }
            composable(
                Screen.TeacherAttendance.route,
                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                TeacherAttendanceScreen(
                    onBackClick = { navController.popBackStack() },
                    courseId = courseId,
                    sessionManager = sessionManager
                )
            }
            composable(
                Screen.TeacherAttendanceAnalytics.route,
                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
                TeacherAttendanceAnalyticsScreen(
                    onBackClick = { navController.popBackStack() },
                    courseId = courseId,
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherGrading.route) {
                TeacherGradeSubmissionsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherAssessmentScheme.route) {
                TeacherAssessmentSchemeScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherClassPerformance.route) {
                TeacherClassPerformanceScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherAssignmentFileManager.route) {
                TeacherAssignmentFileManagerScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherAppointmentHub.route) {
                TeacherAppointmentHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onManageSlotsClick = { navController.navigate(Screen.TeacherManageSlots.route) },
                    onViewRequestsClick = { navController.navigate(Screen.TeacherAppointmentRequests.route) }
                )
            }
            composable(Screen.TeacherManageSlots.route) {
                TeacherManageSlotsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherAppointmentRequests.route) {
                TeacherAppointmentRequestsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherQuizHub.route) {
                TeacherQuizHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onCreateQuizClick = { navController.navigate(Screen.TeacherCreateQuiz.createRoute()) },
                    onManageQuizzesClick = { navController.navigate(Screen.TeacherManageQuizzes.route) },
                    onViewSubmissionsClick = { navController.navigate(Screen.TeacherQuizSubmissions.route) }
                )
            }
            composable(Screen.TeacherManageQuizzes.route) {
                TeacherManageQuizzesScreen(
                    onBackClick = { navController.popBackStack() },
                    onEditQuizClick = { quizId -> 
                        navController.navigate(Screen.TeacherCreateQuiz.createRoute(quizId))
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherQuizSubmissions.route) {
                TeacherQuizSubmissionsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.TeacherCreateQuiz.route,
                arguments = listOf(navArgument("quizId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val quizIdStr = backStackEntry.arguments?.getString("quizId")
                val quizId = quizIdStr?.toIntOrNull()
                TeacherQuizCreatorScreen(
                    quizId = quizId,
                    onBackClick = { navController.popBackStack() },
                    onQuizPublished = {
                        navController.popBackStack(Screen.TeacherQuizHub.route, false)
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherExamHub.route) {
                TeacherExamHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onCreateExamClick = { navController.navigate(Screen.TeacherCreateExam.route) },
                    onManageExamsClick = { navController.navigate(Screen.TeacherManageExams.route) },
                    onViewSubmissionsClick = { navController.navigate(Screen.TeacherExamSubmissions.route) }
                )
            }
            composable(Screen.TeacherManageExams.route) {
                TeacherManageExamsScreen(
                    onBackClick = { navController.popBackStack() },
                    onEditExamClick = { /* Edit */ },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherExamSubmissions.route) {
                TeacherExamSubmissionsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherLiveHub.route) {
                TeacherLiveHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onCreateLiveClick = { navController.navigate(Screen.TeacherCreateLive.route) },
                    onHostClick = { id -> navController.navigate(Screen.TeacherLiveRoom.createRoute(id)) },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherCreateLive.route) {
                TeacherCreateLiveScreen(
                    onBackClick = { navController.popBackStack() },
                    onScheduleSuccess = {
                        navController.popBackStack(Screen.TeacherLiveHub.route, false)
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherLiveRoom.route, arguments = listOf(navArgument("meetingId") { type = NavType.IntType })) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("meetingId") ?: 0
                TeacherLiveRoomScreen(meetingId = id, onEndClick = { navController.popBackStack() })
            }
            composable(Screen.TeacherMaterialsHub.route) {
                TeacherMaterialsHubScreen(
                    onBackClick = { navController.popBackStack() },
                    onUploadClick = { navController.navigate(Screen.TeacherMaterialsUploader.route) },
                    onManageClick = { navController.navigate(Screen.TeacherManageMaterials.route) }
                )
            }
            composable(Screen.TeacherManageMaterials.route) {
                TeacherManageMaterialsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherMaterialsUploader.route) {
                TeacherMaterialsUploaderScreen(
                    onBackClick = { navController.popBackStack() },
                    onUploadSuccess = {
                        navController.popBackStack(Screen.TeacherDashboard.route, false)
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.TeacherCreateExam.route) {
                TeacherExamCreatorScreen(
                    onBackClick = { navController.popBackStack() },
                    onExamPublished = {
                        navController.popBackStack(Screen.TeacherExamHub.route, false)
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Courses.route) {
                CourseListScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.CourseRegistration.route) {
                CourseRegistrationScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Results.route) {
                ResultsScreen(
                    onBackClick = { navController.popBackStack() },
                    onViewTranscriptClick = { navController.navigate(Screen.Transcript.route) },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Transcript.route) {
                TranscriptScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Assessments.route) {
                AssessmentsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.AcademicCalendar.route) {
                AcademicCalendarScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.BookAppointment.route) {
                AppointmentBookingScreen(
                    onBackClick = { navController.popBackStack() },
                    onViewMyAppointments = { navController.navigate(Screen.MyAppointments.route) },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.MyAppointments.route) {
                MyAppointmentsScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Exams.route) {
                ExamListScreen(
                    onBackClick = { navController.popBackStack() },
                    onExamClick = { id -> navController.navigate(Screen.ExamInstructions.createRoute(id)) },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.ExamInstructions.route,
                arguments = listOf(navArgument("examId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("examId") ?: 0
                ExamInstructionsScreen(
                    examId = id,
                    onBackClick = { navController.popBackStack() },
                    onStartExamClick = { examId -> navController.navigate(Screen.TakeExam.createRoute(examId)) }
                )
            }
            composable(
                route = Screen.TakeExam.route,
                arguments = listOf(navArgument("examId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("examId") ?: 0
                ExamTakeScreen(
                    examId = id,
                    onBackClick = { navController.popBackStack() },
                    onSubmitSuccess = { title, score, total ->
                        navController.navigate(Screen.QuizResult.createRoute(title, score, total)) {
                            popUpTo(Screen.Exams.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Timetable.route) {
                TimetableScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Fees.route) {
                FeesScreen(
                    onBackClick = { navController.popBackStack() },
                    onPayNowClick = { navController.navigate(Screen.PayFees.route) },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.PayFees.route) {
                PayFeesScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Notifications.route) {
                NotificationScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.VClassDashboard.route) {
                VClassDashboardScreen(
                    onBackClick = { navController.popBackStack() },
                    onMaterialsClick = { navController.navigate(Screen.VClassMaterials.route) },
                    onAssignmentsClick = { navController.navigate(Screen.VClassAssignments.route) },
                    onLiveClick = { navController.navigate(Screen.VClassLive.route) },
                    onQuizClick = { quizId -> navController.navigate(Screen.QuizInstructions.createRoute(quizId)) },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.QuizInstructions.route,
                arguments = listOf(navArgument("quizId") { type = NavType.IntType })
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getInt("quizId") ?: 0
                VClassQuizInstructionsScreen(
                    quizId = quizId,
                    onBackClick = { navController.popBackStack() },
                    onStartQuizClick = { id -> navController.navigate(Screen.TakeQuiz.createRoute(id)) }
                )
            }
            composable(
                route = Screen.TakeQuiz.route,
                arguments = listOf(navArgument("quizId") { type = NavType.IntType })
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getInt("quizId") ?: 0
                VClassTakeQuizScreen(
                    quizId = quizId,
                    onBackClick = { navController.popBackStack() },
                    onSubmitSuccess = { title, score, total ->
                        navController.navigate(Screen.QuizResult.createRoute(title, score, total)) {
                            popUpTo(Screen.VClassDashboard.route)
                        }
                    },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("score") { type = NavType.FloatType },
                    navArgument("total") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: "Quiz"
                val score = backStackEntry.arguments?.getFloat("score") ?: 0f
                val total = backStackEntry.arguments?.getFloat("total") ?: 0f
                VClassQuizResultScreen(
                    quizTitle = title,
                    score = score,
                    total = total,
                    onBackToDashboard = {
                        navController.popBackStack(Screen.Dashboard.route, false)
                    }
                )
            }
            composable(Screen.VClassMaterials.route) {
                VClassMaterialsScreen(
                    onBackClick = { navController.popBackStack() },
                    onPreviewClick = { id -> navController.navigate(Screen.VClassPdfViewer.createRoute(id)) },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.VClassPdfViewer.route,
                arguments = listOf(navArgument("materialId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("materialId") ?: 0
                VClassPdfViewerScreen(
                    materialId = id,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.VClassLive.route) {
                VClassLiveScreen(
                    onBackClick = { navController.popBackStack() },
                    onJoinClick = { id -> navController.navigate(Screen.JoinMeeting.createRoute(id)) },
                    onRecordingClick = { id -> navController.navigate(Screen.VClassPlayer.createRoute(id)) },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.VClassPlayer.route,
                arguments = listOf(navArgument("recordingId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("recordingId") ?: 0
                VClassVideoPlayerScreen(
                    recordingId = id,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.JoinMeeting.route,
                arguments = listOf(navArgument("meetingId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("meetingId") ?: 0
                VClassJoinMeetingScreen(
                    meetingId = id,
                    onBackClick = { navController.popBackStack() },
                    onJoinNowClick = { meetingId -> 
                        navController.navigate(Screen.LiveClassRoom.createRoute(meetingId))
                    }
                )
            }
            composable(
                route = Screen.LiveClassRoom.route,
                arguments = listOf(navArgument("meetingId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("meetingId") ?: 0
                VClassLiveClassRoomScreen(
                    meetingId = id,
                    onLeaveClick = { navController.popBackStack() }
                )
            }
            composable(Screen.VClassAssignments.route) {
                VClassAssignmentsScreen(
                    onBackClick = { navController.popBackStack() },
                    onSubmitClick = { id -> navController.navigate(Screen.SubmitAssignment.createRoute(id)) },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = Screen.SubmitAssignment.route,
                arguments = listOf(navArgument("assignmentId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("assignmentId") ?: 0
                VClassSubmitAssignmentScreen(
                    assignmentId = id,
                    onBackClick = { navController.popBackStack() },
                    onSubmitSuccess = {
                        navController.popBackStack()
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onIdCardClick = { navController.navigate(Screen.IdCard.route) },
                    onLogoutClick = {
                        sessionManager.logout()
                        navController.navigate(Screen.PortalSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    sessionManager = sessionManager
                )
            }
            composable(Screen.IdCard.route) {
                IdCardScreen(
                    onBackClick = { navController.popBackStack() },
                    sessionManager = sessionManager
                )
            }
        }
    }
}
