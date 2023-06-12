package com.example.recyc.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    /**
     * Dynamic Colors are supported on API level 31 and above
     * */
    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isDarkTheme -> {
            dynamicDarkColorScheme(LocalContext.current)
        }

        dynamicColor && !isDarkTheme -> {
            dynamicLightColorScheme(LocalContext.current)
        }

        isDarkTheme -> DarkThemeColors
        else -> LightThemeColors
    }

    // Make use of Material3 imports
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
    val systemClor =
        if (isDarkTheme) dynamicDarkColorScheme(LocalContext.current).surface
        else dynamicDarkColorScheme(LocalContext.current).surface
    systemUiController.setSystemBarsColor(color = systemClor)
}

private val LightThemeColors = lightColorScheme(
//    primary = md_theme_light_primary, // and 20+ more color schemes
)
private val DarkThemeColors = lightColorScheme(
//    primary = md_theme_light_primary, // and 20+ more color schemes
)