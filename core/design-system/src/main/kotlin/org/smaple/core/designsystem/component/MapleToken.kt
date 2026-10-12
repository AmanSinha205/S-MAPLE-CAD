package org.smaple.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.smaple.core.designsystem.theme.Ink
import org.smaple.core.designsystem.theme.MapleDimensions
import org.smaple.core.designsystem.theme.MapleSpacing
import org.smaple.core.designsystem.theme.MapleTheme
import org.smaple.core.designsystem.theme.MapleTitle
import org.smaple.core.designsystem.theme.Mist2
import org.smaple.core.designsystem.theme.Pine
import org.smaple.core.designsystem.theme.White
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Token state representation per Section 4.1.
 */
enum class MapleTokenState(
    val stateWord: String,
    val description: String
) {
    Ready("Ready", "Waiting for a card or phone tap"),
    Working("Working", "Cards communicating, keep devices touching"),
    Done("Done", "Operation successfully completed"),
    Problem("Problem", "Operation encountered a problem, please try again")
}

/**
 * Creates the closed 5-lobed maple leaf path using the polar equation:
 * r(θ) = R · (1 + 0.085 · cos(5 · (θ + 90°)))
 * so that one lobe points vertically upright.
 */
private fun createMaplePath(
    center: Offset,
    radius: Float,
    steps: Int = 180
): Path {
    val path = Path()
    val angleStep = (2.0 * PI) / steps

    for (i in 0..steps) {
        val theta = i * angleStep
        // Shift by +90 degrees (pi/2) to point the top lobe upward
        val angleDeg = theta * (180.0 / PI)
        val r = radius * (1f + 0.085f * cos((5.0 * (angleDeg + 90.0) * (PI / 180.0)).toFloat()))

        val x = center.x + r * cos(theta).toFloat()
        val y = center.y + r * sin(theta).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

/**
 * S-MAPLE Token Component
 *
 * Implements the shared visual signature of the S-MAPLE ecosystem.
 *
 * @param state The current protocol state of the token ([MapleTokenState.Ready], [Working], [Done], [Problem]).
 * @param size The full canvas bounding box diameter (including ring expansion space).
 * @param enableAnimation Whether outward pulsing of rings is animated (respects system reduced motion).
 */
@Composable
fun MapleToken(
    state: MapleTokenState,
    modifier: Modifier = Modifier,
    size: Dp = MapleDimensions.tokenSizeHome,
    enableAnimation: Boolean = true
) {
    val isInspection = LocalInspectionMode.current
    val shouldAnimate = enableAnimation && !isInspection

    val colors = MapleTheme.extendedColors

    // Animated fill color crossfade (300 ms per spec)
    val targetFillColor = when (state) {
        MapleTokenState.Ready -> colors.pine
        MapleTokenState.Working -> colors.mapleAmber
        MapleTokenState.Done -> colors.pine
        MapleTokenState.Problem -> colors.mapleRed
    }
    val animatedFillColor by animateColorAsState(
        targetValue = targetFillColor,
        animationSpec = tween(durationMillis = 300),
        label = "TokenFillColor"
    )

    // Symbol tint color
    val symbolColor = when (state) {
        MapleTokenState.Ready -> White
        MapleTokenState.Working -> Ink
        MapleTokenState.Done -> White
        MapleTokenState.Problem -> White
    }

    // Rings pulse outward every 1.6 s while Ready or Working
    val infiniteTransition = rememberInfiniteTransition(label = "TokenRingPulse")
    val pulseProgress by if (shouldAnimate && (state == MapleTokenState.Ready || state == MapleTokenState.Working)) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "RingPulseProgress"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(animation = tween(1000)),
            label = "StaticRing"
        )
    }

    val ringColor = when (state) {
        MapleTokenState.Ready -> Mist2
        MapleTokenState.Working -> colors.mapleAmber
        else -> Color.Transparent
    }

    val iconSize = size * 0.28f

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = "${state.stateWord}: ${state.description}"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            // Lobe diameter is ~61% of total component size per Section 4.2
            val baseRadius = (size.toPx() / 2f) * 0.61f

            // Draw outer pulsing rings for Ready and Working
            if (state == MapleTokenState.Ready || state == MapleTokenState.Working) {
                val strokePx = MapleDimensions.tokenRingStroke.toPx()

                // Ring 1 (1.2x radius + pulse expansion)
                val scale1 = 1.2f + (0.08f * pulseProgress)
                val alpha1 = if (shouldAnimate) (1f - (0.4f * pulseProgress)) else 1.0f
                val path1 = createMaplePath(center, baseRadius * scale1)
                drawPath(
                    path = path1,
                    color = ringColor.copy(alpha = alpha1),
                    style = Stroke(width = strokePx)
                )

                // Ring 2 (1.4x radius + pulse expansion)
                val scale2 = 1.4f + (0.12f * pulseProgress)
                val alpha2 = if (shouldAnimate) (0.5f * (1f - pulseProgress)) else 0.5f
                val path2 = createMaplePath(center, baseRadius * scale2)
                drawPath(
                    path = path2,
                    color = ringColor.copy(alpha = alpha2),
                    style = Stroke(width = strokePx)
                )
            }

            // Draw main filled maple body
            val mainLobePath = createMaplePath(center, baseRadius)
            drawPath(
                path = mainLobePath,
                color = animatedFillColor
            )
        }

        // Inner status symbol
        when (state) {
            MapleTokenState.Ready, MapleTokenState.Working -> {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null, // decorative within token
                    tint = symbolColor,
                    modifier = Modifier.size(iconSize)
                )
            }
            MapleTokenState.Done -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = symbolColor,
                    modifier = Modifier.size(iconSize)
                )
            }
            MapleTokenState.Problem -> {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = symbolColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F6F1)
@Composable
private fun MapleTokenPreview() {
    MapleTheme {
        Column(
            modifier = Modifier.padding(MapleSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Token States", style = MapleTitle)
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MapleToken(state = MapleTokenState.Ready, size = 120.dp)
                    Text(text = "Ready")
                }
                Spacer(modifier = Modifier.width(MapleSpacing.medium))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MapleToken(state = MapleTokenState.Working, size = 120.dp)
                    Text(text = "Working")
                }
            }
            Spacer(modifier = Modifier.height(MapleSpacing.medium))
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MapleToken(state = MapleTokenState.Done, size = 120.dp)
                    Text(text = "Done")
                }
                Spacer(modifier = Modifier.width(MapleSpacing.medium))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MapleToken(state = MapleTokenState.Problem, size = 120.dp)
                    Text(text = "Problem")
                }
            }
        }
    }
}
