package com.lexur.yumo.custom_character.presentation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import android.net.Uri
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopeAdjustmentScreen(
    imageUri: Uri,
    maskPath: Path,
    currentStrokePath: Path,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float,
    onRopeScaleChanged: (Float) -> Unit,
    onRopeOffsetXChanged: (Float) -> Unit,
    onRopeOffsetYChanged: (Float) -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adjust Rope Position") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onConfirm) {
                        Icon(Icons.Default.Done, contentDescription = "Confirm")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.LightGray.copy(alpha = 0.1f))
            ) {
                RopePreviewCanvas(
                    imageUri = imageUri,
                    maskPath = maskPath,
                    currentStrokePath = currentStrokePath,
                    ropeResId = ropeResId,
                    ropeScale = ropeScale,
                    ropeOffsetX = ropeOffsetX,
                    ropeOffsetY = ropeOffsetY
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rope Adjustments",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Size",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${(ropeScale * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = ropeScale,
                            onValueChange = onRopeScaleChanged,
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Horizontal Position",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${ropeOffsetX.toInt()}px",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = ropeOffsetX,
                            onValueChange = onRopeOffsetXChanged,
                            valueRange = -200f..200f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vertical Position",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${ropeOffsetY.toInt()}px",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = ropeOffsetY,
                            onValueChange = onRopeOffsetYChanged,
                            valueRange = -200f..200f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onRopeScaleChanged(1f)
                            onRopeOffsetXChanged(0f)
                            onRopeOffsetYChanged(0f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Default")
                    }
                }
            }
        }
    }
}

@Composable
private fun RopePreviewCanvas(
    imageUri: Uri,
    maskPath: Path,
    currentStrokePath: Path,
    ropeResId: Int,
    ropeScale: Float,
    ropeOffsetX: Float,
    ropeOffsetY: Float
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

    val ropeBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = ropeResId) {
        value = try {
            context.getDrawable(ropeResId)?.toBitmap()?.asImageBitmap()
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

    val processedBitmap = remember(imageBitmap, maskPath, currentStrokePath) {
        if (imageBitmap != null) {
            createTransparentBitmap(imageBitmap!!, maskPath, currentStrokePath)
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .onGloballyPositioned { coordinates ->
                if (canvasSize != coordinates.size) {
                    canvasSize = coordinates.size
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCheckerboard()

            if (transformation != null && ropeBitmap != null && processedBitmap != null) {
                val ropeWidth = ropeBitmap!!.width * ropeScale * transformation.scaleFactor
                val ropeHeight = ropeBitmap!!.height * ropeScale * transformation.scaleFactor
                val ropeX = transformation.imageOffset.x +
                        (transformation.displayedImageSize.width - ropeWidth) / 2 +
                        ropeOffsetX * transformation.scaleFactor
                val ropeY = transformation.imageOffset.y + ropeOffsetY * transformation.scaleFactor

                drawImage(
                    image = ropeBitmap!!,
                    dstOffset = IntOffset(ropeX.roundToInt(), ropeY.roundToInt()),
                    dstSize = IntSize(ropeWidth.roundToInt(), ropeHeight.roundToInt())
                )

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
        }
    }
}