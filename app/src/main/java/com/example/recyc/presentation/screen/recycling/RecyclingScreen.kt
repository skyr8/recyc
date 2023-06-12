package com.example.recyc.presentation.screen.recycling

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.presentation.compose.component.Label
import com.example.recyc.presentation.compose.component.Margin
import com.example.recyc.presentation.compose.component.RecyclingCard
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson

@Composable
fun RecyclingScreen(
    viewModel: RecyclingViewModel? = hiltViewModel(),
) {
    val context = LocalContext.current

    val recyclingState = viewModel?.recyclingDays?.observeAsState()?.value
    val days = recyclingState?.recyclingDays ?: emptyList()
    val currentDay = recyclingState?.currentDay
    val isLoading = recyclingState?.isLoading ?: true

    val currentModel = days.find { it.day == currentDay }

    LaunchedEffect(currentModel?.id) {
        val recyclerJson = Gson().toJson(currentModel)
        updateWidget(recyclerJson, context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                currentModel?.let {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Row() {
                            Label(
                                modifier = Modifier.weight(1f),
                                text = "TODAY",
                                style = TextStyle(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Label(
                                text = currentModel.hour,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = TextStyle(fontSize = 18.sp)
                            )
                        }
                        Margin(24)
                        Row {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Label(
                                        text = currentModel.type.joinToString("•"),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = TextStyle(fontSize = 20.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    currentModel.type.forEach {
                                        Image(
                                            painter = painterResource(it.toIcon()),
                                            contentDescription = null,
                                            modifier = Modifier.size(42.dp),
                                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                        )
                                        Margin(margin = 8)
                                    }
                                }
                            }

                        }
                    }
                }
                Margin(16)
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    content = {
                        items(days) {
                            Log.i(">>>current day", currentDay?.name.orEmpty())
                            Log.i(">>> day", it.day.name)
                            RecyclingCard(recyclingDay = it, isCurrentDay = currentDay == it.day)
                            Margin(margin = 8)
                        }
                    })
            }
        }
    }

}

@Composable
@Preview
private fun RecyclingScreenPreview() {
    RecyclingScreen(viewModel = null)
}
