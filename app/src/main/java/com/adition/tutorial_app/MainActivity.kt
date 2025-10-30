package com.adition.tutorial_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adition.tutorial_app.presentation.screens.MainRoute
import com.adition.tutorial_app.presentation.screens.MainScreen
import com.adition.tutorial_app.presentation.screens.inline_screen.InlineRoute
import com.adition.tutorial_app.presentation.screens.inline_screen.InlineScreen
import com.adition.tutorial_app.ui.theme.TutorialAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorialAppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = MainRoute
                ) {
                    composable<MainRoute> { MainScreen(navController = navController) }
                    composable<InlineRoute> { InlineScreen(navController = navController) }
                }
            }
        }
    }
}
