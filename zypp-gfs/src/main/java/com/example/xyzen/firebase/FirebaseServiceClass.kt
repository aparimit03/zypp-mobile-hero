package com.example.xyzen.firebase

import android.net.Uri
import android.util.Log
import com.example.xyzen.model.Playlist
import com.example.xyzen.model.User
import com.example.xyzen.model.Video
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseServiceClass() {
	private val firebaseAuth = Firebase.auth
	private val firestore = Firebase.firestore
	private val firebaseStorage = Firebase.storage

	companion object {
		private const val TAG = "FirebaseAuthService"
	}

	fun isUserLoggedIn(): Boolean {
		return firebaseAuth.currentUser != null
	}

	fun getCurrentUser(): FirebaseUser? {
		return firebaseAuth.currentUser
	}

	suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
		return try {
			val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
			Result.success(result.user!!)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun register(email: String, password: String, username: String): Result<FirebaseUser> {
		return try {
			val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
			val user = result.user!!

			val userModel = User(
				id = user.uid,
				username = username,
				email = email
			)

			firestore.collection("users").document(user.uid)
				.set(userModel)
				.await()

			Result.success(user)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun signOut() {
		firebaseAuth.signOut()
	}

	suspend fun uploadVideo(
		videoUri: Uri,
		videoId: String,
		userId: String,
		caption: String,
		onProgressUpdate: (Float) -> Unit = {}
	): Result<Video> {
		return try {
			// Uploading video to Firebase Storage
			val storageRef = firebaseStorage.reference
				.child("videos/$userId/$videoId.mp4")

			// Create and monitor the upload task
			val uploadTask = storageRef.putFile(videoUri)
				.addOnProgressListener { taskSnapshot ->
					val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
					onProgressUpdate(progress)
				}
				.await()
			val videoUrl = storageRef.downloadUrl.await().toString()

			// Generate a thumbnail URL (using the video URL for now)
			// In a production app, you would generate an actual thumbnail
			val thumbnailUrl = videoUrl

			// Link Video URL in Firestore
			val video = Video(
				id = videoId,
				userId = userId,
				caption = caption,
				videoUrl = videoUrl,
				thumbnailUrl = thumbnailUrl,
				timestamp = Timestamp.now()
			)

			// Save video to videos collection
			firestore.collection("videos")
				.document(videoId)
				.set(video)
				.await()

			// Update user's videos list
			firestore.collection("users")
				.document(userId)
				.update("videos", com.google.firebase.firestore.FieldValue.arrayUnion(videoId))
				.await()

			Result.success(video)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getVideosForFeed(limit: Long = 10): Result<List<Video>> {
		return try {
			val videosSnapshot = firestore.collection("videos")
				.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
				.limit(limit)
				.get()
				.await()

			val videos = videosSnapshot.documents.mapNotNull { doc ->
				doc.toObject(Video::class.java)
			}

			Result.success(videos)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getUserData(userId: String): Result<User> {
		return try {
			val userDoc = firestore.collection("users").document(userId).get().await()
			val user = userDoc.toObject(User::class.java)

			if (user != null) {
				Result.success(user)
			} else {
				Result.failure(Exception("User not found"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getUserVideos(userId: String): Result<List<Video>> {
		return try {
			val videosSnapshot = firestore.collection("videos")
				.whereEqualTo("userId", userId)
				.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
				.get()
				.await()

			val videos = videosSnapshot.documents.mapNotNull { doc ->
				doc.toObject(Video::class.java)
			}

			// Log the number of videos found for debugging
			println("Found ${videos.size} videos for user $userId")

			Result.success(videos)
		} catch (e: Exception) {
			println("Error fetching user videos: ${e.message}")
			Result.failure(e)
		}
	}

	suspend fun getVideoById(videoId: String): Result<Video> {
		return try {
			val videoDoc = firestore.collection("videos").document(videoId).get().await()
			val video = videoDoc.toObject(Video::class.java)

			if (video != null) {
				Result.success(video)
			} else {
				Result.failure(Exception("Video not found"))
			}
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun likeVideo(videoId: String): Result<Boolean> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Check if user already liked the video
			val likeDoc = firestore.collection("likes")
				.whereEqualTo("videoId", videoId)
				.whereEqualTo("userId", currentUser.uid)
				.get()
				.await()

			val videoRef = firestore.collection("videos").document(videoId)
			val isLiked = likeDoc.isEmpty

			if (isLiked) {
				// User hasn't liked the video yet, add like
				val likeData = hashMapOf(
					"videoId" to videoId,
					"userId" to currentUser.uid,
					"timestamp" to Timestamp.now()
				)

				// Add to likes collection
				firestore.collection("likes").add(likeData).await()

				// Update video like count
				videoRef.update("likes", FieldValue.increment(1)).await()
			} else {
				// User already liked the video, remove like
				val likeDocId = likeDoc.documents[0].id

				// Remove from likes collection
				firestore.collection("likes").document(likeDocId).delete().await()

				// Update video like count
				videoRef.update("likes", FieldValue.increment(-1)).await()
			}

			Result.success(isLiked)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun checkIfUserLikedVideo(videoId: String): Result<Boolean> {
		return try {
			val currentUser = getCurrentUser() ?: return Result.success(false)

			val likeDoc = firestore.collection("likes")
				.whereEqualTo("videoId", videoId)
				.whereEqualTo("userId", currentUser.uid)
				.get()
				.await()

			Result.success(!likeDoc.isEmpty)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun createPlaylist(
		name: String,
		description: String,
		coverImageUrl: String? = null,
		videos: List<String> = emptyList(),
		isPublic: Boolean = true
	): Result<Playlist> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Generate a new playlist ID
			val playlistId = firestore.collection("playlists").document().id

			val playlist = Playlist(
				id = playlistId,
				userId = currentUser.uid,
				name = name,
				description = description,
				coverImageUrl = coverImageUrl,
				videos = videos,
				isPublic = isPublic,
				timestamp = Timestamp.now()
			)

			// Save playlist to Firestore
			firestore.collection("playlists").document(playlistId)
				.set(playlist)
				.await()

			Result.success(playlist)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	// Get playlists for a user
	suspend fun getUserPlaylists(userId: String): Result<List<Playlist>> {
		Log.d(TAG, "Getting playlists for user: $userId")
		return try {
			val query = firestore.collection("playlists")
				.whereEqualTo("userId", userId)
			Log.e(TAG, "Got playlists1")

			// If not the current user, only show public playlists
			val currentUser = getCurrentUser()
			if (currentUser == null || currentUser.uid != userId) {
				query.whereEqualTo("isPublic", true)
			}
			Log.e(TAG, "Got playlists2")

			// IMPORTANT FIX: The query needs to be executed with get()
			val playlistsSnapshot = query
				.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
				.get()
				.await()
			Log.e(TAG, "Got playlists3")

			Log.d(TAG, "Playlists snapshot documents count: ${playlistsSnapshot.documents.size}")

			val playlists = playlistsSnapshot.documents.mapNotNull { doc ->
				try {
					val playlist = doc.toObject(Playlist::class.java)
					Log.d(TAG, "Converted playlist: ${playlist?.name}")
					playlist
				} catch (e: Exception) {
					Log.e(TAG, "Error converting document to Playlist", e)
					null
				}
			}

			Log.d(TAG, "Successfully retrieved ${playlists.size} playlists")
			Result.success(playlists)
		} catch (e: Exception) {
			Log.e(TAG, "Error getting playlists", e)
			Result.failure(e)
		}
	}

	// Add videos to a playlist
	suspend fun addVideosToPlaylist(playlistId: String, videoIds: List<String>): Result<Unit> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Get the playlist
			val playlistDoc = firestore.collection("playlists").document(playlistId).get().await()
			val playlist = playlistDoc.toObject(Playlist::class.java)
				?: throw Exception("Playlist not found")

			// Check if the current user owns the playlist
			if (playlist.userId != currentUser.uid) {
				throw Exception("You don't have permission to modify this playlist")
			}

			// Add videos to the playlist (avoiding duplicates)
			val updatedVideos = (playlist.videos + videoIds).distinct()

			// Update the playlist
			firestore.collection("playlists").document(playlistId)
				.update("videos", updatedVideos)
				.await()

			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	// Remove videos from a playlist
	suspend fun removeVideosFromPlaylist(playlistId: String, videoIds: List<String>): Result<Unit> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Get the playlist
			val playlistDoc = firestore.collection("playlists").document(playlistId).get().await()
			val playlist = playlistDoc.toObject(Playlist::class.java)
				?: throw Exception("Playlist not found")

			// Check if the current user owns the playlist
			if (playlist.userId != currentUser.uid) {
				throw Exception("You don't have permission to modify this playlist")
			}

			// Remove videos from the playlist
			val updatedVideos = playlist.videos.filter { it !in videoIds }

			// Update the playlist
			firestore.collection("playlists").document(playlistId)
				.update("videos", updatedVideos)
				.await()

			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	// Delete a playlist
	suspend fun deletePlaylist(playlistId: String): Result<Unit> {
		return try {
			val currentUser = getCurrentUser() ?: throw Exception("User not logged in")

			// Get the playlist
			val playlistDoc = firestore.collection("playlists").document(playlistId).get().await()
			val playlist = playlistDoc.toObject(Playlist::class.java)
				?: throw Exception("Playlist not found")

			// Check if the current user owns the playlist
			if (playlist.userId != currentUser.uid) {
				throw Exception("You don't have permission to delete this playlist")
			}

			// Delete the playlist
			firestore.collection("playlists").document(playlistId)
				.delete()
				.await()

			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	// Get a playlist by ID
	suspend fun getPlaylistById(playlistId: String): Result<Playlist> {
		return try {
			val playlistDoc = firestore.collection("playlists").document(playlistId).get().await()
			val playlist = playlistDoc.toObject(Playlist::class.java)
				?: throw Exception("Playlist not found")

			// If not public and not the owner, throw exception
			val currentUser = getCurrentUser()
			if (!playlist.isPublic && (currentUser == null || currentUser.uid != playlist.userId)) {
				throw Exception("You don't have permission to view this playlist")
			}

			Result.success(playlist)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	// Get videos in a playlist
	suspend fun getPlaylistVideos(playlistId: String): Result<List<Video>> {
		return try {
			// Get the playlist
			val playlistResult = getPlaylistById(playlistId)

			playlistResult.fold(
				onSuccess = { playlist ->
					if (playlist.videos.isEmpty()) {
						return Result.success(emptyList())
					}

					// Get the videos
					val videosSnapshot = firestore.collection("videos")
						.whereIn("id", playlist.videos)
						.get()
						.await()

					val videos = videosSnapshot.documents.mapNotNull { doc ->
						doc.toObject(Video::class.java)
					}

					// Sort videos to match the order in the playlist
					val sortedVideos = playlist.videos.mapNotNull { videoId ->
						videos.find { it.id == videoId }
					}

					Result.success(sortedVideos)
				},
				onFailure = {
					Result.failure(it)
				}
			)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun getRandomizedVideos(limit: Int = 20): Result<List<Video>> {
		return try {
			// Get all videos
			val videosSnapshot = firestore.collection("videos")
				.get()
				.await()

			val allVideos = videosSnapshot.documents.mapNotNull { doc ->
				doc.toObject(Video::class.java)
			}

			// Shuffle the videos to get a random order
			val shuffledVideos = allVideos.shuffled()

			// Limit the number of videos returned
			val limitedVideos = if (shuffledVideos.size > limit) {
				shuffledVideos.take(limit)
			} else {
				shuffledVideos
			}

			Result.success(limitedVideos)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
}