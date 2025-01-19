package com.example.videoplayer.data

data class VideoFile(
    val id: String,
    val path: String,
    val duration: String,
    val thumbnail: String,
    val fileName: String,
    val title: String,
    val folderName: String
)
