package com.lexur.yumo.custom_character.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import androidx.compose.ui.geometry.Offset
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.pow

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
    val characterName: String = ""
)

@HiltViewModel
class CustomCharacterCreationViewModel @Inject constructor(
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomCharacterUiState())
    val uiState = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            isBackgroundRemovalMode = false,
            maskPath = Path(),
            currentStrokePath = Path(),
            strokeHistory = emptyList(),
            lastDrawnPoint = null
        )
    }

    fun toggleBackgroundRemovalMode() {
        _uiState.value = _uiState.value.copy(
            isBackgroundRemovalMode = !_uiState.value.isBackgroundRemovalMode,
            previewPosition = null
        )
    }

    fun updateBrushSize(size: Float) {
        _uiState.value = _uiState.value.copy(brushSize = size)
    }

    fun startDrawing(offset: Offset) {
        val currentState = _uiState.value
        val newHistory = currentState.strokeHistory + currentState.maskPath
        val newStrokePath = Path().apply { moveTo(offset.x, offset.y) }
        _uiState.value = currentState.copy(
            currentStrokePath = newStrokePath,
            strokeHistory = newHistory,
            isDrawing = true,
            lastDrawnPoint = offset
        )
        addPointToMask(offset)
    }

    fun continueDrawing(offset: Offset) {
        if (!_uiState.value.isDrawing) return
        val currentState = _uiState.value
        val lastPoint = currentState.lastDrawnPoint ?: return
        val distance = kotlin.math.sqrt(
            (offset.x - lastPoint.x).pow(2) + (offset.y - lastPoint.y).pow(2)
        )
        val steps = (distance / (currentState.brushSize * 0.25f)).toInt().coerceAtLeast(1)
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val interpolatedPoint = Offset(
                lastPoint.x + (offset.x - lastPoint.x) * t,
                lastPoint.y + (offset.y - lastPoint.y) * t
            )
            addPointToMask(interpolatedPoint)
        }
        _uiState.value = currentState.copy(lastDrawnPoint = offset)
    }

    fun endDrawing() {
        _uiState.value = _uiState.value.copy(
            isDrawing = false,
            currentStrokePath = Path(),
            lastDrawnPoint = null
        )
    }

    private fun addPointToMask(offset: Offset) {
        val currentState = _uiState.value
        val newMaskPath = Path().apply {
            addPath(currentState.maskPath)
            addOval(androidx.compose.ui.geometry.Rect(center = offset, radius = currentState.brushSize / 2))
        }
        val newStrokePath = Path().apply {
            addPath(currentState.currentStrokePath)
            addOval(androidx.compose.ui.geometry.Rect(center = offset, radius = currentState.brushSize / 2))
        }
        _uiState.value = currentState.copy(
            maskPath = newMaskPath,
            currentStrokePath = newStrokePath
        )
    }

    fun updatePreviewPosition(position: Offset?) {
        if (!_uiState.value.isDrawing) {
            _uiState.value = _uiState.value.copy(previewPosition = position)
        }
    }

    fun updateCanvasSize(size: IntSize) {
        _uiState.value = _uiState.value.copy(canvasSize = size)
    }

    fun undoLastStroke() {
        val currentState = _uiState.value
        if (currentState.strokeHistory.isNotEmpty()) {
            val previousPath = currentState.strokeHistory.last()
            _uiState.value = currentState.copy(
                maskPath = previousPath,
                strokeHistory = currentState.strokeHistory.dropLast(1)
            )
        } else {
            _uiState.value = currentState.copy(maskPath = Path())
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
        _uiState.value = _uiState.value.copy(
            isBackgroundRemovalMode = false,
            previewPosition = null
        )
    }

    fun onRopeSelected(ropeResId: Int) {
        _uiState.value = _uiState.value.copy(selectedRopeResId = ropeResId)
    }

    fun onCharacterNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(characterName = name)
    }

    fun saveCustomCharacter(context: Context) {
        viewModelScope.launch {
            val state = _uiState.value
            val imageUri = state.selectedImageUri ?: return@launch
            val ropeResId = state.selectedRopeResId ?: return@launch
            val characterName = state.characterName.ifBlank { "Custom Character" }

            val processedBitmap = processImage(context, imageUri, state.maskPath, state.canvasSize)
            val finalBitmap = combineImageAndRope(context, processedBitmap, ropeResId)

            val file = saveBitmapAsWebp(context, finalBitmap, characterName)
            if (file != null) {
                val customCharacter = CustomCharacter(
                    name = characterName,
                    imagePath = file.absolutePath,
                    ropeResId = ropeResId
                )
                characterRepository.insertCustomCharacter(customCharacter)
            }
        }
    }

    private fun processImage(context: Context, imageUri: Uri, maskPath: Path, canvasSize: IntSize): Bitmap {
        val originalBitmap = context.contentResolver.openInputStream(imageUri).use {
            android.graphics.BitmapFactory.decodeStream(it)
        }
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, canvasSize.width, canvasSize.height, true)

        val maskBitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ALPHA_8)
        val maskCanvas = Canvas(maskBitmap)
        val maskPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        maskCanvas.drawPath(maskPath.asAndroidPath(), maskPaint)

        val resultBitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(resultBitmap)
        val resultPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        resultCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)
        resultCanvas.drawBitmap(maskBitmap, 0f, 0f, resultPaint)

        return resultBitmap
    }

    private fun combineImageAndRope(context: Context, characterBitmap: Bitmap, ropeResId: Int): Bitmap {
        val ropeDrawable = context.getDrawable(ropeResId)
        val ropeBitmap = ropeDrawable?.toBitmap() ?: return characterBitmap

        val ropeWidth = ropeBitmap.width
        val ropeHeight = ropeBitmap.height
        val characterWidth = characterBitmap.width
        val characterHeight = characterBitmap.height

        val combinedBitmap = Bitmap.createBitmap(
            characterWidth,
            ropeHeight + characterHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combinedBitmap)

        val ropeX = (characterWidth - ropeWidth) / 2f
        canvas.drawBitmap(ropeBitmap, ropeX, 0f, null)
        canvas.drawBitmap(characterBitmap, 0f, ropeHeight.toFloat(), null)

        return combinedBitmap
    }

    private fun saveBitmapAsWebp(context: Context, bitmap: Bitmap, name: String): File? {
        return try {
            val directory = File(context.filesDir, "custom_characters")
            if (!directory.exists()) {
                directory.mkdirs()
            }
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
}
