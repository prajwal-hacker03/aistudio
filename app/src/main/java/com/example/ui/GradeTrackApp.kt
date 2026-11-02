package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.SubjectEntity
import com.example.ui.screens.AcademicAnalyticsScreen
import com.example.ui.screens.AttendanceManagerScreen
import com.example.ui.screens.AttendancePredictorScreen
import com.example.ui.screens.CgpaCalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.SgpaCalculatorScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SubjectEditModal
import com.example.ui.screens.SubjectsManagerScreen
import com.example.ui.screens.TargetCgpaPlannerScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.viewmodel.GradeTrackViewModel

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Splash : Screen("splash", "Splash", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Home : Screen("home", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Sgpa : Screen("sgpa", "SGPA", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object Cgpa : Screen("cgpa", "CGPA", Icons.Filled.AutoGraph, Icons.Filled.AutoGraph)
    object Attendance : Screen("attendance", "Attendance", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable)
    object Predictor : Screen("predictor", "Predictor", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable)
    object Subjects : Screen("subjects", "Subjects", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object Analytics : Screen("analytics", "Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object TargetPlanner : Screen("target_planner", "Target GPA", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val BottomNavItems = listOf(
    Screen.Home,
    Screen.Sgpa,
    Screen.Attendance,
    Screen.Subjects,
    Screen.Analytics,
    Screen.Settings
)

@Composable
fun GradeTrackApp(
    viewModel: GradeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val profile by viewModel.studentProfile.collectAsStateWithLifecycle()

    var showGlobalAddSubjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage!!)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute != Screen.Splash.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp
                ) {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSgpa = { navController.navigate(Screen.Sgpa.route) },
                    onNavigateToCgpa = { navController.navigate(Screen.Cgpa.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                    onNavigateToSubjects = { navController.navigate(Screen.Subjects.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                    onOpenProfileSettings = { navController.navigate(Screen.Settings.route) },
                    onAddSubjectClick = { showGlobalAddSubjectDialog = true }
                )
            }

            composable(Screen.Sgpa.route) {
                SgpaCalculatorScreen(viewModel = viewModel)
            }

            composable(Screen.Cgpa.route) {
                CgpaCalculatorScreen(viewModel = viewModel)
            }

            composable(Screen.Attendance.route) {
                AttendanceManagerScreen(
                    viewModel = viewModel,
                    onNavigateToPredictor = { navController.navigate(Screen.Predictor.route) }
                )
            }

            composable(Screen.Predictor.route) {
                AttendancePredictorScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Subjects.route) {
                SubjectsManagerScreen(
                    viewModel = viewModel,
                    onAddSubjectRequest = { showGlobalAddSubjectDialog = true }
                )
            }

            composable(Screen.Analytics.route) {
                AcademicAnalyticsScreen(viewModel = viewModel)
            }

            composable(Screen.TargetPlanner.route) {
                TargetCgpaPlannerScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                ProfileSettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Global Add Subject Modal
    if (showGlobalAddSubjectDialog) {
        val defaultSubject = SubjectEntity(
            name = "",
            code = "",
            semester = profile.currentSemester,
            credits = 3,
            maxMarks = 100.0,
            minimumAttendance = profile.attendanceThreshold
        )
        SubjectEditModal(
            initialSubject = defaultSubject,
            gradingSystem = profile.gradingSystem,
            onDismiss = { showGlobalAddSubjectDialog = false },
            onSave = { created ->
                viewModel.addSubject(created)
                showGlobalAddSubjectDialog = false
            }
        )
    }
}
