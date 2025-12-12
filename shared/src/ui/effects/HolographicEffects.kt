package ui.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * Texte avec effet holographique flottant - scanlines et lueur
 * Inspiré par les interfaces Holo-Antique du moodboard
 */
@Composable
fun HolographicFloatingText(
    text: String,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Animation de shimmer/scintillement
    val shimmer by rememberInfiniteTransition().animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Animation de flottement vertical léger
    val float by rememberInfiniteTransition().animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(modifier = modifier.offset(y = float.dp)) {
        // Couche 1 : Glow externe (halo lumineux)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.4f * shimmer),
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp)
                .blur(8.dp)
        )
        
        // Couche 2 : Ombre/profondeur
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.offset(x = 1.5.dp, y = 1.5.dp)
        )
        
        // Couche 3 : Texte principal avec shimmer
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = shimmer)
        )
        
        // Couche 4 : Highlight supérieur (effet de surface lumineuse)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.3f * shimmer),
            modifier = Modifier.offset(x = 0.dp, y = -0.5.dp)
        )
    }
}

/**
 * Effet de scanlines holographiques pour superposer sur du contenu
 */
@Composable
fun HolographicScanlines(
    color: Color,
    modifier: Modifier = Modifier
) {
    val offset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Canvas(modifier = modifier) {
        val lineSpacing = 4f
        var y = offset % lineSpacing
        
        while (y < size.height) {
            drawLine(
                color = color.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }
    }
}

/**
 * Cadre de statistique holographique avec effet de projection
 */
@Composable
fun HolographicStatFrame(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Animation du cadre (pulsation légère)
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(modifier = modifier) {
        // Fond holographique
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // Rectangle de fond avec bordures lumineuses
            val path = Path().apply {
                // Forme hexagonale stylée
                moveTo(10f, 0f)
                lineTo(w - 10f, 0f)
                lineTo(w, h / 2f)
                lineTo(w - 10f, h)
                lineTo(10f, h)
                lineTo(0f, h / 2f)
                close()
            }
            
            // Remplissage semi-transparent
            drawPath(
                path = path,
                color = color.copy(alpha = 0.15f)
            )
            
            // Bordure lumineuse
            drawPath(
                path = path,
                color = color.copy(alpha = 0.8f * pulse),
                style = Stroke(width = 2f)
            )
            
            // Ligne de scan horizontale
            val scanY = (System.currentTimeMillis() % 2000) / 2000f * h
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, scanY),
                end = Offset(w, scanY),
                strokeWidth = 2f
            )
        }
        
        // Textes holographiques
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            HolographicFloatingText(
                text = label,
                fontSize = 16.sp,
                color = color.copy(alpha = 0.9f)
            )
            
            HolographicFloatingText(
                text = value,
                fontSize = 24.sp,
                color = color
            )
        }
    }
}

/**
 * Effet de grille holographique d'arrière-plan
 */
@Composable
fun HolographicGrid(
    color: Color,
    modifier: Modifier = Modifier
) {
    val shimmer by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Canvas(modifier = modifier) {
        val gridSpacing = 40f
        
        // Lignes verticales
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color.copy(alpha = shimmer * 0.2f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSpacing
        }
        
        // Lignes horizontales
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = color.copy(alpha = shimmer * 0.2f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSpacing
        }
    }
}
