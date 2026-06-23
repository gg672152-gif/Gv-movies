package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE isInWatchlist = 1")
    fun getWatchlist(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE continueProgress IS NOT NULL AND continueProgress > 0")
    fun getContinueWatching(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE lastWatchedTimestamp > 0 ORDER BY lastWatchedTimestamp DESC")
    fun getViewingHistory(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE downloadState != 'NONE'")
    fun getDownloads(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): MediaEntity?

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaFlowById(id: String): Flow<MediaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Query("UPDATE media_items SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("UPDATE media_items SET isInWatchlist = :isWatch WHERE id = :id")
    suspend fun updateWatchlist(id: String, isWatch: Boolean)

    @Query("UPDATE media_items SET continueProgress = :progress, lastWatchedTimestamp = :timestamp, lastWatchedEpisode = :episode WHERE id = :id")
    suspend fun updatePlaybackProgress(id: String, progress: Float?, timestamp: Long, episode: String?)

    @Query("UPDATE media_items SET downloadState = :state, downloadProgress = :progress WHERE id = :id")
    suspend fun updateDownloadState(id: String, state: String, progress: Int)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)
}
