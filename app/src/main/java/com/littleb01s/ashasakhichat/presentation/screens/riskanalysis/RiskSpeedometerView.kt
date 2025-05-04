package com.littleb01s.ashasakhichat.presentation.screens.riskanalysis

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.github.anastr.speedviewlib.SpeedView
import androidx.compose.ui.platform.LocalContext
import com.github.anastr.speedviewlib.Gauge
import com.github.anastr.speedviewlib.Speedometer
import com.github.anastr.speedviewlib.components.Section
import android.graphics.Color as AndroidColor

@Composable
fun RiskSpeedometerView(
    riskLevel: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val riskText = when (riskLevel?.lowercase()) {
        "low risk" -> "LOW RISK"
        "mid risk" -> "MID RISK"
        "high risk" -> "HIGH RISK"
        else -> "NOT ASSESSED"
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx: Context ->
            SpeedView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                minSpeed = 0f
                maxSpeed = 100f
                withTremble = false
                speedometerMode= Speedometer.Mode.TOP
                speedTextPosition= Gauge.Position.CENTER

                speedTextTypeface= Typeface.DEFAULT_BOLD

                clearSections()
                addSections(Section(0f, 0.33f, AndroidColor.GREEN,speedometerWidth), Section(0.33f, 0.66f, AndroidColor.YELLOW,speedometerWidth), Section(0.66f, 1f, AndroidColor.RED,speedometerWidth))
                unitUnderSpeedText=false
                unit=""

                speedTextListener = { speed ->
                    when {
                        speed == 0f -> "NOT ASSESSED"
                        speed < 33.33f -> "LOW RISK"
                        speed < 66.66f -> "MID RISK"
                        speed <= 100f -> "HIGH RISK"
                        else -> "NOT ASSESSED"
                    }
                }

                speedTo(
                    when (riskLevel?.lowercase()) {
                        "low risk" -> 16.7f
                        "mid risk" -> 50f
                        "high risk" -> 83.3f
                        else -> 0f
                    },
                    1200
                )
            }
        },
        update = { speedView ->
            speedView.speedTo(
                when (riskLevel?.lowercase()) {
                    "low risk" -> 16.7f
                    "mid risk" -> 50f
                    "high risk" -> 83.3f
                    else -> 0f
                },
                1200
            )
        }
    )
} 