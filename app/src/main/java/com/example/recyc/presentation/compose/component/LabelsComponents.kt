package com.example.recyc.presentation.compose.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Label(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = TextStyle(fontSize = 14.sp),
    maxLines: Int = Int.MAX_VALUE,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        style = style.copy(fontWeight = fontWeight),
        maxLines = maxLines,
    )
}