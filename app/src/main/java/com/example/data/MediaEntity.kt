package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey val id: String, // e.g., "movie_1" or "tv_101"
    val title: String,
    val mediaType: String, // "movie" or "tv"
    val posterUrl: String,
    val backdropUrl: String,
    val synopsis: String,
    val releaseDate: String,
    val runtime: String, // e.g., "2h 15m" or "45m"
    val rating: Double,
    val genres: String, // Comma separated, e.g., "Action, Sci-Fi"
    val cast: String, // Comma separated cast names
    
    // User flags
    val isFavorite: Boolean = false,
    val isInWatchlist: Boolean = false,
    
    // Continue Watching / Watching History
    val continueProgress: Float? = null, // null means not started, or float 0.0f - 1.0f
    val lastWatchedEpisode: String? = null, // e.g. "S2 E3" (for TV shows)
    val lastWatchedTimestamp: Long = 0L, // 0L means not in history, otherwise timestamp
    
    // Download Manager
    val downloadState: String = "NONE", // NONE, DOWNLOADING, COMPLETED
    val downloadProgress: Int = 0 // 0 to 100
)
