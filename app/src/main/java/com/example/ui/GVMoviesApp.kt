package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GVMoviesApp(
    viewModel: AppViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val showSplash by viewModel.showSplash.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            showSplash -> {
                SplashScreen()
            }
            !isAuthenticated -> {
                AuthScreen(onLogin = { email, name ->
                    viewModel.login(email, name, "Email")
                })
            }
            else -> {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

// 1. Beautiful Splash Screen with GVMovies Logo zoom/fade animation
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowValue"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070707), Color(0xFF160304), Color(0xFF070707))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(MovieRed.copy(alpha = 0.28f), Color.Transparent)
                            ),
                            radius = size.minDimension * glowScale
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = "GVMovies Logo Icon",
                    tint = MovieRed,
                    modifier = Modifier.size(90.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GVMovies",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = MovieRed
            )
            Text(
                text = "PREMIUM CINEMA INTERACTIVE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = AccentGold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = MovieRed,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 2. Immersive Netflix-Inspired Authentication Screen
@Composable
fun AuthScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSimulatingLogin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Shared Backdrop structure
        Image(
            painter = painterResource(id = R.drawable.img_movies_hero_banner),
            contentDescription = "Login Backdrop Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.18f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000), Color(0xFF0A0A0A)),
                        startY = 0f,
                        endY = 1600f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEE141414)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GVMovies",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MovieRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isSignUp) "Create Premium Account" else "Welcome Back, Cinema Lover",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MovieRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Fields
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = { Text("Email", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MovieRed) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MovieRed,
                            unfocusedBorderColor = Color(0xFF3B3B3B)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field")
                    )
                    
                    if (isSignUp) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Profile Name (Optional)", color = TextSecondaryDark) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MovieRed) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MovieRed,
                                unfocusedBorderColor = Color(0xFF3B3B3B)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Password", color = TextSecondaryDark) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MovieRed) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MovieRed,
                            unfocusedBorderColor = Color(0xFF3B3B3B)
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both Email and Password."
                                return@Button
                            }
                            if (!email.contains("@")) {
                                errorMessage = "Please provide an authentic Email."
                                return@Button
                            }
                            if (password.length < 4) {
                                errorMessage = "Password must be at least 4 characters."
                                return@Button
                            }

                            scope.launch {
                                isSimulatingLogin = true
                                delay(1000)
                                isSimulatingLogin = false
                                onLogin(email, nickname)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MovieRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSimulatingLogin) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.0.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (isSignUp) "SIGN UP" else "SIGN IN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Or Secure Sign-in Via Partner Accounts",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Google Simulate Integration Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isSimulatingLogin = true
                                    delay(800)
                                    isSimulatingLogin = false
                                    onLogin("google.user@gmail.com", "Google Buddy")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B3B3B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = "G",
                                color = Color.Red,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Apple Simulate Integration Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isSimulatingLogin = true
                                    delay(800)
                                    isSimulatingLogin = false
                                    onLogin("apple.cinema@icloud.com", "Apple Critic")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B3B3B)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Apple ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    TextButton(onClick = { isSignUp = !isSignUp; errorMessage = "" }) {
                        Text(
                            text = if (isSignUp) "Already have an account? Sign In" else "New to GVMovies? Join Premium Now",
                            color = MovieRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// 3. Main Frame Application enclosing tabs and overlay logic
@Composable
fun MainContent(viewModel: AppViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedMedia by viewModel.selectedMedia.collectAsState()
    val activePlayingMedia by viewModel.activePlayingMedia.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
                AppTab.MOVIES -> MoviesTab(viewModel = viewModel)
                AppTab.TV_SHOWS -> TVShowsTab(viewModel = viewModel)
                AppTab.SEARCH -> SearchTab(viewModel = viewModel)
                AppTab.PROFILE -> ProfileTab(viewModel = viewModel)
            }

            // Media detail sheet overlay over current panel
            AnimatedVisibility(
                visible = selectedMedia != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                selectedMedia?.let { media ->
                    MediaDetailScreen(
                        media = media,
                        viewModel = viewModel,
                        onClose = { viewModel.hideMediaDetails() }
                    )
                }
            }

            // Premium Full-Screen Cinema Simulator Overlay
            AnimatedVisibility(
                visible = activePlayingMedia != null,
                enter = fadeIn() + scaleIn(initialScale = 0.95f),
                exit = fadeOut() + scaleOut(targetScale = 0.85f)
            ) {
                activePlayingMedia?.let { media ->
                    CinemaPlayerScreen(
                        media = media,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// 4. Custom Bottom Navigation element matching responsive styling
@Composable
fun BottomNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0C0C0C),
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = selectedTab == AppTab.HOME,
            onClick = { onTabSelected(AppTab.HOME) },
            icon = { Icon(if (selectedTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRed,
                selectedTextColor = MovieRed,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = Color(0xFF1E1112)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.MOVIES,
            onClick = { onTabSelected(AppTab.MOVIES) },
            icon = { Icon(if (selectedTab == AppTab.MOVIES) Icons.Filled.LocalActivity else Icons.Outlined.LocalActivity, contentDescription = "Movies") },
            label = { Text("Movies", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRed,
                selectedTextColor = MovieRed,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = Color(0xFF1E1112)
            ),
            modifier = Modifier.testTag("nav_item_movies")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.TV_SHOWS,
            onClick = { onTabSelected(AppTab.TV_SHOWS) },
            icon = { Icon(if (selectedTab == AppTab.TV_SHOWS) Icons.Filled.Tv else Icons.Outlined.Tv, contentDescription = "TV") },
            label = { Text("TV Shows", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRed,
                selectedTextColor = MovieRed,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = Color(0xFF1E1112)
            ),
            modifier = Modifier.testTag("nav_item_tv")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.SEARCH,
            onClick = { onTabSelected(AppTab.SEARCH) },
            icon = { Icon(if (selectedTab == AppTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search, contentDescription = "Search") },
            label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRed,
                selectedTextColor = MovieRed,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = Color(0xFF1E1112)
            ),
            modifier = Modifier.testTag("nav_item_search")
        )
        NavigationBarItem(
            selected = selectedTab == AppTab.PROFILE,
            onClick = { onTabSelected(AppTab.PROFILE) },
            icon = { Icon(if (selectedTab == AppTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRed,
                selectedTextColor = MovieRed,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = Color(0xFF1E1112)
            ),
            modifier = Modifier.testTag("nav_item_profile")
        )
    }
}

// 5. The home screen detailing cinema categories and shelves
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val allMedia by viewModel.allMedia.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val aiState by viewModel.aiRecommendState.collectAsState()

    // Filter media segments
    val movies = allMedia.filter { it.mediaType == "movie" }
    val tvShows = allMedia.filter { it.mediaType == "tv" }

    val trendingItems = allMedia.take(4)
    val popularItems = allMedia.reversed().take(5)
    val topRatedItems = allMedia.sortedByDescending { it.rating }.take(5)
    val newReleases = allMedia.filter { it.releaseDate.contains("2024") || it.releaseDate.contains("2023") }.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // High fidelity hero backdrop cover banner at the top
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_movies_hero_banner),
                    contentDescription = "Featured Showcase Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Cinema Bottom Fade out overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA0A0203), Color(0xFF0C0C0C)),
                                startY = 100f
                            )
                        )
                )

                // Branding text overlapping left
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MovieRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PREMIUM PICKS",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Deadpool & Wolverine",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 36.sp
                    )
                    Text(
                        text = "Action • Comedy • Blockbuster Superheroes",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quick Play Button
                        Button(
                            onClick = {
                                // Find actual item or build a fallback
                                val deadpool = allMedia.firstOrNull { it.id == "movie_5" }
                                if (deadpool != null) {
                                    viewModel.playMedia(deadpool)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MovieRed),
                            modifier = Modifier.height(42.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play Theme", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }

                        // Info Details Button
                        Button(
                            onClick = {
                                val deadpool = allMedia.firstOrNull { it.id == "movie_5" }
                                if (deadpool != null) {
                                    viewModel.showMediaDetails(deadpool)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x55FFFFFF)),
                            modifier = Modifier.height(42.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("More Details", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Active continue watching progress shelf
        if (continueWatching.isNotEmpty()) {
            item {
                Text(
                    text = "Continue Watching",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatching) { item ->
                        ContinueWatchingCard(item = item, onClick = { viewModel.playMedia(item, item.lastWatchedEpisode) })
                    }
                }
            }
        }

        // Horizontal Shelves
        item {
            MediaShelf(
                title = "Trending Movies & Shows",
                items = trendingItems,
                onMediaSelect = { viewModel.showMediaDetails(it) }
            )
        }

        // Generative AI personalized Recommendations
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 20.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF261011), Color(0xFF140D15))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(BorderStroke(1.dp, Color(0xFFE50914).copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recommended For You",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "AI engine analyzing your history, watchlist & favorites",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.fetchAiRecommendations() },
                            modifier = Modifier.background(MovieRed.copy(0.15f), CircleShape)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Regenerate Recommendations", tint = MovieRed)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    when (val state = aiState) {
                        is AiRecommendState.Idle -> {
                            Text(
                                text = "Click the stars icon to dynamically trigger GVMovies AI personalized engine based on your profile!",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        is AiRecommendState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(14.dp)
                            ) {
                                CircularProgressIndicator(color = MovieRed, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Analyzing taste profile in real-time...", color = Color.White, fontSize = 11.sp)
                            }
                        }
                        is AiRecommendState.Success -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                state.list.take(3).forEach { pick ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0x33000000), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.White.copy(0.04f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val match = allMedia.find { it.title.equals(pick.title, ignoreCase = true) }
                                                if (match != null) {
                                                    viewModel.showMediaDetails(match)
                                                } else {
                                                    val mockMedia = MediaEntity(
                                                        id = "ai_" + pick.title.hashCode(),
                                                        title = pick.title,
                                                        mediaType = pick.mediaType,
                                                        posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=500",
                                                        backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800",
                                                        synopsis = pick.synopsis,
                                                        releaseDate = "2024",
                                                        runtime = "2h 15m",
                                                        rating = pick.rating,
                                                        genres = pick.genres,
                                                        cast = "Curated AI Recommendation Cast"
                                                    )
                                                    viewModel.showMediaDetails(mockMedia)
                                                }
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                      ) {
                                        Icon(
                                            imageVector = if (pick.mediaType == "movie") Icons.Default.Movie else Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = MovieRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = pick.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier.background(AccentGold.copy(0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("★ ${pick.rating}", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(
                                                text = pick.whyRecommended,
                                                color = TextSecondaryDark,
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Details",
                                            tint = TextSecondaryDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is AiRecommendState.Error -> {
                            Text("Could not fetch Gemini recommendations. Using local intelligence fallback.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            MediaShelf(
                title = "Popular Curations",
                items = popularItems,
                onMediaSelect = { viewModel.showMediaDetails(it) }
            )
        }

        item {
            MediaShelf(
                title = "Top Rated Masterpieces",
                items = topRatedItems,
                onMediaSelect = { viewModel.showMediaDetails(it) }
            )
        }

        item {
            MediaShelf(
                title = "New Cinematic Releases",
                items = newReleases,
                onMediaSelect = { viewModel.showMediaDetails(it) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Movies category grid tab
@Composable
fun MoviesTab(viewModel: AppViewModel) {
    val allMedia by viewModel.allMedia.collectAsState()
    val movies = allMedia.filter { it.mediaType == "movie" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F0F))
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "Feature Films",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        LazyVerticalGrid(
            items = movies,
            onMediaSelect = { viewModel.showMediaDetails(it) }
        )
    }
}

// TV show category tab showing interactive seasons
@Composable
fun TVShowsTab(viewModel: AppViewModel) {
    val allMedia by viewModel.allMedia.collectAsState()
    val tvShows = allMedia.filter { it.mediaType == "tv" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F0F))
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Text(
                text = "Prestige Television",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }

        LazyVerticalGrid(
            items = tvShows,
            onMediaSelect = { viewModel.showMediaDetails(it) }
        )
    }
}

// 6. Generic Media horizontal shelf
@Composable
fun MediaShelf(
    title: String,
    items: List<MediaEntity>,
    onMediaSelect: (MediaEntity) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(start = 20.dp, bottom = 10.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MediaCoverCard(item = item, onClick = { onMediaSelect(item) })
            }
        }
    }
}

// Movie / TV cover card for horizontal shelf
@Composable
fun MediaCoverCard(
    item: MediaEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(135.dp)
            .height(200.dp)
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = "${item.title} Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Rating and Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(0.75f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = item.rating.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom title overlay for confidence
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.9f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Horizontal card specialized for Continue Watching with visual progress progress bar
@Composable
fun ContinueWatchingCard(
    item: MediaEntity,
    onClick: () -> Unit
) {
    val progress = item.continueProgress ?: 0.0f
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(210.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.backdropUrl,
                contentDescription = "${item.title} Continue Watching Backdrop",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Blur dark overlay on bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.85f)),
                            startY = 60f
                        )
                    )
            )

            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = "Resume",
                tint = Color.White.copy(0.8f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.mediaType == "tv") item.lastWatchedEpisode ?: "Last Watched" else "Movie Progress",
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Red progress bar matching user watching track
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MovieRed,
                    trackColor = Color.Gray.copy(0.4f)
                )
            }
        }
    }
}

// Dual columns responsive flexible movie/tv shows grid
@Composable
fun LazyVerticalGrid(
    items: List<MediaEntity>,
    onMediaSelect: (MediaEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Chunk items into lists of 2 elements per row
        val rows = items.chunked(2)
        items(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        MediaCoverCardWithRatingLabel(item = item, onClick = { onMediaSelect(item) })
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MediaCoverCardWithRatingLabel(item: MediaEntity, onClick: () -> Unit) {
    Column {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clickable(onClick = onClick)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("★ ${item.rating}", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = item.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = "${item.releaseDate.takeLast(4)} • ${item.runtime}",
            color = TextSecondaryDark,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

// 7. Interactive Instant Search and Smart Filters tab
@Composable
fun SearchTab(viewModel: AppViewModel) {
    val searchText by viewModel.searchText.collectAsState()
    val genres = listOf("All", "Sci-Fi", "Drama", "Action", "Biography", "Crime", "Thriller")
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()

    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMinRating by viewModel.selectedMinRating.collectAsState()
    val selectedMaxRuntime by viewModel.selectedMaxRuntime.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Helper to parse runtime to minutes dynamically
    fun parseRuntimeToMinutes(runtimeStr: String): Int {
        var hours = 0
        var minutes = 0
        if (runtimeStr.contains("h")) {
            val hPart = runtimeStr.substringBefore("h").trim()
            hours = hPart.toIntOrNull() ?: 0
            val mPart = runtimeStr.substringAfter("h").replace("m", "").trim()
            minutes = mPart.toIntOrNull() ?: 0
        } else {
            val clean = runtimeStr.replace("m", "").replace("Season", "").replace("Episodes", "").trim()
            if (runtimeStr.contains("Episode")) {
                minutes = 120 // Treat typical TV show episode bundle block as standard movie lengths
            } else {
                minutes = clean.toIntOrNull() ?: 0
            }
        }
        return hours * 60 + minutes
    }

    // Query filter logic
    val filteredCatalog = allMedia.filter { item ->
        val matchesText = item.title.contains(searchText, ignoreCase = true) ||
                item.genres.contains(searchText, ignoreCase = true) ||
                item.synopsis.contains(searchText, ignoreCase = true) ||
                item.cast.contains(searchText, ignoreCase = true)
        val matchesGenre = selectedGenre == "All" || item.genres.contains(selectedGenre, ignoreCase = true)
        val matchesType = when (selectedType) {
            "All" -> true
            "Movies" -> item.mediaType == "movie"
            "TV Shows" -> item.mediaType == "tv"
            else -> true
        }
        val matchesYear = when (selectedYear) {
            "All" -> true
            "2024" -> item.releaseDate.contains("2024")
            "2023" -> item.releaseDate.contains("2023")
            "Older" -> {
                val yearRegex = "\\d{4}".toRegex()
                val itemYear = yearRegex.find(item.releaseDate)?.value?.toIntOrNull() ?: 2024
                itemYear < 2023
            }
            else -> true
        }
        val matchesRating = item.rating >= selectedMinRating
        val matchesRuntime = if (selectedMaxRuntime == 999) {
            true
        } else {
            val mins = parseRuntimeToMinutes(item.runtime)
            mins <= selectedMaxRuntime
        }
        matchesText && matchesGenre && matchesType && matchesYear && matchesRating && matchesRuntime
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Search bar input with Advanced Filter Toggle Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F0F))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { viewModel.updateSearchText(it) },
                placeholder = { Text("Search title, genres, or actors...", color = TextSecondaryDark, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MovieRed) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchText("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear text", tint = TextSecondaryDark)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MovieRed,
                    unfocusedBorderColor = Color(0xFF262626)
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input")
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .background(if (showFilters) MovieRed else Color(0xFF1B1B1B), RoundedCornerShape(10.dp))
                    .size(48.dp)
                    .testTag("filter_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Toggle Fine-Tuning Filters",
                    tint = Color.White
                )
            }
        }

        // Expandable Smooth Advanced fine-tuning filters segment
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C0C0C))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ADVANCED FINE-TUNING",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Reset",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color(0xFF1B1B1B), RoundedCornerShape(4.dp))
                            .clickable { viewModel.resetFilters() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)

                // Year Filter Row
                Column {
                    Text("Release Year", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val years = listOf("All", "2024", "2023", "Older")
                        years.forEach { year ->
                            val isSelected = selectedYear == year
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) MovieRed else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectYear(year) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (year == "Older") "2022 & Older" else year,
                                    color = if (isSelected) Color.White else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Minimum Rating Filter Row
                Column {
                    Text("Minimum Rating", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ratings = listOf(Pair("All", 0.0), Pair("★ 8.0+", 8.0), Pair("★ 8.5+", 8.5), Pair("★ 9.0+", 9.0))
                        ratings.forEach { rPair ->
                            val isSelected = selectedMinRating == rPair.second
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) MovieRed else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectMinRating(rPair.second) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = rPair.first,
                                    color = if (isSelected) Color.White else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Maximum Runtime Filter Row
                Column {
                    Text("Maximum Runtime Length", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val runtimes = listOf(Pair("Any Length", 999), Pair("< 2 Hours", 120), Pair("< 2.5 Hours", 150), Pair("< 3 Hours", 180))
                        runtimes.forEach { rPair ->
                            val isSelected = selectedMaxRuntime == rPair.second
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) MovieRed else Color(0xFF161616), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectMaxRuntime(rPair.second) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = rPair.first,
                                    color = if (isSelected) Color.White else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Category Selectors list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                FilterChip(
                    selected = selectedGenre == genre,
                    onClick = { viewModel.selectGenre(genre) },
                    label = { Text(genre, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = Color.White,
                        selectedContainerColor = MovieRed,
                        labelColor = TextSecondaryDark,
                        containerColor = Color(0xFF1B1B1B)
                    ),
                    border = null
                )
            }
        }

        // Segment Type Filter Switches (All, Movies, TV)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val typesList = listOf("All", "Movies", "TV Shows")
            typesList.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) MovieRed else Color(0xFF161616), RoundedCornerShape(6.dp))
                        .clickable { viewModel.selectType(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type,
                        color = if (isSelected) Color.White else TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Results panel
        if (filteredCatalog.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SentimentDissatisfied, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No results fit your filters.", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Try typing something else or clear chips.", color = TextSecondaryDark, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            Text(
                text = "Discover ${filteredCatalog.size} matches",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            LazyVerticalGrid(
                items = filteredCatalog,
                onMediaSelect = { viewModel.showMediaDetails(it) }
            )
        }
    }
}

// 8. Profile panel displaying credentials, watchlist, viewing history, and download manager
@Composable
fun ProfileTab(viewModel: AppViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val viewingHistory by viewModel.viewingHistory.collectAsState()
    val downloads by viewModel.downloads.collectAsState()
    val themePreference by viewModel.isDarkThemePreference.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    var activeSubTab by remember { mutableStateOf("Watchlist") } // "Watchlist", "Downloads", "History", "Settings"
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // High fidelity luxury banner overlay
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0F0F))
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Profile Glowing outline
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .border(BorderStroke(2.dp, AccentGold), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MovieRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = userEmail,
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dynamic profile sub navigation tabs: Watchlist, Active Downloads, History, App Settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141414))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val subTabs = listOf("Watchlist", "Downloads", "History", "Config")
                subTabs.forEach { tab ->
                    val isSelected = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .clickable { activeSubTab = tab }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .drawBehind {
                                if (isSelected) {
                                    drawRect(
                                        color = MovieRed,
                                        topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 4.dp.toPx()),
                                        size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                                    )
                                }
                            }
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sub tab details
        when (activeSubTab) {
            "Watchlist" -> {
                if (watchlist.isEmpty()) {
                    item {
                        EmptyPanePlaceholder(
                            icon = Icons.Default.BookmarkBorder,
                            title = "Watchlist is Empty",
                            sub = "Explore movies and click plus bookmark to add content."
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    val rows = watchlist.chunked(2)
                    items(rows) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    MediaCoverCardWithRatingLabel(item = item, onClick = { viewModel.showMediaDetails(item) })
                                }
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            "Downloads" -> {
                if (downloads.isEmpty()) {
                    item {
                        EmptyPanePlaceholder(
                            icon = Icons.Default.CloudDownload,
                            title = "No downloads found",
                            sub = "Offline items you've saved will display securely here."
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Offline Media (Simulated Storage)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    items(downloads) { media ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = media.posterUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp, 75.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(media.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (media.downloadState == "COMPLETED") "Offline Access Ready" else "Downloading Content",
                                    color = if (media.downloadState == "COMPLETED") AccentGold else TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (media.downloadState == "DOWNLOADING") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LinearProgressIndicator(
                                            progress = media.downloadProgress / 100f,
                                            color = MovieRed,
                                            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("${media.downloadProgress}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Play Completed local file
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.playMedia(media) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MovieRed),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Play Local File", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.cancelOrRemoveDownload(media.id) },
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(1.dp, Color.Gray.copy(0.3f)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Delete", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                            if (media.downloadState == "DOWNLOADING") {
                                IconButton(onClick = { viewModel.cancelOrRemoveDownload(media.id) }) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Cancel Download", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            "History" -> {
                if (viewingHistory.isEmpty()) {
                    item {
                        EmptyPanePlaceholder(
                            icon = Icons.Default.History,
                            title = "History is clear",
                            sub = "Play standard video items to review your activity history."
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Playback Activity Log", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    items(viewingHistory) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .background(Color(0xFF141414), RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MovieRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (item.mediaType == "tv") "Watched episode: ${item.lastWatchedEpisode ?: "S1 E1"}" else "Watched Movie Feature",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .background(MovieRed.copy(0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${((item.continueProgress ?: 0.0f) * 100).toInt()}% Done",
                                    color = MovieRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            "Config" -> {
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Application Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Dark mode toggle simulating user preferences
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Aesthetic Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Force Premium Dark Mode", color = TextSecondaryDark, fontSize = 11.sp)
                            }
                            Switch(
                                checked = themePreference,
                                onCheckedChange = { viewModel.toggleTheme() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MovieRed, checkedTrackColor = MovieRed.copy(0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Smart push notifications toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("New Release Alerts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Notify instantly about trending cinema additions", color = TextSecondaryDark, fontSize = 11.sp)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { viewModel.toggleNotifications() },
                                colors = SwitchDefaults.colors(checkedThumbColor = MovieRed, checkedTrackColor = MovieRed.copy(0.5f))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // App Version and Platform attributes info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181818), RoundedCornerShape(8.dp))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("GVMovies Build", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Jetpack Compose v1.0 • Client Release", color = TextSecondaryDark, fontSize = 11.sp)
                            }
                            Text("100% Secure", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 9. Simple Empty Pane visual helper
@Composable
fun EmptyPanePlaceholder(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Videocam,
    title: String,
    sub: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector ?: icon, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(sub, color = TextSecondaryDark, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, start = 30.dp, end = 30.dp), textAlign = TextAlign.Center)
        }
    }
}

// 10. Complex Media Detail Panel Screen (with specific TV episodic selects if appropriate)
@Composable
fun MediaDetailScreen(
    media: MediaEntity,
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val isFav = media.isFavorite
    val isInWatch = media.isInWatchlist
    val scope = rememberCoroutineScope()
    var selectedSeason by remember { mutableStateOf(1) }

    val allMediaLocal by viewModel.allMedia.collectAsState()
    val reactiveMedia = allMediaLocal.firstOrNull { it.id == media.id } ?: media

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFA050505))
            .clickable(enabled = true, onClick = {}) // block taps leaking under
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    AsyncImage(
                        model = reactiveMedia.backdropUrl,
                        contentDescription = "Backdrop picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Bottom Darkening Fade out
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xBB050505), Color(0xFA050505)),
                                    startY = 100f
                                )
                            )
                    )

                    // Back Button top left
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(14.dp)
                            .background(Color.Black.copy(0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back icon", tint = Color.White)
                    }
                }
            }

            // Cinematic Info Block
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = reactiveMedia.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(AccentGold.copy(0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("★ ${reactiveMedia.rating}", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(reactiveMedia.releaseDate, color = TextSecondaryDark, fontSize = 12.sp)
                        Text("•", color = Color.Gray)
                        Text(reactiveMedia.runtime, color = TextSecondaryDark, fontSize = 12.sp)
                        Text("•", color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = reactiveMedia.mediaType.uppercase(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media Action Control Pill row (Play, Watchlist, Favorite, Download)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val epCode = if (reactiveMedia.mediaType == "tv") "S${selectedSeason} E1" else null
                                viewModel.playMedia(reactiveMedia, epCode)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MovieRed),
                            modifier = Modifier.weight(1.3f).height(46.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Content", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        // Bookmark Favorite Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite(reactiveMedia.id) },
                            modifier = Modifier
                                .background(Color(0xFF1F1F1F), RoundedCornerShape(6.dp))
                                .size(46.dp)
                                .testTag("detail_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (reactiveMedia.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Add Favorite",
                                tint = if (reactiveMedia.isFavorite) MovieRed else Color.White
                            )
                        }

                        // Playlist Bookmark
                        IconButton(
                            onClick = { viewModel.toggleWatchlist(reactiveMedia.id) },
                            modifier = Modifier
                                .background(Color(0xFF1F1F1F), RoundedCornerShape(6.dp))
                                .size(46.dp)
                        ) {
                            Icon(
                                imageVector = if (reactiveMedia.isInWatchlist) Icons.Filled.AddTask else Icons.Outlined.Add,
                                contentDescription = "Add Watchlist",
                                tint = if (reactiveMedia.isInWatchlist) AccentGold else Color.White
                            )
                        }

                        // Secure Download Manager Trigger
                        IconButton(
                            onClick = {
                                if (reactiveMedia.downloadState == "NONE") {
                                    viewModel.downloadMedia(reactiveMedia.id)
                                } else {
                                    viewModel.cancelOrRemoveDownload(reactiveMedia.id)
                                }
                            },
                            modifier = Modifier
                                .background(Color(0xFF1F1F1F), RoundedCornerShape(6.dp))
                                .size(46.dp)
                        ) {
                            Icon(
                                imageVector = when (reactiveMedia.downloadState) {
                                    "DOWNLOADING" -> Icons.Default.HourglassEmpty
                                    "COMPLETED" -> Icons.Default.FileDownloadDone
                                    else -> Icons.Default.CloudDownload
                                },
                                contentDescription = "Download Item",
                                tint = when (reactiveMedia.downloadState) {
                                    "DOWNLOADING" -> MovieRed
                                    "COMPLETED" -> AccentGold
                                    else -> Color.White
                                }
                            )
                        }
                    }

                    // Download active status bar helper
                    if (reactiveMedia.downloadState == "DOWNLOADING") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MovieRed.copy(0.1f), RoundedCornerShape(4.dp)).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = MovieRed, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Downloading offline licensing package... ${reactiveMedia.downloadProgress}%", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Synopsis
                    Text("Synopsis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = reactiveMedia.synopsis,
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                    )

                    // Cast & Crew
                    Text("Top-Billed Cast", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = reactiveMedia.cast,
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // Media Genres list tags
                    Text("Genres", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reactiveMedia.genres.split(",").forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF262626), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(genre.trim(), color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // TV show Episodial content if media type is tv
                    if (reactiveMedia.mediaType == "tv") {
                        Divider(color = Color.Gray.copy(0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Season Selector dropdown trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seasons & Episodes", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    border = BorderStroke(1.dp, MovieRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Season $selectedSeason", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MovieRed)
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF141414))
                                ) {
                                    (1..3).forEach { sNum ->
                                        DropdownMenuItem(
                                            text = { Text("Season $sNum", color = Color.White) },
                                            onClick = {
                                                selectedSeason = sNum
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Render beautiful mock episodes list
                        val mockEpisodesList = getMockEpisodes(reactiveMedia.id, selectedSeason)
                        mockEpisodesList.forEach { ep ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color(0xFF111111), RoundedCornerShape(6.dp))
                                    .clickable { viewModel.playMedia(reactiveMedia, ep.code) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 60.dp)
                                        .background(Color(0xFF1C1C1C), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayCircle, contentDescription = "Play icon", tint = MovieRed, modifier = Modifier.size(30.dp))
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${ep.code} • ${ep.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(ep.duration, color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(ep.description, color = TextSecondaryDark, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

// TV mock episodes definition helpers
data class MockEpisode(val code: String, val title: String, val duration: String, val description: String)
fun getMockEpisodes(mediaId: String, season: Int): List<MockEpisode> {
    return when(mediaId) {
        "tv_1" -> listOf(
            MockEpisode("S${season} E1", "When You're Lost in the Darkness", "1h 21m", "Twenty years after outbreak day, Joel is offered an escape route in exchange for smuggling fourteen-year-old Ellie across the country."),
            MockEpisode("S${season} E2", "Infected", "55m", "Joel and Tess explore the cordyceps mutated underground lines while escorting Ellie past dense waves of aggressive infected."),
            MockEpisode("S${season} E3", "Long, Long Time", "1h 15m", "A cinematic tale of long-term isolation, survival, and deep humanity in the countryside with Bill and Frank.")
        )
        "tv_2" -> listOf(
            MockEpisode("S${season} E1", "The Vanishing of Will Byers", "48m", "On his way home, young Will Byers encounters an otherworldly predator. Deep town secrets begin unraveling."),
            MockEpisode("S${season} E2", "The Weirdo on Maple Street", "55m", "Mike hides a bizarre telekinetic girl in his basement, while police chief Hopper pursues his investigation of Hawkins laboratory."),
            MockEpisode("S${season} E3", "Holly, Jolly", "51m", "Joyce becomes convinced Will is communicating via colorful Christmas lights, while Jonathan discovers secret photographs.")
        )
        else -> listOf(
            MockEpisode("S${season} E1", "Pilots & Prophecies", "45m", "An introduction into royal conflict and deep power struggles set inside an expansive dark fantasy realm."),
            MockEpisode("S${season} E2", "Clashing Regimes", "50m", "Tensions flare up as political heirs outline their claims and declare absolute blockades on trading lines."),
            MockEpisode("S${season} E3", "Battle of Fire", "55m", "An immersive fiery clash of empires that tests blood bonds and reshapes ancient castles.")
        )
    }
}

// 11. Premium Immersive Media Player Overlay
@Composable
fun CinemaPlayerScreen(
    media: MediaEntity,
    viewModel: AppViewModel
) {
    val progress by viewModel.playbackProgressValue.collectAsState()
    val isPlaying by viewModel.isPlayingVideo.collectAsState()
    val episodeCode by viewModel.activePlayingEpisode.collectAsState()
    val scope = rememberCoroutineScope()

    // Simulate playback tick increment
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (viewModel.isPlayingVideo.value) {
                delay(1000)
                val currentP = viewModel.playbackProgressValue.value
                if (currentP < 1.0f) {
                    viewModel.updatePlaybackProgress(currentP + 0.01f)
                } else {
                    viewModel.updatePlaybackProgress(0.0f)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                // Click to toggle quick controls display
            }
    ) {
        // High immersive backdrop
        AsyncImage(
            model = media.backdropUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.45f
        )

        // Backdrop glowing black mask to establish video playback focus
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.7f), Color.Transparent, Color.Black.copy(0.9f))
                    )
                )
        )

        // Close/Minimize icon top left
        IconButton(
            onClick = { viewModel.stopPlayback() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp)
                .background(Color.Black.copy(0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Minimize Player", tint = Color.White)
        }

        // Animated Loader circle simulator to denote buffering
        val infiniteTransition = rememberInfiniteTransition(label = "player_buffer")
        val rotateAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing)),
            label = "spin"
        )

        if (isPlaying) {
            // Tiny buffer spinner overlaying center background
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
            ) {
                CircularProgressIndicator(
                    color = MovieRed.copy(0.3f),
                    strokeWidth = 3.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Icon(
                imageVector = Icons.Filled.PauseCircleFilled,
                contentDescription = "Paused Icon Graphic",
                tint = MovieRed.copy(0.7f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
            )
        }

        // Bottom Controls interface
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(MovieRed, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE STREAM", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = media.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!episodeCode.isNullOrEmpty()) {
                    Text(
                        text = " • $episodeCode",
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action ticks row (+10s, Play/Pause, -10s)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek back 10s
                IconButton(
                    onClick = {
                        val newP = (progress - 0.05f).coerceAtLeast(0.0f)
                        viewModel.updatePlaybackProgress(newP)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Play / Pause core toggle
                IconButton(
                    onClick = { viewModel.togglePlayPauseVideo() },
                    modifier = Modifier
                        .size(64.dp)
                        .background(MovieRed, CircleShape)
                        .testTag("player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Seek forward 10s
                IconButton(
                    onClick = {
                        val newP = (progress + 0.05f).coerceAtMost(1.0f)
                        viewModel.updatePlaybackProgress(newP)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Forward10, contentDescription = "Forward 10", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seek progress bar slider element
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elapsed time simulation
                val elapsedMinutes = ((progress * 132)).toInt()
                val totalMinutes = 132
                Text(
                    text = "${elapsedMinutes / 60}h ${elapsedMinutes % 60}m",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = progress,
                    onValueChange = { viewModel.updatePlaybackProgress(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = MovieRed,
                        activeTrackColor = MovieRed,
                        inactiveTrackColor = Color.Gray.copy(0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )

                Text(
                    text = "2h 12m",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
