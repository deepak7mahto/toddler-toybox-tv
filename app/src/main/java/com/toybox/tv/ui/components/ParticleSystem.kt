package com.toybox.tv.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleShape {
    CIRCLE,
    STAR,
    CONFETTI
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var color: Color,
    var life: Float,
    var maxLife: Float,
    var shape: ParticleShape = ParticleShape.CIRCLE
)

/**
 * Lightweight 60fps particle burst manager shared across all 4 play modes.
 */
class ParticleSystem(private val maxParticles: Int = 180) {
    val particles = mutableListOf<Particle>()

    private val palette = listOf(
        Color(0xFFFF5252),
        Color(0xFFFFD600),
        Color(0xFF69F0AE),
        Color(0xFF40C4FF),
        Color(0xFFFF80AB),
        Color(0xFFFFAB40),
        Color(0xFFE040FB)
    )

    fun burst(
        x: Float,
        y: Float,
        count: Int = 26,
        baseColor: Color? = null,
        speed: Float = 420f,
        shape: ParticleShape = ParticleShape.STAR
    ) {
        repeat(count) {
            if (particles.size >= maxParticles) {
                particles.removeAt(0)
            }
            val angle = Random.nextFloat() * (2f * PI.toFloat())
            val mag = speed * (0.35f + Random.nextFloat() * 0.85f)
            val chosenColor = if (baseColor != null && Random.nextFloat() < 0.65f) {
                baseColor
            } else {
                palette[Random.nextInt(palette.size)]
            }
            val life = 0.75f + Random.nextFloat() * 0.55f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * mag,
                    vy = sin(angle) * mag - 140f,
                    radius = 10f + Random.nextFloat() * 14f,
                    color = chosenColor,
                    life = life,
                    maxLife = life,
                    shape = if (Random.nextFloat() < 0.5f) shape else ParticleShape.CIRCLE
                )
            )
        }
    }

    fun update(dtSec: Float) {
        val gravity = 520f
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.life -= dtSec
            if (p.life <= 0f) {
                iter.remove()
            } else {
                p.vy += gravity * dtSec
                p.x += p.vx * dtSec
                p.y += p.vy * dtSec
            }
        }
    }

    fun draw(scope: DrawScope) {
        for (p in particles) {
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            val color = p.color.copy(alpha = alpha)
            when (p.shape) {
                ParticleShape.CIRCLE -> {
                    scope.drawCircle(
                        color = color,
                        radius = p.radius * (0.5f + 0.5f * alpha),
                        center = Offset(p.x, p.y)
                    )
                }
                ParticleShape.CONFETTI -> {
                    scope.drawRect(
                        color = color,
                        topLeft = Offset(p.x - p.radius, p.y - p.radius * 0.55f),
                        size = Size(p.radius * 2f, p.radius * 1.1f)
                    )
                }
                ParticleShape.STAR -> {
                    drawStar(scope, Offset(p.x, p.y), p.radius * (0.6f + 0.4f * alpha), color)
                }
            }
        }
    }

    companion object {
        private val starPath = Path()

        fun drawStar(scope: DrawScope, center: Offset, outerRadius: Float, color: Color) {
            val innerRadius = outerRadius * 0.45f
            starPath.reset()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (-90.0 + i * 36.0) * PI / 180.0
                val px = center.x + (cos(angle) * r).toFloat()
                val py = center.y + (sin(angle) * r).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            scope.drawPath(starPath, color = color)
        }
    }
}
