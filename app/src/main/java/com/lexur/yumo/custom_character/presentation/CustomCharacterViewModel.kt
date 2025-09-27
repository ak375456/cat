package com.lexur.yumo.custom_character.presentation

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val lastDrawnPoint: Offset? = null
)

@HiltViewModel
class CustomCharacterCreationViewModel @Inject constructor() : ViewModel() {

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

        // Save current mask to history before starting new stroke
        val newHistory = currentState.strokeHistory + currentState.maskPath

        // Start a new stroke path
        val newStrokePath = Path().apply {
            moveTo(offset.x, offset.y)
        }

        _uiState.value = currentState.copy(
            currentStrokePath = newStrokePath,
            strokeHistory = newHistory,
            isDrawing = true,
            lastDrawnPoint = offset
        )

        // Add the first point
        addPointToMask(offset)
    }

    fun continueDrawing(offset: Offset) {
        if (!_uiState.value.isDrawing) return

        val currentState = _uiState.value
        val lastPoint = currentState.lastDrawnPoint ?: return

        // Interpolate points between last and current position for smooth line
        val distance = kotlin.math.sqrt(
            (offset.x - lastPoint.x) * (offset.x - lastPoint.x) +
                    (offset.y - lastPoint.y) * (offset.y - lastPoint.y)
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
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = offset,
                    radius = currentState.brushSize / 2
                )
            )
        }

        val newStrokePath = Path().apply {
            addPath(currentState.currentStrokePath)
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = offset,
                    radius = currentState.brushSize / 2
                )
            )
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

    fun saveProcessedImage() {
        viewModelScope.launch {
            // TODO: Implement actual image processing and saving
            // This would involve:
            // 1. Converting the mask path to a bitmap
            // 2. Applying the mask to remove background from original image
            // 3. Saving the processed image

            // For now, just finish editing
            finishEditing()
        }
    }
}