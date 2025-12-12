package ui.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import data.CardRepository
import models.Card
import models.Faction
import models.Rarity
import ui.booster.getFactionColor
import ui.components.CardImage
import ui.theme.*
import ui.common.SharedBackground

/**
 * Écran principal de la collection - Le Sanctuaire
 */
@Composable
fun CollectionScreen(
    onCardSelected: (Card) -> Unit,
    onBack: () -> Unit
) {
    var selectedFaction by remember { mutableStateOf<Faction?>(null) }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    
    val cards = remember(selectedFaction) {
        if (selectedFaction != null) {
            CardRepository.getCollectionByFaction(selectedFaction!!)
        } else {
            CardRepository.getCollection()
        }
    }
    
    val totalCount = remember { CardRepository.getTotalCardCount() }
    
    // Animation du blur quand une carte est sélectionnée
    val blurAmount by animateFloatAsState(
        targetValue = if (selectedCard != null) 12f else 0f,
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SharedBackground(imagePath = "backgrounds/collection_runes.jpg", overlayAlpha = 0.3f)
        
        // Contenu principal avec blur conditionnel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurAmount.dp)
        ) {
            // En-tête
            SanctuaryHeader(
                selectedFaction = selectedFaction,
                cardCount = cards.sumOf { it.second },
                totalCards = totalCount
            )
            
            // Colonnes avec cartes dessus
            CollectionColumnsWithCards(
                cards = cards,
                onCardSelected = { card ->
                    selectedCard = card
                }
            )
            
            // Barre de filtres (artefacts)
            FactionFilterBar(
                selectedFaction = selectedFaction,
                onFactionSelected = { faction ->
                    selectedFaction = if (selectedFaction == faction) null else faction
                }
            )
        }
        
        // Carte sélectionnée au premier plan
        AnimatedVisibility(
            visible = selectedCard != null,
            enter = fadeIn(animationSpec = tween(300)) + 
                    scaleIn(
                        initialScale = 0.3f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    ),
            exit = fadeOut(animationSpec = tween(250)) + 
                   scaleOut(
                       targetScale = 0.3f,
                       animationSpec = tween(
                           durationMillis = 400,
                           easing = FastOutSlowInEasing
                       )
                   ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
        ) {
            selectedCard?.let { card ->
                CardDetailOverlay(
                    card = card,
                    onClose = {
                        selectedCard = null
                    }
                )
            }
        }
    }
}

/**
 * Affiche les 3 colonnes avec les cartes positionnées dessus
 */
@Composable
fun CollectionColumnsWithCards(
    cards: List<Pair<Card, Int>>,
    onCardSelected: (Card) -> Unit
) {
    val columnImageGrec = remember {
        try {
            val file = File("shared/resources/collection_columns/greek_column.png")
            if (file.exists()) {
                SkiaImage.makeFromEncoded(file.readBytes()).asImageBitmap()
            } else null
        } catch (e: Exception) { null }
    }
    
    val columnImageEgyptian = remember {
        try {
            val file = File("shared/resources/collection_columns/egyptian_column.png")
            if (file.exists()) {
                SkiaImage.makeFromEncoded(file.readBytes()).asImageBitmap()
            } else null
        } catch (e: Exception) { null }
    }
    
    val columnImageNordic = remember {
        try {
            val file = File("shared/resources/collection_columns/nordic_column.png")
            if (file.exists()) {
                SkiaImage.makeFromEncoded(file.readBytes()).asImageBitmap()
            } else null
        } catch (e: Exception) { null }
    }
    
    // Grouper les cartes par faction
    val grecCards = cards.filter { it.first.faction == Faction.GREC }
    val egyptienCards = cards.filter { it.first.faction == Faction.EGYPTIEN }
    val nordiqueCards = cards.filter { it.first.faction == Faction.NORDIQUE }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Rang 1 : Colonne grecque avec cartes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.33f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer { clip = false }
        ) {
            if (columnImageGrec != null) {
                Image(
                    bitmap = columnImageGrec,
                    contentDescription = "Greek Column",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            // Cartes grecques
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grecCards) { (card, count) ->
                    ShelfCardItem(
                        card = card,
                        count = count,
                        onClick = { onCardSelected(card) }
                    )
                }
            }
        }
        
        // Rang 2 : Colonne égyptienne avec cartes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.33f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer { clip = false }
        ) {
            if (columnImageEgyptian != null) {
                Image(
                    bitmap = columnImageEgyptian,
                    contentDescription = "Egyptian Column",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            // Cartes égyptiennes
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(egyptienCards) { (card, count) ->
                    ShelfCardItem(
                        card = card,
                        count = count,
                        onClick = { onCardSelected(card) }
                    )
                }
            }
        }
        
        // Rang 3 : Colonne nordique avec cartes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.33f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer { clip = false }
        ) {
            if (columnImageNordic != null) {
                Image(
                    bitmap = columnImageNordic,
                    contentDescription = "Nordic Column",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            // Cartes nordiques
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nordiqueCards) { (card, count) ->
                    ShelfCardItem(
                        card = card,
                        count = count,
                        onClick = { onCardSelected(card) }
                    )
                }
            }
        }
    }
}
/**
 * Particules animées pour cartes légendaires selon la faction
 */
