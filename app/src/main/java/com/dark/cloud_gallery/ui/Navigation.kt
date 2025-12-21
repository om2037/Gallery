package com.dark.cloud_gallery.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dark.cloud_gallery.ui.features.MainScreen
import com.dark.cloud_gallery.ui.features.gallery.MediaDetailScreen
import com.dark.cloud_gallery.ui.features.login.LoginScreen
import com.dark.cloud_gallery.ui.features.login.OtpScreen
import com.dark.cloud_gallery.ui.features.webview.WebViewScreen

@Composable
fun Navigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController, viewModel)
        }
        composable(
            "mediaDetail/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) {
            MediaDetailScreen()
        }
        composable(
            "webview/{url}",
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            WebViewScreen(url = url)
        }
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
                        popUpTo("otp") { inclusive = true }
                    }
                }
            )
        }
    }
}
