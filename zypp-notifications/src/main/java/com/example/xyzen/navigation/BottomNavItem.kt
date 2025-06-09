package com.example.xyzen.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Feed : BottomNavItem("feed", "Feed", Icons.Default.Home)
    object Upload : BottomNavItem("upload", "Upload", Icons.Default.Add)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
    object Notifications : BottomNavItem("notifications", "Notifications", Icons.Default.Notifications)

    companion object {
        val items = listOf(Feed, Upload, Profile, Notifications)
    }
}