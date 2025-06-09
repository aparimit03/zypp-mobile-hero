package com.example.xyzen.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.xyzen.firebase.FirebaseServiceClass
import com.example.xyzen.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlaylistScreen(navController: NavController, playlistId: String) {
	val firebaseService = remember { FirebaseServiceClass() }
	val coroutineScope = rememberCoroutineScope()

	// State variables
	var playlist by remember { mutableStateOf<Playlist?>(null) }
	var playlistName by remember { mutableStateOf("") }
	var playlistDescription by remember { mutableStateOf("") }
	var isPublic by remember { mutableStateOf(true) }
	var isLoading by remember { mutableStateOf(true) }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	// Load playlist data
	LaunchedEffect(playlistId) {
		coroutineScope.launch {
			try {
				val playlistResult = firebaseService.getPlaylistById(playlistId)
				playlistResult.fold(
					onSuccess = { loadedPlaylist ->
						playlist = loadedPlaylist
						playlistName = loadedPlaylist.name
						playlistDescription = loadedPlaylist.description
						isPublic = loadedPlaylist.isPublic
						isLoading = false
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
				title = { Text("Edit Playlist") },
				navigationIcon = {
					IconButton(onClick = { navController.popBackStack() }) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					// Save button
					IconButton(
						onClick = {
							if (playlistName.isBlank()) {
								errorMessage = "Please enter a playlist name"
								return@IconButton
							}

							isLoading = true
							coroutineScope.launch {
								try {
									// Update playlist in Firestore
									Firebase.firestore.collection("playlists").document(playlistId)
										.update(
											mapOf(
												"name" to playlistName,
												"description" to playlistDescription,
												"isPublic" to isPublic
											)
										)
										.await()

									// Navigate back
									navController.popBackStack()
								} catch (e: Exception) {
									errorMessage = "Failed to update playlist: ${e.message}"
									isLoading = false
								}
							}
						},
						enabled = !isLoading && playlistName.isNotBlank()
					) {
						Icon(
							imageVector = Icons.Default.Check,
							contentDescription = "Save"
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

				errorMessage != null && playlist == null -> {
					Text(
						text = errorMessage ?: "Unknown error",
						color = MaterialTheme.colorScheme.error,
						modifier = Modifier
							.align(Alignment.Center)
							.padding(16.dp)
					)
				}

				playlist != null -> {
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(16.dp)
							.verticalScroll(rememberScrollState())
					) {
						// Playlist name
						OutlinedTextField(
							value = playlistName,
							onValueChange = { playlistName = it },
							label = { Text("Playlist Name") },
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
							isError = errorMessage != null && playlistName.isBlank()
						)

						Spacer(modifier = Modifier.height(16.dp))

						// Playlist description
						OutlinedTextField(
							value = playlistDescription,
							onValueChange = { playlistDescription = it },
							label = { Text("Description (Optional)") },
							modifier = Modifier.fillMaxWidth(),
							minLines = 3,
							maxLines = 5
						)

						Spacer(modifier = Modifier.height(16.dp))

						// Visibility toggle
						Row(
							modifier = Modifier.fillMaxWidth(),
							verticalAlignment = Alignment.CenterVertically
						) {
							Text(
								text = "Visibility",
								style = MaterialTheme.typography.titleMedium
							)

							Spacer(modifier = Modifier.weight(1f))

							Switch(
								checked = isPublic,
								onCheckedChange = { isPublic = it }
							)

							Spacer(modifier = Modifier.width(8.dp))

							Icon(
								imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
								contentDescription = if (isPublic) "Public" else "Private"
							)

							Spacer(modifier = Modifier.width(4.dp))

							Text(
								text = if (isPublic) "Public" else "Private",
								style = MaterialTheme.typography.bodyMedium
							)
						}

						// Error message
						if (errorMessage != null) {
							Spacer(modifier = Modifier.height(16.dp))
							Text(
								text = errorMessage!!,
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.bodyMedium
							)
						}
					}
				}
			}
		}
	}
}