@Composable
fun LegendaryParticles(faction: Faction) {
    val particles = remember {
        List(8) {
            LegendaryParticle(
                offsetX = kotlin.random.Random.nextFloat() * 2f - 1f,
                offsetY = kotlin.random.Random.nextFloat() * 2f - 1f,
                delay = kotlin.random.Random.nextInt(0, 2000)
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
                radius = 8f + kotlin.random.Random.nextFloat() * 4f,
                center = Offset(dx, dy)
            )
            
            // Halo de lueur autour de la particule
            drawCircle(
                color = particleColor.copy(alpha = alpha * 0.4f),
                radius = (12f + kotlin.random.Random.nextFloat() * 6f) * 1.5f,
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
private data class Rune(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float
)

/**
 * En-tête du sanctuaire
 */
@Composable
fun SanctuaryHeader(
    selectedFaction: Faction?,
    cardCount: Int,
    totalCards: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkStone,
                        Color.Transparent
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚱️ LE SANCTUAIRE ⚱️",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GlowWhite,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (selectedFaction != null) {
                "${selectedFaction.displayName} • $cardCount cartes"
            } else {
                "Toutes les Mythologies • $totalCards cartes"
            },
            fontSize = 16.sp,
            color = GlowWhite.copy(alpha = 0.7f)
        )
    }
}

/**
 * Item de carte sur étagère - style comme l'image de référence
 */
@Composable
fun ShelfCardItem(
    card: Card,
    count: Int,
    onClick: () -> Unit
) {
    val rarityColor = when (card.rarity) {
        Rarity.COMMUNE -> RarityCommon
        Rarity.RARE -> RarityRare
        Rarity.LEGENDAIRE -> RarityLegendary
    }
    
    val factionColor = getFactionColor(card.faction)
    
    // Animation de hover/pulse pour les légendaires
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = if (card.rarity == Rarity.LEGENDAIRE) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        modifier = Modifier
            .width(140.dp)
            .scale(pulseScale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // La carte
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(180.dp)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(8.dp), clip = false)
                .clickable { onClick() }
        ) {
            
            
            // Carte au-dessus de la lueur
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                CardImage(
                    imagePath = card.imagePath,
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            
            // Effet de brillance pour les légendaires
            if (card.rarity == Rarity.LEGENDAIRE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    rarityColor.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    rarityColor.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            }
            
            // Particules légendaires au premier plan
            if (card.rarity == Rarity.LEGENDAIRE) {
                LegendaryParticles(faction = card.faction)
            }
            
            // Badge de quantité si count > 1
            if (count > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.8f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = rarityColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "x$count",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlowWhite
                    )
                }
            }
        }
        
        // Étagère/Socle selon la faction
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(
                    when (card.faction) {
                        Faction.GREC -> Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1F1F1F),
                                Color(0xFFDAD5C6), // Marbre clair
                                Color(0xFF1F1F1F)
                            )
                        )
                        Faction.EGYPTIEN -> Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2A1B0C),
                                Color(0xFFE2B66A), // Or ocre riche
                                Color(0xFF2A1B0C)
                            )
                        )
                        Faction.NORDIQUE -> Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1C242B),
                                Color(0xFF7E8C97), // Bois/fer bleuté
                                Color(0xFF1C242B)
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = factionColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                )
        ) {
            // Lumière projetée sur la pierre
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(10.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                rarityColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Barre de filtres avec artefacts physiques
 */
@Composable
fun FactionFilterBar(
    selectedFaction: Faction?,
    onFactionSelected: (Faction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        DarkStone
                    )
                )
            )
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FactionArtifact(
            faction = Faction.GREC,
            icon = "🔱",
            isSelected = selectedFaction == Faction.GREC,
            onClick = { onFactionSelected(Faction.GREC) }
        )
        
        FactionArtifact(
            faction = Faction.EGYPTIEN,
            icon = "☥",
            isSelected = selectedFaction == Faction.EGYPTIEN,
            onClick = { onFactionSelected(Faction.EGYPTIEN) }
        )
        
        FactionArtifact(
            faction = Faction.NORDIQUE,
            icon = "🔨",
            isSelected = selectedFaction == Faction.NORDIQUE,
            onClick = { onFactionSelected(Faction.NORDIQUE) }
        )
    }
}

