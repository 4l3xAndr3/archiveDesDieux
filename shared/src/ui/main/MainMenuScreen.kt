package ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import models.Card
import models.Faction
import ui.booster.BoosterOpeningScreen
import ui.booster.getFactionColor
import ui.collection.CardDetailScreen
import ui.collection.CollectionScreen
import ui.battle.FactionSelectionScreen
import ui.battle.BattleArenaScreen
import ui.theme.*
import ui.common.SharedBackground

/**
 * Écran principal avec menu de navigation
 */
@Composable
fun MainMenuScreen() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Contenu principal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(onNavigate = { screen ->
                            currentScreen = screen
                        })
                    }
                    is Screen.Packs -> {
                        PacksScreen(
                            onOpenBooster = { faction, packType ->
                                currentScreen = Screen.BoosterOpening(faction, packType)
                            }
                        )
                    }
                    is Screen.BoosterOpening -> {
                        BoosterOpeningScreen(
                            faction = screen.faction,
                            packType = screen.packType,
                            onBoosterOpened = { cards ->
                                currentScreen = Screen.Collection
                            },
                            onBack = {
                                currentScreen = Screen.Packs
                            }
                        )
                    }
                    is Screen.Collection -> {
                        CollectionScreen(
                            onCardSelected = { },
                            onBack = {
                                currentScreen = Screen.Home
                            }
                        )
                    }
                    is Screen.FactionSelection -> {
                        FactionSelectionScreen(
                            onFactionSelected = { faction ->
                                currentScreen = Screen.BattleArena(faction)
                            },
                            onBack = {
                                currentScreen = Screen.Home
                            }
                        )
                    }
                    is Screen.BattleArena -> {
                        BattleArenaScreen(
                            faction = screen.faction,
                            onBack = {
                                currentScreen = Screen.Home
                            }
                        )
                    }
                }
            }
            
            // Barre de navigation en bas
            if (currentScreen !is Screen.BoosterOpening) {
                NavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                    }
                )
            }
        }
    }
}

/**
 * Les différents écrans de l'application
 */
sealed class Screen {
    object Home : Screen()
    object Packs : Screen()
    object Collection : Screen()
    object FactionSelection : Screen()
    data class BattleArena(val faction: Faction) : Screen()
    data class BoosterOpening(val faction: Faction, val packType: models.PackType = models.PackType.STANDARD) : Screen()
}

/**
 * Barre de navigation en bas
 */
@Composable
fun NavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
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
        NavigationItem(
            iconPath = "menu_icons/temple.png",
            isSelected = currentScreen is Screen.Home,
            onClick = { onNavigate(Screen.Home) }
        )
        
        NavigationItem(
            iconPath = "menu_icons/chest.png",
            isSelected = currentScreen is Screen.Packs || currentScreen is Screen.BoosterOpening,
            onClick = { onNavigate(Screen.Packs) }
        )
        
        NavigationItem(
            iconPath = "menu_icons/book.png",
            isSelected = currentScreen is Screen.Collection,
            onClick = { onNavigate(Screen.Collection) }
        )
    }
}

/**
 * Item de navigation avec image
 */
@Composable
fun NavigationItem(
    iconPath: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconImage = remember(iconPath) {
        try {
            val file = File("shared/resources/$iconPath")
            if (file.exists()) {
                val bytes = file.readBytes()
                SkiaImage.makeFromEncoded(bytes).asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                GlowBlue.copy(alpha = 0.3f)
            } else {
                Color.Transparent
            }
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .size(120.dp)
    ) {
        if (iconImage != null) {
            Image(
                bitmap = iconImage,
                contentDescription = "Menu",
                modifier = Modifier.size(if (isSelected) 110.dp else 100.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Page d'accueil
 */
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SharedBackground(overlayAlpha = 0.6f)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Titre du jeu
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚱️",
                    fontSize = 100.sp
                )
                Text(
                    text = "L'ARCHIVE",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowWhite,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "DES DIEUX",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlowBlue,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Holo-Antique",
                    fontSize = 14.sp,
                    color = GlowWhite.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
            }
            
            // Description
            Text(
                text = "Collectionne les cartes des dieux de toutes les mythologies",
                fontSize = 16.sp,
                color = GlowWhite.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Bouton PLAY
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth(0.6f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFF1744)
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { onNavigate(Screen.FactionSelection) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶ JOUER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }
            
            // Description
            
            // Statistiques rapides
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    icon = "⚡",
                    value = "5",
                    label = "Grec"
                )
                StatCard(
                    icon = "☥",
                    value = "5",
                    label = "Égyptien"
                )
                StatCard(
                    icon = "❄️",
                    value = "5",
                    label = "Nordique"
                )
            }
        }
    }
}

/**
 * Carte de statistique
 */
@Composable
fun StatCard(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                DarkStone.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = icon,
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GlowBlue
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = GlowWhite.copy(alpha = 0.7f)
        )
    }
}

