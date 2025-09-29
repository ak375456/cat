package com.lexur.yumo.custom_character.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
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
import kotlinx.coroutines.withContext

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
    val ropeScale: Float = 1f,
    val ropeOffsetX: Float = 0f,
    val ropeOffsetY: Float = 0f,
    val showRopeAdjustment: Boolean = false
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
            ropeOffsetY = 0f
        )}
    }

    fun onCharacterNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(characterName = name)
    }

    // --- FIX START: Reworked save function for background execution and UI state updates ---
    fun saveCustomCharacter(context: Context) {
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val state = _uiState.value
                    val imageUri = state.selectedImageUri ?: return@withContext false
                    val ropeResId = state.selectedRopeResId ?: return@withContext false
                    val characterName = state.characterName.ifBlank { "Custom Character" }

                    val processedBitmap = processImage(context, imageUri, state.maskPath, state.canvasSize)
                    val finalBitmap = combineImageAndRope(
                        context,
                        processedBitmap,
                        ropeResId,
                        state.ropeScale,
                        state.ropeOffsetX,
                        state.ropeOffsetY
                    )
                    val file = saveBitmapAsWebp(context, finalBitmap, characterName)
                    if (file != null) {
                        val customCharacter = CustomCharacter(
                            name = characterName,
                            imagePath = file.absolutePath,
                            ropeResId = ropeResId
                        )
                        characterRepository.insertCustomCharacter(customCharacter)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                _uiState.update { it.copy(isSaving = false, saveComplete = true) }
            } else {
                // Optionally handle the error case, e.g., show a toast
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onNavigationComplete() {
        _uiState.update { it.copy(saveComplete = false) }
    }
    // --- FIX END ---


    private suspend fun processImage(
        context: Context,
        imageUri: Uri,
        maskPath: Path,
        canvasSize: IntSize
    ): Bitmap = withContext(Dispatchers.IO) {
        val originalBitmap = context.contentResolver.openInputStream(imageUri).use {
            BitmapFactory.decodeStream(it)
        }

        val imageRatio = originalBitmap.width.toFloat() / originalBitmap.height
        val canvasRatio = canvasSize.width.toFloat() / canvasSize.height

        val scaleFactor: Float
        val displayedImageSize: Size

        if (imageRatio > canvasRatio) {
            scaleFactor = canvasSize.width.toFloat() / originalBitmap.width
            displayedImageSize = Size(width = canvasSize.width.toFloat(), height = originalBitmap.height * scaleFactor)
        } else {
            scaleFactor = canvasSize.height.toFloat() / originalBitmap.height
            displayedImageSize = Size(width = originalBitmap.width * scaleFactor, height = canvasSize.height.toFloat())
        }

        val resultBitmap = createBitmap(displayedImageSize.width.toInt(), displayedImageSize.height.toInt())
        val resultCanvas = android.graphics.Canvas(resultBitmap)
        val scaledBitmap = originalBitmap.scale(displayedImageSize.width.toInt(), displayedImageSize.height.toInt())

        resultCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        val erasePaint = android.graphics.Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }

        val matrix = android.graphics.Matrix().apply { setScale(scaleFactor, scaleFactor) }
        val scaledPath = android.graphics.Path()
        maskPath.asAndroidPath().transform(matrix, scaledPath)
        resultCanvas.drawPath(scaledPath, erasePaint)

        originalBitmap.recycle()
        scaledBitmap.recycle()

        return@withContext resultBitmap
    }

    private fun combineImageAndRope(
        context: Context,
        characterBitmap: Bitmap,
        ropeResId: Int,
        ropeScale: Float = 1f,
        ropeOffsetX: Float = 0f,
        ropeOffsetY: Float = 0f
    ): Bitmap {
        val ropeDrawable = context.getDrawable(ropeResId)
        val originalRopeBitmap = ropeDrawable?.toBitmap() ?: return characterBitmap

        val scaledRopeWidth = (originalRopeBitmap.width * ropeScale).toInt()
        val scaledRopeHeight = (originalRopeBitmap.height * ropeScale).toInt()
        val ropeBitmap = Bitmap.createScaledBitmap(
            originalRopeBitmap,
            scaledRopeWidth,
            scaledRopeHeight,
            true
        )

        val characterWidth = characterBitmap.width
        val characterHeight = characterBitmap.height

        val combinedBitmap = createBitmap(characterWidth, scaledRopeHeight + characterHeight)
        val canvas = android.graphics.Canvas(combinedBitmap)

        val ropeX = (characterWidth - scaledRopeWidth) / 2f + ropeOffsetX
        val ropeY = ropeOffsetY
        canvas.drawBitmap(ropeBitmap, ropeX, ropeY, null)
        canvas.drawBitmap(characterBitmap, 0f, scaledRopeHeight.toFloat(), null)

        originalRopeBitmap.recycle()
        ropeBitmap.recycle()

        return combinedBitmap
    }

    private fun saveBitmapAsWebp(context: Context, bitmap: Bitmap, name: String): File? {
        return try {
            val directory = File(context.filesDir, "custom_characters")
            if (!directory.exists()) { directory.mkdirs() }
            val file = File(directory, "${name.replace(" ", "_")}_${System.currentTimeMillis()}.webp")
            FileOutputStream(file).use { out ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateImageTransformation(transformation: ImageTransformation?) {
        _uiState.update { it.copy(imageTransformation = transformation) }
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
