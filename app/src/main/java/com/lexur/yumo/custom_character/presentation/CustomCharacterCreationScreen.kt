package com.lexur.yumo.custom_character.presentation


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavController
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCharacterCreationScreen(
    navController: NavController,
    viewModel: CustomCharacterCreationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    var showRopeSelection by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            navController.popBackStack()
            viewModel.onNavigationComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.showRopeAdjustment && uiState.selectedRopeResId != null -> {
                RopeAdjustmentScreen(
                    imageUri = uiState.selectedImageUri!!,
                    maskPath = uiState.maskPath,
                    currentStrokePath = uiState.currentStrokePath,
                    ropeResId = uiState.selectedRopeResId!!,
                    ropeScale = uiState.ropeScale,
                    ropeOffsetX = uiState.ropeOffsetX,
                    ropeOffsetY = uiState.ropeOffsetY,
                    onRopeScaleChanged = viewModel::updateRopeScale,
                    onRopeOffsetXChanged = viewModel::updateRopeOffsetX,
                    onRopeOffsetYChanged = viewModel::updateRopeOffsetY,
                    onConfirm = {
                        viewModel.finishRopeAdjustment()
                        showNameDialog = true
                    },
                    onNavigateBack = {
                        viewModel.finishRopeAdjustment()
                        showRopeSelection = true
                    },
                    characterScale = uiState.characterScale,
                    onCharacterScaleChanged = viewModel::updateCharacterScale,
                    featheringSize = uiState.featheringSize
                )
            }

            showRopeSelection -> {
                RopeSelectionScreen(
                    onRopeSelected = { ropeResId ->
                        viewModel.onRopeSelected(ropeResId)
                        showRopeSelection = false
                    },
                    onNavigateBack = { showRopeSelection = false }
                )
            }

            showNameDialog -> {
                NameCharacterDialog(
                    onNameSelected = { name ->
                        viewModel.onCharacterNameChanged(name)
                        viewModel.saveCustomCharacter(context)
                        showNameDialog = false
                    },
                    onDismiss = { showNameDialog = false }
                )
            }

            else -> {
                Scaffold(
                    topBar = {
                        if (uiState.selectedImageUri != null) {
                            TopAppBar(
                                title = { Text("Background Removal") },
                                actions = {
                                    // Toggle Brush Mode
                                    IconButton(onClick = { viewModel.toggleBackgroundRemovalMode() }) {
                                        Icon(
                                            Icons.Default.Brush,
                                            contentDescription = "Toggle background removal mode",
                                            tint = if (uiState.isBackgroundRemovalMode && !uiState.isPanningMode)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    // Toggle Pan Mode
                                    if (uiState.isBackgroundRemovalMode) {
                                        IconButton(onClick = { viewModel.togglePanningMode() }) {
                                            Icon(
                                                Icons.Default.PanTool,
                                                contentDescription = "Toggle pan mode",
                                                tint = if (uiState.isPanningMode)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    // Undo
                                    IconButton(
                                        onClick = { viewModel.undoLastStroke() },
                                        enabled = uiState.strokeHistory.isNotEmpty() && !uiState.isPanningMode
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                                    }
                                    // Done
                                    IconButton(onClick = {
                                        viewModel.finishEditing()
                                        showRopeSelection = true
                                    }) {
                                        Icon(Icons.Default.Done, contentDescription = "Done")
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when {
                            uiState.selectedImageUri == null -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Button(onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }) {
                                        Text("Select Image")
                                    }
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)
                                ) {
                                    BackgroundRemovalCanvas(
                                        imageUri = uiState.selectedImageUri!!,
                                        brushSize = uiState.brushSize,
                                        isBackgroundRemovalMode = uiState.isBackgroundRemovalMode,
                                        isPanningMode = uiState.isPanningMode,
                                        maskPath = uiState.maskPath,
                                        currentStrokePath = uiState.currentStrokePath,
                                        previewPosition = uiState.previewPosition,
                                        canvasOffset = uiState.canvasOffset,
                                        canvasScale = uiState.canvasScale,
                                        featheringSize = uiState.featheringSize,
                                        onDrawStart = viewModel::startDrawing,
                                        onDrawContinue = viewModel::continueDrawing,
                                        onDrawEnd = viewModel::endDrawing,
                                        onPreviewMove = viewModel::updatePreviewPosition,
                                        onCanvasSize = viewModel::updateCanvasSize,
                                        onCanvasTransform = viewModel::onCanvasTransform
                                    )
                                }
                                if (uiState.isBackgroundRemovalMode && !uiState.isPanningMode) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Brush Size: ${uiState.brushSize.toInt()}px",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Slider(
                                                value = uiState.brushSize,
                                                onValueChange = { viewModel.updateBrushSize(it) },
                                                valueRange = 10f..300f,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .align(Alignment.CenterHorizontally),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(60.dp)) {
                                                    drawCheckerboard()
                                                    drawCircle(
                                                        color = Color.Red.copy(alpha = 0.5f),
                                                        radius = uiState.brushSize / 2,
                                                        center = center
                                                    )
                                                    drawCircle(
                                                        color = Color.Red,
                                                        radius = uiState.brushSize / 2,
                                                        center = center,
                                                        style = Stroke(width = 2.dp.toPx())
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                "Edge Softness: ${uiState.featheringSize.toInt()}",
                                                style = MaterialTheme. typography.labelMedium
                                            )
                                            Slider(
                                                value = uiState.featheringSize,
                                                onValueChange = { viewModel.updateFeatheringSize(it) },
                                                valueRange = 0f..50f, // 0 means sharp edge
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.resetImage() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reset")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.finishEditing()
                                            showRopeSelection = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Next")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Saving Character...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun NameCharacterDialog(
    onNameSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name Your Character") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Character Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onNameSelected(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BackgroundRemovalCanvas(
    imageUri: Uri,
    brushSize: Float,
    isBackgroundRemovalMode: Boolean,
    isPanningMode: Boolean,
    maskPath: Path,
    currentStrokePath: Path,
    previewPosition: Offset?,
    canvasOffset: Offset,
    canvasScale: Float,
    featheringSize: Float,
    onDrawStart: (Offset) -> Unit,
    onDrawContinue: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onPreviewMove: (Offset?) -> Unit,
    onCanvasSize: (IntSize) -> Unit,
    onCanvasTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = try {
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            }?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val transformation = remember(imageBitmap, canvasSize) {
        if (imageBitmap == null || canvasSize == IntSize.Zero) {
            null
        } else {
            calculateImageTransformation(imageBitmap!!, canvasSize)
        }
    }

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath, featheringSize) {
        if (imageBitmap != null) {
            createTransparentBitmap(imageBitmap!!, maskPath, currentStrokePath, featheringSize)
        } else null
    }

    val interactionModifier = if (isBackgroundRemovalMode && transformation != null) {
        if (isPanningMode) {
            Modifier.pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    onCanvasTransform(centroid, pan, zoom)
                }
            }
        } else {
            Modifier.pointerInteropFilter { event ->
                val canvasTouchPos = Offset(event.x, event.y)

                // Convert canvas coordinates to image coordinates for accurate drawing
                val imageTouchPos = canvasToImageCoordinates(
                    screenPosition = canvasTouchPos,
                    transformation = transformation,
                    canvasOffset = canvasOffset,
                    canvasScale = canvasScale,
                    canvasSize = canvasSize
                )

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onDrawStart(imageTouchPos)
                        onPreviewMove(imageTouchPos)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        onDrawContinue(imageTouchPos)
                        onPreviewMove(imageTouchPos)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onDrawEnd()
                        onPreviewMove(null)
                        true
                    }
                    else -> false
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.1f))
            .onGloballyPositioned { coordinates ->
                if (canvasSize != coordinates.size) {
                    canvasSize = coordinates.size
                    onCanvasSize(coordinates.size)
                }
            }
            .then(interactionModifier)
    ) {
        // Layer 1 & 2: Checkerboard and Image (transformed together)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = canvasOffset.x
                    translationY = canvasOffset.y
                    scaleX = canvasScale
                    scaleY = canvasScale
                    // The default pivot is center, which is what our math now assumes
                }
        ) {
            drawCheckerboard()
            if (processedBitmap != null && transformation != null) {
                drawImage(
                    image = processedBitmap,
                    dstOffset = IntOffset(transformation.imageOffset.x.roundToInt(), transformation.imageOffset.y.roundToInt()),
                    dstSize = IntSize(transformation.displayedImageSize.width.roundToInt(), transformation.displayedImageSize.height.roundToInt())
                )
            } else if (imageBitmap != null && transformation != null) {
                drawImage(
                    image = imageBitmap!!,
                    dstOffset = IntOffset(transformation.imageOffset.x.roundToInt(), transformation.imageOffset.y.roundToInt()),
                    dstSize = IntSize(transformation.displayedImageSize.width.roundToInt(), transformation.displayedImageSize.height.roundToInt())
                )
            }
        }

        if (imageBitmap == null) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = "Character Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Layer 3: Drawing interaction and preview (on top, not transformed by graphicsLayer)
        if (isBackgroundRemovalMode && !isPanningMode && canvasSize != IntSize.Zero) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                previewPosition?.let { position ->
                    // Convert image coordinates back to screen coordinates for preview cursor
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)

                    // 1. Convert from image coordinates to the inner Canvas's layer coordinates
                    val layerPos = Offset(
                        x = position.x * transformation!!.scaleFactor + transformation.imageOffset.x,
                        y = position.y * transformation.scaleFactor + transformation.imageOffset.y
                    )

                    // 2. Apply the graphicsLayer transformations (scaling around center, then translation)
                    val screenPosition = ((layerPos - canvasCenter) * canvasScale) + canvasCenter + canvasOffset

                    val scaledBrushRadius = (brushSize / 2 * transformation.scaleFactor * canvasScale)
                    drawCircle(
                        color = Color.White,
                        radius = scaledBrushRadius + 2.dp.toPx(),
                        center = screenPosition,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Red,
                        radius = scaledBrushRadius,
                        center = screenPosition,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Magnifier
        val currentImageBitmap = imageBitmap
        if (isBackgroundRemovalMode && !isPanningMode &&
            previewPosition != null &&
            currentImageBitmap != null &&
            transformation != null &&
            canvasSize != IntSize.Zero
        ) {
            MagnifierPreview(
                position = previewPosition,
                imageBitmap = currentImageBitmap,
                transformation = transformation,
                canvasOffset = canvasOffset,
                canvasScale = canvasScale,
                canvasSize = canvasSize,
                maskPath = maskPath,
                currentStrokePath = currentStrokePath,
                featheringSize = featheringSize,
                brushSize = brushSize  // ADD THIS
            )
        }
    }
}

private fun canvasToImageCoordinates(
    screenPosition: Offset,
    transformation: ImageTransformation,
    canvasOffset: Offset,
    canvasScale: Float,
    canvasSize: IntSize
): Offset {
    // The center of the canvas is the pivot point for the graphicsLayer scale
    val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

    // The forward transformation is: screen_pos = ( (layer_pos - center) * scale ) + center + offset
    // We need to reverse this to find layer_pos from screen_pos.

    // 1. Reverse the canvasOffset translation
    val posAfterOffsetUndo = screenPosition - canvasOffset

    // 2. Reverse the scaling around the center pivot
    val layerPosition = ((posAfterOffsetUndo - canvasCenter) / canvasScale) + canvasCenter

    // `layerPosition` is now the coordinate within the inner Canvas's coordinate space.
    // This is the coordinate system where the image is drawn with the fit-transformation.

    // 3. Reverse the initial fit transformation to get the coordinate on the original image.
    val imageX = (layerPosition.x - transformation.imageOffset.x) / transformation.scaleFactor
    val imageY = (layerPosition.y - transformation.imageOffset.y) / transformation.scaleFactor

    return Offset(imageX, imageY)
}

@Composable
private fun MagnifierPreview(
    position: Offset,
    imageBitmap: ImageBitmap,
    transformation: ImageTransformation,
    canvasOffset: Offset,
    canvasScale: Float,
    canvasSize: IntSize,
    maskPath: Path,
    currentStrokePath: Path,
    brushSize: Float,
    featheringSize: Float
) {
    val loupeSize = 120.dp
    val loupeSizePx = with(LocalDensity.current) { loupeSize.toPx() }

    val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val layerPos = Offset(
        x = position.x * transformation.scaleFactor + transformation.imageOffset.x,
        y = position.y * transformation.scaleFactor + transformation.imageOffset.y
    )
    val screenTouchPosition = ((layerPos - canvasCenter) * canvasScale) + canvasCenter + canvasOffset

    val imagePosition = position

    if (imagePosition.x < 0 || imagePosition.y < 0 ||
        imagePosition.x >= imageBitmap.width || imagePosition.y >= imageBitmap.height) {
        return
    }

    // This is the actual issue - we need to get the brush size from uiState
    // But since we don't have access to it here, we need to pass it as a parameter
    // For now, let's use a calculation that works regardless of brush size

    // The magnifier should always show the same relative area
    // Let's use a fixed reasonable size that shows context around the brush
    val viewportSize = (brushSize * 2.5f).coerceIn(100f, 400f) // Show 150 pixels of the actual image

    val srcSize = IntSize(
        viewportSize.toInt(),
        viewportSize.toInt()
    )

    val srcOffset = IntOffset(
        (imagePosition.x - srcSize.width / 2)
            .coerceIn(0f, (imageBitmap.width - srcSize.width).coerceAtLeast(0).toFloat())
            .roundToInt(),
        (imagePosition.y - srcSize.height / 2)
            .coerceIn(0f, (imageBitmap.height - srcSize.height).coerceAtLeast(0).toFloat())
            .roundToInt()
    )

    val clampedSrcSize = IntSize(
        srcSize.width.coerceAtMost(imageBitmap.width - srcOffset.x).coerceAtLeast(1),
        srcSize.height.coerceAtMost(imageBitmap.height - srcOffset.y).coerceAtLeast(1)
    )

    if (clampedSrcSize.width <= 0 || clampedSrcSize.height <= 0) {
        return
    }

    val loupeOffset = calculateLoupePosition(
        touchPosition = screenTouchPosition,
        loupeSizePx = loupeSizePx,
        containerSize = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    )

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath, featheringSize) {
        createTransparentBitmap(imageBitmap, maskPath, currentStrokePath, featheringSize)
    }

    Canvas(
        modifier = Modifier
            .size(loupeSize)
            .offset { IntOffset(loupeOffset.x.roundToInt(), loupeOffset.y.roundToInt()) }
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color.DarkGray, CircleShape)
    ) {
        drawCheckerboard()

        drawImage(
            image = processedBitmap,
            srcOffset = srcOffset,
            srcSize = clampedSrcSize,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )

        val center = this.center
        val crosshairSize = 8.dp.toPx()
        drawLine(
            Color.Black.copy(alpha = 0.5f),
            start = Offset(center.x - crosshairSize, center.y),
            end = Offset(center.x + crosshairSize, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            Color.Black.copy(alpha = 0.5f),
            start = Offset(center.x, center.y - crosshairSize),
            end = Offset(center.x, center.y + crosshairSize),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun calculateImagePosition(touchPosition: Offset, transformation: ImageTransformation): Offset {
    val imageX = (touchPosition.x - transformation.imageOffset.x) / transformation.scaleFactor
    val imageY = (touchPosition.y - transformation.imageOffset.y) / transformation.scaleFactor
    return Offset(imageX, imageY)
}

@Composable
private fun calculateLoupePosition(
    touchPosition: Offset,
    loupeSizePx: Float,
    containerSize: Size
): Offset {
    val density = LocalDensity.current
    val margin = with(density) { 16.dp.toPx() }

    // Position the loupe above the finger, with a small margin.
    val yPos = touchPosition.y - loupeSizePx - margin

    // Center the loupe horizontally on the finger.
    val xPos = touchPosition.x - (loupeSizePx / 2f)

    // Ensure the loupe stays within the screen bounds.
    return Offset(
        x = xPos.coerceIn(
            margin,
            containerSize.width - loupeSizePx - margin
        ),
        y = yPos.coerceIn(
            margin,
            containerSize.height - loupeSizePx - margin
        )
    )
}

fun DrawScope.drawCheckerboard() {
    val checkSize = 20.dp.toPx()
    val lightGray = Color.LightGray.copy(alpha = 0.3f)
    val darkGray = Color.Gray.copy(alpha = 0.3f)

    val cols = (size.width / checkSize).toInt() + 1
    val rows = (size.height / checkSize).toInt() + 1

    for (row in 0..rows) {
        for (col in 0..cols) {
            val color = if ((row + col) % 2 == 0) lightGray else darkGray
            drawRect(
                color = color,
                topLeft = Offset(col * checkSize, row * checkSize),
                size = Size(checkSize, checkSize)
            )
        }
    }
}

data class ImageTransformation(
    val scaleFactor: Float,
    val imageOffset: Offset,
    val displayedImageSize: Size
)

fun calculateImageTransformation(
    imageBitmap: ImageBitmap,
    canvasSize: IntSize
): ImageTransformation {
    val imageRatio = imageBitmap.width.toFloat() / imageBitmap.height
    val canvasRatio = canvasSize.width.toFloat() / canvasSize.height

    val scaleFactor: Float
    val displayedImageSize: Size
    val imageOffset: Offset

    if (imageRatio > canvasRatio) {
        scaleFactor = canvasSize.width.toFloat() / imageBitmap.width
        displayedImageSize = Size(
            width = canvasSize.width.toFloat(),
            height = imageBitmap.height * scaleFactor
        )
        imageOffset = Offset(
            x = 0f,
            y = (canvasSize.height - displayedImageSize.height) / 2
        )
    } else {
        scaleFactor = canvasSize.height.toFloat() / imageBitmap.height
        displayedImageSize = Size(
            width = imageBitmap.width * scaleFactor,
            height = canvasSize.height.toFloat()
        )
        imageOffset = Offset(
            x = (canvasSize.width - displayedImageSize.width) / 2,
            y = 0f
        )
    }

    return ImageTransformation(scaleFactor, imageOffset, displayedImageSize)
}

fun createTransparentBitmap(
    originalBitmap: ImageBitmap,
    maskPath: Path,
    currentStrokePath: Path,
    featheringSize: Float
): ImageBitmap {
    val androidBitmap = originalBitmap.asAndroidBitmap()
    val mutableBitmap = androidBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(mutableBitmap)
    val erasePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        // If featheringSize is greater than 0, apply a blur effect to the erase tool
        if (featheringSize > 0f) {
            maskFilter = BlurMaskFilter(featheringSize, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val androidMaskPath = maskPath.asAndroidPath()
    val androidCurrentPath = currentStrokePath.asAndroidPath()
    canvas.drawPath(androidMaskPath, erasePaint)
    canvas.drawPath(androidCurrentPath, erasePaint)
    return mutableBitmap.asImageBitmap()
}

