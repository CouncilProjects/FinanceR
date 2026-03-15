package com.afterdark.financer.ui.screens.graphs

import android.R.attr.textSize
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jaikeerthick.composable_graphs.composables.pie.PieChart
import com.jaikeerthick.composable_graphs.composables.pie.model.PieData
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint
import androidx.compose.foundation.layout.aspectRatio

@Composable
fun GraphsScreen(viewModel: GraphsViewModel = viewModel(factory = GraphsViewModel.FACTORY), modifier: Modifier= Modifier){
    val uiState by viewModel.uiState.collectAsState()
    LocalContext.current

    listOf(
        PieData(value = 130F, label = "HTC", color = Color.Green),
        PieData(value = 260F, label = "Apple", labelColor = Color.Blue),
        PieData(value = 500F, label = "Google"),
    )

    Column(
        Modifier.fillMaxSize()
    ) {
        when(uiState.categories){
            is UiState.Loading ->{
                CircularProgressIndicator()
            }

            is UiState.Error -> {
                Text(text=(uiState.categories as UiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            is UiState.Success -> {
                if(uiState.view== ViewTypes.BAR){
                    //maybe do a bar chart some time
                } else {

                    CustomPieChart(
                        data = (uiState.categories as UiState.Success).data
                            .filter { entry -> entry.currentExpense>0.0 }
                            .mapIndexed {index,entry ->
                                PieData(
                                    value = entry.currentExpense.toFloat(),
                                    label = entry.name,
                                    labelColor = MaterialTheme.colorScheme.onPrimary,
                                    color = Color.hsv((index * 45f) % 360f, 0.7f, 0.9f)
                                )
                            },
                        modifier = Modifier.fillMaxSize().aspectRatio(1f)
                    )
                }
            }
        }
    }
}

data class PieData(
    val value: Float,
    val label: String,
    val color: Color,
    val labelColor: Color = Color.White
)

@Composable
fun CustomPieChart(
    data: List<PieData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val density = LocalDensity.current
    val textPaint = Paint().apply {
        textSize = density.run { 14.sp.toPx() }
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        data.forEach { slice ->
            val sweep = 360 * (slice.value / total)

            // Draw slice
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )

            // Draw label
            val angleRad = Math.toRadians((startAngle + sweep / 2).toDouble())
            val labelRadius = radius * 0.6f
            val x = center.x + (labelRadius * cos(angleRad)).toFloat()
            val y = center.y + (labelRadius * sin(angleRad)).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = slice.labelColor!!.toArgb()
                drawText(slice.label ?: "", x, y, textPaint) // safe call
            }

            startAngle += sweep
        }
    }
}