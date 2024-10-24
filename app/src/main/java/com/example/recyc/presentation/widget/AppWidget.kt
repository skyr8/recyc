package com.example.recyc.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.recyc.R
import com.example.recyc.domain.mapper.toIcon
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.presentation.MainActivity
import com.google.gson.Gson

class AppWidget : GlanceAppWidget() {

    companion object {
        private val SMALL_SQUARE = DpSize(80.dp, 40.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(120.dp, 90.dp)
        private val BIG_SQUARE = DpSize(160.dp, 160.dp)
        val radius = 32.dp
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, BIG_SQUARE)
    )


    override val stateDefinition = CustomGlanceStateDefinition

    @Composable
    override fun Content() {
        val state = currentState<Preferences>()
        val recyclingDayJson = state[stringPreferencesKey("recycle_data_key")] ?: ""
        val recyclingDayModel: RecyclingDayModel? =
            Gson().fromJson(recyclingDayJson, RecyclingDayModel::class.java)
        val context = LocalContext.current
        val size = LocalSize.current
        CompositionLocalProvider {
            when (size) {
                SMALL_SQUARE -> {
                    SmallWidget(
                        recyclingDayModel = recyclingDayModel,
                        context = context,
                        modifier = GlanceModifier.clickable(
                            onClick = actionStartActivity<MainActivity>()
                        )
                    )
                }

                HORIZONTAL_RECTANGLE -> {
                    MediumWidget(
                        recyclingDayModel = recyclingDayModel,
                        modifier = GlanceModifier.clickable(
                            onClick = actionStartActivity<MainActivity>()
                        ),
                        context = context
                    )
                }

                BIG_SQUARE -> LargeWidget(
                    recyclingDayModel = recyclingDayModel,
                    context = context,
                    modifier = GlanceModifier.clickable(
                        onClick = actionStartActivity<MainActivity>()
                    )
                )
            }

        }
    }
}


@Composable
private fun LargeWidget(
    recyclingDayModel: RecyclingDayModel?,
    context: Context,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(R.color.surface)
            .padding(16.dp).cornerRadius(AppWidget.radius),
    ) {
        Text(
            text = recyclingDayModel?.type?.joinToString("•").orEmpty(), // Header
            style = TextStyle(
                color = ColorProvider(R.color.text),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Row(
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            recyclingDayModel?.type?.forEach {
                val d = context.resources.getDrawable(it.toIcon(), null)
                val color = context.getColor(R.color.primary)
                d.setTint(color)
                val bm = d.toBitmap(width = 200, height = 200)
                val ip = ImageProvider(bm)
                Image(
                    provider = ip,
                    contentDescription = null,
                    modifier = GlanceModifier.size(68.dp),
                )
            }
        }
        Text(
            text = recyclingDayModel?.hour.orEmpty(), // Header
            style = TextStyle(
                color = ColorProvider(R.color.text_secondary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SmallWidget(
    recyclingDayModel: RecyclingDayModel?,
    context: Context,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(R.color.surface)
            .padding(16.dp).cornerRadius(AppWidget.radius),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Row {
            recyclingDayModel?.type?.forEach {
                val d = context.resources.getDrawable(it.toIcon(), null)
                val color = context.getColor(R.color.primary)
                d.setTint(color)
                val bm = d.toBitmap(width = 128, height = 128)
                val ip = ImageProvider(bm)
                Image(
                    provider = ip,
                    contentDescription = null,
                    modifier = GlanceModifier.size(40.dp),
                )
            }
        }
    }
}

@Composable
private fun MediumWidget(
    recyclingDayModel: RecyclingDayModel?,
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(R.color.surface)
            .padding(start = 16.dp)
            .padding(vertical = 16.dp)
            .padding(end = 8.dp)
            .cornerRadius(AppWidget.radius)
    ) {
        Text(
            text = recyclingDayModel?.type?.joinToString("-").orEmpty(),
            maxLines = 1,
            style = TextStyle(
                color = ColorProvider(R.color.text),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxWidth()
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(horizontalAlignment = Alignment.End, modifier = GlanceModifier.fillMaxWidth()) {
            recyclingDayModel?.type?.forEachIndexed { index, type ->
                val d = context.resources.getDrawable(type.toIcon(), null)
                val color = context.getColor(R.color.primary)
                d.setTint(color)
                val bm = d.toBitmap(width = 128, height = 128)
                val ip = ImageProvider(bm)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.size(28.dp).cornerRadius(28.dp)
                        .background(R.color.text_secondary),
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(R.color.accent_600)
                            .size(24.dp).cornerRadius(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ip,
                            contentDescription = null,
                            modifier = GlanceModifier.size(21.dp),
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.size(1.dp))
            }
        }
    }
}