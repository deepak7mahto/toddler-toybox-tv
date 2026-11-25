package com.toybox.tv.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toybox.tv.audio.SoundEffect
import com.toybox.tv.audio.SoundEngine
import com.toybox.tv.audio.VoiceSpeaker
import com.toybox.tv.input.ToyboxKey
import com.toybox.tv.ui.components.ParticleShape
import com.toybox.tv.ui.components.ParticleSystem
import com.toybox.tv.ui.theme.TvOverscan
import kotlinx.coroutines.launch

enum class ToyVehicle(
    val displayName: String,
    val phrase: String,
    val sound: SoundEffect,
    val bodyColor: Color,
    val accentColor: Color
) {
    FIRE_TRUCK("Fire Truck", "Fire Truck! Wee woo!", SoundEffect.SIREN, Color(0xFFEF5350), Color(0xFFFFEE58)),
    SCHOOL_BUS("School Bus", "School Bus! Beep beep!", SoundEffect.HONK, Color(0xFFFFCA28), Color(0xFF263238)),
    TRAIN("Train", "Train! Choo choo!", SoundEffect.TRAIN, Color(0xFF42A5F5), Color(0xFFFF7043)),
    POLICE_CAR("Police Car", "Police Car! Wee woo!", SoundEffect.SIREN, Color(0xFF3949AB), Color(0xFF29B6F6)),
    ROCKET("Rocket", "Rocket! Blast off!", SoundEffect.ROCKET, Color(0xFFAB47BC), Color(0xFFFF7043)),
    TRACTOR("Tractor", "Tractor! Honk honk!", SoundEffect.HONK, Color(0xFF66BB6A), Color(0xFFFFD600))
}

@Composable
fun HonkAndGoScreen(
    lastKeyEvent: Pair<Int, ToyboxKey>?,
    soundEngine: SoundEngine,
    voiceSpeaker: VoiceSpeaker
) {
    val vehicles = ToyVehicle.entries
    var currentIndex by remember { mutableIntStateOf(0) }
    val hop = remember { Animatable(0f) }
    val slideX = remember { Animatable(0f) }
    var roadOffset by remember { mutableFloatStateOf(0f) }
    var flashTimer by remember { mutableFloatStateOf(0f) }
    val particles = remember { ParticleSystem() }
    var canvasWidth by remember { mutableStateOf(1920f) }
    var canvasHeight by remember { mutableStateOf(1080f) }
    var frameTick by remember { mutableLongStateOf(0L) }

    val vehicle = vehicles[currentIndex]

    LaunchedEffect(Unit) {
        voiceSpeaker.speak(vehicle.phrase)
    }

    LaunchedEffect(lastKeyEvent) {
        if (lastKeyEvent == null) return@LaunchedEffect
        val key = lastKeyEvent.second
        var switched = false
        when (key) {
            ToyboxKey.LEFT -> {
                currentIndex = (currentIndex - 1 + vehicles.size) % vehicles.size
                switched = true
            }
            ToyboxKey.RIGHT -> {
                currentIndex = (currentIndex + 1) % vehicles.size
                switched = true
            }
            ToyboxKey.UP, ToyboxKey.DOWN, ToyboxKey.OK -> {
                // Honk / siren / blast off!
            }
        }
        val active = vehicles[currentIndex]
        soundEngine.play(active.sound)
        voiceSpeaker.speak(active.phrase)
        flashTimer = 1.1f

        particles.burst(
            x = canvasWidth * 0.5f,
            y = canvasHeight * 0.56f,
            count = 28,
            baseColor = active.accentColor,
            shape = if (active == ToyVehicle.ROCKET) ParticleShape.STAR else ParticleShape.CIRCLE
        )

        if (switched) {
            launch {
                slideX.snapTo(if (key == ToyboxKey.RIGHT) 1f else -1f)
                slideX.animateTo(0f, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow))
            }
        }
        launch {
            hop.snapTo(1f)
            hop.animateTo(0f, spring(dampingRatio = 0.38f, stiffness = Spring.StiffnessMedium))
        }
    }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (lastNanos == 0L) 0.016f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = now
                roadOffset = (roadOffset + dt * (if (flashTimer > 0f) 520f else 220f)) % 240f
                if (flashTimer > 0f) flashTimer = (flashTimer - dt).coerceAtLeast(0f)
                particles.update(dt)
                frameTick = now
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC), Color(0xFFE1F5FE))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tick = frameTick
            canvasWidth = size.width
            canvasHeight = size.height

            val w = size.width
            val h = size.height

            // Sun
            drawCircle(Color(0xFFFFEB3B), radius = h * 0.095f, center = Offset(w * 0.12f, h * 0.16f))

            // Rolling green meadow
            drawRect(Color(0xFF66BB6A), topLeft = Offset(0f, h * 0.64f), size = Size(w, h * 0.36f))

            // Asphalt road
            val roadTop = h * 0.72f
            val roadH = h * 0.22f
            drawRect(Color(0xFF455A64), topLeft = Offset(0f, roadTop), size = Size(w, roadH))

            // Animated dashed road stripes
            var stripeX = -roadOffset
            while (stripeX < w + 240f) {
                drawRoundRect(
                    color = Color(0xFFFFEE58),
                    topLeft = Offset(stripeX, roadTop + roadH * 0.46f),
                    size = Size(120f, 16f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                stripeX += 240f
            }

            // Vehicle position
            val vx = w * 0.5f + slideX.value * w * 0.65f
            val lift = if (vehicle == ToyVehicle.ROCKET) hop.value * h * 0.22f else hop.value * h * 0.08f
            val vy = roadTop - lift
            val unit = minOf(w, h) * 0.24f

            drawVehicle(vehicle, Offset(vx, vy), unit, flashing = flashTimer > 0f)
            particles.draw(this)
            @Suppress("UNUSED_EXPRESSION")
            tick
        }

        Text(
            text = vehicle.displayName.uppercase(),
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 3.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = TvOverscan.Vertical + 12.dp)
        )
    }
}