/**
 * Artefact de faction (bouton de filtre)
 */
@Composable
fun FactionArtifact(
    faction: Faction,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val tilt by animateFloatAsState(
        targetValue = if (isSelected) -6f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )
    
    val glowIntensity by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .size(92.dp)
            .scale(scale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Socle physique oval pour l'artefact
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(18.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer { rotationZ = tilt }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DarkStone,
                            getFactionColor(faction).copy(alpha = 0.4f),
                            DarkStone
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
        
        Box(
            modifier = Modifier
                .size(82.dp)
                .graphicsLayer { rotationZ = tilt }
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                getFactionColor(faction).copy(alpha = glowIntensity),
                                getFactionColor(faction).copy(alpha = 0.15f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(LightStone, DarkStone)
                        )
                    }
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) getFactionColor(faction) else Color.Gray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = faction.displayName,
                fontSize = 10.sp,
                color = if (isSelected) {
                    getFactionColor(faction)
                } else {
                    Color.Gray
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
        
        // Halo d'activation projeté
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                getFactionColor(faction).copy(alpha = glowIntensity * 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Overlay pour afficher une carte en détail au-dessus de la collection
 */
@Composable
fun CardDetailOverlay(
    card: Card,
    onClose: () -> Unit
) {
    // État pour le parallax (simulation gyroscope avec drag)
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX = (offsetX + dragAmount.x * 0.05f).coerceIn(-30f, 30f)
                    offsetY = (offsetY + dragAmount.y * 0.05f).coerceIn(-30f, 30f)
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClose()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Carte avec effet parallax + lueur arrière
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(460.dp)
            ) {
                // Lueur derrière la carte selon la rareté
                val rarityColor = when (card.rarity) {
                    Rarity.COMMUNE -> RarityCommon
                    Rarity.RARE -> RarityRare
                    Rarity.LEGENDAIRE -> RarityLegendary
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .zIndex(-1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    rarityColor.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )
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
                
                // Couche 1 : L'image de la carte avec effet parallax
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = offsetX * 0.8f
                            translationY = offsetY * 0.8f
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
                
                // Particules légendaires si applicable
                if (card.rarity == Rarity.LEGENDAIRE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        LegendaryParticles(faction = card.faction)
                    }
                }
                
                // Retirer la bordure, la lueur suffit pour le style demandé
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nom de la carte
            Text(
                text = card.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = when (card.rarity) {
                    Rarity.COMMUNE -> RarityCommon
                    Rarity.RARE -> RarityRare
                    Rarity.LEGENDAIRE -> RarityLegendary
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Attaque
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚔️",
                        fontSize = 32.sp
                    )
                    Text(
                        text = card.attack.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                }
                
                // Santé
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "❤️",
                        fontSize = 32.sp
                    )
                    Text(
                        text = card.health.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ECDC4)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Description
            Text(
                text = card.description,
                fontSize = 14.sp,
                color = GlowWhite,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bouton de fermeture
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Touche l'arrière-plan pour fermer",
                fontSize = 12.sp,
                color = GlowWhite.copy(alpha = 0.6f)
            )
        }
    }
}
