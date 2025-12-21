package com.dark.cloud_gallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun Navigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AuthState.LoggedIn -> {
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen(navController)
                }
import com.dark.cloud_gallery.ui.features.webview.WebViewScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
            }
        }

        is AuthState.LoggedOut -> {
            NavHost(
                navController = navController,
                startDestination = "login"
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
            }
        }
    }
}
