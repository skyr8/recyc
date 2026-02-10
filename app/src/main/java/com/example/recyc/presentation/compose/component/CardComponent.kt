package com.example.recyc.presentation.compose.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recyc.R
import com.example.recyc.data.model.CheckedState
import com.example.recyc.data.model.DayEnum
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RecyclingCard(
    recyclingDay: RecyclingDayModel,
    isCurrentDay: Boolean = false,
    onClick: (Int) -> Unit = {},
    checkedState: CheckedState = CheckedState.UNCHECKED,
    onConfirmClick: (CheckedState) -> Unit = {},
) {
    val cardColor =
        if (isCurrentDay) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val iconColor =
        if (isCurrentDay) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline

    val typeColor =
        if (isCurrentDay) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface

    val cardModifier = if (isCurrentDay) {
        Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(percent = 10))
    } else {
        Modifier.fillMaxWidth()
    }
    val scope = rememberCoroutineScope()
    val swipeOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    var iconRowX by remember { mutableStateOf(Float.NaN) }
    var trashX by remember { mutableStateOf(Float.NaN) }
    var maxDragPx by remember { mutableStateOf(0f) }

    LaunchedEffect(iconRowX, trashX) {
        if (!iconRowX.isNaN() && !trashX.isNaN()) {
            maxDragPx = (trashX - iconRowX).coerceAtLeast(0f)
            swipeOffsetX.snapTo(swipeOffsetX.value.coerceIn(0f, maxDragPx))
        }
    }

    val cardOnClick = {
        if (!isDragging) onClick(recyclingDay.id)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(percent = 10),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            contentColor = cardColor,
            containerColor = cardColor
        ),
        onClick = cardOnClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Label(text = recyclingDay.day.name)
                AnimatedContent(targetState = recyclingDay.type, transitionSpec = {
                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn(
                        animationSpec = tween(
                            220,
                            delayMillis = 90
                        )
                    ) with
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut(
                        animationSpec = tween(
                            90
                        )
                    )
                }) { target ->
                    Label(
                        text = target.joinToString("•"),
                        style = TextStyle(fontSize = 16.sp),
                        color = typeColor
                    )
                }
            }
            Margin(8)
            Row(modifier = Modifier.weight(1f)) {
                OffsetPxLayout(
                    offsetXPx = swipeOffsetX.value,
                    modifier = Modifier
                        .testTag("icon_row")
                        .onGloballyPositioned { coords ->
                            iconRowX = coords.localToRoot(Offset.Zero).x
                        }
                        .draggable(
                            enabled = isCurrentDay && maxDragPx > 0f,
                            orientation = Orientation.Horizontal,
                            onDragStarted = { isDragging = true },
                            onDragStopped = {
                                isDragging = false
                                scope.launch {
                                    val threshold = maxDragPx * 0.8f
                                    if (swipeOffsetX.value >= threshold) {
                                        swipeOffsetX.animateTo(
                                            maxDragPx,
                                            animationSpec = tween(120)
                                        )
                                        onConfirmClick(CheckedState.CONFIRM)
                                        delay(200)
                                        swipeOffsetX.animateTo(0f, animationSpec = tween(200))
                                    } else {
                                        swipeOffsetX.animateTo(0f, animationSpec = tween(180))
                                    }
                                }
                            },
                            state = remember {
                                androidx.compose.foundation.gestures.DraggableState { delta ->
                                    scope.launch {
                                        val newValue =
                                            (swipeOffsetX.value + delta).coerceIn(0f, maxDragPx)
                                        swipeOffsetX.snapTo(newValue)
                                    }
                                }
                            }
                        ),
                ) {
                    if (checkedState != CheckedState.CONFIRM || !isCurrentDay) {
                        recyclingDay.type.forEach {
                            AnimatedContent(targetState = it.toIcon()) { icon ->
                                Image(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).alpha(0.9f),
                                    colorFilter = ColorFilter.tint(iconColor)
                                )
                            }
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row {
                    if (isCurrentDay) {
                        Icon(
                            modifier = Modifier
                                .padding(1.dp)
                                .height(40.dp)
                                .width(28.dp)
                                .alpha(if(checkedState == CheckedState.CONFIRM) 0.7f else 1f)
                                .testTag("trash_icon")
                                .onGloballyPositioned { coords ->
                                    trashX = coords.localToRoot(Offset.Zero).x
                                },
                            imageVector = ImageVector.vectorResource(id = R.drawable.trash),
                            contentDescription = null,
                            tint = if(checkedState == CheckedState.CONFIRM) MaterialTheme.colorScheme.secondary else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isCurrentDay) {
                        AnimatedContent(targetState = CheckedState.CONFIRM) {
                            StateButton(checkedState, onClick = onConfirmClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateButton(checkedState: CheckedState, onClick: (CheckedState) -> Unit = {}){
    val stateIcon: Pair<ImageVector?, Color> = when(checkedState){
        CheckedState.CONFIRM -> Icons.Default.Check to Color.Green
        CheckedState.SKIP -> Icons.Default.Close to Color.Red
        CheckedState.UNCHECKED -> Icons.Default.Check to Color.Gray
    }

    Column(
        modifier = Modifier.height(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val newSate = when(checkedState){
            CheckedState.CONFIRM -> CheckedState.UNCHECKED
            CheckedState.SKIP -> CheckedState.CONFIRM
            CheckedState.UNCHECKED -> CheckedState.CONFIRM
        }

        Box(
            modifier = Modifier
                .border(
                    1.5.dp,
                    stateIcon.second,
                    RoundedCornerShape(50)
                )
                .clickable(onClick = { onClick(newSate) })
        ) {
            stateIcon.first?.let {
                Icon(
                    modifier = Modifier.padding(1.dp),
                    imageVector = it,
                    contentDescription = null,
                    tint = if(checkedState == CheckedState.UNCHECKED) Color.Transparent else stateIcon.second
                )
            }
        }
    }
}

@Composable
private fun OffsetPxLayout(
    offsetXPx: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables: List<androidx.compose.ui.layout.Measurable>, constraints ->
        val placeables: List<Placeable> = measurables.map { m: androidx.compose.ui.layout.Measurable ->
            m.measure(constraints)
        }
        val width = placeables.maxOfOrNull { p: Placeable -> p.width } ?: constraints.minWidth
        val height = placeables.maxOfOrNull { p: Placeable -> p.height } ?: constraints.minHeight
        layout(width, height) {
            val dx = offsetXPx.toInt()
            placeables.forEach { p: Placeable ->
                p.placeRelative(dx, 0)
            }
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
        id = 0,
    )
    RecyclingCard(recyclingDay = data, isCurrentDay = true, checkedState = CheckedState.UNCHECKED)
}

@Preview
@Composable
private fun RecyclingCardCurrentDayPreviewSkip() {
    val data = RecyclingDayModel(
        hour = "20 - 22",
        type = listOf(RecyclingType.ORGANIC, RecyclingType.GLASS),
        day = DayEnum.MONDAY,
        id = 0,
    )
    RecyclingCard(recyclingDay = data, isCurrentDay = true, checkedState = CheckedState.CONFIRM)
}

@Preview
@Composable
private fun RecyclingCardCurrentDayUncheck() {
    val data = RecyclingDayModel(
        hour = "20 - 22",
        type = listOf(RecyclingType.ORGANIC, RecyclingType.GLASS),
        day = DayEnum.MONDAY,
        id = 0,
    )
    RecyclingCard(recyclingDay = data, isCurrentDay = true, checkedState = CheckedState.SKIP)
}

@Preview
@Composable
private fun StateIconPreview(){
    Column {
        StateButton(checkedState = CheckedState.CONFIRM)
        Margin(margin = 8)
        StateButton(checkedState = CheckedState.SKIP)
        Margin(margin = 8)
        StateButton(checkedState = CheckedState.UNCHECKED)
    }
}