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
    onNavigateBack: () -> Unit,
    characterScale: Float,
    onCharacterScaleChanged: (Float) -> Unit,
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
                    ropeOffsetY = ropeOffsetY,
                    characterScale = characterScale
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Character Size",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${(characterScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = characterScale,
                        onValueChange = onCharacterScaleChanged,
                        valueRange = 0.3f..3.0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider()

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
                            valueRange = -100f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onRopeScaleChanged(1f)
                            onRopeOffsetXChanged(0f)
                            onRopeOffsetYChanged(0f)
                            onCharacterScaleChanged(1f)
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
    ropeOffsetY: Float,
    characterScale: Float
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

            if (ropeBitmap != null && processedBitmap != null && canvasSize != IntSize.Zero) {
                // Calculate the total content size (rope + character)
                val ropeScaledWidth = ropeBitmap!!.width * ropeScale
                val ropeScaledHeight = ropeBitmap!!.height * ropeScale
                val characterWidth = processedBitmap.width.toFloat() * characterScale
                val characterHeight = processedBitmap.height.toFloat() * characterScale

                // Total content dimensions
                val totalContentHeight = ropeScaledHeight + characterHeight
                val totalContentWidth = characterWidth.coerceAtLeast(ropeScaledWidth)

                // Calculate scale to fit everything in the canvas
                val scaleToFitWidth = size.width / totalContentWidth
                val scaleToFitHeight = size.height / totalContentHeight
                val scaleFactor = minOf(scaleToFitWidth, scaleToFitHeight) * 0.9f // 0.9f for padding

                // Apply scale to all dimensions
                val displayRopeWidth = ropeScaledWidth * scaleFactor
                val displayRopeHeight = ropeScaledHeight * scaleFactor
                val displayCharacterWidth = characterWidth * scaleFactor
                val displayCharacterHeight = characterHeight * scaleFactor

                // Calculate total display size
                val totalDisplayHeight = displayRopeHeight + displayCharacterHeight
                val totalDisplayWidth = displayCharacterWidth.coerceAtLeast(displayRopeWidth)

                // Center the entire composition in the canvas
                val startX = (size.width - totalDisplayWidth) / 2
                val startY = (size.height - totalDisplayHeight) / 2

                // Calculate rope position (centered horizontally with offset, at the top)
                val ropeX = startX + (totalDisplayWidth - displayRopeWidth) / 2 + (ropeOffsetX * scaleFactor)
                val ropeY = startY + (ropeOffsetY * scaleFactor)

                // Draw rope
                drawImage(
                    image = ropeBitmap!!,
                    dstOffset = IntOffset(ropeX.roundToInt(), ropeY.roundToInt()),
                    dstSize = IntSize(displayRopeWidth.roundToInt(), displayRopeHeight.roundToInt())
                )

                // Calculate character position (always below the rope, centered)
                val characterX = startX + (totalDisplayWidth - displayCharacterWidth) / 2
                val characterY = startY + displayRopeHeight // Character position is fixed relative to composition start

                // Draw character
                drawImage(
                    image = processedBitmap,
                    dstOffset = IntOffset(characterX.roundToInt(), characterY.roundToInt()),
                    dstSize = IntSize(displayCharacterWidth.roundToInt(), displayCharacterHeight.roundToInt())
                )
            }
        }
    }
}