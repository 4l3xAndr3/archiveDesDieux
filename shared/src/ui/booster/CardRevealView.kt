package ui.booster

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Card
import models.Faction
import models.Rarity
import ui.theme.*
import ui.components.CardImage
import ui.effects.LegendaryParticleEffect
import ui.effects.LegendaryParticles
import kotlin.math.min

/**
 * Vue de révélation des cartes après ouverture du booster
 */
@Composable
fun CardRevealView(
    cards: List<Card>,
    onAllRevealed: () -> Unit
) {
    var revealedIndices by remember { mutableStateOf(setOf<Int>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tes nouvelles cartes !",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GlowWhite,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                FlippableCard(
                    card = card,
                    isRevealed = revealedIndices.contains(index),
                    onClick = {
                        revealedIndices = revealedIndices + index
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (revealedIndices.size == cards.size) {
            Button(
                onClick = onAllRevealed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FactionGrec
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text(
                    text = "Continuer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkStone
                )
            }
        } else {
            Text(
                text = "Touche les cartes pour les révéler (${revealedIndices.size}/${cards.size})",
                fontSize = 16.sp,
                color = GlowWhite.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Carte retournable avec animation 3D
 */
@Composable
fun FlippableCard(
    card: Card,
    isRevealed: Boolean,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var currentRotation by remember { mutableStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    )
    
    LaunchedEffect(rotation) {
        currentRotation = rotation
        if (rotation > 90f && card.rarity == Rarity.LEGENDAIRE) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    
    // Animation de pulsation pour la bordure selon la rareté
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(320.dp)
            .scale(if (!isRevealed) pulseScale else 1f)
            .graphicsLayer {
                rotationY = currentRotation
                cameraDistance = 12f * density
            }
            .clickable(enabled = !isRevealed) {
                if (card.rarity == Rarity.LEGENDAIRE) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }
    ) {
        if (currentRotation <= 90f) {
            // Dos de la carte
            CardBack(faction = card.faction)
        } else {
            // Face de la carte (mirrorée pour compenser la rotation)
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f
                }
            ) {
                CardFront(card = card, isFlipping = currentRotation in 90f..180f)
            }
        }
    }
}

/**
 * Dos de la carte avec image selon la faction
 */
@Composable
fun CardBack(faction: Faction) {
    val cardBackImagePath = when (faction) {
        Faction.GREC -> "card_backs/greek_back.jpg"
        Faction.EGYPTIEN -> "card_backs/egyptian_back.jpg"
        Faction.NORDIQUE -> "card_backs/nordic_back.jpg"
    }
    
    val cardBackImage = remember(cardBackImagePath) {
        try {
            val file = File("shared/resources/$cardBackImagePath")
            if (file.exists()) {
                val bytes = file.readBytes()
                SkiaImage.makeFromEncoded(bytes).asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        getFactionColor(faction),
                        getFactionColor(faction).copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                width = 3.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cardBackImage != null) {
            Image(
                bitmap = cardBackImage,
                contentDescription = "Dos carte ${faction.displayName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback sur l'icône emoji
            Text(
                text = when (faction) {
                    Faction.GREC -> "🔱"
                    Faction.EGYPTIEN -> "☥"
                    Faction.NORDIQUE -> "🔨"
                },
                fontSize = 100.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Face de la carte - affiche simplement l'image complète
 */
@Composable
fun CardFront(card: Card, isFlipping: Boolean = false) {
    val rarityColor = when (card.rarity) {
        Rarity.COMMUNE -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.LEGENDAIRE -> RarityLegendary
    }
    
    // Animation de glow renforcée pendant le flip pour les légendaires
    val glowAlpha by animateFloatAsState(
        targetValue = if (isFlipping && card.rarity == Rarity.LEGENDAIRE) 0.8f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
    
    Box(
        
    ) {
        // L'image de la carte complète
        CardImage(
            imagePath = card.imagePath,
            contentDescription = card.name,
            modifier = Modifier.fillMaxSize()
        )
        
        // Halo blanc intensifié pendant le flip légendaire
        if (card.rarity == Rarity.LEGENDAIRE && glowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        // Effet légendaire de particules par-dessus
        if (card.rarity == Rarity.LEGENDAIRE) {
            LegendaryParticles(
                faction = card.faction
            )
        }
    }
}

/**
 * Boîte de statistique
 */
@Composable
fun StatBox(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp
        )
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
