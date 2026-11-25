package com.toybox.tv.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.ui.theme.ToyboxColors

/**
 * Subtle top-left indicator shown only while a parent holds BACK for 2 seconds.
 */
@Composable
fun HoldBackIndicator(
    isHolding: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (!isHolding && progress <= 0f) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xAA1A237E), RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
            Canvas(modifier = Modifier.size(34.dp)) {
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx())
                )
                drawArc(
                    color = ToyboxColors.FocusGold,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Hold BACK for Toybox",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
