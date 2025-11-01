package com.lexur.yumo.custom_character.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.Dispatchers
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.lexur.yumo.R
import com.lexur.yumo.util.EmojiUtils

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
    val isPanningMode: Boolean = false,
    val canvasOffset: Offset = Offset.Zero,
    val canvasScale: Float = 1f,
    val featheringSize: Float = 10f,
    val isStrokeEnabled: Boolean = false,
    val strokeColor: Color = Color.White,
    val selectedEmoji: String? = null,
    val showEmojiPicker: Boolean = false,
    val emojiError: String? = null
)

@HiltViewModel
class CustomCharacterCreationViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomCharacterUiState())
    val uiState = _uiState.asStateFlow()

    companion object {
        private const val MAX_BITMAP_DIMENSION = 4096
        private const val MAX_BITMAP_MEMORY_MB = 50
        private const val MAX_PIXELS = (MAX_BITMAP_MEMORY_MB * 1024 * 1024) / 4
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                isBackgroundRemovalMode = false,
                isPanningMode = false,
                maskPath = Path(),
                currentStrokePath = Path(),
                strokeHistory = emptyList(),
                lastDrawnPoint = null,
                canvasOffset = Offset.Zero,
                canvasScale = 1f
            )
        }
    }

    fun toggleBackgroundRemovalMode() {
        _uiState.update {
            val newMode = !it.isBackgroundRemovalMode
            val panningMode = if (newMode) it.isPanningMode else false
            it.copy(
                isBackgroundRemovalMode = newMode,
                isPanningMode = panningMode,
                previewPosition = null
            )
        }
    }

    fun toggleImageStroke(enabled: Boolean) {
        _uiState.update { it.copy(isStrokeEnabled = enabled) }
    }

    fun setStrokeColor(color: Color) {
        _uiState.update { it.copy(strokeColor = color) }
    }

    fun togglePanningMode() {
        _uiState.update {
            if (!it.isBackgroundRemovalMode) return@update it
            it.copy(isPanningMode = !it.isPanningMode, previewPosition = null)
        }
    }

    fun onCanvasTransform(centroid: Offset, pan: Offset, zoom: Float) {
        _uiState.update { currentState ->
            val newScale = (currentState.canvasScale * zoom).coerceIn(0.5f, 5f)
            val newOffset =
                currentState.canvasOffset + centroid - (centroid * newScale / currentState.canvasScale) + pan
            currentState.copy(
                canvasScale = newScale,
                canvasOffset = newOffset
            )
        }
    }

    fun updateBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun updateFeatheringSize(size: Float) {
        _uiState.update { it.copy(featheringSize = size) }
    }

    fun startDrawing(offset: Offset) {
        _uiState.update { currentState ->
            val newHistory = currentState.strokeHistory + currentState.maskPath
            val newStrokePath = Path().apply {
                addOval(ComposeRect(center = offset, radius = currentState.brushSize / 2))
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
                    addOval(ComposeRect(center = interpolatedPoint, radius = currentState.brushSize / 2))
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
            lastDrawnPoint = null,
            canvasScale = 1f,
            canvasOffset = Offset.Zero,
            isPanningMode = false
        )
    }

    fun finishEditing() {
        _uiState.value = _uiState.value.copy(
            isBackgroundRemovalMode = false,
            previewPosition = null,
            isPanningMode = false,
            canvasScale = 1f,
            canvasOffset = Offset.Zero
        )
    }

    fun onRopeSelected(ropeResId: Int) {
        _uiState.update {
            it.copy(
                selectedRopeResId = ropeResId,
                showRopeAdjustment = true,
                ropeScale = 1f,
                ropeOffsetX = 0f,
                ropeOffsetY = 0f,
                characterScale = 1f,
                isStrokeEnabled = false,
                strokeColor = Color.White
            )
        }
    }

    fun onCharacterNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(characterName = name)
    }

    fun updateCharacterScale(scale: Float) {
        _uiState.update { it.copy(characterScale = scale) }
    }

    fun saveCustomCharacter(context: Context) {
        val currentState = _uiState.value
        if (currentState.selectedImageUri == null || currentState.selectedRopeResId == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val characterBitmap = createTransparentBitmapFromUri(
                    context,
                    currentState.selectedImageUri,
                    currentState.maskPath,
                    currentState.featheringSize
                )
                val ropeBitmap =
                    BitmapFactory.decodeResource(context.resources, currentState.selectedRopeResId)

                val combinedBitmap = combineCharacterAndRope(
                    characterBitmap,
                    ropeBitmap,
                    currentState.ropeScale,
                    currentState.ropeOffsetX,
                    currentState.ropeOffsetY,
                    currentState.characterScale,
                    currentState.isStrokeEnabled,
                    currentState.strokeColor.toArgb()
                )
                val fileName = "custom_char_${System.currentTimeMillis()}.png"
                val savedImagePath = saveBitmapToFile(context, combinedBitmap, fileName)
                val character = CustomCharacter(
                    name = currentState.characterName,
                    imagePath = savedImagePath,
                    ropeResId = currentState.selectedRopeResId,
                    ropeScale = currentState.ropeScale,
                    ropeOffsetX = currentState.ropeOffsetX,
                    ropeOffsetY = currentState.ropeOffsetY,
                    characterScale = currentState.characterScale
                )
                characterRepository.insertCustomCharacter(character)
                _uiState.update { it.copy(isSaving = false, saveComplete = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun addStrokeToAndroidBitmap(input: Bitmap, strokePx: Float, color: Int): Bitmap {
        val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            maskFilter = BlurMaskFilter(strokePx, BlurMaskFilter.Blur.NORMAL)
        }
        val offsetXY = IntArray(2)
        val outlineBitmap = input.extractAlpha(blurPaint, offsetXY)
        val finalBitmap = createBitmap(outlineBitmap.width, outlineBitmap.height)
        val canvas = Canvas(finalBitmap)
        canvas.drawBitmap(outlineBitmap, 0f, 0f, null)
        outlineBitmap.recycle()
        canvas.drawBitmap(input, -offsetXY[0].toFloat(), -offsetXY[1].toFloat(), null)
        return finalBitmap
    }

    fun onNavigationComplete() {
        _uiState.update { it.copy(saveComplete = false) }
    }

    private fun createTransparentBitmapFromUri(
        context: Context,
        imageUri: Uri,
        maskPath: Path,
        featheringSize: Float,
    ): Bitmap {
        val originalBitmap = loadScaledBitmapFromUri(context, imageUri)

        // CRITICAL FIX: Check if mask is empty (pre-cut PNG case)
        val androidMaskPath = maskPath.asAndroidPath()
        if (androidMaskPath.isEmpty) {
            // No mask applied - return original bitmap directly (it's already transparent PNG)
            return originalBitmap
        }

        // Mask exists - apply background removal
        val mutableBitmap = createBitmap(originalBitmap.width, originalBitmap.height)
        val canvas = Canvas(mutableBitmap)

        // Draw original bitmap first
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Then erase the masked areas
        val erasePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            if (featheringSize > 0f) {
                maskFilter = BlurMaskFilter(featheringSize, BlurMaskFilter.Blur.NORMAL)
            }
        }
        canvas.drawPath(androidMaskPath, erasePaint)

        return mutableBitmap
    }

    private fun cropTransparentBorders(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        var top = 0
        var bottom = height
        var left = 0
        var right = width

        topLoop@ for (y in 0 until height) {
            for (x in 0 until width) {
                if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                    top = y
                    break@topLoop
                }
            }
        }
        bottomLoop@ for (y in height - 1 downTo top) {
            for (x in 0 until width) {
                if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                    bottom = y + 1
                    break@bottomLoop
                }
            }
        }
        leftLoop@ for (x in 0 until width) {
            for (y in top until bottom) {
                if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                    left = x
                    break@leftLoop
                }
            }
        }
        rightLoop@ for (x in width - 1 downTo left) {
            for (y in top until bottom) {
                if (android.graphics.Color.alpha(bitmap[x, y]) > 0) {
                    right = x + 1
                    break@rightLoop
                }
            }
        }
        if (top >= bottom || left >= right) {
            return bitmap
        }
        val croppedWidth = right - left
        val croppedHeight = bottom - top
        return Bitmap.createBitmap(bitmap, left, top, croppedWidth, croppedHeight)
    }

    private fun combineCharacterAndRope(
        characterBitmap: Bitmap,
        ropeBitmap: Bitmap,
        ropeScale: Float,
        ropeOffsetX: Float,
        ropeOffsetY: Float,
        characterScale: Float,
        isStrokeEnabled: Boolean,
        strokeColor: Int,
    ): Bitmap {
        var croppedCharacterBitmap = cropTransparentBorders(characterBitmap)

        if (isStrokeEnabled) {
            val strokeWidthPx = 2f
            val strokedBitmap = addStrokeToAndroidBitmap(croppedCharacterBitmap, strokeWidthPx, strokeColor)
            croppedCharacterBitmap.recycle()
            croppedCharacterBitmap = strokedBitmap
        }

        var scaledCharacterWidth = (croppedCharacterBitmap.width * characterScale).toInt()
        var scaledCharacterHeight = (croppedCharacterBitmap.height * characterScale).toInt()
        var scaledRopeWidth = (ropeBitmap.width * ropeScale).toInt()
        var scaledRopeHeight = (ropeBitmap.height * ropeScale).toInt()

        val maxCharDimension = maxOf(scaledCharacterWidth, scaledCharacterHeight)
        if (maxCharDimension > MAX_BITMAP_DIMENSION) {
            val scaleFactor = MAX_BITMAP_DIMENSION.toFloat() / maxCharDimension
            scaledCharacterWidth = (scaledCharacterWidth * scaleFactor).toInt()
            scaledCharacterHeight = (scaledCharacterHeight * scaleFactor).toInt()
        }

        val maxRopeDimension = maxOf(scaledRopeWidth, scaledRopeHeight)
        if (maxRopeDimension > MAX_BITMAP_DIMENSION) {
            val scaleFactor = MAX_BITMAP_DIMENSION.toFloat() / maxRopeDimension
            scaledRopeWidth = (scaledRopeWidth * scaleFactor).toInt()
            scaledRopeHeight = (scaledRopeHeight * scaleFactor).toInt()
        }

        val totalContentWidth = scaledCharacterWidth.coerceAtLeast(scaledRopeWidth)
        val totalContentHeight = scaledRopeHeight + scaledCharacterHeight
        val paddingTop = kotlin.math.max(0f, -ropeOffsetY).toInt()
        val paddingBottom = kotlin.math.max(0f, ropeOffsetY).toInt()
        val paddingLeft = kotlin.math.max(0f, -ropeOffsetX).toInt()
        val paddingRight = kotlin.math.max(0f, ropeOffsetX).toInt()

        var canvasWidth = totalContentWidth + paddingLeft + paddingRight
        var canvasHeight = totalContentHeight + paddingTop + paddingBottom

        if (canvasWidth > MAX_BITMAP_DIMENSION || canvasHeight > MAX_BITMAP_DIMENSION) {
            val scaleFactor = minOf(
                MAX_BITMAP_DIMENSION.toFloat() / canvasWidth,
                MAX_BITMAP_DIMENSION.toFloat() / canvasHeight
            )
            canvasWidth = (canvasWidth * scaleFactor).toInt()
            canvasHeight = (canvasHeight * scaleFactor).toInt()
            scaledCharacterWidth = (scaledCharacterWidth * scaleFactor).toInt()
            scaledCharacterHeight = (scaledCharacterHeight * scaleFactor).toInt()
            scaledRopeWidth = (scaledRopeWidth * scaleFactor).toInt()
            scaledRopeHeight = (scaledRopeHeight * scaleFactor).toInt()
        }

        val totalPixels = canvasWidth.toLong() * canvasHeight.toLong()
        if (totalPixels > MAX_PIXELS) {
            val scaleFactor = kotlin.math.sqrt(MAX_PIXELS.toDouble() / totalPixels).toFloat()
            canvasWidth = (canvasWidth * scaleFactor).toInt()
            canvasHeight = (canvasHeight * scaleFactor).toInt()
            scaledCharacterWidth = (scaledCharacterWidth * scaleFactor).toInt()
            scaledCharacterHeight = (scaledCharacterHeight * scaleFactor).toInt()
            scaledRopeWidth = (scaledRopeWidth * scaleFactor).toInt()
            scaledRopeHeight = (scaledRopeHeight * scaleFactor).toInt()
        }

        // CRITICAL: Manual scaling with proper transparency handling
        val scaledCharacterBitmap = createBitmap(scaledCharacterWidth, scaledCharacterHeight)
        Canvas(scaledCharacterBitmap).apply {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val srcRect = Rect(0, 0, croppedCharacterBitmap.width, croppedCharacterBitmap.height)
            val dstRect = Rect(0, 0, scaledCharacterWidth, scaledCharacterHeight)
            drawBitmap(croppedCharacterBitmap, srcRect, dstRect, paint)
        }

        val scaledRopeBitmap = createBitmap(scaledRopeWidth, scaledRopeHeight)
        Canvas(scaledRopeBitmap).apply {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val srcRect = Rect(0, 0, ropeBitmap.width, ropeBitmap.height)
            val dstRect = Rect(0, 0, scaledRopeWidth, scaledRopeHeight)
            drawBitmap(ropeBitmap, srcRect, dstRect, paint)
        }

        // CRITICAL: Create transparent canvas - DO NOT call eraseColor or drawColor
        val combinedBitmap = createBitmap(canvasWidth, canvasHeight)
        val canvas = Canvas(combinedBitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val actualTotalWidth = scaledCharacterWidth.coerceAtLeast(scaledRopeWidth)
        val actualPaddingLeft = kotlin.math.max(0f, -ropeOffsetX * (canvasWidth.toFloat() / (totalContentWidth + paddingLeft + paddingRight))).toInt()
        val actualPaddingTop = kotlin.math.max(0f, -ropeOffsetY * (canvasHeight.toFloat() / (totalContentHeight + paddingTop + paddingBottom))).toInt()

        val ropeX = actualPaddingLeft + (actualTotalWidth - scaledRopeWidth) / 2f +
                (ropeOffsetX * (canvasWidth.toFloat() / (totalContentWidth + paddingLeft + paddingRight)))
        val ropeY = actualPaddingTop +
                (ropeOffsetY * (canvasHeight.toFloat() / (totalContentHeight + paddingTop + paddingBottom)))

        canvas.drawBitmap(scaledRopeBitmap, ropeX, ropeY, paint)

        val characterX = actualPaddingLeft + (actualTotalWidth - scaledCharacterWidth) / 2f
        val characterY = actualPaddingTop + scaledRopeHeight.toFloat()

        canvas.drawBitmap(scaledCharacterBitmap, characterX, characterY, paint)

        scaledRopeBitmap.recycle()
        scaledCharacterBitmap.recycle()
        croppedCharacterBitmap.recycle()

        return combinedBitmap
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): String {
        // DEBUG: Check if bitmap has transparency
        android.util.Log.d("SaveBitmap", "Bitmap config: ${bitmap.config}")
        android.util.Log.d("SaveBitmap", "Has alpha: ${bitmap.hasAlpha()}")

        // CRITICAL: Ensure bitmap has alpha enabled
        if (!bitmap.hasAlpha()) {
            bitmap.setHasAlpha(true)
        }

        val directory = File(context.filesDir, "custom_characters")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        FileOutputStream(file).use {
            // Use PNG with 100 quality to preserve transparency
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

    // Add these methods to the existing ViewModel class

    fun showEmojiPicker() {
        _uiState.update { it.copy(showEmojiPicker = true, emojiError = null) }
    }

    fun dismissEmojiPicker() {
        _uiState.update { it.copy(showEmojiPicker = false, emojiError = null) }
    }

    fun onEmojiSelected(emoji: String) {
        if (EmojiUtils.isValidSingleEmojiSimple(emoji)) { // Changed to use Simple version
            _uiState.update {
                it.copy(
                    selectedEmoji = emoji,
                    showEmojiPicker = false,
                    emojiError = null
                )
            }
            // Proceed to rope selection
            // Don't auto-select rope, let user choose
        } else {
            _uiState.update {
                it.copy(
                    emojiError = "Please select only one emoji (no text or multiple emojis)"
                )
            }
        }
    }

    fun saveEmojiCharacter(context: Context) {
        val currentState = _uiState.value
        if (currentState.selectedEmoji == null || currentState.selectedRopeResId == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save emoji as bitmap
                val emojiImagePath = EmojiUtils.saveEmojiToFile(context, currentState.selectedEmoji)

                // Load the bitmap
                val emojiBitmap = BitmapFactory.decodeFile(emojiImagePath)
                val ropeBitmap = BitmapFactory.decodeResource(context.resources, currentState.selectedRopeResId)

                // Combine emoji and rope
                val combinedBitmap = combineCharacterAndRope(
                    emojiBitmap,
                    ropeBitmap,
                    currentState.ropeScale,
                    currentState.ropeOffsetX,
                    currentState.ropeOffsetY,
                    currentState.characterScale,
                    currentState.isStrokeEnabled,
                    currentState.strokeColor.toArgb()
                )

                // Save combined image
                val fileName = "emoji_combined_${System.currentTimeMillis()}.png"
                val savedImagePath = saveBitmapToFile(context, combinedBitmap, fileName)

                // Create custom character entry with emoji flag
                val character = CustomCharacter(
                    name = currentState.characterName.ifBlank { currentState.selectedEmoji },
                    imagePath = savedImagePath,
                    ropeResId = currentState.selectedRopeResId,
                    ropeScale = currentState.ropeScale,
                    ropeOffsetX = currentState.ropeOffsetX,
                    ropeOffsetY = currentState.ropeOffsetY,
                    characterScale = currentState.characterScale,
                    isEmoji = true // Mark as emoji character
                )

                characterRepository.insertCustomCharacter(character)

                // Cleanup
                emojiBitmap.recycle()

                _uiState.update { it.copy(isSaving = false, saveComplete = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetEmojiSelection() {
        _uiState.update {
            it.copy(
                selectedEmoji = null,
                emojiError = null,
                showEmojiPicker = false
            )
        }
    }

}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun loadScaledBitmapFromUri(
    context: Context,
    imageUri: Uri,
    reqWidth: Int = 2048,
    reqHeight: Int = 2048,
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    context.contentResolver.openInputStream(imageUri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    options.inPremultiplied = true

    val bitmap = context.contentResolver.openInputStream(imageUri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: throw IllegalStateException("Could not load scaled bitmap from URI")

    return if (bitmap.config != Bitmap.Config.ARGB_8888) {
        val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        bitmap.recycle()
        argbBitmap
    } else {
        bitmap
    }
}