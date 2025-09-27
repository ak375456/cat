package com.lexur.yumo.custom_character.presentation

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCharacterCreationScreen(
    navController: NavController,
    viewModel: CustomCharacterCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.onImageSelected(uri) }
    )

    var showRopeSelection by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

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
                navController.popBackStack() // Go back to home screen
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
                                enabled = uiState.strokeHistory.isNotEmpty() || uiState.maskPath != Path()
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
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

                        if (uiState.isBackgroundRemovalMode) {
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
                                        valueRange = 10f..100f,
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
                        }

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
                                onClick = {
                                    viewModel.finishEditing()
                                    showRopeSelection = true
                                },
                                modifier = Modifier.weight(1f),
                                enabled = uiState.maskPath != Path()
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

@Composable
fun NameCharacterDialog(
    onNameSelected: (String) -> Unit,
    onDismiss: () -> Unit
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
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(model = imageUri)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Create a processed image with the mask applied
    val processedImageBitmap = remember(maskPath, canvasSize) {
        if (canvasSize == IntSize.Zero) return@remember null

        // Load the original bitmap
        val originalBitmap = context.contentResolver.openInputStream(imageUri)?.use {
            android.graphics.BitmapFactory.decodeStream(it)
        } ?: return@remember null

        // Scale to canvas size
        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
            originalBitmap,
            canvasSize.width,
            canvasSize.height,
            true
        )

        // Create result bitmap with transparency
        val resultBitmap = android.graphics.Bitmap.createBitmap(
            canvasSize.width,
            canvasSize.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val resultCanvas = android.graphics.Canvas(resultBitmap)

        // Draw the original image
        resultCanvas.drawBitmap(scaledBitmap, 0f, 0f, null)

        // Apply the inverse mask to remove areas
        if (maskPath != Path()) {
            val erasePaint = android.graphics.Paint().apply {
                xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT)
                isAntiAlias = true
            }
            resultCanvas.drawPath(maskPath.asAndroidPath(), erasePaint)
        }

        resultBitmap.asImageBitmap()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray.copy(alpha = 0.1f))
    ) {
        // Background checkerboard pattern to show transparency
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCheckerboard()
        }

        // Show either processed image or original based on whether we have edits
        if (processedImageBitmap != null && maskPath != Path()) {
            // Show the processed image with background removed
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        if (canvasSize != coordinates.size) {
                            canvasSize = coordinates.size
                            onCanvasSize(coordinates.size)
                        }
                    }
            ) {
                drawImage(
                    image = processedImageBitmap,
                    topLeft = Offset.Zero
                )
            }
        } else {
            // Show original image
            Image(
                painter = painter,
                contentDescription = "Character Image",
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        canvasSize = coordinates.size
                        onCanvasSize(coordinates.size)
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Overlay canvas for drawing and preview
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
                // Draw semi-transparent overlay to show what will be removed
                drawPath(
                    path = maskPath,
                    color = Color.Red.copy(alpha = 0.2f),
                    style = Fill
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

                    // Inner ring (red to indicate removal)
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
