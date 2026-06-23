package com.example.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME, MOVIES, TV_SHOWS, SEARCH, PROFILE
}

sealed interface AiRecommendState {
    object Idle : AiRecommendState
    object Loading : AiRecommendState
    data class Success(val list: List<RecommendedMovie>) : AiRecommendState
    data class Error(val message: String) : AiRecommendState
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GVMoviesDatabase.getDatabase(application)
    private val repository = MediaRepository(database.mediaDao())

    // App state
    val allMedia: StateFlow<List<MediaEntity>> = repository.allMedia.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favorites: StateFlow<List<MediaEntity>> = repository.favorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val watchlist: StateFlow<List<MediaEntity>> = repository.watchlist.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val continueWatching: StateFlow<List<MediaEntity>> = repository.continueWatching.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val viewingHistory: StateFlow<List<MediaEntity>> = repository.viewingHistory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val downloads: StateFlow<List<MediaEntity>> = repository.downloads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Splash State
    private val _showSplash = MutableStateFlow(true)
    val showSplash = _showSplash.asStateFlow()

    // Auth State
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    // Current screen layout tab
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    // Search and filter state
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre = _selectedGenre.asStateFlow()

    private val _selectedType = MutableStateFlow("All") // "All", "Movies", "TV Shows"
    val selectedType = _selectedType.asStateFlow()

    private val _selectedYear = MutableStateFlow("All") // "All", "2024", "2023", "2014 & Older"
    val selectedYear = _selectedYear.asStateFlow()

    private val _selectedMinRating = MutableStateFlow(0.0) // 0.0, 8.0, 8.5, 9.0
    val selectedMinRating = _selectedMinRating.asStateFlow()

    private val _selectedMaxRuntime = MutableStateFlow(999) // 999 (Any), 120 (2h), 150 (2.5h), 180 (3h)
    val selectedMaxRuntime = _selectedMaxRuntime.asStateFlow()

    // Overlay Views state
    private val _selectedMedia = MutableStateFlow<MediaEntity?>(null)
    val selectedMedia = _selectedMedia.asStateFlow()

    private val _activePlayingMedia = MutableStateFlow<MediaEntity?>(null)
    val activePlayingMedia = _activePlayingMedia.asStateFlow()
    
    // Episode metadata if playing a TV show episode
    private val _activePlayingEpisode = MutableStateFlow<String?>(null)
    val activePlayingEpisode = _activePlayingEpisode.asStateFlow()

    // Active playback progress trackers
    private val _playbackProgressValue = MutableStateFlow(0f)
    val playbackProgressValue = _playbackProgressValue.asStateFlow()
    
    private val _isPlayingVideo = MutableStateFlow(false)
    val isPlayingVideo = _isPlayingVideo.asStateFlow()

    // Gemini Recommendation State
    private val _aiRecommendState = MutableStateFlow<AiRecommendState>(AiRecommendState.Idle)
    val aiRecommendState = _aiRecommendState.asStateFlow()

    // Handle ongoing download jobs to avoid duplicates
    private val activeDownloadJobs = mutableMapOf<String, Job>()

    // Notification states
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // Setting theme
    private val _isDarkThemePreference = MutableStateFlow(true)
    val isDarkThemePreference = _isDarkThemePreference.asStateFlow()

