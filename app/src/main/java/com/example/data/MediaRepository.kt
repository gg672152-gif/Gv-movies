package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import com.example.BuildConfig

class MediaRepository(private val mediaDao: MediaDao) {

    val allMedia: Flow<List<MediaEntity>> = mediaDao.getAllMedia()
    val favorites: Flow<List<MediaEntity>> = mediaDao.getFavorites()
    val watchlist: Flow<List<MediaEntity>> = mediaDao.getWatchlist()
    val continueWatching: Flow<List<MediaEntity>> = mediaDao.getContinueWatching()
    val viewingHistory: Flow<List<MediaEntity>> = mediaDao.getViewingHistory()
    val downloads: Flow<List<MediaEntity>> = mediaDao.getDownloads()

    fun getMediaById(id: String): Flow<MediaEntity?> = mediaDao.getMediaFlowById(id)

    suspend fun toggleFavorite(id: String) = withContext(Dispatchers.IO) {
        val current = mediaDao.getMediaById(id)
        if (current != null) {
            mediaDao.updateFavorite(id, !current.isFavorite)
        }
    }

    suspend fun toggleWatchlist(id: String) = withContext(Dispatchers.IO) {
        val current = mediaDao.getMediaById(id)
        if (current != null) {
            mediaDao.updateWatchlist(id, !current.isInWatchlist)
        }
    }

    suspend fun savePlaybackProgress(id: String, progress: Float?, episode: String? = null) = withContext(Dispatchers.IO) {
        mediaDao.updatePlaybackProgress(id, progress, System.currentTimeMillis(), episode)
    }

    suspend fun saveDownloadState(id: String, state: String, progress: Int) = withContext(Dispatchers.IO) {
        mediaDao.updateDownloadState(id, state, progress)
    }

    suspend fun deleteMedia(mediaId: String) = withContext(Dispatchers.IO) {
        val current = mediaDao.getMediaById(mediaId)
        if (current != null) {
            mediaDao.deleteMedia(current)
        }
    }

