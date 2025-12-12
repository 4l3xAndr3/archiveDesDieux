package ui.booster

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.CardRepository
import models.BoosterPack
import models.Card
import models.Faction
import ui.theme.*
import ui.effects.CrackEffect
import ui.effects.ImprovedExplosionEffect
import ui.common.SharedBackground

/**
 * Écran d'ouverture de booster
 */
@Composable
fun BoosterOpeningScreen(
    faction: Faction,
    packType: models.PackType = models.PackType.STANDARD,
    onBoosterOpened: (List<Card>) -> Unit,
    onBack: () -> Unit
) {
    var boosterState by remember { mutableStateOf(BoosterState.SEALED) }
    var chargeProgress by remember { mutableStateOf(0f) }
    var revealedCards by remember { mutableStateOf<List<Card>>(emptyList()) }
    
    val shakeOffset by animateFloatAsState(
        targetValue = if (boosterState == BoosterState.CHARGING) 5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SharedBackground(overlayAlpha = 0.7f)
        when (boosterState) {
            BoosterState.SEALED -> {
                SealedBoosterView(
                    faction = faction,
                    shakeOffset = shakeOffset,
                    onChargeStart = {
                        boosterState = BoosterState.CHARGING
                    },
                    onChargeProgress = { progress ->
                        chargeProgress = progress
                    },
                    onChargeComplete = {
                        boosterState = BoosterState.OPENING
                        revealedCards = CardRepository.generateBooster(faction, packType)
                    }
                )
            }
            BoosterState.CHARGING -> {
                SealedBoosterView(
                    faction = faction,
                    shakeOffset = shakeOffset,
                    onChargeStart = { },
                    onChargeProgress = { progress ->
                        chargeProgress = progress
                    },
                    onChargeComplete = {
                        boosterState = BoosterState.OPENING
                        revealedCards = CardRepository.generateBooster(faction, packType)
                    }
                )
            }
            BoosterState.OPENING -> {
                ImprovedExplosionEffect(
                    faction = faction,
                    onComplete = {
                        boosterState = BoosterState.REVEALING
                    }
                )
            }
            BoosterState.REVEALING -> {
                CardRevealView(
                    cards = revealedCards,
                    onAllRevealed = {
                        revealedCards.forEach { CardRepository.addToCollection(it) }
                        onBoosterOpened(revealedCards)
                    }
                )
            }
        }
    }
}

/**
 * États du booster
 */
enum class BoosterState {
    SEALED,      // Scellé, prêt à ouvrir
    CHARGING,    // En cours de charge
    OPENING,     // Explosion
    REVEALING    // Révélation des cartes
}

/**
 * Vue du booster scellé
 */
@Composable
fun SealedBoosterView(
    faction: Faction,
    shakeOffset: Float,
    onChargeStart: () -> Unit,
    onChargeProgress: (Float) -> Unit,
    onChargeComplete: () -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    var chargeProgress by remember { mutableStateOf(0f) }
    
    // Animation de flottement continu
    val floatOffset by rememberInfiniteTransition().animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    LaunchedEffect(isPressing) {
        if (isPressing) {
            onChargeStart()
            while (isPressing && chargeProgress < 1f) {
                kotlinx.coroutines.delay(16) // ~60fps
                chargeProgress += 0.01f
                onChargeProgress(chargeProgress)
                if (chargeProgress >= 1f) {
                    onChargeComplete()
                    isPressing = false
                }
            }
        } else {
            // Reset si relâché trop tôt
            chargeProgress = 0f
            onChargeProgress(0f)
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(
                    x = shakeOffset.dp, 
                    y = (floatOffset + (-shakeOffset)).dp
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressing = true
                            tryAwaitRelease()
                            isPressing = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Particules magiques qui tournent autour du pack
            MagicParticlesAround(faction = faction, chargeProgress = chargeProgress)
            
            // Conteneur selon la faction
            BoosterContainer(faction = faction, glowIntensity = chargeProgress)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = if (chargeProgress > 0) "Maintiens appuyé..." else "Appuie et maintiens",
            color = GlowWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Barre de progression
        if (chargeProgress > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(chargeProgress)
                        .background(
                            getFactionColor(faction),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

/**
 * Conteneur du booster avec image du pack selon la faction
 */
@Composable
fun BoosterContainer(faction: Faction, glowIntensity: Float) {
    val packImagePath = when (faction) {
        Faction.GREC -> "packs/amphore.png"
        Faction.EGYPTIEN -> "packs/canope.png"
        Faction.NORDIQUE -> "packs/runestone.png"
    }
    
    val packImage = remember(packImagePath) {
        try {
            val file = File("shared/resources/$packImagePath")
            if (file.exists()) {
                val bytes = file.readBytes()
                SkiaImage.makeFromEncoded(bytes).asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = Modifier.size(450.dp),
        contentAlignment = Alignment.Center
    ) {
        if (packImage != null) {
            Image(
                bitmap = packImage,
                contentDescription = "Pack ${faction.displayName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // Fallback sur l'icône emoji
            Text(
                text = when (faction) {
                    Faction.GREC -> "⚡"
                    Faction.EGYPTIEN -> "☥"
                    Faction.NORDIQUE -> "🔨"
                },
                fontSize = 120.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Particules magiques qui tournent autour du pack avec lignes d'énergie
 */
@Composable
fun MagicParticlesAround(faction: Faction, chargeProgress: Float) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val particles = remember {
        List(12) { index ->
            val angle = (360f / 12) * index
            MagicParticle(
                baseAngle = angle,
                radius = 180f + (index % 3) * 20f,
                size = 8f + (index % 4) * 3f
            )
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        particles.forEach { particle ->
            val angle = Math.toRadians((particle.baseAngle + rotation).toDouble())
            val x = centerX + kotlin.math.cos(angle).toFloat() * particle.radius
            val y = centerY + kotlin.math.sin(angle).toFloat() * particle.radius
            
            // Lignes d'énergie convergentes pendant la charge
            if (chargeProgress > 0) {
                // Ligne directe de la particule vers le centre
                drawLine(
                    color = getFactionColor(faction).copy(alpha = chargeProgress * 0.6f),
                    start = Offset(x, y),
                    end = Offset(centerX, centerY),
                    strokeWidth = 3f * chargeProgress
                )
            }
            
            // Particule avec effet de lueur
            val glowMultiplier = if (chargeProgress > 0) 1f + chargeProgress else 1f
            drawCircle(
                color = getFactionColor(faction).copy(alpha = 0.6f * glowMultiplier),
                radius = particle.size * 2 * glowMultiplier,
                center = Offset(x, y)
            )
            drawCircle(
                color = getFactionColor(faction),
                radius = particle.size * glowMultiplier,
                center = Offset(x, y)
            )
        }
        
        // Cercle d'énergie concentrée au centre quand charge > 70%
        if (chargeProgress > 0.7f) {
            val intensity = (chargeProgress - 0.7f) / 0.3f
            drawCircle(
                color = getFactionColor(faction).copy(alpha = intensity * 0.4f),
                radius = 100f * intensity,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = getFactionColor(faction).copy(alpha = intensity * 0.8f),
                radius = 50f * intensity,
                center = Offset(centerX, centerY)
            )
        }
    }
}

data class MagicParticle(
    val baseAngle: Float,
    val radius: Float,
    val size: Float
)

/**
 * Récupère la couleur associée à une faction
 */
fun getFactionColor(faction: Faction): Color {
    return when (faction) {
        Faction.GREC -> FactionGrec
        Faction.EGYPTIEN -> FactionEgyptien
        Faction.NORDIQUE -> FactionNordique
    }
}
