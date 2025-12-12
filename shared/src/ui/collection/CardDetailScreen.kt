package ui.collection

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Card
import models.Faction
import models.Rarity
import ui.booster.getFactionColor
import ui.components.CardImage
import ui.theme.*
import ui.effects.LegendaryParticleEffect
import ui.effects.HolographicFloatingText
import ui.effects.HolographicStatFrame
import ui.effects.HolographicScanlines
import ui.effects.HolographicGrid
import ui.common.SharedBackground

/**
 * Vue détaillée d'une carte avec effet parallax gyroscopique et transition animée
 */
@Composable
fun CardDetailScreen(
    card: Card,
    onClose: () -> Unit
) {
    // État pour le parallax (simulation gyroscope avec drag)
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Animation d'entrée : scale de 0.3 à 1.0 sur 500ms
    val enterScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    )
    
    // Animation du blur du fond : de 0 à 12dp sur 400ms
    val backgroundBlur by animateFloatAsState(
        targetValue = 12f,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        )
    )
    
    // Animation de l'overlay alpha : de 0 à 0.85 sur 400ms
    val overlayAlpha by animateFloatAsState(
        targetValue = 0.85f,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Reset progressif
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    // Limiter l'amplitude du parallax
                    offsetX = (offsetX + dragAmount.x * 0.05f).coerceIn(-30f, 30f)
                    offsetY = (offsetY + dragAmount.y * 0.05f).coerceIn(-30f, 30f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Fond avec blur animé
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(backgroundBlur.dp)
        ) {
            SharedBackground(overlayAlpha = overlayAlpha)
        }
        
        HolographicGrid(
            color = GlowWhite.copy(alpha = 0.25f * enterScale),
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .graphicsLayer {
                    scaleX = enterScale
                    scaleY = enterScale
                    alpha = enterScale
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Carte avec effet parallax
            ParallaxCard(
                card = card,
                offsetX = offsetX,
                offsetY = offsetY
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Statistiques holographiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HolographicStatFrame(
                    label = "⚔️",
                    value = card.attack.toString(),
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                )
                
                HolographicStatFrame(
                    label = "❤️",
                    value = card.health.toString(),
                    color = Color(0xFF4ECDC4),
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description holographique
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                HolographicFloatingText(
                    text = card.description,
                    fontSize = 14.sp,
                    color = GlowWhite,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bouton de fermeture
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = getFactionColor(card.faction)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Fermer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkStone
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Glisse ton doigt pour voir l'effet 3D",
                fontSize = 12.sp,
                color = GlowWhite.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Carte avec effet parallax multicouche
 */
@Composable
fun ParallaxCard(
    card: Card,
    offsetX: Float,
    offsetY: Float
) {
    val rarityColor = when (card.rarity) {
        Rarity.COMMUNE -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.LEGENDAIRE -> RarityLegendary
    }
    
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(460.dp)
    ) {
        // Couche 0 : Fond avec effet de profondeur
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX * 0.2f
                    translationY = offsetY * 0.2f
                }
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            getFactionColor(card.faction).copy(alpha = 0.3f),
                            Color.Black
                        )
                    )
                )
        )
        
        // Couche 1 : Particules légendaires en arrière-plan
        if (card.rarity == Rarity.LEGENDAIRE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX * 0.4f
                        translationY = offsetY * 0.4f
                    }
                    .clip(RoundedCornerShape(20.dp))
            ) {
                LegendaryParticleEffect(
                    faction = card.faction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Couche 2 : L'image de la carte avec effet parallax
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX * 0.8f
                    translationY = offsetY * 0.8f
                    // Rotation légère pour accentuer l'effet 3D
                    rotationY = offsetX * 0.05f
                    rotationX = -offsetY * 0.05f
                }
                .clip(RoundedCornerShape(20.dp))
        ) {
            CardImage(
                imagePath = card.imagePath,
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Couche 2.5 : Particules légendaires au-dessus de la carte (effet visible en plein écran)
        if (card.rarity == Rarity.LEGENDAIRE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = offsetX * 0.9f
                        translationY = offsetY * 0.9f
                    }
                    .clip(RoundedCornerShape(20.dp))
            ) {
                LegendaryParticleEffect(
                    faction = card.faction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Couche 3 : Nom de la carte flottant au-dessus
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    translationX = offsetX * 1.2f
                    translationY = offsetY * 1.2f
                }
        ) {
            HolographicFloatingText(
                text = card.name,
                fontSize = 22.sp,
                color = rarityColor
            )
        }
        
        // Couche 4 : Scanlines holographiques
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
        ) {
            HolographicScanlines(
                color = rarityColor,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Couche 5 : Bordure avec rareté (fixe, au premier plan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            rarityColor,
                            rarityColor.copy(alpha = 0.5f),
                            rarityColor
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}

/**
 * Texte avec effet holographique (scanlines et glow)
 */
@Composable
fun HolographicText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    val shimmer by rememberInfiniteTransition().animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box {
        // Ombre/glow
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.3f),
            modifier = Modifier
                .offset(x = 2.dp, y = 2.dp)
                .blur(4.dp)
        )
        
        // Texte principal avec effet shimmer
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = shimmer)
        )
    }
}