private fun DrawScope.drawVehicle(v: ToyVehicle, base: Offset, u: Float, flashing: Boolean) {
    when (v) {
        ToyVehicle.ROCKET -> {
            // Rocket flame when blasting off
            if (flashing) {
                val flame = Path().apply {
                    moveTo(base.x - u * 0.35f, base.y - u * 0.15f)
                    lineTo(base.x, base.y + u * 0.65f)
                    lineTo(base.x + u * 0.35f, base.y - u * 0.15f)
                    close()
                }
                drawPath(flame, Color(0xFFFF9800))
            }
            // Fins
            val leftFin = Path().apply {
                moveTo(base.x - u * 0.45f, base.y - u * 0.55f)
                lineTo(base.x - u * 0.9f, base.y - u * 0.05f)
                lineTo(base.x - u * 0.45f, base.y - u * 0.1f)
                close()
            }
            val rightFin = Path().apply {
                moveTo(base.x + u * 0.45f, base.y - u * 0.55f)
                lineTo(base.x + u * 0.9f, base.y - u * 0.05f)
                lineTo(base.x + u * 0.45f, base.y - u * 0.1f)
                close()
            }
            drawPath(leftFin, v.accentColor)
            drawPath(rightFin, v.accentColor)
            // Body
            drawOval(v.bodyColor, Offset(base.x - u * 0.52f, base.y - u * 1.65f), Size(u * 1.04f, u * 1.6f))
            // Porthole window
            drawCircle(Color.White, u * 0.28f, Offset(base.x, base.y - u * 0.98f))
            drawCircle(Color(0xFF4FC3F7), u * 0.2f, Offset(base.x, base.y - u * 0.98f))
        }
        ToyVehicle.TRAIN -> {
            // Smokestack
            drawRoundRect(Color(0xFF263238), Offset(base.x + u * 0.55f, base.y - u * 1.45f), Size(u * 0.34f, u * 0.6f), CornerRadius(12f, 12f))
            // Steam puff
            if (flashing) {
                drawCircle(Color.White.copy(alpha = 0.85f), u * 0.28f, Offset(base.x + u * 0.72f, base.y - u * 1.75f))
                drawCircle(Color.White.copy(alpha = 0.65f), u * 0.22f, Offset(base.x + u * 0.42f, base.y - u * 1.98f))
            }
            // Cab & boiler
            drawRoundRect(v.bodyColor, Offset(base.x - u * 1.25f, base.y - u * 1.25f), Size(u * 1.15f, u * 1.05f), CornerRadius(24f, 24f))
            drawRoundRect(v.accentColor, Offset(base.x - u * 0.25f, base.y - u * 0.92f), Size(u * 1.45f, u * 0.72f), CornerRadius(24f, 24f))
            drawRoundRect(Color(0xFFB3E5FC), Offset(base.x - u * 1.02f, base.y - u * 1.04f), Size(u * 0.65f, u * 0.45f), CornerRadius(12f, 12f))
            drawWheels(base, u, listOf(-0.85f, 0.05f, 0.82f))
        }
        else -> {
            // Car / Truck / Bus / Tractor body
            val bodyW = u * 2.55f
            val bodyH = u * 1.08f
            drawRoundRect(
                color = v.bodyColor,
                topLeft = Offset(base.x - bodyW / 2f, base.y - bodyH - u * 0.12f),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(34f, 34f)
            )
            // Windows
            drawRoundRect(
                color = Color(0xFFB3E5FC),
                topLeft = Offset(base.x + u * 0.25f, base.y - bodyH + u * 0.06f),
                size = Size(u * 0.78f, u * 0.46f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFFB3E5FC),
                topLeft = Offset(base.x - u * 0.92f, base.y - bodyH + u * 0.06f),
                size = Size(u * 0.92f, u * 0.46f),
                cornerRadius = CornerRadius(16f, 16f)
            )
            // Roof siren / beacon
            if (v == ToyVehicle.FIRE_TRUCK || v == ToyVehicle.POLICE_CAR) {
                val beaconColor = if (flashing) Color(0xFFFFEE58) else v.accentColor
                drawCircle(beaconColor, radius = u * (if (flashing) 0.28f else 0.18f), center = Offset(base.x + u * 0.1f, base.y - bodyH - u * 0.24f))
            }
            // Headlight beam when honking
            if (flashing) {
                val beam = Path().apply {
                    moveTo(base.x + bodyW / 2f, base.y - u * 0.48f)
                    lineTo(base.x + bodyW / 2f + u * 0.95f, base.y - u * 0.78f)
                    lineTo(base.x + bodyW / 2f + u * 0.95f, base.y - u * 0.18f)
                    close()
                }
                drawPath(beam, Color(0x88FFF176))
            }
            drawWheels(base, u, listOf(-0.72f, 0.72f))
        }
    }
}

private fun DrawScope.drawWheels(base: Offset, u: Float, offsets: List<Float>) {
    for (o in offsets) {
        val wc = Offset(base.x + u * o, base.y - u * 0.06f)
        drawCircle(Color(0xFF263238), radius = u * 0.34f, center = wc)
        drawCircle(Color.White, radius = u * 0.14f, center = wc)
    }
}