    init {
        // Prepopulate catalog on launch and turn off splash after a small cinematic delay
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
            delay(1500) // Cinematic splash hold
            _showSplash.value = false
        }
    }

    // Toggle custom dark theme settings
    fun toggleTheme() {
        _isDarkThemePreference.value = !_isDarkThemePreference.value
    }

    // Authentication simulation
    fun login(email: String, name: String, method: String) {
        viewModelScope.launch {
            _userEmail.value = email
            _userName.value = if (name.isNotBlank()) name else email.substringBefore("@")
            _isAuthenticated.value = true
            // Grab warm recommendations right away
            fetchAiRecommendations()
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _userEmail.value = ""
        _userName.value = ""
    }

    // Tab Navigation
    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Details actions
    fun showMediaDetails(media: MediaEntity) {
        _selectedMedia.value = media
    }

    fun hideMediaDetails() {
        _selectedMedia.value = null
    }

    // Search input
    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
    }

    fun selectType(type: String) {
        _selectedType.value = type
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
    }

    fun selectMinRating(rating: Double) {
        _selectedMinRating.value = rating
    }

    fun selectMaxRuntime(runtimeMinutes: Int) {
        _selectedMaxRuntime.value = runtimeMinutes
    }

    fun resetFilters() {
        _searchText.value = ""
        _selectedGenre.value = "All"
        _selectedType.value = "All"
        _selectedYear.value = "All"
        _selectedMinRating.value = 0.0
        _selectedMaxRuntime.value = 999
    }

    // Favorite / Watchlist togglers
    fun toggleFavorite(mediaId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(mediaId)
            // Re-fetch recommendations if favorites changed
            fetchAiRecommendations()
        }
    }

    fun toggleWatchlist(mediaId: String) {
        viewModelScope.launch {
            repository.toggleWatchlist(mediaId)
        }
    }

    // Playback Simulation
    fun playMedia(media: MediaEntity, episodeCode: String? = null) {
        viewModelScope.launch {
            _activePlayingMedia.value = media
            _activePlayingEpisode.value = episodeCode
            _isPlayingVideo.value = true
            _playbackProgressValue.value = media.continueProgress ?: 0.0f
            
            // Register into viewing history immediately with an active progress bookmark
            repository.savePlaybackProgress(media.id, _playbackProgressValue.value, episodeCode)
        }
    }

    fun togglePlayPauseVideo() {
        _isPlayingVideo.value = !_isPlayingVideo.value
    }

    fun updatePlaybackProgress(newProgress: Float) {
        _playbackProgressValue.value = newProgress
        val media = _activePlayingMedia.value
        if (media != null) {
            viewModelScope.launch {
                repository.savePlaybackProgress(media.id, newProgress, _activePlayingEpisode.value)
            }
        }
    }

    fun stopPlayback() {
        val media = _activePlayingMedia.value
        if (media != null) {
            viewModelScope.launch {
                repository.savePlaybackProgress(media.id, _playbackProgressValue.value, _activePlayingEpisode.value)
                _activePlayingMedia.value = null
                _activePlayingEpisode.value = null
                _isPlayingVideo.value = false
            }
        }
    }

    // Offline Downloads engine simulation
    fun downloadMedia(mediaId: String) {
        if (activeDownloadJobs.containsKey(mediaId)) return

        val job = viewModelScope.launch {
            repository.saveDownloadState(mediaId, "DOWNLOADING", 0)
            
            for (progress in 5..100 step 15) {
                delay(800)
                repository.saveDownloadState(mediaId, "DOWNLOADING", progress)
            }
            repository.saveDownloadState(mediaId, "COMPLETED", 100)
            activeDownloadJobs.remove(mediaId)
        }
        activeDownloadJobs[mediaId] = job
    }

    fun cancelOrRemoveDownload(mediaId: String) {
        viewModelScope.launch {
            activeDownloadJobs[mediaId]?.cancel()
            activeDownloadJobs.remove(mediaId)
            repository.saveDownloadState(mediaId, "NONE", 0)
        }
    }

    // Toggle Notifications
    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    // Trigger AI Recommendations
    fun fetchAiRecommendations() {
        viewModelScope.launch {
            _aiRecommendState.value = AiRecommendState.Loading
            try {
                val favs = favorites.value
                val watch = watchlist.value
                val history = viewingHistory.value
                val recs = repository.getAiRecommendations(favs, watch, history)
                _aiRecommendState.value = AiRecommendState.Success(recs)
            } catch (e: Exception) {
                Log.e("AppViewModel", "AI Recommendation trigger failed", e)
                _aiRecommendState.value = AiRecommendState.Error(e.message ?: "Unknown Exception")
            }
        }
    }
}
