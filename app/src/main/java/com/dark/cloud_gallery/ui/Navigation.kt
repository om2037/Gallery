package com.dark.cloud_gallery.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navArgument
import com.dark.cloud_gallery.ui.features.MainScreen
import com.dark.cloud_gallery.ui.features.gallery.MediaDetailScreen
import com.dark.cloud_gallery.ui.features.login.LoginScreen
import com.dark.cloud_gallery.ui.features.login.OtpScreen

@Composable
fun Navigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.isLoggedIn()) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToOtp = {
                    navController.navigate("otp")
                }
            )
        }
        composable("otp") {
            OtpScreen(
                onNavigateToGallery = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(navController)
        }
        composable(
            "mediaDetail/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) {
            MediaDetailScreen()
        }
    }
}
