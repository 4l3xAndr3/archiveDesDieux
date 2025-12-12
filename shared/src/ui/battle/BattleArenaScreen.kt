package ui.battle

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Card
import models.Faction
import data.CardRepository
import ui.booster.getFactionColor
import ui.common.SharedBackground
import ui.components.CardImage
import ui.theme.DarkStone
import ui.theme.GlowWhite

data class BoardPosition(val x: Int, val y: Int) {
    override fun toString(): String = "($x,$y)"
}

data class PlacedCard(val card: Card, val position: BoardPosition)

/**
 * Arène de bataille où placer les cartes
 */
@Composable
fun BattleArenaScreen(
    faction: Faction,
    onBack: () -> Unit
) {
    var userDeck by remember { mutableStateOf(CardRepository.getCollectionByFaction(faction)) }
    var playerCards by remember { mutableStateOf(listOf<PlacedCard>()) }
    var enemyCards by remember { mutableStateOf(listOf<PlacedCard>()) }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var highlightedPosition by remember { mutableStateOf<BoardPosition?>(null) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    
    // Constantes de grille
    val gridWidth = 3
    val gridHeight = 2
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SharedBackground(imagePath = "backgrounds/collection_runes.jpg", overlayAlpha = 0.5f)
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // En-tête
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkStone, Color.Transparent)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚔️ ${faction.displayName.uppercase()} - ARÈNE DE BATAILLE ⚔️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = getFactionColor(faction),
                    letterSpacing = 1.sp
                )
            }
            
            // Contenu principal
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Plateau de jeu avec joueur et ennemi
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = getFactionColor(faction).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Zone ENNEMI (en haut) - Affichage seulement
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "⚔️ ENNEMI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridWidth),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(gridWidth * gridHeight) { index ->
                                val x = index % gridWidth
                                val y = index / gridWidth
                                val position = BoardPosition(x, y + 10) // Offset pour ennemi
                                val placedCard = enemyCards.find { it.position == position }
                                
                                BoardSlot(
                                    position = position,
                                    placedCard = placedCard,
                                    isHighlighted = highlightedPosition == position,
                                    faction = faction,
                                    onSlotClicked = { },
                                    onHover = { hovering ->
                                        highlightedPosition = if (hovering) position else null
                                    },
                                    isPlayerZone = false
                                )
                            }
                        }
                    }
                    
                    // Séparateur
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        getFactionColor(faction).copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    
                    // Zone JOUEUR (en bas) - Placement possible
                    Column(modifier = Modifier.weight(0.5f)) {
                        Text(
                            text = "👤 TU",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ECDC4),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridWidth),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(gridWidth * gridHeight) { index ->
                                val x = index % gridWidth
                                val y = index / gridWidth
                                val position = BoardPosition(x, y)
                                val placedCard = playerCards.find { it.position == position }
                                
                                BoardSlot(
                                    position = position,
                                    placedCard = placedCard,
                                    isHighlighted = highlightedPosition == position,
                                    faction = faction,
                                    onSlotClicked = {
                                        if (selectedCard != null && placedCard == null) {
                                            playerCards = playerCards + PlacedCard(selectedCard!!, position)
                                            selectedCard = null
                                        } else if (placedCard != null) {
                                            playerCards = playerCards.filterNot { it == placedCard }
                                        }
                                    },
                                    onHover = { hovering ->
                                        highlightedPosition = if (hovering) position else null
                                    },
                                    isPlayerZone = true
                                )
                            }
                        }
                    }
                }
                
                // Panneau de droite : deck et cartes placées
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Cartes placées
                    if (playerCards.isNotEmpty()) {
                        Text(
                            text = "Cartes Placées (${playerCards.size}/6)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlowWhite,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        playerCards.forEach { placed ->
                            PlacedCardItem(
                                card = placed.card,
                                position = placed.position,
                                onRemove = {
                                    playerCards = playerCards.filterNot { it == placed }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Deck disponible
                    Text(
                        text = "Deck Disponible",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowWhite,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    userDeck.forEach { (card, count) ->
                        if (count > 0) {
                            DeckCardItem(
                                card = card,
                                count = count,
                                isSelected = selectedCard == card,
                                onClick = {
                                    selectedCard = if (selectedCard == card) null else card
                                }
                            )
                        }
                    }
                }
            }
            
            // Barre de contrôle en bas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkStone)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    
                    Text(
                        text = "${playerCards.size}/6 cartes placées",
                        fontSize = 14.sp,
                        color = GlowWhite
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Bouton lancer bataille (quand 6 cartes)
                    if (playerCards.size == 6) {
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .background(
                                    getFactionColor(faction).copy(alpha = 0.7f),
                                    RoundedCornerShape(25.dp)
                                )
                                .clickable { }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚔️ LANCER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slot sur le plateau
 */
@Composable
fun BoardSlot(
    position: BoardPosition,
    placedCard: PlacedCard?,
    isHighlighted: Boolean,
    faction: Faction,
    onSlotClicked: () -> Unit,
    onHover: (Boolean) -> Unit,
    isPlayerZone: Boolean = true
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(
                color = if (isHighlighted) {
                    getFactionColor(faction).copy(alpha = 0.2f)
                } else {
                    Color.Black.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (placedCard != null) 2.dp else 1.dp,
                color = if (placedCard != null) {
                    getFactionColor(faction).copy(alpha = 0.8f)
                } else if (isHighlighted) {
                    getFactionColor(faction).copy(alpha = 0.6f)
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSlotClicked() },
        contentAlignment = Alignment.Center
    ) {
        if (placedCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
            ) {
                CardImage(
                    imagePath = placedCard.card.imagePath,
                    contentDescription = placedCard.card.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Afficher position
            Text(
                text = placedCard.position.toString(),
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        } else {
            // Slot vide
            Text(
                text = position.toString(),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Item pour une carte placée
 */
@Composable
fun PlacedCardItem(
    card: Card,
    position: BoardPosition,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = card.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowWhite,
                    maxLines = 1
                )
                Text(
                    text = "Pos: ${position}",
                    fontSize = 10.sp,
                    color = GlowWhite.copy(alpha = 0.6f)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Red.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(6.dp))
}

/**
 * Item pour une carte du deck
 */
@Composable
fun DeckCardItem(
    card: Card,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                color = if (isSelected) {
                    Color.Cyan.copy(alpha = 0.3f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Cyan else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = card.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlowWhite,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            
            Text(
                text = "×$count",
                fontSize = 11.sp,
                color = GlowWhite.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
