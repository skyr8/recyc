package com.example.recyc.presentation.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieIcon(
    modifier: Modifier = Modifier,
    animationRes: Int,
    iterations: Int = Int.MAX_VALUE,
    tint: Color? = null,
    isPlaying: Boolean = false
) {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress = animateLottieCompositionAsState(
        composition = composition.value,
        isPlaying = isPlaying,
        iterations = iterations
    )

    val tintedModifier = tint?.let {
        modifier.drawWithContent {
            drawContent()
            drawRect(
                color = it,
                blendMode = BlendMode.SrcIn
            )
        }
    } ?: modifier

    LottieAnimation(
        composition = composition.value,
        progress = { progress.value },
        modifier = tintedModifier,
        contentScale = ContentScale.Crop,
    )
}
