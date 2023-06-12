package com.example.recyc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.recyc.domain.usecase.RunPeriodicWorkUseCase
import com.example.recyc.presentation.screen.recycling.RecyclingScreen
import com.example.recyc.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var runPeriodicWorkUseCase: RunPeriodicWorkUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runPeriodicWorkUseCase()
        setContent {
            AppTheme(isDynamicColor = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                ) {
                    RecyclingScreen()
                }
            }
        }
    }
}
