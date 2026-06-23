package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.AppViewModel
import com.example.ui.GVMoviesApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    // Instantiate our unified state-engine
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Match immersive Material edge-to-edge guideline
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme by appViewModel.isDarkThemePreference.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    GVMoviesApp(
                        viewModel = appViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
