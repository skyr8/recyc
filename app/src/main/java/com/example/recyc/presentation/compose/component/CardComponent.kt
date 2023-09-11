package com.example.recyc.presentation.compose.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel

@Composable
fun RecyclingCard(
    recyclingDay: RecyclingDayModel,
    isCurrentDay: Boolean = false
) {
    val cardColor =
        if (isCurrentDay) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val iconColor =
        if (isCurrentDay) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline

    val typeColor =
        if (isCurrentDay) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(percent = 10),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            contentColor = cardColor,
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Label(text = recyclingDay.day.name)
                Label(
                    text = recyclingDay.type.joinToString("•"),
                    style = TextStyle(fontSize = 16.sp),
                    color = typeColor
                )
            }
            Margin(8)
            Row(modifier = Modifier.weight(1f)) {
                recyclingDay.type.forEach {
                    Image(
                        painter = painterResource(it.toIcon()),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(iconColor)
                    )
                }
            }
            Label(text = recyclingDay.hour)
        }
    }
}

@Preview
@Composable
private fun RecyclingCardPreview() {
    val data = RecyclingDayModel(
        hour = "20 - 22",
        type = listOf(RecyclingType.ORGANIC),
        day = DayEnum.MONDAY,
        id = 0
    )
    RecyclingCard(recyclingDay = data)
}

@Preview
@Composable
private fun RecyclingCardCurrentDayPreview() {
    val data = RecyclingDayModel(
        hour = "20 - 22",
        type = listOf(RecyclingType.ORGANIC, RecyclingType.GLASS),
        day = DayEnum.MONDAY,
        id = 0
    )
    RecyclingCard(recyclingDay = data, isCurrentDay = true)
}