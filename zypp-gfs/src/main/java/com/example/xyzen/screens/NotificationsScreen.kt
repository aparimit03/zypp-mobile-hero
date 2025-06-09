package com.example.xyzen.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xyzen.components.NeumorphicCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen() {
	val coroutineScope = rememberCoroutineScope()
	var isLoading by remember { mutableStateOf(true) }
	var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

	// Simulate loading notifications
	LaunchedEffect(Unit) {
		coroutineScope.launch {
			delay(1000) // Simulate network delay
			notifications = sampleNotifications
			isLoading = false
		}
	}

	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		if (isLoading) {
			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(48.dp)
			)
		} else if (notifications.isEmpty()) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.padding(16.dp)
			) {
				Icon(
					imageVector = Icons.Default.NotificationsNone,
					contentDescription = null,
					modifier = Modifier.size(64.dp),
					tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
				)
				Spacer(modifier = Modifier.height(16.dp))
				Text(
					text = "No notifications yet",
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Medium
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "We'll notify you when something interesting happens",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		} else {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(horizontal = 16.dp, vertical = 8.dp)
			) {
				item {
					Text(
						text = "Notifications",
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.padding(vertical = 16.dp)
					)
				}

				items(notifications) { notification ->
					NotificationCard(notification)
					Spacer(modifier = Modifier.height(12.dp))
				}
			}
		}
	}
}

@Composable
fun NotificationCard(notification: NotificationItem) {
	NeumorphicCard(
		modifier = Modifier.fillMaxWidth(),
		elevation = 4
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = Icons.Default.Notifications,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(24.dp)
			)

			Spacer(modifier = Modifier.width(16.dp))

			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = notification.title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.SemiBold
				)

				Spacer(modifier = Modifier.height(4.dp))

				Text(
					text = notification.message,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)

				Spacer(modifier = Modifier.height(4.dp))

				Text(
					text = notification.time,
					style = MaterialTheme.typography.bodySmall,
					color = Color.Gray
				)
			}
		}
	}
}

data class NotificationItem(
	val id: String,
	val title: String,
	val message: String,
	val time: String
)

// Sample notifications for UI preview
private val sampleNotifications = listOf(
	NotificationItem(
		id = "1",
		title = "New Follower",
		message = "User123 started following you",
		time = "2 hours ago"
	),
	NotificationItem(
		id = "2",
		title = "New Like",
		message = "User456 liked your video",
		time = "Yesterday"
	),
	NotificationItem(
		id = "3",
		title = "New Comment",
		message = "User789 commented: 'Great video!'",
		time = "2 days ago"
	)
)