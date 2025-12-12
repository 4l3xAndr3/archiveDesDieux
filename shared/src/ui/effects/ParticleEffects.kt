package ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.Faction
import ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Système de particules pour les effets légendaires selon les spécifications du moodboard :
 * - GREC : "Ascension d'Ambroisie" - Particules dorées montant vers le haut
 * - NORDIQUE : "Blizzard Éternel" - Flocons de neige tombant avec voile bleu givré
 * - ÉGYPTIEN : "Tempête de Sable" - Grains ocres/dorés traversant latéralement
 */

/**
 * Effet de particules légendaires pour une carte
 */
@Composable
fun LegendaryParticleEffect(
    faction: Faction,
    modifier: Modifier = Modifier
) {
    when (faction) {
        Faction.GREC -> AmbrosiaAscensionEffect(modifier)
        Faction.NORDIQUE -> EternalBlizzardEffect(modifier)
        Faction.EGYPTIEN -> SandstormEffect(modifier)
    }
}

/**
 * Effet "Ascension d'Ambroisie" pour les cartes légendaires Grecques
 * Particules de lumière dorée et crème montant doucement vers le haut
 */
@Composable
fun AmbrosiaAscensionEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(60) { index ->
            AmbrosiaParticle(
                startX = Random.nextFloat(),
                startY = 1.2f + Random.nextFloat() * 0.3f,
                speed = 0.35f + Random.nextFloat() * 0.5f,
                size = 2.5f + Random.nextFloat() * 5f,
                delay = index * 35L,
                color = if (Random.nextBoolean()) 
                    EffectGrec // Doré chaud
                else 
                    Color(0xFFFFF3D6) // Crème douce
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Halo ambroisie renforcé
        drawRect(
            color = EffectGrec.copy(alpha = 0.18f),
            size = size
        )
        drawCircle(
            color = Color(0xFFFFE5B4).copy(alpha = 0.28f),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.5f, size.height * 0.82f)
        )
        particles.forEach { particle ->
            val adjustedTime = (time - particle.delay).coerceAtLeast(0f)
            val progress = (adjustedTime / 10000f * particle.speed) % 1f
            
            // Position Y : monte vers le haut (1.0 -> 0.0)
            val y = particle.startY - progress * 1.4f
            
            // Oscillation horizontale légère
            val xOffset = sin(adjustedTime / 200f + particle.startX * 10f) * 0.02f
            val x = particle.startX + xOffset
            
            // Fade in/out
            val alpha = when {
                progress < 0.1f -> progress / 0.1f
                progress > 0.9f -> (1f - progress) / 0.1f
                else -> 1f
            }
            
            if (y in 0f..1f && x in 0f..1f) {
                drawCircle(
                    color = particle.color.copy(alpha = alpha * 0.8f),
                    radius = particle.size,
                    center = Offset(x * size.width, y * size.height)
                )
                // Halo lumineux
                drawCircle(
                    color = particle.color.copy(alpha = alpha * 0.3f),
                    radius = particle.size * 2f,
                    center = Offset(x * size.width, y * size.height)
                )
            }
        }
    }
}

/**
 * Effet "Blizzard Éternel" pour les cartes légendaires Nordiques
 * Flocons de neige tombant avec voile bleu givré
 */
