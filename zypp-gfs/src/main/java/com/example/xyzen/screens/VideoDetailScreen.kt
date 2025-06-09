package com.example.xyzen.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.Playlist
import com.example.xyzen.model.Video
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(navController: NavController, videoId: String) {
	val context = LocalContext.current
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var video by remember { mutableStateOf<Video?>(null) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var isPlaying by remember { mutableStateOf(true) }
	var showAddToPlaylistDialog by remember { mutableStateOf(false) }

	// Load video data
	LaunchedEffect(videoId) {
		coroutineScope.launch {
			try {
				val result = firebaseService.getVideoById(videoId)
				result.fold(
					onSuccess = { videoData ->
						video = videoData
						isLoading = false
					},
					onFailure = { error ->
						errorMessage = "Failed to load video: ${error.message}"
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
				title = { Text("Video") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					IconButton(onClick = { showAddToPlaylistDialog = true }) {
						Icon(
							imageVector = Icons.Default.PlaylistAdd,
							contentDescription = "Add to playlist"
						)
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
				video == null -> {
					Text(
						text = "Video not found",
						modifier = Modifier.align(Alignment.Center)
					)
				}
				else -> {
					Column(
						modifier = Modifier.fillMaxSize()
					) {
						// Video player (takes 2/3 of the screen)
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.weight(2f)
								.background(Color.Black)
						) {
							// Video player
							video?.videoUrl?.let { url ->
								FullVideoPlayer(
									videoUrl = url,
									isPlaying = isPlaying,
									onPlayPauseToggle = { isPlaying = !isPlaying }
								)
							}
						}

						// Video details (takes 1/3 of the screen)
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.weight(1f)
								.padding(16.dp)
						) {
							// Caption
							if (video?.caption?.isNotEmpty() == true) {
								Text(
									text = video?.caption ?: "",
									style = MaterialTheme.typography.titleMedium,
									fontWeight = FontWeight.Bold,
									modifier = Modifier.padding(bottom = 8.dp)
								)
							}

							// Upload date
							video?.timestamp?.let { timestamp ->
								val date = timestamp.toDate()
								val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
								Text(
									text = "Uploaded on ${dateFormat.format(date)}",
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.padding(bottom = 16.dp)
								)
							}

							// Stats
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(bottom = 16.dp),
								horizontalArrangement = Arrangement.SpaceEvenly
							) {
								// Views
								Column(horizontalAlignment = Alignment.CenterHorizontally) {
									Text(
										text = "${video?.views ?: 0}",
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.Bold
									)
									Text(
										text = "Views",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								}

								// Likes
								Column(horizontalAlignment = Alignment.CenterHorizontally) {
									Text(
										text = "${video?.likes ?: 0}",
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.Bold
									)
									Text(
										text = "Likes",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								}

								// Comments
								Column(horizontalAlignment = Alignment.CenterHorizontally) {
									Text(
										text = "${video?.comments?.size ?: 0}",
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.Bold
									)
									Text(
										text = "Comments",
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								}
							}
						}
					}
				}
			}
			if (showAddToPlaylistDialog) {
				AddToPlaylistDialog(
					videoId = videoId,
					onDismiss = { showAddToPlaylistDialog = false },
					navController = navController  // Pass the navController to the dialog
				)
			}
		}
	}
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun FullVideoPlayer(videoUrl: String, isPlaying: Boolean, onPlayPauseToggle: () -> Unit) {
	val context = LocalContext.current

	// Create and remember the ExoPlayer
	val exoPlayer = remember {
		ExoPlayer.Builder(context).build().apply {
			repeatMode = Player.REPEAT_MODE_ONE
			playWhenReady = isPlaying
			prepare()
		}
	}

	// Set up the media item when the video URL changes
	LaunchedEffect(videoUrl) {
		exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
		exoPlayer.prepare()
	}

	// Control playback based on isPlaying state
	LaunchedEffect(isPlaying) {
		exoPlayer.playWhenReady = isPlaying
	}

	// Clean up the ExoPlayer when the composable is disposed
	DisposableEffect(Unit) {
		onDispose {
			exoPlayer.release()
		}
	}

	// Render the PlayerView
	Box(modifier = Modifier.fillMaxSize()) {
		AndroidView(
			factory = { ctx ->
				PlayerView(ctx).apply {
					player = exoPlayer
					useController = true // Show the default controls
					resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Fit the video
					setControllerOnFullScreenModeChangedListener { isFullScreen ->
						// Handle fullscreen mode changes if needed
					}
				}
			},
			modifier = Modifier.fillMaxSize(),
			update = { playerView ->
				// Update player view if needed
			}
		)
	}
}

@Composable
fun AddToPlaylistDialog(
	videoId: String,
	onDismiss: () -> Unit,
	navController: NavController  // Add navController parameter
) {
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	var userPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	// Load user playlists
	LaunchedEffect(Unit) {
		coroutineScope.launch {
			try {
				val currentUser = firebaseService.getCurrentUser()
				if (currentUser != null) {
					val playlistsResult = firebaseService.getUserPlaylists(currentUser.uid)
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
				} else {
					errorMessage = "You need to be logged in to add videos to playlists"
					isLoading = false
				}
			} catch (e: Exception) {
				errorMessage = "An error occurred: ${e.message}"
				isLoading = false
			}
		}
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Add to Playlist") },
		text = {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(300.dp)
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
							modifier = Modifier.align(Alignment.Center)
						)
					}

					userPlaylists.isEmpty() -> {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier.align(Alignment.Center)
						) {
							Text("You don't have any playlists yet")

							Spacer(modifier = Modifier.height(16.dp))

							Button(
								onClick = {
									onDismiss()
									navController.navigate("create_playlist")
								}
							) {
								Text("Create Playlist")
							}
						}
					}

					else -> {
						LazyColumn {
							items(userPlaylists) { playlist ->
								Row(
									verticalAlignment = Alignment.CenterVertically,
									modifier = Modifier
										.fillMaxWidth()
										.clickable {
											coroutineScope.launch {
												firebaseService.addVideosToPlaylist(
													playlistId = playlist.id,
													videoIds = listOf(videoId)
												)
												onDismiss()
											}
										}
										.padding(vertical = 8.dp)
								) {
									Icon(
										imageVector = Icons.Default.PlaylistAddCheck,
										contentDescription = null
									)

									Spacer(modifier = Modifier.width(16.dp))

									Text(
										text = playlist.name,
										style = MaterialTheme.typography.bodyLarge
									)
								}
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text("Cancel")
			}
		}
	)
}