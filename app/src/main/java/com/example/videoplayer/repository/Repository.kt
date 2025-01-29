package com.example.videoplayer.repository

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import com.example.videoplayer.data.VideoFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class Repository {

    // This function fetches video files from the device's storage and organizes them by folders.
    // It uses Kotlin's Flow to handle the data asynchronously and efficiently.
    suspend fun GetVideosByFolder(application: Application): Flow<Map<String, List<VideoFile>>> = flow {

        // Create a mutable map to store video files grouped by their folder names.
        // Keys: Folder names (e.g., "Movies").
        // Values: Lists of video files in those folders.
        val videosByFolder = mutableMapOf<String, MutableList<VideoFile>>()

        // Define the columns (fields) we want to retrieve from the MediaStore database.
        val projection = arrayOf(
            MediaStore.Video.Media._ID,         // Unique ID for each video file.
            MediaStore.Video.Media.DATA,       // Full file path of the video.
            MediaStore.Video.Media.DURATION,   // Duration of the video in milliseconds.
            MediaStore.Video.Media.TITLE,      // Title of the video (can be user-defined).
            MediaStore.Video.Media.DISPLAY_NAME // File name of the video (e.g., "video.mp4").
        )

        // Define the URI to query video files from external storage.
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // Query the MediaStore using the content resolver.
        // This fetches video data based on the URI and projection provided.
        val cursor = application.contentResolver.query(uri, projection, null, null, null)

        // Check if the cursor is not null to ensure that the query was successful.
        if (cursor != null) {
            // Loop through the result set (rows) returned by the query.
            while (cursor.moveToNext()) {

                // Retrieve data from the current row using the column index.
                val id = cursor.getString(0)           // Column 0: Unique ID of the video.
                val path = cursor.getString(1)         // Column 1: Full file path.
                val duration = cursor.getString(2)     // Column 2: Video duration.
                val title = cursor.getString(3)        // Column 3: Title of the video.
                val fileName = cursor.getString(4)     // Column 4: File name of the video.

                // Extract the folder name from the file path.
                // Example: "/storage/Movies/video.mp4" → "Movies".
                val folderName = path.substringBeforeLast('/') // Get the folder path.
                    .substringAfterLast('/')                  // Get only the folder name.

                // Generate a URI for the video's thumbnail.
                // This is used to display a small preview image of the video.
                val thumbnail = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id.toLong() // Convert ID to Long since ContentUris requires it.
                ).toString() // Convert the URI to a String.

                // Create a VideoFile object to represent the video and its metadata.
                val videoFile = VideoFile(
                    id = id,                  // Unique ID of the video.
                    path = path,              // Full file path.
                    duration = duration,      // Video duration.
                    thumbnail = thumbnail,    // URI of the video's thumbnail.
                    fileName = fileName,      // File name of the video.
                    title = title,            // Title of the video.
                    folderName = folderName   // Folder name containing the video.
                )

                // Add the video to the list of videos for its folder.
                // If the folder doesn't exist in the map, create a new list for it.
                videosByFolder.getOrPut(folderName) { mutableListOf() }.add(videoFile)
            }

            // Close the cursor to release resources once we finish using it.
            cursor.close()
        }

        // Emit the map of videos grouped by folders as the result of this Flow.
        emit(videosByFolder)
    }
}
