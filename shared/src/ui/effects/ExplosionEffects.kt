package ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import models.Faction
import ui.booster.getFactionColor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Effet de fissures progressives sur le booster pendant la charge
 */
@Composable
fun CrackEffect(
    progress: Float,
    faction: Faction,
    modifier: Modifier = Modifier
) {
    val factionColor = getFactionColor(faction)
    
    val cracks = remember {
        // Génère des fissures pré-calculées pour la cohérence visuelle
        List(8) { index ->
            Crack(
                startAngle = (index * 45f),
                length = 50f + Random.nextFloat() * 100f,
                branches = Random.nextInt(2, 4),
                startProgress = index * 0.1f
            )
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        
        cracks.forEach { crack ->
            // La fissure n'apparaît que quand le progress atteint son point de départ
            val crackProgress = ((progress - crack.startProgress) / (1f - crack.startProgress)).coerceIn(0f, 1f)
            
            if (crackProgress > 0) {
                // Fissure principale
                val endX = center.x + cos(Math.toRadians(crack.startAngle.toDouble())).toFloat() * crack.length * crackProgress
                val endY = center.y + sin(Math.toRadians(crack.startAngle.toDouble())).toFloat() * crack.length * crackProgress
                
                drawLine(
                    color = factionColor.copy(alpha = 0.9f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 3f + (crackProgress * 2f)
                )
                
                // Lueur interne de la fissure
                drawLine(
                    color = Color.White.copy(alpha = 0.6f * crackProgress),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.5f
                )
                
                // Branches secondaires
                repeat(crack.branches) { branchIndex ->
                    val branchAngle = crack.startAngle + (branchIndex * 20f - 20f)
                    val branchLength = crack.length * 0.4f
                    val branchStart = Offset(
                        center.x + cos(Math.toRadians(crack.startAngle.toDouble())).toFloat() * crack.length * 0.5f * crackProgress,
                        center.y + sin(Math.toRadians(crack.startAngle.toDouble())).toFloat() * crack.length * 0.5f * crackProgress
                    )
                    val branchEnd = Offset(
                        branchStart.x + cos(Math.toRadians(branchAngle.toDouble())).toFloat() * branchLength * crackProgress,
                        branchStart.y + sin(Math.toRadians(branchAngle.toDouble())).toFloat() * branchLength * crackProgress
                    )
                    
                    drawLine(
                        color = factionColor.copy(alpha = 0.7f),
                        start = branchStart,
                        end = branchEnd,
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

/**
 * Effet d'explosion amélioré avec particules physiques
 */
@Composable
fun ImprovedExplosionEffect(
    faction: Faction,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit
) {
    val factionColor = getFactionColor(faction)
    
    val explosionParticles = remember {
        List(50) { index ->
            ExplosionParticle(
                angle = Random.nextFloat() * 360f,
                speed = 200f + Random.nextFloat() * 300f,
                size = 5f + Random.nextFloat() * 15f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }
    
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (animationProgress < 1f) {
            animationProgress = ((System.currentTimeMillis() - startTime) / 1000f).coerceAtMost(1f)
            kotlinx.coroutines.delay(16) // ~60fps
        }
        onComplete()
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        
        // Flash lumineux initial
        if (animationProgress < 0.2f) {
            val flashAlpha = (1f - animationProgress / 0.2f) * 0.9f
            drawCircle(
                color = Color.White.copy(alpha = flashAlpha),
                radius = size.width * animationProgress * 3f,
                center = center
            )
        }
        
        // Onde de choc
        if (animationProgress < 0.5f) {
            val shockwaveProgress = animationProgress / 0.5f
            val shockwaveAlpha = 1f - shockwaveProgress
            drawCircle(
                color = factionColor.copy(alpha = shockwaveAlpha * 0.5f),
                radius = size.width * shockwaveProgress * 1.5f,
                center = center,
                style = Stroke(width = 8f * (1f - shockwaveProgress * 0.5f))
            )
        }
        
        // Particules projetées
        explosionParticles.forEach { particle ->
            val distance = particle.speed * animationProgress
            val x = center.x + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * distance
            val y = center.y + sin(Math.toRadians(particle.angle.toDouble())).toFloat() * distance + (animationProgress * animationProgress * 200f) // Gravité
            
            val alpha = (1f - animationProgress).coerceAtLeast(0f)
            val currentRotation = particle.rotation + (particle.rotationSpeed * animationProgress * 360f)
            
            if (x in 0f..size.width && y in 0f..size.height) {
                // Particule avec forme (débris du conteneur)
                drawPath(
                    path = createDebrisPath(x, y, particle.size, currentRotation),
                    color = factionColor.copy(alpha = alpha * 0.8f)
                )
                
                // Traînée lumineuse
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.4f),
                    radius = particle.size * 0.5f,
                    center = Offset(x, y)
                )
            }
        }
        
        // Particules de lumière secondaires
        repeat(20) { index ->
            val angle = (index * 18f) + (animationProgress * 45f)
            val distance = 100f + (index * 20f) + (animationProgress * 200f)
            val x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * distance
            
            val sparkAlpha = (1f - animationProgress * 1.5f).coerceAtLeast(0f)
            
            drawCircle(
                color = Color.White.copy(alpha = sparkAlpha),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Crée un chemin de débris irrégulier
 */
private fun createDebrisPath(x: Float, y: Float, size: Float, rotation: Float): Path {
    return Path().apply {
        // Triangle irrégulier pour simuler un débris
        val angle1 = Math.toRadians(rotation.toDouble())
        val angle2 = Math.toRadians((rotation + 120).toDouble())
        val angle3 = Math.toRadians((rotation + 240).toDouble())
        
        moveTo(
            x + cos(angle1).toFloat() * size,
            y + sin(angle1).toFloat() * size
        )
        lineTo(
            x + cos(angle2).toFloat() * size * 0.8f,
            y + sin(angle2).toFloat() * size * 0.8f
        )
        lineTo(
            x + cos(angle3).toFloat() * size * 1.2f,
            y + sin(angle3).toFloat() * size * 1.2f
        )
        close()
    }
}

// Data classes
private data class Crack(
    val startAngle: Float,
    val length: Float,
    val branches: Int,
    val startProgress: Float
)

private data class ExplosionParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)
