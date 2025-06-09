package com.example.xyzen.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.xyzen.components.AudioWaveform
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeedScreen(navController: NavController) {
	val context = LocalContext.current
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	// Fetch videos when the screen is first displayed
	LaunchedEffect(Unit) {
		coroutineScope.launch {
			try {
//                val result = firebaseService.getVideosForFeed(20) // Fetch up to 20 videos
//                result.fold(
//                    onSuccess = { fetchedVideos ->
//                        videos = fetchedVideos
//                        isLoading = false
//                    },
//                    onFailure = { error ->
//                        errorMessage = "Failed to load videos: ${error.message}"
//                        isLoading = false
//                    }
//                )
				val result = firebaseService.getRandomizedVideos()
				result.fold(
					onSuccess = { videoList ->
						videos = videoList
						isLoading = false
					},
					onFailure = { error ->
						errorMessage = "Failed to load videos: ${error.message}"
						isLoading = false
					}
				)
			} catch (e: Exception) {
				errorMessage = "An error occurred: ${e.message}"
				isLoading = false
			}
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black)
	) {
		when {
			isLoading -> {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center)
				)
			}

			errorMessage != null -> {
				Column(
					modifier = Modifier
						.align(Alignment.Center)
						.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = "Failed to load videos",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.error
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = errorMessage ?: "Unknown error",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			videos.isEmpty() -> {
				Column(
					modifier = Modifier
						.align(Alignment.Center)
						.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = "No videos available",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "Videos you upload will appear here",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			else -> {
				VideoFeed(videos = videos, navController)
			}
		}
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun VideoFeed(videos: List<Video>, navController: NavController) {
	val pagerState = rememberPagerState()

	// Track the currently playing video
	var currentlyPlayingPage by remember { mutableStateOf(0) }

	// Update the currently playing page when the pager changes
	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.currentPage }.collect { page ->
			currentlyPlayingPage = page
		}
	}

	VerticalPager(
		count = videos.size,
		state = pagerState,
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black),
	) { page ->
		val video = videos[page]
		val isCurrentlyPlaying = page == currentlyPlayingPage

		var creator by remember { mutableStateOf<User?>(null) }
		val firebaseService = remember { FirebaseServiceClass() }
		val coroutineScope = rememberCoroutineScope()

		LaunchedEffect(video.userId) {
			coroutineScope.launch {
				firebaseService.getUserData(video.userId).fold(
					onSuccess = { user ->
						creator = user
					},
					onFailure = { /* Handle error silently */ }
				)
			}
		}

		Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
			// Video player
			VideoPlayer(
				videoUrl = video.videoUrl,
				isPlaying = isCurrentlyPlaying
			)

			// Overlay with video info and actions
			Column(
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(16.dp)
					.fillMaxWidth()
			) {
				// User info
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.padding(bottom = 8.dp)
						.clickable {
							// Navigate to creator's profile
							navController.navigate("profile/${video.userId}")
						}
				) {
					// Profile picture - use creator's profile image if available
					AsyncImage(
						model = creator?.profileImage ?: "https://via.placeholder.com/40",
						contentDescription = "User profile",
						modifier = Modifier
							.size(40.dp)
							.clip(CircleShape)
							.background(MaterialTheme.colorScheme.surfaceVariant),
						contentScale = ContentScale.Crop
					)

					Spacer(modifier = Modifier.width(8.dp))

					// Username
					Text(
						text = creator?.username ?: "...",
						style = MaterialTheme.typography.titleMedium,
						color = Color.White,
						fontWeight = FontWeight.Bold
					)

					// Audio waveform overlay
					AudioWaveform(
						isPlaying = isCurrentlyPlaying,
						color = Color.White.copy(alpha = 0.4f),
						modifier = Modifier
							.width(100.dp)
							.height(10.dp)
					)
				}

				// Caption (if any)
//				if (video.caption.isNotEmpty()) {
//					Text(
//						text = video.caption,
//						style = MaterialTheme.typography.bodyMedium,
//						color = Color.White,
//						maxLines = 2,
//						overflow = TextOverflow.Ellipsis,
//						modifier = Modifier
//							.padding(bottom = 16.dp)
//							.background(Color.Black)
//					)
//				}
			}

			// Action buttons (like, share, etc.)
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.align(Alignment.BottomEnd)
					.padding(16.dp)
			) {
				// Like button
				LikeButton(videoId = video.id, initialLikeCount = video.likes)

                Spacer(modifier = Modifier.height(16.dp))

				var showAddToPlaylistDialog by remember { mutableStateOf(false) }

				IconButton(
					onClick = { showAddToPlaylistDialog = true },
					modifier = Modifier
						.size(48.dp)
						.background(
							color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
							shape = CircleShape
						)
				) {
					Icon(
						imageVector = Icons.Default.PlaylistAdd,
						contentDescription = "Add to playlist",
						tint = Color.White
					)
				}

				// Add the dialog
				if (showAddToPlaylistDialog) {
					AddToPlaylistDialog(
						videoId = video.id,
						onDismiss = { showAddToPlaylistDialog = false },
						navController = navController
					)
				}
			}
		}
	}
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayer(videoUrl: String, isPlaying: Boolean) {
	val context = LocalContext.current

	// Create and remember the ExoPlayer
	val exoPlayer = remember {
		ExoPlayer.Builder(context).build().apply {
			repeatMode = Player.REPEAT_MODE_ONE
			playWhenReady = false
			prepare()
		}
	}

	// Set up the media item when the video URL changes
	LaunchedEffect(videoUrl) {
		exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
		exoPlayer.prepare()
	}

	// Control playback based on whether this video is currently visible
	LaunchedEffect(isPlaying) {
		if (isPlaying) {
			exoPlayer.playWhenReady = true
		} else {
			exoPlayer.playWhenReady = false
		}
	}

	// Clean up the ExoPlayer when the composable is disposed
	DisposableEffect(Unit) {
		onDispose {
			exoPlayer.release()
		}
	}

	// Render the PlayerView
	AndroidView(
		factory = { ctx ->
			PlayerView(ctx).apply {
				player = exoPlayer
				useController = false // Hide the default controls
				resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT // Fit in the screen
			}
		},
		modifier = Modifier.fillMaxSize()
	)
}

@Composable
fun LikeButton(videoId: String, initialLikeCount: Int) {
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var isLiked by remember { mutableStateOf(false) }
	var likeCount by remember { mutableStateOf(initialLikeCount) }

	// Check if video is liked when component is first displayed
	LaunchedEffect(videoId) {
		coroutineScope.launch {
			firebaseService.checkIfUserLikedVideo(videoId).fold(
				onSuccess = { liked ->
					isLiked = liked
				},
				onFailure = { /* Handle error */ }
			)
		}
	}

	// Like button
	IconButton(
		onClick = {
			coroutineScope.launch {
				firebaseService.likeVideo(videoId).fold(
					onSuccess = { liked ->
						isLiked = !isLiked
						likeCount = if (isLiked) likeCount + 1 else likeCount - 1
					},
					onFailure = { /* Handle error */ }
				)
			}
		}
	) {
		Icon(
			imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
			contentDescription = "Like",
			tint = if (isLiked) Color.Red else Color.White,
			modifier = Modifier.size(28.dp)
		)
	}

	Text(
		text = "$likeCount",
		style = MaterialTheme.typography.bodySmall,
		color = Color.White
	)
}