/**
 * Page des packs à ouvrir
 */
@Composable
fun PacksScreen(
    onOpenBooster: (Faction, models.PackType) -> Unit
) {
    var selectedPackType by remember { mutableStateOf(models.PackType.STANDARD) }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SharedBackground(overlayAlpha = 0.6f)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Titre
            Text(
                text = "📦 BOUTIQUE DE PACKS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = GlowWhite,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Sélection du type de pack
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PackTypeButton(
                    packType = models.PackType.STANDARD,
                    isSelected = selectedPackType == models.PackType.STANDARD,
                    onClick = { selectedPackType = models.PackType.STANDARD }
                )
                
                PackTypeButton(
                    packType = models.PackType.LEGENDARY,
                    isSelected = selectedPackType == models.PackType.LEGENDARY,
                    onClick = { selectedPackType = models.PackType.LEGENDARY }
                )
            }
            
            Text(
                text = "Choisis une mythologie",
                fontSize = 16.sp,
                color = GlowWhite.copy(alpha = 0.7f)
            )
            
            // Boutons de factions
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FactionButton(
                    faction = Faction.GREC,
                    icon = "⚡",
                    description = "Boîte de Pandore",
                    onClick = { onOpenBooster(Faction.GREC, selectedPackType) }
                )
                
                FactionButton(
                    faction = Faction.EGYPTIEN,
                    icon = "☥",
                    description = "Sarcophage Sacré",
                    onClick = { onOpenBooster(Faction.EGYPTIEN, selectedPackType) }
                )
                
                FactionButton(
                    faction = Faction.NORDIQUE,
                    icon = "❄️",
                    description = "Pierre Runique Gelée",
                    onClick = { onOpenBooster(Faction.NORDIQUE, selectedPackType) }
                )
            }
        }
    }
}

/**
 * Bouton pour sélectionner le type de pack
 */
@Composable
fun PackTypeButton(
    packType: models.PackType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                packType.displayName.takeIf { packType == models.PackType.LEGENDARY }?.let {
                    RarityLegendary.copy(alpha = 0.3f)
                } ?: RarityRare.copy(alpha = 0.3f)
            } else {
                Color.Transparent
            }
        ),
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(100.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    if (packType == models.PackType.LEGENDARY) RarityLegendary else RarityRare
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = packType.icon,
                fontSize = 40.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = packType.displayName,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) GlowWhite else GlowWhite.copy(alpha = 0.7f)
            )
            Text(
                text = packType.description,
                fontSize = 12.sp,
                color = if (isSelected) {
                    if (packType == models.PackType.LEGENDARY) RarityLegendary else RarityRare
                } else {
                    GlowWhite.copy(alpha = 0.5f)
                }
            )
        }
    }
}

/**
 * Bouton pour sélectionner une faction avec image du pack
 */
@Composable
fun FactionButton(
    faction: Faction,
    icon: String,
    description: String,
    onClick: () -> Unit
) {
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
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        getFactionColor(faction).copy(alpha = 0.3f),
                        getFactionColor(faction).copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Image du pack
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        getFactionColor(faction).copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (packImage != null) {
                    Image(
                        bitmap = packImage,
                        contentDescription = "Pack ${faction.displayName}",
                        modifier = Modifier.size(70.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback sur l'icône emoji
                    Text(
                        text = icon,
                        fontSize = 40.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Texte
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = faction.displayName.uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = getFactionColor(faction)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = GlowWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}
