package ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import models.Faction
import ui.booster.getFactionColor
import ui.common.SharedBackground
import ui.theme.DarkStone
import ui.theme.GlowWhite

/**
 * Écran de sélection de faction pour le mode bataille
 */
@Composable
fun FactionSelectionScreen(
    onFactionSelected: (Faction) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SharedBackground(imagePath = "backgrounds/collection_runes.jpg", overlayAlpha = 0.4f)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titre
            Text(
                text = "⚔️ CHOISIR TA MYTHOLOGIE ⚔️",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GlowWhite,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Cards des factions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple(Faction.GREC, "🏛️ GREC", "Classique et équilibré"),
                    Triple(Faction.EGYPTIEN, "🏜️ ÉGYPTIEN", "Fort et imposant"),
                    Triple(Faction.NORDIQUE, "❄️ NORDIQUE", "Rapide et violent")
                ).forEach { (faction, label, desc) ->
                    FactionSelectionCard(
                        faction = faction,
                        label = label,
                        description = desc,
                        onClick = { onFactionSelected(faction) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Bouton retour
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = GlowWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Carte interactive pour sélectionner une faction
 */
@Composable
fun FactionSelectionCard(
    faction: Faction,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    val scale = remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale.value,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(200.dp)
            .scale(animatedScale)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        getFactionColor(faction).copy(alpha = 0.3f),
                        Color.Black.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                onClick = {
                    scale.value = 0.95f
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = getFactionColor(faction)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = GlowWhite.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        // Lueur de rareté
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(-1f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            getFactionColor(faction).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )
    }
}
