package com.example.xyzen.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.xyzen.components.GradientButton
import com.example.xyzen.firebase.FirebaseServiceClass
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen() {
	val context = LocalContext.current
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
	var isUploading by remember { mutableStateOf(false) }
	var uploadProgress by remember { mutableStateOf(0f) }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var successMessage by remember { mutableStateOf<String?>(null) }

	// Video picker launcher
	val videoPickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent()
	) { uri: Uri? ->
		selectedVideoUri = uri
		errorMessage = null
		successMessage = null
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Upload Video") }
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Video selection area
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(300.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(Color.LightGray.copy(alpha = 0.3f))
					.border(
						width = 2.dp,
						color = MaterialTheme.colorScheme.primary,
						shape = RoundedCornerShape(12.dp)
					)
					.clickable {
						if (!isUploading) {
							videoPickerLauncher.launch("video/*")
						}
					},
				contentAlignment = Alignment.Center
			) {
				if (selectedVideoUri != null) {
					// Show video thumbnail
					AsyncImage(
						model = selectedVideoUri,
						contentDescription = "Selected video",
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop
					)

					// Clear button
					IconButton(
						onClick = {
							if (!isUploading) {
								selectedVideoUri = null
							}
						},
						modifier = Modifier
							.align(Alignment.TopEnd)
							.padding(24.dp)
							.size(36.dp)
							.background(
								color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
								shape = RoundedCornerShape(18.dp)
							),
						enabled = !isUploading
					) {
						Icon(
							imageVector = Icons.Default.Close,
							contentDescription = "Clear selection",
							tint = MaterialTheme.colorScheme.error
						)
					}
				} else {
					// Show placeholder
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					) {
						Icon(
							imageVector = Icons.Default.VideoLibrary,
							contentDescription = "Select video",
							modifier = Modifier.size(64.dp),
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.height(16.dp))
						Text(
							text = "Tap to select a video",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.primary
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(24.dp))

			// Error message
			errorMessage?.let {
				Text(
					text = it,
					color = MaterialTheme.colorScheme.error,
					modifier = Modifier.padding(bottom = 16.dp)
				)
			}

			// Success message
			successMessage?.let {
				Text(
					text = it,
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.padding(bottom = 16.dp)
				)
			}

			// Upload button
			GradientButton(
				text = if (isUploading) "Uploading... ${(uploadProgress * 100).toInt()}%" else "Upload Video",
				onClick = {
					val videoUri = selectedVideoUri
					if (videoUri == null) {
						errorMessage = "Please select a video first"
						return@GradientButton
					}

					val currentUser = firebaseService.getCurrentUser()
					if (currentUser == null) {
						errorMessage = "You must be logged in to upload videos"
						return@GradientButton
					}

					isUploading = true
					errorMessage = null
					successMessage = null

					coroutineScope.launch {
						try {
							val videoId = UUID.randomUUID().toString()
							val result = firebaseService.uploadVideo(
								videoUri = videoUri,
								videoId = videoId,
								userId = currentUser.uid,
								caption = "", // Empty caption
								onProgressUpdate = { progress ->
									// Update the progress on the main thread
									MainScope().launch {
										uploadProgress = progress
									}
								}
							)

							result.fold(
								onSuccess = {
									successMessage = "Video uploaded successfully!"
									// Reset form
									selectedVideoUri = null
								},
								onFailure = { error ->
									errorMessage = "Upload failed: ${error.message}"
								}
							)
						} catch (e: Exception) {
							errorMessage = "An error occurred: ${e.message}"
						} finally {
							isUploading = false
							uploadProgress = 0f
						}
					}
				},
				modifier = Modifier
					.fillMaxWidth(),
				enabled = selectedVideoUri != null && !isUploading
			)
		}
	}
}