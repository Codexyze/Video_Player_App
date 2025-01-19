package com.example.videoplayer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoplayer.data.VideoFile
import com.example.videoplayer.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoViewModel(private val repository: Repository) : ViewModel() {
    private val _videosByFolder = MutableStateFlow<Map<String, List<VideoFile>>>(emptyMap())
    val videosByFolder: StateFlow<Map<String, List<VideoFile>>> = _videosByFolder

    fun fetchVideosByFolder(application: Application) {
        viewModelScope.launch {
            repository.GetVideosByFolder(application).collect { videos ->
                _videosByFolder.value = videos
            }
        }
    }
}
