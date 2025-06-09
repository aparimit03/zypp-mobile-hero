package com.example.xyzen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.Playlist
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(navController: NavController, playlistId: String) {
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var playlist by remember { mutableStateOf<Playlist?>(null) }
	var playlistVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
	var creator by remember { mutableStateOf<User?>(null) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var isCurrentUserOwner by remember { mutableStateOf(false) }

	// Load playlist data
	LaunchedEffect(playlistId) {
		coroutineScope.launch {
			try {
				// Get playlist
				val playlistResult = firebaseService.getPlaylistById(playlistId)
				playlistResult.fold(
					onSuccess = { loadedPlaylist ->
						playlist = loadedPlaylist

						// Check if current user is the owner
						val currentUser = firebaseService.getCurrentUser()
						isCurrentUserOwner =
							currentUser != null && currentUser.uid == loadedPlaylist.userId

						// Get creator info
						val creatorResult = firebaseService.getUserData(loadedPlaylist.userId)
						creatorResult.fold(
							onSuccess = { user ->
								creator = user
							},
							onFailure = { /* Handle silently */ }
						)

						// Get videos in playlist
						val videosResult = firebaseService.getPlaylistVideos(playlistId)
						videosResult.fold(
							onSuccess = { videos ->
								playlistVideos = videos
								isLoading = false
							},
							onFailure = { error ->
								errorMessage = "Failed to load videos: ${error.message}"
								isLoading = false
							}
						)
					},
					onFailure = { error ->
						errorMessage = "Failed to load playlist: ${error.message}"
						isLoading = false
					}
				)
			} catch (e: Exception) {
				errorMessage = "An error occurred: ${e.message}"
				isLoading = false
			}
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(playlist?.name ?: "Playlist") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					// Only show edit/delete options if current user is the owner
					if (isCurrentUserOwner) {
						IconButton(onClick = {
							navController.navigate("edit_playlist/$playlistId")
						}) {
							Icon(
								imageVector = Icons.Default.Edit,
								contentDescription = "Edit Playlist"
							)
						}

						IconButton(onClick = {
							// Show delete confirmation dialog
							// ...
						}) {
							Icon(
								imageVector = Icons.Default.Delete,
								contentDescription = "Delete Playlist"
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

				playlist == null -> {
					Text(
						text = "Playlist not found",
						modifier = Modifier.align(Alignment.Center)
					)
				}

				else -> {
					LazyColumn(
						modifier = Modifier.fillMaxSize()
					) {
						// Playlist header
						item {
							PlaylistHeader(
								playlist = playlist!!,
								creator = creator,
								videoCount = playlistVideos.size
							)
						}

						// Videos
						if (playlistVideos.isEmpty()) {
							item {
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.height(200.dp),
									contentAlignment = Alignment.Center
								) {
									Text("This playlist has no videos")
								}
							}
						} else {
							items(playlistVideos.size) { index ->
								val video = playlistVideos[index]
								PlaylistVideoItem(
									video = video,
									index = index,
									onClick = {
										navController.navigate("video_detail/${video.id}")
									},
									onRemove = if (isCurrentUserOwner) {
										{
											coroutineScope.launch {
												firebaseService.removeVideosFromPlaylist(
													playlistId = playlistId,
													videoIds = listOf(video.id)
												).fold(
													onSuccess = {
														// Update the list
														playlistVideos =
															playlistVideos.filter { it.id != video.id }
													},
													onFailure = { error ->
														// Show error
													}
												)
											}
										}
									} else null
								)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
fun PlaylistHeader(
	playlist: Playlist,
	creator: User?,
	videoCount: Int
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
	) {
		// Playlist cover image
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(180.dp)
				.clip(RoundedCornerShape(8.dp))
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
						.size(80.dp)
						.align(Alignment.Center),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Playlist name
		Text(
			text = playlist.name,
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Bold
		)

		// Playlist description
		if (playlist.description.isNotEmpty()) {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = playlist.description,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		// Creator info
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Created by ",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Text(
				text = creator?.username ?: "Unknown",
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			// Visibility icon
			Icon(
				imageVector = if (playlist.isPublic) Icons.Default.Public else Icons.Default.Lock,
				contentDescription = if (playlist.isPublic) "Public" else "Private",
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(16.dp)
			)

			Spacer(modifier = Modifier.width(4.dp))

			Text(
				text = if (playlist.isPublic) "Public" else "Private",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		Spacer(modifier = Modifier.height(4.dp))

		// Video count
		Text(
			text = "$videoCount ${if (videoCount == 1) "video" else "videos"}",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Divider(modifier = Modifier.padding(vertical = 16.dp))
	}
}

@Composable
fun PlaylistVideoItem(
	video: Video,
	index : Int,
	onClick: () -> Unit,
	onRemove: (() -> Unit)? = null
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 4.dp)
			.clickable(onClick = onClick),
		elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Video thumbnail
			AsyncImage(
				model = video.thumbnailUrl ?: "https://via.placeholder.com/120x80",
				contentDescription = "Video thumbnail",
				modifier = Modifier
					.width(120.dp)
					.height(80.dp)
					.clip(RoundedCornerShape(4.dp)),
				contentScale = ContentScale.Crop
			)

			Spacer(modifier = Modifier.width(12.dp))

			// Video info
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = "Video ${index + 1}",
					style = MaterialTheme.typography.titleMedium,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
			}

			// Remove button (only if onRemove is provided)
			if (onRemove != null) {
				IconButton(onClick = onRemove) {
					Icon(
						imageVector = Icons.Default.RemoveCircle,
						contentDescription = "Remove from playlist",
						tint = MaterialTheme.colorScheme.error
					)
				}
			}
		}
	}
}