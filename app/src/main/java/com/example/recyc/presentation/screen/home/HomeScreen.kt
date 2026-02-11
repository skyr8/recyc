package com.example.recyc.presentation.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recyc.R
import com.example.recyc.SharedViewModel
import com.example.recyc.data.model.CheckedState
import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.toCheckState
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.presentation.compose.component.Label
import com.example.recyc.presentation.compose.component.Margin
import com.example.recyc.presentation.compose.component.RecyclingCard
import com.example.recyc.presentation.theme.AppTheme
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson

@Composable
fun HomeScreen(
    viewModel: HomeViewModel? = hiltViewModel(),
    sharedViewModel: SharedViewModel,
    onItemClick: (Int) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val recyclingState = viewModel?.recyclingDays?.observeAsState()?.value
    val sharedState = sharedViewModel.confirmationState.observeAsState().value

    val days = recyclingState?.recyclingDays ?: emptyList()
    val currentDay = recyclingState?.currentDay
    val isLoading = recyclingState?.isLoading ?: true
    val isCurrentDayConfirmed = recyclingState?.isCurrentDayConfirmed ?: false
    val isCurrentDaySkipped = recyclingState?.isCurrentDaySkipped ?: false

    val currentModel = days.find { it.day == currentDay }
    if (sharedState == true) {
        viewModel?.refresh()
    }

    LaunchedEffect(currentModel?.id) {
        val recyclerJson = Gson().toJson(currentModel?.copy(isDone = isCurrentDayConfirmed))
        updateWidget(recyclerJson, context)
    }

    RecyclingScreenContent(
        days = days,
        currentDay = currentDay,
        isLoading = isLoading,
        currentModel = currentModel,
        onItemClick = onItemClick,
        isCurrentDayConfirmed = isCurrentDayConfirmed,
        onSettingsClick = onSettingsClick,
        isCurrentDaySkipped = isCurrentDaySkipped,
        onConfirmClick = { viewModel?.setCurrentDayConfirmation(it) }
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RecyclingScreenContent(
    days: List<RecyclingDayModel>,
    currentDay: DayEnum?,
    isLoading: Boolean,
    currentModel: RecyclingDayModel?,
    onItemClick: (Int) -> Unit,
    isCurrentDayConfirmed: Boolean,
    isCurrentDaySkipped: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onConfirmClick: (CheckedState) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { onSettingsClick() }
                        .size(40.dp)
                        .padding(6.dp)
                )
            }
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    currentModel?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentDayConfirmed) {
                                Image(
                                    painter = painterResource(R.drawable.stamp),
                                    contentDescription = "stamp",
                                    modifier = Modifier.size(180.dp)
                                )
                                Text(
                                    "Done",
                                    style = TextStyle(
                                        fontSize = 42.sp,
                                        fontFamily = FontFamily(
                                            Font(R.font.stamp)
                                        )
                                    ),
                                    color = Color.Green,
                                    modifier = Modifier
                                        .rotate(-16f)
                                        .alpha(0.7f)
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Row {
                                    Label(
                                        modifier = Modifier.weight(1f),
                                        text = "TODAY",
                                        style = TextStyle(fontSize = 18.sp),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Label(
                                        text = currentModel.type.joinToString("•"),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(fontSize = 20.sp),
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    currentModel.type.forEach {
                                        Image(
                                            painter = painterResource(it.toIcon()),
                                            contentDescription = null,
                                            modifier = Modifier.size(42.dp),
                                            colorFilter = ColorFilter
                                                .tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        content = {
                            items(days) {
                                val isCurrentDay = currentDay == it.day
                                RecyclingCard(
                                    recyclingDay = it,
                                    isCurrentDay = isCurrentDay,
                                    onClick = onItemClick,
                                    checkedState = toCheckState(
                                        isCurrentDayConfirmed,
                                        isCurrentDaySkipped
                                    ),
                                    onConfirmClick = onConfirmClick
                                )
                                Margin(margin = 8)
                            }
                        })
                }
            }
        }
    }
}

@Composable
@Preview
private fun RecyclingScreenPreview() {
    AppTheme(isDynamicColor = true) {
        RecyclingScreenContent(
            days = listOf(
                RecyclingDayModel(
                    id = 1,
                    day = DayEnum.MONDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 2,
                    day = DayEnum.TUESDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 3,
                    day = DayEnum.WEDNESDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 4,
                    day = DayEnum.THURSDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 5,
                    day = DayEnum.FRIDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 6,
                    day = DayEnum.SATURDAY,
                    type = listOf(),
                    hour = "10:00"
                ),
                RecyclingDayModel(
                    id = 7,
                    day = DayEnum.SUNDAY,
                    type = listOf(),
                    hour = "10:00"
                )
            ),
            currentDay = DayEnum.MONDAY,
            isLoading = false,
            currentModel = null,
            onItemClick = {},
            isCurrentDayConfirmed = true
        )
    }
}
