package com.example.xyzen.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import com.example.xyzen.MainActivity
import com.example.xyzen.R
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.Playlist
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String? = null) {
	val context = LocalContext.current
	val activity = context as? MainActivity
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var user by remember { mutableStateOf<User?>(null) }
	var userVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var isCurrentUser by remember { mutableStateOf(false) }
	var userPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
	var selectedTab by remember { mutableStateOf(0) } // 0 for Videos, 1 for Playlists

	// Load user data when the screen is first displayed
	LaunchedEffect(userId) {
		try {
			val currentUser = firebaseService.getCurrentUser()
		} catch (e: Exception) {
			Log.e("ProfileScreen", "Error getting current user", e)
			// Handle error appropriately
		}
		val currentUser = firebaseService.getCurrentUser()
		// Determine which user ID to use
		val targetUserId = userId ?: currentUser?.uid

		if (targetUserId != null) {
			// Check if viewing own profile
			isCurrentUser = currentUser != null && targetUserId == currentUser.uid

			coroutineScope.launch {
				try {
					// Fetch user data from Firestore
					val userResult = firebaseService.getUserData(targetUserId)
					userResult.fold(
						onSuccess = { userData ->
							user = userData

							// Fetch user videos
							val videosResult = firebaseService.getUserVideos(targetUserId)
							videosResult.fold(
								onSuccess = { videos ->
									userVideos = videos
								},
								onFailure = { error ->
									errorMessage = "Failed to load videos: ${error.message}"
								}
							)

							// Fetch user playlists
							val playlistsResult = firebaseService.getUserPlaylists(targetUserId)
							playlistsResult.fold(
								onSuccess = { playlists ->
									userPlaylists = playlists
									isLoading = false
								},
								onFailure = { error ->
									errorMessage = "Failed to load playlists: ${error.message}"
									isLoading = false
								}
							)
						},
						onFailure = { error ->
							errorMessage = "Failed to load user data: ${error.message}"
							isLoading = false
						}
					)
				} catch (e: Exception) {
					errorMessage = "An error occurred: ${e.message}"
					isLoading = false
				}
			}
		} else {
			// User not logged in and no userId provided
			errorMessage = "User not found"
			isLoading = false
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(if (isCurrentUser) "My Profile" else "${user?.username}'s Profile") },
				actions = {
					// Only show sign out button if viewing own profile
					if (isCurrentUser) {
						IconButton(onClick = {
							firebaseService.signOut()
							activity?.signOutFromApp()
						}) {
							Icon(
								imageVector = Icons.Default.Logout,
								contentDescription = "Sign Out"
							)
						}
					}
				},
				navigationIcon = {
					// Add back button if viewing someone else's profile
					if (!isCurrentUser) {
						IconButton(onClick = { navController.popBackStack() }) {
							Icon(
								imageVector = Icons.Default.ArrowBack,
								contentDescription = "Back"
							)
						}
					}
				}
			)
		}
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			when {
				isLoading -> {
					CircularProgressIndicator(
						modifier = Modifier.align(Alignment.Center)
					)
				}

				errorMessage != null -> {
					Text(
						text = errorMessage ?: "Unknown error",
						color = MaterialTheme.colorScheme.error,
						modifier = Modifier
							.align(Alignment.Center)
							.padding(16.dp)
					)
				}

				user == null -> {
					Text(
						text = "User not found",
						modifier = Modifier.align(Alignment.Center)
					)
				}

				else -> {
					// User profile content
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						// Profile header with user info
						ProfileHeader(
							user = user!!,
							showEditButton = isCurrentUser
						)

						Spacer(modifier = Modifier.height(24.dp))

						// Bio section
						if (user?.bio?.isNotEmpty() == true) {
							Text(
								text = user?.bio ?: "",
								style = MaterialTheme.typography.bodyMedium,
								textAlign = TextAlign.Center,
								modifier = Modifier.padding(horizontal = 32.dp)
							)
							Spacer(modifier = Modifier.height(24.dp))
						}

						// Tab row for Videos and Playlists
						TabRow(
							selectedTabIndex = selectedTab,
							modifier = Modifier.fillMaxWidth()
						) {
							Tab(
								selected = selectedTab == 0,
								onClick = { selectedTab = 0 },
								text = { Text("Videos") }
							)
							Tab(
								selected = selectedTab == 1,
								onClick = { selectedTab = 1 },
								text = { Text("Playlists") }
							)
						}

						Spacer(modifier = Modifier.height(16.dp))

						// Content based on selected tab
						when (selectedTab) {
							0 -> {
								// Videos tab
								if (userVideos.isEmpty()) {
									Box(
										modifier = Modifier
											.fillMaxWidth()
											.height(200.dp),
										contentAlignment = Alignment.Center
									) {
										Text("No videos uploaded yet")
									}
								} else {
									// Grid of videos
									LazyVerticalGrid(
										columns = GridCells.Fixed(3),
										horizontalArrangement = Arrangement.spacedBy(4.dp),
										verticalArrangement = Arrangement.spacedBy(4.dp),
										modifier = Modifier.fillMaxWidth()
									) {
										items(userVideos) { video ->
											VideoThumbnail(video = video) {
												// Handle video click - navigate to video detail
												navController.navigate("video_detail/${video.id}")
											}
										}
									}
								}
							}

							1 -> {
								if (isCurrentUser) {
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.padding(bottom = 8.dp),
										horizontalArrangement = Arrangement.End
									) {
										Button(
											onClick = { navController.navigate("create_playlist") },
											colors = ButtonDefaults.buttonColors(
												containerColor = MaterialTheme.colorScheme.primary
											)
										) {
											Icon(
												imageVector = Icons.Default.Add,
												contentDescription = null,
												modifier = Modifier.size(20.dp)
											)
											Spacer(modifier = Modifier.width(8.dp))
											Text("Create Playlist")
										}
									}
								}

								if (userPlaylists.isEmpty()) {
									Box(
										modifier = Modifier
											.fillMaxWidth()
											.height(200.dp),
										contentAlignment = Alignment.Center
									) {
										Text(
											text = if (isCurrentUser)
												"You haven't created any playlists yet"
											else
												"${user?.username} hasn't created any playlists yet"
										)
									}
								} else {
									// List of playlists
									LazyColumn(
										modifier = Modifier.fillMaxWidth(),
										verticalArrangement = Arrangement.spacedBy(8.dp)
									) {
										items(userPlaylists) { playlist ->
											PlaylistItem(
												playlist = playlist,
												onClick = {
													navController.navigate("playlist/${playlist.id}")
												}
											)
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
fun ProfileHeader(user: User, showEditButton: Boolean = true) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth()
	) {
		// Profile image
		Box(
			modifier = Modifier
				.size(100.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
		) {
			if (user.profileImage != null) {
				Image(
					painter = rememberAsyncImagePainter(user.profileImage),
					contentDescription = "Profile Picture",
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			} else {
				// Default profile image
				Icon(
					painter = painterResource(id = R.drawable.ic_person),
					contentDescription = "Default Profile",
					modifier = Modifier
						.size(60.dp)
						.align(Alignment.Center),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			// Modify the edit button to only show if showEditButton is true
			if (showEditButton) {
				// Add edit button
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Username
		Text(
			text = "@${user.username}",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold
		)

		Spacer(modifier = Modifier.height(4.dp))

		// Email
		Text(
			text = user.email,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(16.dp))

		// Stats row
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			StatItem(count = user.videos.size, label = "Videos")
			// You can add more stats here like followers, following, etc.
		}
	}
}

@Composable
fun StatItem(count: Int, label: String) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = count.toString(),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold
		)
		Text(
			text = label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
fun VideoThumbnail(video: Video, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.aspectRatio(9f / 16f)
			.clip(RoundedCornerShape(4.dp))
			.background(MaterialTheme.colorScheme.surfaceVariant)
			.clickable(onClick = onClick)
	) {
		// Try to load the video thumbnail or fallback to the video itself
		val imageUrl = if (video.thumbnailUrl.isNotEmpty()) {
			video.thumbnailUrl
		} else if (video.videoUrl.isNotEmpty()) {
			video.videoUrl
		} else {
			""
		}

		if (imageUrl.isNotEmpty()) {
			AsyncImage(
				model = imageUrl,
				contentDescription = "Video thumbnail",
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				onError = {
					// If loading fails, we'll show the placeholder below
				}
			)
		}

		// Always show a semi-transparent overlay with a play icon
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black.copy(alpha = 0.3f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Default.PlayArrow,
				contentDescription = "Play video",
				tint = Color.White,
				modifier = Modifier.size(48.dp)
			)
		}
	}
}

@Composable
fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		elevation = 2.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Playlist cover image
			Box(
				modifier = Modifier
					.size(80.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(MaterialTheme.colorScheme.surfaceVariant)
			) {
				if (playlist.coverImageUrl != null) {
					AsyncImage(
						model = playlist.coverImageUrl,
						contentDescription = "Playlist cover",
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop
					)
				} else {
					// Default playlist icon
					Icon(
						imageVector = Icons.Default.PlaylistPlay,
						contentDescription = "Playlist",
						modifier = Modifier
							.size(40.dp)
							.align(Alignment.Center),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}

				// Video count badge
				Box(
					modifier = Modifier
						.align(Alignment.BottomEnd)
						.background(
							color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
							shape = RoundedCornerShape(4.dp)
						)
						.padding(horizontal = 4.dp, vertical = 2.dp)
				) {
					Text(
						text = "${playlist.videos.size}",
						style = MaterialTheme.typography.labelSmall,
						color = MaterialTheme.colorScheme.onPrimary
					)
				}
			}

			Spacer(modifier = Modifier.width(12.dp))

			// Playlist info
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = playlist.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				if (playlist.description.isNotEmpty()) {
					Text(
						text = playlist.description,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}
			}

			// Visibility icon
			Icon(
				imageVector = if (playlist.isPublic) Icons.Default.Public else Icons.Default.Lock,
				contentDescription = if (playlist.isPublic) "Public" else "Private",
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(8.dp)
			)
		}
	}
}