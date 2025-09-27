// CustomCharacterCreationScreen.kt
package com.lexur.yumo.custom_character.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomCharacterCreationScreen(
    viewModel: CustomCharacterCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    Scaffold(
        topBar = {
            if (uiState.selectedImageUri != null) {
                TopAppBar(
                    title = { Text("Background Removal") },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleBackgroundRemovalMode() }
                        ) {
                            Icon(
                                Icons.Default.Brush,
                                contentDescription = "Toggle brush mode",
                                tint = if (uiState.isBackgroundRemovalMode)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { viewModel.undoLastStroke() },
                            enabled = uiState.strokeHistory.isNotEmpty() || uiState.maskPath != Path()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }
                        IconButton(onClick = { viewModel.finishEditing() }) {
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
                    // Initial state - show image selection
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text("Select Image")
                        }
                    }
                }
                else -> {
                    // Show image with background removal tools
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        BackgroundRemovalCanvas(
                            imageUri = uiState.selectedImageUri!!,
                            brushSize = uiState.brushSize,
                            isBackgroundRemovalMode = uiState.isBackgroundRemovalMode,
                            maskPath = uiState.maskPath,
                            previewPosition = uiState.previewPosition,
                            onDrawStart = { offset -> viewModel.startDrawing(offset) },
                            onDrawContinue = { offset -> viewModel.continueDrawing(offset) },
                            onDrawEnd = { viewModel.endDrawing() },
                            onPreviewMove = { offset -> viewModel.updatePreviewPosition(offset) },
                            onCanvasSize = { size -> viewModel.updateCanvasSize(size) }
                        )
                    }

                    // Brush size controls
                    if (uiState.isBackgroundRemovalMode) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Brush Size: ${uiState.brushSize.toInt()}px",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Slider(
                                    value = uiState.brushSize,
                                    onValueChange = { viewModel.updateBrushSize(it) },
                                    valueRange = 10f..100f,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Brush preview
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.CenterHorizontally),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        // Background checkerboard
                                        drawCheckerboard()

                                        // Brush circle
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
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.resetImage() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = { viewModel.saveProcessedImage() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.maskPath != Path()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BackgroundRemovalCanvas(
    imageUri: android.net.Uri,
    brushSize: Float,
    isBackgroundRemovalMode: Boolean,
    maskPath: Path,
    previewPosition: Offset?,
    onDrawStart: (Offset) -> Unit,
    onDrawContinue: (Offset) -> Unit,
    onDrawEnd: () -> Unit,
    onPreviewMove: (Offset?) -> Unit,
    onCanvasSize: (IntSize) -> Unit
) {
    val painter = rememberAsyncImagePainter(model = imageUri)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.1f))
    ) {
        // Background checkerboard pattern to show transparency
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCheckerboard()
        }

        // Original image
        Image(
            painter = painter,
            contentDescription = "Character Image",
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    canvasSize = coordinates.size
                    onCanvasSize(canvasSize)
                },
            contentScale = ContentScale.Fit
        )

        // Overlay canvas for drawing mask and preview
        if (isBackgroundRemovalMode) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                val offset = Offset(event.x, event.y)
                                onDrawStart(offset)
                                onPreviewMove(offset)
                                true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val offset = Offset(event.x, event.y)
                                onDrawContinue(offset)
                                onPreviewMove(offset)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                onDrawEnd()
                                onPreviewMove(null)
                                true
                            }
                            MotionEvent.ACTION_HOVER_MOVE -> {
                                val offset = Offset(event.x, event.y)
                                onPreviewMove(offset)
                                true
                            }
                            MotionEvent.ACTION_HOVER_EXIT -> {
                                onPreviewMove(null)
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                // Draw the mask (areas to be removed)
                drawPath(
                    path = maskPath,
                    color = Color.Red.copy(alpha = 0.4f),
                    style = Fill
                )

                // Draw mask border for better visibility
                drawPath(
                    path = maskPath,
                    color = Color.Red.copy(alpha = 0.8f),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Draw preview circle cursor
                previewPosition?.let { position ->
                    // Outer ring (white for contrast)
                    drawCircle(
                        color = Color.White,
                        radius = brushSize / 2 + 2.dp.toPx(),
                        center = position,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Inner ring (brush color)
                    drawCircle(
                        color = Color.Red,
                        radius = brushSize / 2,
                        center = position,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Semi-transparent fill to show area
                    drawCircle(
                        color = Color.Red.copy(alpha = 0.2f),
                        radius = brushSize / 2,
                        center = position,
                        style = Fill
                    )
                }
            }
        }
    }
}

// Extension function to draw checkerboard pattern
private fun DrawScope.drawCheckerboard() {
    val checkSize = 10.dp.toPx()
    val numChecksX = (size.width / checkSize).toInt() + 1
    val numChecksY = (size.height / checkSize).toInt() + 1

    for (x in 0 until numChecksX) {
        for (y in 0 until numChecksY) {
            val isEvenCheck = (x + y) % 2 == 0
            drawRect(
                color = if (isEvenCheck) Color.White else Color.Gray.copy(alpha = 0.2f),
                topLeft = Offset(x * checkSize, y * checkSize),
                size = Size(checkSize, checkSize)
            )
        }
    }
}