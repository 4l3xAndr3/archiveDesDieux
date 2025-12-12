package ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

/**
 * Composant pour afficher une image de carte
 */
@Composable
fun CardImage(
    imagePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(imagePath) {
        try {
            // Essayer de charger l'image depuis les ressources
            val file = File("shared/resources/$imagePath")
            if (file.exists()) {
                val bytes = file.readBytes()
                SkiaImage.makeFromEncoded(bytes).asImageBitmap()
            } else {
                println("Fichier introuvable: ${file.absolutePath}")
                null
            }
        } catch (e: Exception) {
            println("Erreur de chargement: ${e.message}")
            null
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder si l'image ne charge pas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3A3A3A),
                                Color(0xFF1A1A1A)
                            )
                        )
                    )
            )
        }
    }
}
