package com.example.videoplayer

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.rememberAsyncImagePainter
import com.example.videoplayer.data.VideoFile

@Composable
fun SimpleVideoUI(viewModel: VideoViewModel, application: Application) {
    val videosByFolder = viewModel.videosByFolder.collectAsState()
    val (currentFolder, setCurrentFolder) = remember { mutableStateOf<String?>(null) }
    val (selectedVideoPath, setSelectedVideoPath) = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchVideosByFolder(application)
    }

    when {
        selectedVideoPath != null -> {
            // Show the video player if a video is selected
            VideoPlayerScreen(videoPath = selectedVideoPath!!)
        }

        currentFolder != null -> {
            // Show the videos in the selected folder
            videosByFolder.value[currentFolder]?.let { videos ->
                VideoListScreen(videos = videos) { videoPath ->
                    setSelectedVideoPath(videoPath)
                }
            }
        }

        else -> {
            // Show the folder list
            FolderScreen(viewModel = viewModel) { folderName ->
                setCurrentFolder(folderName)
            }
        }
    }
}
@Composable
fun FolderScreen(viewModel: VideoViewModel, onFolderClick: (String) -> Unit) {
    val videosByFolder = viewModel.videosByFolder.collectAsState()

    LazyColumn {
        videosByFolder.value.forEach { (folderName, videos) ->
            item {
                FolderItem(folderName = folderName, videoCount = videos.size) {
                    onFolderClick(folderName)
                }
            }
        }
    }
}

@Composable
fun FolderItem(folderName: String, videoCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "$folderName ($videoCount videos)"

        )
    }
}
@Composable
fun VideoListScreen(videos: List<VideoFile>, onVideoClick: (String) -> Unit) {
    LazyColumn {
        items(videos) { video ->
            VideoItem(video = video, onClick = { onVideoClick(video.path) })
        }
    }
}

@Composable
fun VideoItem(video: VideoFile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(video.thumbnail),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = video.title)
            Text(text = video.duration)
        }
    }
}
@Composable
fun VideoPlayerScreen(videoPath: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoPath))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(factory = {
        PlayerView(it).apply {
            player = exoPlayer
        }
    }, modifier = Modifier.fillMaxSize())
}
