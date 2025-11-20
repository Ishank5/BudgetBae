package com.humblecoders.fintrack

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.InputStream

object ReceiptScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Process an image URI and extract text using ML Kit
     */
    suspend fun extractTextFromUri(context: android.content.Context, imageUri: Uri): Result<String> {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                return Result.failure(Exception("Failed to decode image"))
            }
            
            extractTextFromBitmap(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Process a bitmap and extract text using ML Kit
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): Result<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            
            val extractedText = result.text
            // Note: Text object doesn't need explicit closing in ML Kit v2
            
            Result.success(extractedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Close the recognizer when done (call this when app is destroyed)
     * Note: In ML Kit, the recognizer is managed automatically, but you can close it if needed
     */
    fun close() {
        // Recognizer is managed by ML Kit and doesn't need explicit closing
        // This method is kept for future use if needed
    }
}

