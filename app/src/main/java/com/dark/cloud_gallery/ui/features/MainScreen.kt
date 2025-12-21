package com.dark.cloud_gallery.ui.features

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dark.cloud_gallery.ui.AuthState
import com.dark.cloud_gallery.ui.MainViewModel
import com.dark.cloud_gallery.ui.features.gallery.GalleryScreen
import com.dark.cloud_gallery.ui.features.sms.SmsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val bottomNavController = rememberNavController()
    val items = listOf("gallery", "sms")
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }
    }
    LaunchedEffect(authError) {
        authError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = "Retry"
                )
                viewModel.clearAuthError()
                viewModel.checkAuthStatus()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Cloud Gallery") },
                    actions = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                when (screen) {
                                    "gallery" -> Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                                    "sms" -> Icon(Icons.Filled.Sms, contentDescription = null)
                                }
                            },
                            label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen } == true,
                            onClick = {
                                bottomNavController.navigate(screen) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                bottomNavController,
                startDestination = "gallery",
                Modifier.padding(innerPadding)
            ) {
                composable("gallery") { GalleryScreen(navController) }
                composable("sms") { SmsScreen(navController) }
            }
        }

        if (isSyncing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        viewModel.saveSyncStartDate(datePickerState.selectedDateMillis)
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
