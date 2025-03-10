package com.example.recyc.presentation.screen.edit

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.presentation.compose.component.RecyclingCard
import com.example.recyc.presentation.compose.component.SwitchRecyc
import com.example.recyc.presentation.theme.AppTheme

@Composable
fun DetailScreen(
    id: Int,
    onBackPressed: () -> Unit,
    onSaveChanges: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val detailState = viewModel.detailDays.observeAsState().value
    LaunchedEffect("") {
        viewModel.getDetail(id)
    }
    val day = detailState?.recyclingDayModel
    val isLoading = detailState?.isLoading ?: false
    val isCurrentDay = detailState?.isCurrentDay ?: false
    val isSkipDay = detailState?.isDaySkipped ?: false
    DetailScreenContent(
        dayModel = day, isLoading = isLoading,
        onDayUpdate = {
            viewModel.updateDay(it)

        },
        onBackPressed = onBackPressed,
        onSaveChanges = {
            viewModel.saveChanges()
            onSaveChanges()
        },
        isDayDone = detailState?.isCurrentDayConfirmed ?: false,
        onConfirmDayCheckChange = {
            viewModel.updateConfirmationDay(it)
        },
        isCurrentDay = isCurrentDay,
        isSkipDay = isSkipDay,
        onSkipDayCheckChange = {
            viewModel.updateSkipDay(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(
    dayModel: RecyclingDayModel?,
    isLoading: Boolean = false,
    onDayUpdate: (List<String>) -> Unit = {},
    onBackPressed: () -> Unit = {},
    onSaveChanges: () -> Unit = {},
    isDayDone: Boolean = false,
    onConfirmDayCheckChange: (Boolean) -> Unit = {},
    onSkipDayCheckChange: (Boolean) -> Unit = {},
    isCurrentDay: Boolean = false,
    isSkipDay: Boolean = false,
) {
    val context = LocalContext.current

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "",
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        onBackPressed()
                    }
            )
            Text(
                text = dayModel?.day?.name.orEmpty().lowercase(),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(16.dp)
            )
        }

    }, bottomBar = {
        Row {
            Button(
                onClick = onSaveChanges, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Save", style = MaterialTheme.typography.titleMedium)
            }
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                dayModel?.let {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RecyclingCard(
                            recyclingDay = it,
                            isCurrentDay = false
                        )
                        Spacer(modifier = Modifier.size(24.dp))
                        ChipGroup(
                            items = RecyclingType.values().map { it.name },
                            selectedItems = dayModel.type.map { it.name }) {
                            onDayUpdate(it)
                        }
                        Spacer(modifier = Modifier.size(24.dp))
                        if (isCurrentDay) {
                            SwitchRecyc(
                                text =
                                "Have you already taken out ${dayModel.type.joinToString("and ")}?",
                                isChecked = isDayDone,
                                onCheckedChange = onConfirmDayCheckChange,
                                enabled = !isSkipDay
                            )
                            SwitchRecyc(
                                text =
                                "Skip this day",
                                isChecked = isSkipDay,
                                onCheckedChange = onSkipDayCheckChange,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onSelectionChanged: (String) -> Unit
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable { onSelectionChanged(text) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = contentColor)
    }
}

@Composable
fun ChipGroup(
    items: List<String>,
    selectedItems: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    SubcomposeLayout { constraints ->
        val chipConstraints = constraints.copy(minWidth = 0)
        val rows = mutableListOf<List<MeasuredItem>>()
        var currentRow = mutableListOf<MeasuredItem>()
        var currentRowWidth = 0

        items.forEach { item ->
            val placeable = subcompose(item) {
                SelectableChip(
                    text = item,
                    isSelected = selectedItems.contains(item),
                    onSelectionChanged = {
                        val newSelection = if (selectedItems.contains(it)) {
                            selectedItems - it
                        } else {
                            selectedItems + it
                        }
                        onSelectionChanged(newSelection)
                    }
                )
            }.first().measure(chipConstraints)

            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }

            currentRow.add(MeasuredItem(item, placeable))
            currentRowWidth += placeable.width
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row -> row.maxOf { it.placeable.height } }

        layout(constraints.maxWidth, height) {
            var yOffset = 0

            rows.forEach { row ->
                var xOffset = 0
                row.forEach { measuredItem ->
                    measuredItem.placeable.placeRelative(x = xOffset, y = yOffset)
                    xOffset += measuredItem.placeable.width
                }
                yOffset += row.maxOf { it.placeable.height }
            }
        }
    }
}

data class MeasuredItem(val item: String, val placeable: Placeable)

@Composable
@Preview
fun ChipGroupPreview() {
    AppTheme {
        ChipGroup(
            items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 3336"),
            selectedItems = listOf("Item 1"),
            onSelectionChanged = {}
        )
    }
}

@Composable
@Preview
fun DetailScreenPreview() {
    AppTheme {
        DetailScreenContent(
            dayModel = RecyclingDayModel(
                id = 1,
                day = DayEnum.FRIDAY,
                type = listOf(RecyclingType.PLASTIC),
                hour = "10:00"
            ),
            isLoading = false
        )
    }
}

@Composable
@Preview
fun DetailScreenPreview_2() {
    AppTheme {
        DetailScreenContent(
            dayModel = RecyclingDayModel(
                id = 1,
                day = DayEnum.FRIDAY,
                type = listOf(RecyclingType.PLASTIC),
                hour = "10:00",
            ),
            isLoading = false,
            isCurrentDay = true,
            isSkipDay = true
        )
    }
}