    // AI recommendation client
    suspend fun getAiRecommendations(
        favoritesList: List<MediaEntity>,
        watchlistList: List<MediaEntity>,
        viewingHistoryList: List<MediaEntity>
    ): List<RecommendedMovie> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            Log.d("MediaRepository", "Gemini API key is placeholder; using local smart recommend model.")
            return@withContext getLocalFallbackRecommendations(favoritesList, watchlistList, viewingHistoryList)
        }

        val likedTitles = favoritesList.joinToString { it.title }
        val watchTitles = watchlistList.joinToString { it.title }
        val viewedTitles = viewingHistoryList.joinToString { it.title }

        val prompt = buildString {
            append("The user is using GVMovies. ")
            if (likedTitles.isNotEmpty() || watchTitles.isNotEmpty() || viewedTitles.isNotEmpty()) {
                append("Here is their viewing profile to analyze: ")
                if (likedTitles.isNotEmpty()) {
                    append("Favorited: [$likedTitles]. ")
                }
                if (watchTitles.isNotEmpty()) {
                    append("Saved to Watchlist: [$watchTitles]. ")
                }
                if (viewedTitles.isNotEmpty()) {
                    append("Recently viewed: [$viewedTitles]. ")
                }
                append("Please identify their specific cinematic taste (genres, pacing, tone) and recommend 5 similar high-quality trending movies or TV shows. ")
            } else {
                append("The user is interested in highly-rated prestige TV and films. Please recommend 5 blockbuster top-tier movies or TV shows. ")
            }
            append("Respond with a JSON object containing a property 'recommendations' which is a list. Each item in 'recommendations' must contain: 'title' (string), 'mediaType' (either 'movie' or 'tv'), 'synopsis' (string, max 3 sentences), 'genres' (string, comma-separated), 'rating' (number from 1 to 10), and 'whyRecommended' (string, explanation of how it relates to their taste). Avoid any markdown except returning clean raw json conforming to RecommendedMovieList schema.")
        }

        try {
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7f
                )
            )
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!rawJson.isNullOrEmpty()) {
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter(RecommendedMovieList::class.java)
                val cleanJson = cleanJsonResponse(rawJson)
                adapter.fromJson(cleanJson)?.recommendations ?: getLocalFallbackRecommendations(favoritesList, watchlistList, viewingHistoryList)
            } else {
                getLocalFallbackRecommendations(favoritesList, watchlistList, viewingHistoryList)
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Error requesting Gemini AI recommendations", e)
            getLocalFallbackRecommendations(favoritesList, watchlistList, viewingHistoryList)
        }
    }

    private fun cleanJsonResponse(json: String): String {
        var result = json.trim()
        if (result.startsWith("```json")) {
            result = result.substringAfter("```json")
        } else if (result.startsWith("```")) {
            result = result.substringAfter("```")
        }
        if (result.endsWith("```")) {
            result = result.substringBeforeLast("```")
        }
        return result.trim()
    }

    private fun getLocalFallbackRecommendations(
        favorites: List<MediaEntity>,
        watchlist: List<MediaEntity>,
        history: List<MediaEntity>
    ): List<RecommendedMovie> {
        val combined = favorites + watchlist + history
        val hasSciFi = combined.any { it.genres.contains("Sci-Fi", ignoreCase = true) }
        val hasDrama = combined.any { it.genres.contains("Drama", ignoreCase = true) }
        
        return if (hasSciFi) {
            listOf(
                RecommendedMovie("Interstellar", "movie", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.", "Sci-Fi, Adventure, Drama", 8.6, "Based on your love for deep sci-fi thrillers."),
                RecommendedMovie("Blade Runner 2049", "movie", "A new blade runner, LAPD Officer K, unearths a long-buried secret that has the potential to plunge what's left of society into chaos.", "Drama, Sci-Fi", 8.4, "Since you enjoy stunning visual masterpieces like Dune."),
                RecommendedMovie("Foundation", "tv", "A complex saga of humans scattered on planets throughout the galaxy all living under the rule of the Galactic Empire.", "Sci-Fi, Drama", 8.1, "Matches your interest in epic sci-fi sagas."),
                RecommendedMovie("Inception", "movie", "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea.", "Sci-Fi, Action, Thriller", 8.8, "A mind-bending thrill matching your favorite sci-fi themes."),
                RecommendedMovie("Severance", "tv", "Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives.", "Thriller, Sci-Fi", 8.7, "An absolute mental thriller masterpiece for thought-provoking stories.")
            )
        } else if (hasDrama) {
            listOf(
                RecommendedMovie("Succession", "tv", "The Roy family is known for controlling the biggest media and entertainment company in the world. However, their world changes when their father steps down.", "Drama, Comedy", 8.8, "A masterful dialogue-heavy character study about corporate family dynasty."),
                RecommendedMovie("The Dark Knight", "movie", "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological tests.", "Action, Crime, Drama", 9.0, "The definitive prestige drama of the superhero genre."),
                RecommendedMovie("Better Call Saul", "tv", "The trials and tribulations of criminal lawyer Jimmy McGill in the years leading up to his representation of Breaking Bad's Walter White.", "Drama, Crime", 8.9, "Matches your enjoyment of deep crime dramas and complex writing."),
                RecommendedMovie("Chernobyl", "tv", "In April 1986, an explosion at the Chernobyl nuclear power plant in the USSR becomes one of the world's worst man-made catastrophes.", "Drama, History", 9.4, "An intense historical drama with incredible tension and realism."),
                RecommendedMovie("Whiplash", "movie", "A promising young drummer enrolls at a cut-throat music conservatory where his dreams of greatness are mentored by an abusive instructor.", "Drama, Music", 8.5, "High-intensity human drama with masterful acting and pacing.")
            )
        } else {
            // General high prestige recommendations
            listOf(
                RecommendedMovie("Oppenheimer", "movie", "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.", "Drama, History, Biography", 8.7, "Our top featured cinema pick representing peak directing, acting and score."),
                RecommendedMovie("House of the Dragon", "tv", "An internal succession war within House Targaryen at the height of its power 172 years before the birth of Daenerys.", "Drama, Fantasy", 8.6, "A legendary political war fantasy with stunning dragons and performances."),
                RecommendedMovie("Spider-Man: Across the Spider-Verse", "movie", "Miles Morales is catapulted across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.", "Animation, Action, Sci-Fi", 8.9, "Hands down the most visually creative film of this decade."),
                RecommendedMovie("Stranger Things", "tv", "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.", "Drama, Fantasy, Sci-Fi", 8.7, "Engaging 80s nostalgia with high adventure and lovable characters."),
                RecommendedMovie("Dune: Part Two", "movie", "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.", "Action, Adventure, Sci-Fi", 8.8, "An majestic theatrical spectacle with a rich score and sweeping desert views.")
            )
        }
    }

    // Prepopulate DB with curated, stunning content
    suspend fun prepopulateDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val count = mediaDao.getFavorites().first().size + mediaDao.getWatchlist().first().size + mediaDao.getAllMedia().first().size
        if (count == 0) {
            Log.d("MediaRepository", "Database is empty! Curating GVMovies catalog...")
            val initialCatalog = getPreloadedCatalog()
            mediaDao.insertMediaList(initialCatalog)
        }
    }

    private fun getPreloadedCatalog(): List<MediaEntity> {
        return listOf(
            // Movies (Trending)
            MediaEntity(
                id = "movie_1",
                title = "Dune: Part Two",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/cz062JuieiVvFNvTT5zsa6i6G3X.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/xOM9Z3j6j61656gFB68IQ6m67A5.jpg",
                synopsis = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the universe, he endeavors to prevent a terrible future only he can foresee.",
                releaseDate = "March 1, 2024",
                runtime = "2h 46m",
                rating = 8.8,
                genres = "Sci-Fi, Adventure, Drama",
                cast = "Timothée Chalamet, Zendaya, Rebecca Ferguson, Josh Brolin, Austin Butler"
            ),
            // Movies (Popular)
            MediaEntity(
                id = "movie_2",
                title = "Spider-Man: Across the Spider-Verse",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/8Vtbi7Suwcn0g6SgQI86o2vZJvL.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/gJVla9s7m69036mscWeSj6YvX6G.jpg",
                synopsis = "After reuniting with Gwen Stacy, Brooklyn's full-time, friendly neighborhood Spider-Man is catapulted across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence. However, when the heroes clash on how to handle a new threat, Miles must redefine what it means to be a hero.",
                releaseDate = "June 2, 2023",
                runtime = "2h 20m",
                rating = 8.9,
                genres = "Animation, Action, Sci-Fi",
                cast = "Shameik Moore, Hailee Steinfeld, Oscar Isaac, Jake Johnson, Issa Rae"
            ),
            // Movies (Top Rated)
            MediaEntity(
                id = "movie_3",
                title = "Oppenheimer",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/8Gxv2gSj0Y0Gvl7g8cl1g6N06vt.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/fm6m0Y720vzsF6BhVX86X3YvH65.jpg",
                synopsis = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb. Delving deep into his brilliant mind, the film chronicles how Oppenheimer led the Manhattan Project to create a weapon that would end World War II, but forever transform mankind.",
                releaseDate = "July 21, 2023",
                runtime = "3h 0m",
                rating = 8.7,
                genres = "Drama, Biography, History",
                cast = "Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr., Florence Pugh"
            ),
            // Movies (Top Rated)
            MediaEntity(
                id = "movie_4",
                title = "Interstellar",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/gEU2Qv6g6gb7Es9QPM9vuo86gXz.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/xb7Eb6v9ZOSTf74ZgCuIFv767To.jpg",
                synopsis = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival on another planet. As Earth faces an ecological collapse, pilot Cooper must leave his children behind to navigate deep space and quantum mysteries to find a new home.",
                releaseDate = "November 7, 2014",
                runtime = "2h 49m",
                rating = 8.6,
                genres = "Sci-Fi, Adventure, Drama",
                cast = "Matthew McConaughey, Anne Hathaway, Jessica Chastain, Michael Caine, Timothée Chalamet"
            ),
            // Movies (New Releases)
            MediaEntity(
                id = "movie_5",
                title = "Deadpool & Wolverine",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/8cdWv6ZteA993wb3g63asS66HIO.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/yD17m9sSgy0vvSgY98vL8m6p7u.jpg",
                synopsis = "A listless Wade Wilson toils in civilian life, his days as the morally flexible mercenary Deadpool behind him. But when the Time Variance Authority drags him into a new mission to save his peaceful universe, Wade must team up with an incredibly reluctant, angry Wolverine.",
                releaseDate = "July 26, 2024",
                runtime = "2h 7m",
                rating = 8.1,
                genres = "Action, Comedy, Sci-Fi",
                cast = "Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen, Morena Baccarin"
            ),
            // Movies (New Releases)
            MediaEntity(
                id = "movie_6",
                title = "Inception",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/l97OfHeGg2j7IM6pA86mR8XsgXS.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/tMnasMns0S8Y6pzsYcl12z0XS.jpg",
                synopsis = "Cobb, a skilled thief who steals valuable corporate secrets from deep within the subconscious during the dream state, gets a chance at redemption: he must perform inception—planting an idea into a target's mind rather than stealing one.",
                releaseDate = "July 16, 2010",
                runtime = "2h 28m",
                rating = 8.8,
                genres = "Sci-Fi, Action, Thriller",
                cast = "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page, Tom Hardy, Ken Watanabe"
            ),
            // Movies (Popular)
            MediaEntity(
                id = "movie_7",
                title = "The Dark Knight",
                mediaType = "movie",
                posterUrl = "https://image.tmdb.org/t/p/w500/qJ2tW69uSM9X9gCH38m8g14FwbS.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/o9O89X6j696m726mU7e3m8XsgXS.jpg",
                synopsis = "With the help of allies District Attorney Harvey Dent and Lieutenant Jim Gordon, Batman sets out to dismantle the remaining criminal organizations that plague Gotham City. The partnership proves effective, but they soon find themselves prey to a rising mastermind of chaos known to the terrified citizens as The Joker.",
                releaseDate = "July 18, 2008",
                runtime = "2h 32m",
                rating = 9.0,
                genres = "Action, Crime, Drama",
                cast = "Christian Bale, Heath Ledger, Aaron Eckhart, Maggie Gyllenhaal, Gary Oldman"
            ),

            // TV Shows (Trending)
            MediaEntity(
                id = "tv_1",
                title = "The Last of Us",
                mediaType = "tv",
                posterUrl = "https://image.tmdb.org/t/p/w500/uKVDFRI69SgU8Y89v0SgS86gS8v.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/uD3Z6Y83YV1p7SgS6y8Y9898S.jpg",
                synopsis = "Twenty years after modern civilization has been destroyed, Joel, a hardened survivor, is hired to smuggle Ellie, a 14-year-old girl, out of an oppressive quarantine zone. What starts as a small job soon becomes a brutal, heartbreaking journey, as they both must traverse the U.S. and depend on each other for survival.",
                releaseDate = "January 15, 2023",
                runtime = "1 Season (9 Episodes)",
                rating = 8.8,
                genres = "Drama, Action, Sci-Fi",
                cast = "Pedro Pascal, Bella Ramsey, Gabriel Luna, Anna Torv, Nico Parker"
            ),
            // TV Shows (Popular)
            MediaEntity(
                id = "tv_2",
                title = "Stranger Things",
                mediaType = "tv",
                posterUrl = "https://image.tmdb.org/t/p/w500/49Wp6S7H9HUf5TepFm0mR3Rgn6r.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/56v2ve1v98v68S80e3m98v7vtH9.jpg",
                synopsis = "When a young boy vanishes, a small Indiana town uncovers a mystery involving top-secret government experiments, terrifying supernatural forces, and an extraordinary little girl with telekinetic powers. Friendly bonds and deep courage are tested.",
                releaseDate = "July 15, 2016",
                runtime = "4 Seasons (34 Episodes)",
                rating = 8.7,
                genres = "Sci-Fi, Drama, Mystery",
                cast = "Winona Ryder, David Harbour, Millie Bobby Brown, Finn Wolfhard, Gaten Matarazzo"
            ),
            // TV Shows (Top Rated)
            MediaEntity(
                id = "tv_3",
                title = "House of the Dragon",
                mediaType = "tv",
                posterUrl = "https://image.tmdb.org/t/p/w500/7gKIv3v933VTGO6rj6gH0GXv79c.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/etjKofzsa66E99X9V0vsX9gH0Fv.jpg",
                synopsis = "An internal succession war within House Targaryen at the height of its power, nearly 200 years before the events of Game of Thrones. As King Viserys must decide on an heir, plotting, rivalry, and magnificent dragons clash in a bid for the Iron Throne.",
                releaseDate = "August 21, 2022",
                runtime = "2 Seasons (18 Episodes)",
                rating = 8.6,
                genres = "Drama, Sci-Fi & Fantasy",
                cast = "Matt Smith, Emma D'Arcy, Olivia Colman, Paddy Considine, Rhys Ifans"
            ),
            // TV Shows (Top Rated)
            MediaEntity(
                id = "tv_4",
                title = "Breaking Bad",
                mediaType = "tv",
                posterUrl = "https://image.tmdb.org/t/p/w500/ztkUQv69SuS7vSgS98v688v6H0u.jpg",
                backdropUrl = "https://image.tmdb.org/t/p/original/9v9vY98Yg0vvvsXpY9v688v6H0u.jpg",
                synopsis = "A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine with a former student in order to secure his family's financial future. He descends into a thrilling realm of greed and survival.",
                releaseDate = "January 20, 2008",
                runtime = "5 Seasons (62 Episodes)",
                rating = 9.5,
                genres = "Drama, Crime",
                cast = "Bryan Cranston, Aaron Paul, Anna Gunn, Bob Odenkirk, Dean Norris"
            ),
            // TV Shows (New Releases)
            MediaEntity(
                id = "tv_5",
                title = "Severance",
                mediaType = "tv",
                posterUrl = "https://image.tmdb.org/t/p/w500/yD17m9sSgy0vvSgY98vL8m6p7u.jpg", // placeholder
                backdropUrl = "https://image.tmdb.org/t/p/original/qdIMHd4s3z6Yi79b696v2gSj0Y0.jpg", // placeholder
                synopsis = "Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives. When a mysterious colleague appears outside of work, it begins a journey to discover the truth about their jobs.",
                releaseDate = "February 18, 2022",
                runtime = "1 Season (9 Episodes)",
                rating = 8.7,
                genres = "Thriller, Sci-Fi",
                cast = "Adam Scott, Patricia Arquette, John Turturro, Christopher Walken, Britt Lower"
            )
        )
    }
}
