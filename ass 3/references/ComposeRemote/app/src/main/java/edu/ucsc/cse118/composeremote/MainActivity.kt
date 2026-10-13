/*
#######################################################################
#
# Copyright (C) 2026 David C. Harrison. All rights reserved.
#
# You may not use, distribute, publish, or modify this code without
# the express written permission of the copyright holder.
#
#######################################################################
*/
package edu.ucsc.cse118.composeremote

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import androidx.lifecycle.ViewModelProvider

import edu.ucsc.cse118.composeremote.model.SharedViewModel
import edu.ucsc.cse118.composeremote.model.LocalClubViewModel
import edu.ucsc.cse118.composeremote.screen.ErrorScreen
import edu.ucsc.cse118.composeremote.screen.DetailsScreen
import edu.ucsc.cse118.composeremote.screen.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val clubViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        setContent {
            MaterialTheme {
                MainAppNavigation(clubViewModel)
            }
        }
    }
}

private const val HOME = "home"
private const val DETAILS = "details"
private const val ERROR = "error"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppNavigation(clubViewModel: SharedViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val title = when (route) {
        DETAILS -> clubViewModel.selectedClub?.name.orEmpty()
        ERROR -> "Error"
        else -> "MLB Clubs"
    }
    val error = clubViewModel.error

    LaunchedEffect(error, route) {
        if (error != null && route != null && route != ERROR) {
            navController.navigate(ERROR) {
                launchSingleTop = true
            }
        }
    }

    val goBack: () -> Unit = {
        if (route == ERROR) clubViewModel.error = null
        navController.popBackStack()
    }
    BackHandler(enabled = route == ERROR, onBack = goBack)

    CompositionLocalProvider(LocalClubViewModel provides clubViewModel) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (route == DETAILS || route == ERROR) {
                            IconButton(onClick = goBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(HOME) {
                    HomeScreen(onNavigateToDetails = {
                        navController.navigate(DETAILS) {
                            launchSingleTop = true
                        }
                    })
                }
                composable(DETAILS) {
                    DetailsScreen()
                }
                composable(ERROR) {
                    ErrorScreen()
                }
            }
        }
    }
}
