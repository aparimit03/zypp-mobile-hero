package com.example.xyzen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.xyzen.navigation.BottomNavItem
import com.example.xyzen.ui.theme.DividerColor
import com.example.xyzen.ui.theme.NeutralLight

@Composable
fun BottomNavigationBar(navController: NavHostController) {
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route

	NavigationBar(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.padding(horizontal = 16.dp, vertical = 8.dp)
			.clip(RoundedCornerShape(32.dp))
			.background(Color.White),
		containerColor = DividerColor,
		contentColor = Color.Black,
		windowInsets = WindowInsets(8.dp,8.dp,8.dp,8.dp)
	) {
		BottomNavItem.items.forEach { item ->
			NavigationBarItem(
				icon = { Icon(item.icon, contentDescription = item.label) },
//				label = { Text(item.label) },
				alwaysShowLabel = false,
				selected = currentRoute == item.route,
				onClick = {
					if (currentRoute != item.route) {
						navController.navigate(item.route) {
							popUpTo(navController.graph.startDestinationId)
							launchSingleTop = true

							anim {
								this.enter = 0
								this.exit = 0
							}
						}
					}
				}
			)
		}
	}
}