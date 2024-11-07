package com.example.recyc.presentation.compose.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SwitchRecyc(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val hapticFeedback = LocalHapticFeedback.current

    val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier.weight(1f),
            color = textColor
        )
        Spacer(modifier = Modifier.size(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = { check ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(check)
            },
            enabled = enabled
        )
    }
}