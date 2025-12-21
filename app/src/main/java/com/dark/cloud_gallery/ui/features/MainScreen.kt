package com.dark.cloud_gallery.ui.features

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dark.cloud_gallery.ui.features.gallery.GalleryScreen

@Composable
fun MainScreen(navController: NavController) {
    val bottomNavController = rememberNavController()
    val items = listOf("photos", "albums", "sms")

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "photos" -> Icon(Icons.Filled.Photo, contentDescription = null)
                                "albums" -> Icon(Icons.Filled.Album, contentDescription = null)
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
            startDestination = "photos",
            Modifier.padding(innerPadding)
        ) {
            composable("photos") { GalleryScreen(navController) }
            composable("albums") { Text("Albums") }
            composable("sms") { Text("SMS Backups") }
        }
    }
}