@Composable
fun EternalBlizzardEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(70) { index ->
            SnowflakeParticle(
                startX = Random.nextFloat(),
                startY = -0.2f - Random.nextFloat() * 0.3f,
                speed = 0.22f + Random.nextFloat() * 0.35f,
                size = 2f + Random.nextFloat() * 3.5f,
                delay = index * 32L,
                windStrength = 0.01f + Random.nextFloat() * 0.025f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Voile bleu givré
        drawRect(
            color = EffectNordique.copy(alpha = 0.26f),
            size = size
        )
        drawRect(
            color = Color.White.copy(alpha = 0.1f),
            size = size
        )
        
        particles.forEach { particle ->
            val adjustedTime = (time - particle.delay).coerceAtLeast(0f)
            val progress = (adjustedTime / 15000f * particle.speed) % 1f
            
            // Position Y : tombe vers le bas (0.0 -> 1.0)
            val y = particle.startY + progress * 1.4f
            
            // Effet de vent - oscillation
            val xOffset = sin(adjustedTime / 300f + particle.startY * 5f) * particle.windStrength
            val x = particle.startX + xOffset + (progress * 0.05f) // Légère dérive
            
            // Fade in/out
            val alpha = when {
                progress < 0.15f -> progress / 0.15f
                progress > 0.85f -> (1f - progress) / 0.15f
                else -> 1f
            }
            
            if (y in 0f..1f && x in 0f..1f) {
                // Flocon de neige (petit + avec un centre)
                val centerX = x * size.width
                val centerY = y * size.height
                
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    radius = particle.size,
                    center = Offset(centerX, centerY)
                )
                
                // Branches du flocon
                drawCircle(
                    color = EffectNordique.copy(alpha = alpha * 0.6f),
                    radius = particle.size * 1.4f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

/**
 * Effet "Tempête de Sable" pour les cartes légendaires Égyptiennes
 * Grains de sable ocres et dorés traversant rapidement de manière horizontale
 */
@Composable
fun SandstormEffect(modifier: Modifier = Modifier) {
    val particles = remember {
        List(80) { index ->
            SandParticle(
                startX = -0.2f - Random.nextFloat() * 0.3f,
                startY = Random.nextFloat(),
                speed = 1.1f + Random.nextFloat() * 0.8f,
                size = 1.8f + Random.nextFloat() * 3.2f,
                delay = index * 24L,
                yVariation = Random.nextFloat() * 0.02f,
                color = if (Random.nextBoolean())
                    EffectEgyptien // Doré
                else
                    Color(0xFFD8B073) // Ocre clair
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Haze chaud renforcé
        drawRect(color = EffectEgyptien.copy(alpha = 0.22f), size = size)
        drawRect(color = Color(0xFFC98E4A).copy(alpha = 0.12f), size = size)
        particles.forEach { particle ->
            val adjustedTime = (time - particle.delay).coerceAtLeast(0f)
            val progress = (adjustedTime / 8000f * particle.speed) % 1f
            
            // Position X : traverse de gauche à droite rapidement
            val x = particle.startX + progress * 1.4f
            
            // Variation verticale pour simuler le vent turbulent
            val yOffset = sin(adjustedTime / 90f + particle.startX * 22f) * particle.yVariation
            val y = particle.startY + yOffset
            
            // Fade in/out
            val alpha = when {
                progress < 0.07f -> progress / 0.07f
                progress > 0.93f -> (1f - progress) / 0.07f
                else -> 1f
            }
            
            if (x in 0f..1f && y in 0f..1f) {
                val cx = x * size.width
                val cy = y * size.height
                // Grain de sable allongé (effet de vitesse)
                drawCircle(
                    color = particle.color.copy(alpha = alpha * 0.85f),
                    radius = particle.size,
                    center = Offset(cx, cy)
                )
                // Traînée plus visible
                drawLine(
                    color = particle.color.copy(alpha = alpha * 0.5f),
                    start = Offset(cx - particle.size * 1.8f, cy),
                    end = Offset(cx, cy),
                    strokeWidth = particle.size * 0.9f
                )
            }
        }
    }
}

// Data classes pour les particules
private data class AmbrosiaParticle(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val delay: Long,
    val color: Color
)

private data class SnowflakeParticle(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val delay: Long,
    val windStrength: Float
)

private data class SandParticle(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val delay: Long,
    val yVariation: Float,
    val color: Color
)

/**
 * Particules légendaires style collection - simples boules animées
 */
@Composable
fun LegendaryParticles(faction: Faction) {
    val particles = remember {
        List(8) {
            LegendaryParticle(
                offsetX = Random.nextFloat() * 2f - 1f,
                offsetY = Random.nextFloat() * 2f - 1f,
                delay = Random.nextInt(0, 2000)
            )
        }
    }
    
    // Animation infinie pour forcer le redessinage
    val animatedTime by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val time = (animatedTime * 3000f) + particle.delay
            
            val (dx, dy) = when (faction) {
                Faction.GREC -> {
                    // Boules jaunes qui montent
                    val progress = ((time / 2000f) % 1f)
                    Pair(
                        particle.offsetX * size.width * 0.3f + size.width / 2f,
                        size.height * (1f - progress)
                    )
                }
                Faction.NORDIQUE -> {
                    // Boules blanches qui descendent
                    val progress = ((time / 2000f) % 1f)
                    Pair(
                        particle.offsetX * size.width * 0.3f + size.width / 2f,
                        size.height * progress
                    )
                }
                Faction.EGYPTIEN -> {
                    // Tempête de sable horizontale
                    val progress = ((time / 1500f) % 1f)
                    Pair(
                        size.width * progress,
                        particle.offsetY * size.height * 0.4f + size.height / 2f
                    )
                }
            }
            
            val particleColor = when (faction) {
                Faction.GREC -> Color(0xFFFFD700) // Jaune doré
                Faction.NORDIQUE -> Color(0xFFE8F4F8) // Blanc glacé
                Faction.EGYPTIEN -> Color(0xFFD4A574) // Sable ocre
            }
            
            val alpha = when (faction) {
                Faction.GREC, Faction.NORDIQUE -> {
                    val progress = ((time / 2000f) % 1f)
                    (1f - progress) * 0.9f
                }
                Faction.EGYPTIEN -> {
                    val progress = ((time / 1500f) % 1f)
                    (1f - progress) * 0.8f
                }
            }
            
            drawCircle(
                color = particleColor.copy(alpha = alpha),
                radius = 8f + Random.nextFloat() * 4f,
                center = Offset(dx, dy)
            )
            
            // Halo de lueur autour de la particule
            drawCircle(
                color = particleColor.copy(alpha = alpha * 0.4f),
                radius = (12f + Random.nextFloat() * 6f) * 1.5f,
                center = Offset(dx, dy)
            )
        }
    }
}

private data class LegendaryParticle(
    val offsetX: Float,
    val offsetY: Float,
    val delay: Int
)
