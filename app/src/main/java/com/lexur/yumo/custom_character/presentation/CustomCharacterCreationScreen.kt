package com.lexur.yumo.custom_character.presentation


import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.drawscope.Fill
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
import androidx.navigation.NavController
import kotlin.math.roundToInt

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
            viewModel.onNavigationComplete() // Reset the event
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (showRopeSelection) {
            RopeSelectionScreen(
                onRopeSelected = { ropeResId ->
                    viewModel.onRopeSelected(ropeResId)
                    showRopeSelection = false
                    showNameDialog = true
                },
                onNavigateBack = { showRopeSelection = false }
            )
        } else if (showNameDialog) {
            NameCharacterDialog(
                onNameSelected = { name ->
                    viewModel.onCharacterNameChanged(name)
                    viewModel.saveCustomCharacter(context)
                    showNameDialog = false
                },
                onDismiss = { showNameDialog = false }
            )
        } else {
            Scaffold(
                topBar = {
                    if (uiState.selectedImageUri != null) {
                        TopAppBar(
                            title = { Text("Background Removal") },
                            actions = {
                                IconButton(onClick = { viewModel.toggleBackgroundRemovalMode() }) {
                                    Icon(
                                        Icons.Default.Brush,
                                        contentDescription = "Toggle brush mode",
                                        tint = if (uiState.isBackgroundRemovalMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.undoLastStroke() },
                                    enabled = uiState.strokeHistory.isNotEmpty()
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                                }
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
                                    maskPath = uiState.maskPath,
                                    currentStrokePath = uiState.currentStrokePath,
                                    previewPosition = uiState.previewPosition,
                                    onDrawStart = viewModel::startDrawing,
                                    onDrawContinue = viewModel::continueDrawing,
                                    onDrawEnd = viewModel::endDrawing,
                                    onPreviewMove = viewModel::updatePreviewPosition,
                                    onCanvasSize = viewModel::updateCanvasSize
                                )
                            }
                            if (uiState.isBackgroundRemovalMode) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Brush Size: ${uiState.brushSize.toInt()}px", style = MaterialTheme.typography.labelMedium)
                                        Slider(
                                            value = uiState.brushSize,
                                            onValueChange = { viewModel.updateBrushSize(it) },
                                            valueRange = 10f..300f,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Box(
                                            modifier = Modifier.size(60.dp).align(Alignment.CenterHorizontally),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(60.dp)) {
                                                drawCheckerboard()
                                                drawCircle(color = Color.Red.copy(alpha = 0.5f), radius = uiState.brushSize / 2, center = center)
                                                drawCircle(color = Color.Red, radius = uiState.brushSize / 2, center = center, style = Stroke(width = 2.dp.toPx()))
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(onClick = { viewModel.resetImage() }, modifier = Modifier.weight(1f)) { Text("Reset") }
                                Button(
                                    onClick = {
                                        viewModel.finishEditing()
                                        showRopeSelection = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Next") }
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
    maskPath: Path,
    currentStrokePath: Path,
    previewPosition: Offset?,
    onDrawStart: (Offset) -> Unit,
    onDrawContinue: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onPreviewMove: (Offset?) -> Unit,
    onCanvasSize: (IntSize) -> Unit,
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

    // Create processed bitmap with transparency applied
    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath) {
        if (imageBitmap != null) {
            createTransparentBitmap(imageBitmap!!, maskPath, currentStrokePath)
        } else null
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
    ) {
        // Layer 1: Checkerboard background (for transparency visualization)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCheckerboard()
        }

        // Layer 2: Image with transparent areas
        if (processedBitmap != null && transformation != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = processedBitmap,
                    dstOffset = IntOffset(
                        transformation.imageOffset.x.roundToInt(),
                        transformation.imageOffset.y.roundToInt()
                    ),
                    dstSize = IntSize(
                        transformation.displayedImageSize.width.roundToInt(),
                        transformation.displayedImageSize.height.roundToInt()
                    )
                )
            }
        } else if (imageBitmap != null && transformation != null) {
            // Fallback to original image if processing failed
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = imageBitmap!!,
                    dstOffset = IntOffset(
                        transformation.imageOffset.x.roundToInt(),
                        transformation.imageOffset.y.roundToInt()
                    ),
                    dstSize = IntSize(
                        transformation.displayedImageSize.width.roundToInt(),
                        transformation.displayedImageSize.height.roundToInt()
                    )
                )
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = "Character Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Layer 3: Drawing interaction and preview
        if (isBackgroundRemovalMode) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { event ->
                        if (transformation == null) return@pointerInteropFilter false

                        val canvasOffset = Offset(event.x, event.y)
                        // Convert to image coordinates for accurate drawing
                        val imageOffset = Offset(
                            (canvasOffset.x - transformation.imageOffset.x) / transformation.scaleFactor,
                            (canvasOffset.y - transformation.imageOffset.y) / transformation.scaleFactor
                        )

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                onDrawStart(imageOffset)
                                onPreviewMove(canvasOffset)
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                onDrawContinue(imageOffset)
                                onPreviewMove(canvasOffset)
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
            ) {
                // Draw brush preview cursor
                previewPosition?.let { position ->
                    drawCircle(
                        color = Color.White,
                        radius = brushSize / 2 * (transformation?.scaleFactor ?: 1f) + 2.dp.toPx(),
                        center = position,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Red,
                        radius = brushSize / 2 * (transformation?.scaleFactor ?: 1f),
                        center = position,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Magnifier
        if (isBackgroundRemovalMode &&
            previewPosition != null &&
            imageBitmap != null &&
            transformation != null) {
            MagnifierPreview(
                position = previewPosition,
                imageBitmap = imageBitmap!!,
                transformation = transformation,
                brushSize = brushSize
            )
        }
    }
}
@Composable
private fun MagnifierPreview(
    position: Offset,
    imageBitmap: ImageBitmap,
    transformation: ImageTransformation,
    brushSize: Float
) {
    val loupeSize = 120.dp
    val magnification = 2.5f
    val loupeSizePx = with(LocalDensity.current) { loupeSize.toPx() }

    // Calculate the actual position on the original image
    val imagePosition = calculateImagePosition(position, transformation)

    // Only show magnifier if the position is within the image bounds
    if (imagePosition.x < 0 || imagePosition.y < 0 ||
        imagePosition.x >= imageBitmap.width || imagePosition.y >= imageBitmap.height) {
        return
    }

    // Calculate source rectangle for the magnifier
    val srcSize = IntSize(
        (loupeSizePx / magnification).roundToInt(),
        (loupeSizePx / magnification).roundToInt()
    )

    val srcOffset = IntOffset(
        (imagePosition.x - srcSize.width / 2).coerceIn(0f, (imageBitmap.width - srcSize.width).toFloat()).roundToInt(),
        (imagePosition.y - srcSize.height / 2).coerceIn(0f, (imageBitmap.height - srcSize.height).toFloat()).roundToInt()
    )

    // Calculate magnifier position (avoid going off-screen)
    val loupeOffset = calculateLoupePosition(position, loupeSizePx, transformation.displayedImageSize)

    Canvas(
        modifier = Modifier
            .size(loupeSize)
            .offset { IntOffset(loupeOffset.x.roundToInt(), loupeOffset.y.roundToInt()) }
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color.DarkGray, CircleShape)
    ) {
        // Draw magnified image
        drawImage(
            image = imageBitmap,
            srcOffset = srcOffset,
            srcSize = srcSize,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
        )

        // Draw brush preview in magnifier
        val brushRadiusInLoupe = (brushSize / 2) / transformation.scaleFactor * magnification
        val center = this.center

        drawCircle(
            color = Color.Red.copy(alpha = 0.3f),
            radius = brushRadiusInLoupe,
            center = center,
            style = Fill
        )
        drawCircle(
            color = Color.Red,
            radius = brushRadiusInLoupe,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Draw crosshair
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
    // Convert touch position to image coordinates
    val imageX = (touchPosition.x - transformation.imageOffset.x) / transformation.scaleFactor
    val imageY = (touchPosition.y - transformation.imageOffset.y) / transformation.scaleFactor

    return Offset(imageX, imageY)
}

@Composable
private fun calculateLoupePosition(
    touchPosition: Offset,
    loupeSizePx: Float,
    imageSize: Size
): Offset {
    val density = LocalDensity.current
    val margin = with(density) { 16.dp.toPx() }

    val loupeRadius = loupeSizePx / 2

    return Offset(
        x = (touchPosition.x - loupeRadius).coerceIn(
            margin,
            imageSize.width - loupeSizePx - margin
        ),
        y = (touchPosition.y - loupeSizePx - margin).coerceIn(
            margin,
            imageSize.height - loupeSizePx - margin
        )
    )
}

private fun DrawScope.drawCheckerboard() {
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
        // Image is wider than canvas - fit to width
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
        // Image is taller than canvas - fit to height
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

private fun createTransparentBitmap(
    originalBitmap: ImageBitmap,
    maskPath: Path,
    currentStrokePath: Path
): ImageBitmap {
    // Convert ImageBitmap to Android Bitmap for processing
    val androidBitmap = originalBitmap.asAndroidBitmap()

    // Create a mutable copy with ARGB_8888 config for transparency support
    val mutableBitmap = androidBitmap.copy(Bitmap.Config.ARGB_8888, true)

    // Create canvas to draw on the bitmap
    val canvas = android.graphics.Canvas(mutableBitmap)

    // Create paint for erasing (making transparent)
    val erasePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        // This blend mode will make the drawn areas transparent
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // Convert Compose Path to Android Path and apply mask
    val androidMaskPath = maskPath.asAndroidPath()
    val androidCurrentPath = currentStrokePath.asAndroidPath()

    // Erase the masked areas (make them transparent)
    canvas.drawPath(androidMaskPath, erasePaint)
    canvas.drawPath(androidCurrentPath, erasePaint)

    // Convert back to ImageBitmap
    return mutableBitmap.asImageBitmap()
}