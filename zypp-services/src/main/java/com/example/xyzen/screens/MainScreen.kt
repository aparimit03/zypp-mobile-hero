package com.example.xyzen.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.xyzen.components.BottomNavigationBar
import com.example.xyzen.screens.FeedScreen
import com.example.xyzen.screens.NotificationsScreen
import com.example.xyzen.screens.ProfileScreen
import com.example.xyzen.screens.UploadScreen
import com.example.xyzen.navigation.BottomNavItem

@Composable
fun MainScreen() {
	val navController = rememberNavController()

	Scaffold(
		bottomBar = { BottomNavigationBar(navController = navController) }
	) { innerPadding ->
		NavHost(
			navController = navController,
			startDestination = BottomNavItem.Feed.route,
			modifier = Modifier.padding(innerPadding),
			enterTransition = { EnterTransition.None },
			exitTransition = { ExitTransition.None }
		) {
			composable(BottomNavItem.Feed.route) { FeedScreen(navController) }
			composable(BottomNavItem.Upload.route) { UploadScreen() }
			composable(BottomNavItem.Profile.route) { ProfileScreen(navController) }
			composable(BottomNavItem.Notifications.route) { NotificationsScreen() }

			composable(
				route = "video_detail/{videoId}",
				arguments = listOf(navArgument("videoId") { type = NavType.StringType })
			) { backStackEntry ->
				val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
				VideoDetailScreen(navController = navController, videoId = videoId)
			}

			composable(
				route = "profile/{userId}",
				arguments = listOf(navArgument("userId") { type = NavType.StringType })
			) { backStackEntry ->
				val userId = backStackEntry.arguments?.getString("userId")
				ProfileScreen(navController = navController, userId = userId)
			}

			composable("create_playlist") {
				CreatePlaylistScreen(navController = navController)
			}

			composable(
				route = "playlist/{playlistId}",
				arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
			) { backStackEntry ->
				val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
				PlaylistDetailScreen(navController = navController, playlistId = playlistId)
			}

			composable(
				route = "edit_playlist/{playlistId}",
				arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
			) { backStackEntry ->
				val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
				EditPlaylistScreen(navController = navController, playlistId = playlistId)
			}
		}
	}
}