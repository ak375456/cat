package com.lexur.yumo.custom_character.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexur.yumo.custom_character.domain.CustomCharacter
import com.lexur.yumo.home_screen.data.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.pow
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers

data class CustomCharacterUiState(
    val selectedImageUri: Uri? = null,
    val isBackgroundRemovalMode: Boolean = false,
    val brushSize: Float = 30f,
    val maskPath: Path = Path(),
    val currentStrokePath: Path = Path(),
    val previewPosition: Offset? = null,
    val canvasSize: IntSize = IntSize.Zero,
    val strokeHistory: List<Path> = emptyList(),
    val isDrawing: Boolean = false,
    val lastDrawnPoint: Offset? = null,
    val selectedRopeResId: Int? = null,
    val characterName: String = "",
    val imageTransformation: ImageTransformation? = null,
    val isSaving: Boolean = false,
    val saveComplete: Boolean = false,
    val ropeScale: Float = 0.7f,
    val ropeOffsetX: Float = 0f,
    val ropeOffsetY: Float = 0f,
    val showRopeAdjustment: Boolean = false,
    val characterScale: Float = 1f,
)

@HiltViewModel
class CustomCharacterCreationViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomCharacterUiState())
    val uiState = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                isBackgroundRemovalMode = false,
                maskPath = Path(),
                currentStrokePath = Path(),
                strokeHistory = emptyList(),
                lastDrawnPoint = null
            )
        }
    }

    fun toggleBackgroundRemovalMode() {
        _uiState.update {
            it.copy(
                isBackgroundRemovalMode = !it.isBackgroundRemovalMode,
                previewPosition = null
            )
        }
    }

    fun updateBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun startDrawing(offset: Offset) {
        _uiState.update { currentState ->
            val newHistory = currentState.strokeHistory + currentState.maskPath
            val newStrokePath = Path().apply {
                addOval(Rect(center = offset, radius = currentState.brushSize / 2))
            }
            currentState.copy(
                currentStrokePath = newStrokePath,
                strokeHistory = newHistory,
                isDrawing = true,
                lastDrawnPoint = offset
            )
        }
    }

    fun continueDrawing(offset: Offset) {
        if (!_uiState.value.isDrawing) return
        _uiState.update { currentState ->
            val lastPoint = currentState.lastDrawnPoint ?: return@update currentState
            val newStrokePath = Path().apply {
                addPath(currentState.currentStrokePath)
                val distance = kotlin.math.sqrt((offset.x - lastPoint.x).pow(2) + (offset.y - lastPoint.y).pow(2))
                val steps = (distance / (currentState.brushSize * 0.25f)).toInt().coerceAtLeast(1)
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val interpolatedPoint = Offset(
                        lastPoint.x + (offset.x - lastPoint.x) * t,
                        lastPoint.y + (offset.y - lastPoint.y) * t
                    )
                    addOval(Rect(center = interpolatedPoint, radius = currentState.brushSize / 2))
                }
            }
            currentState.copy(currentStrokePath = newStrokePath, lastDrawnPoint = offset)
        }
    }

    fun endDrawing() {
        _uiState.update { currentState ->
            val newMaskPath = Path().apply {
                addPath(currentState.maskPath)
                addPath(currentState.currentStrokePath)
            }
            currentState.copy(
                maskPath = newMaskPath,
                currentStrokePath = Path(),
                isDrawing = false,
                lastDrawnPoint = null
            )
        }
    }

    fun updatePreviewPosition(position: Offset?) {
        _uiState.value = _uiState.value.copy(previewPosition = position)
    }

    fun updateCanvasSize(size: IntSize) {
        _uiState.value = _uiState.value.copy(canvasSize = size)
    }

    fun undoLastStroke() {
        _uiState.update { currentState ->
            if (currentState.strokeHistory.isNotEmpty()) {
                val previousPath = currentState.strokeHistory.last()
                currentState.copy(
                    maskPath = previousPath,
                    strokeHistory = currentState.strokeHistory.dropLast(1),
                    currentStrokePath = Path()
                )
            } else {
                currentState.copy(maskPath = Path(), currentStrokePath = Path())
            }
        }
    }

    fun resetImage() {
        _uiState.value = _uiState.value.copy(
            maskPath = Path(),
            currentStrokePath = Path(),
            strokeHistory = emptyList(),
            isBackgroundRemovalMode = false,
            lastDrawnPoint = null
        )
    }

    fun finishEditing() {
        _uiState.value = _uiState.value.copy(isBackgroundRemovalMode = false, previewPosition = null)
    }

    fun onRopeSelected(ropeResId: Int) {
        _uiState.update { it.copy(
            selectedRopeResId = ropeResId,
            showRopeAdjustment = true,
            ropeScale = 1f,
            ropeOffsetX = 0f,
            ropeOffsetY = 0f,
            characterScale = 1f
        )}
    }

    fun onCharacterNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(characterName = name)
    }

    fun updateCharacterScale(scale: Float) {
        _uiState.update { it.copy(characterScale = scale) }
    }

    // --- FIX START: Reworked save function for background execution and UI state updates ---
    fun saveCustomCharacter(context: Context) {
        val currentState = _uiState.value
        if (currentState.selectedImageUri == null || currentState.selectedRopeResId == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Get the character bitmap with background removed
                val characterBitmap = createTransparentBitmapFromUri(
                    context,
                    currentState.selectedImageUri,
                    currentState.maskPath
                )

                // 2. Get the selected rope bitmap
                val ropeBitmap = BitmapFactory.decodeResource(context.resources, currentState.selectedRopeResId)

                // 3. Combine them into a single bitmap
                val combinedBitmap = combineCharacterAndRope(
                    characterBitmap,
                    ropeBitmap,
                    currentState.ropeScale,
                    currentState.ropeOffsetX,
                    currentState.ropeOffsetY,
                    currentState.characterScale
                )

                // 4. Save the combined bitmap to a file
                val fileName = "custom_char_${System.currentTimeMillis()}.png"
                val savedImagePath = saveBitmapToFile(context, combinedBitmap, fileName)

                // 5. Create the database entity with the new fields
                val character = CustomCharacter(
                    name = currentState.characterName,
                    imagePath = savedImagePath,
                    ropeResId = currentState.selectedRopeResId,
                    ropeScale = currentState.ropeScale,
                    ropeOffsetX = currentState.ropeOffsetX,
                    ropeOffsetY = currentState.ropeOffsetY,
                    characterScale = currentState.characterScale
                )

                // 6. Insert into the database
                characterRepository.insertCustomCharacter(character)

                _uiState.update { it.copy(isSaving = false, saveComplete = true) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onNavigationComplete() {
        _uiState.update { it.copy(saveComplete = false) }
    }

    private fun createTransparentBitmapFromUri(context: Context, imageUri: Uri, maskPath: Path): Bitmap {
        // Load original bitmap
        val originalBitmap = context.contentResolver.openInputStream(imageUri)?.use {
            BitmapFactory.decodeStream(it)
        } ?: throw IllegalStateException("Could not load bitmap from URI")

        // Create a mutable copy for drawing
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Create paint to "erase" parts of the bitmap
        val erasePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        // Apply the mask path
        canvas.drawPath(maskPath.asAndroidPath(), erasePaint)

        return mutableBitmap
    }

    private fun cropTransparentBorders(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        var top = 0
        var bottom = height
        var left = 0
        var right = width

        // Find top boundary
        topLoop@ for (y in 0 until height) {
            for (x in 0 until width) {
                if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 0) {
                    top = y
                    break@topLoop
                }
            }
        }

        // Find bottom boundary
        bottomLoop@ for (y in height - 1 downTo top) {
            for (x in 0 until width) {
                if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 0) {
                    bottom = y + 1
                    break@bottomLoop
                }
            }
        }

        // Find left boundary
        leftLoop@ for (x in 0 until width) {
            for (y in top until bottom) {
                if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 0) {
                    left = x
                    break@leftLoop
                }
            }
        }

        // Find right boundary
        rightLoop@ for (x in width - 1 downTo left) {
            for (y in top until bottom) {
                if (android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 0) {
                    right = x + 1
                    break@rightLoop
                }
            }
        }

        // If no non-transparent pixels found, return original
        if (top >= bottom || left >= right) {
            return bitmap
        }

        // Create cropped bitmap
        val croppedWidth = right - left
        val croppedHeight = bottom - top

        return Bitmap.createBitmap(bitmap, left, top, croppedWidth, croppedHeight)
    }

    // Replace your existing combineCharacterAndRope function
    private fun combineCharacterAndRope(
        characterBitmap: Bitmap,
        ropeBitmap: Bitmap,
        ropeScale: Float,
        ropeOffsetX: Float,
        ropeOffsetY: Float,
        characterScale: Float,
    ): Bitmap {
        // Crop transparent borders from character
        val croppedCharacterBitmap = cropTransparentBorders(characterBitmap)

        // Scale the cropped character bitmap
        val scaledCharacterWidth = (croppedCharacterBitmap.width * characterScale).toInt()
        val scaledCharacterHeight = (croppedCharacterBitmap.height * characterScale).toInt()
        val scaledCharacterBitmap = croppedCharacterBitmap.scale(scaledCharacterWidth, scaledCharacterHeight)

        // Scale the rope bitmap
        val scaledRopeWidth = (ropeBitmap.width * ropeScale).toInt()
        val scaledRopeHeight = (ropeBitmap.height * ropeScale).toInt()
        val scaledRopeBitmap = ropeBitmap.scale(scaledRopeWidth, scaledRopeHeight)

        // Calculate total content dimensions
        val totalContentWidth = scaledCharacterWidth.coerceAtLeast(scaledRopeWidth)
        val totalContentHeight = scaledRopeHeight + scaledCharacterHeight

        // Add padding for offsets
        val paddingTop = kotlin.math.max(0f, -ropeOffsetY).toInt()
        val paddingBottom = kotlin.math.max(0f, ropeOffsetY).toInt()
        val paddingLeft = kotlin.math.max(0f, -ropeOffsetX).toInt()
        val paddingRight = kotlin.math.max(0f, ropeOffsetX).toInt()

        val canvasWidth = totalContentWidth + paddingLeft + paddingRight
        val canvasHeight = totalContentHeight + paddingTop + paddingBottom

        // Create combined bitmap
        val combinedBitmap = createBitmap(canvasWidth, canvasHeight)
        val canvas = Canvas(combinedBitmap)

        // Calculate rope position
        val ropeX = paddingLeft + (totalContentWidth - scaledRopeWidth) / 2f + ropeOffsetX
        val ropeY = paddingTop + ropeOffsetY

        // Draw rope first
        canvas.drawBitmap(scaledRopeBitmap, ropeX, ropeY, null)

        // Calculate character position (directly below rope)
        val characterX = paddingLeft + (totalContentWidth - scaledCharacterWidth) / 2f
        val characterY = paddingTop + scaledRopeHeight.toFloat()

        // Draw character
        canvas.drawBitmap(scaledCharacterBitmap, characterX, characterY, null)

        scaledRopeBitmap.recycle()
        scaledCharacterBitmap.recycle()
        croppedCharacterBitmap.recycle()

        return combinedBitmap
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): String {
        val directory = File(context.filesDir, "custom_characters")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.absolutePath
    }

    fun updateRopeScale(scale: Float) {
        _uiState.update { it.copy(ropeScale = scale) }
    }

    fun updateRopeOffsetX(offsetX: Float) {
        _uiState.update { it.copy(ropeOffsetX = offsetX) }
    }

    fun updateRopeOffsetY(offsetY: Float) {
        _uiState.update { it.copy(ropeOffsetY = offsetY) }
    }

    fun finishRopeAdjustment() {
        _uiState.update { it.copy(showRopeAdjustment = false) }
    }
}
