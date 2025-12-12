package ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

/**
 * Fond commun pour tous les écrans - image du temple ou fallback
 */
@Composable
fun SharedBackground(
    imagePath: String = "backgrounds/sanctuary_bg.jpg",
    modifier: Modifier = Modifier,
    overlayAlpha: Float = 0.5f
) {
    val imageBitmap = remember(imagePath) {
        try {
            val file = File("shared/resources/$imagePath")
            if (file.exists()) {
                val bytes = file.readBytes()
                SkiaImage.makeFromEncoded(bytes).asImageBitmap()
            } else {
                println("Image de fond introuvable: ${file.absolutePath}")
                null
            }
        } catch (e: Exception) {
            println("Erreur de chargement du fond: ${e.message}")
            null
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (imageBitmap != null) {
            // Image du temple
            Image(
                bitmap = imageBitmap,
                contentDescription = "Fond",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay sombre pour lisibilité
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha))
            )
        } else {
            // Fallback gradient sombre
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0A0A),
                                Color(0xFF1A1A1A),
                                Color(0xFF0A0A0A)
                            )
                        )
                    )
            )
        }
    }
}
