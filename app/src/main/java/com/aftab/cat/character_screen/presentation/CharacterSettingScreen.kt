package com.aftab.cat.character_screen.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterSettingsScreen(
    characterId: String,
    onNavigateBack: () -> Unit,
    isCharacterRunning: Boolean = false, // Pass this from the calling screen
    viewModel: CharacterSettingsViewModel = hiltViewModel()
) {
    val character by viewModel.character.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val width by viewModel.width.collectAsState()
    val height by viewModel.height.collectAsState()
    val animationDelay by viewModel.animationDelay.collectAsState()
    val yPosition by viewModel.yPosition.collectAsState()
    val linkedDimensions by viewModel.linkedDimensions.collectAsState()
    val characterRunning by viewModel.isCharacterRunning.collectAsState()

    // UI state
    val animationSpeedPresets = listOf(50L, 80L, 100L, 140L, 200L, 300L, 500L)
    var showPresetInfo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Load character on startup and set running state
    LaunchedEffect(characterId, isCharacterRunning) {
        viewModel.loadCharacter(characterId)
        viewModel.setCharacterRunning(isCharacterRunning)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${character?.name} Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Updates Info Card
            if (characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Character is running - changes will be applied instantly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Test Controls (only show when character is NOT running)
            if (!characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Your Settings",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Start the character to see changes in real-time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.saveSettings() // Save current settings first
                                viewModel.startCharacterTest()
                            },
                            enabled = character != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Start Test", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            } else {
                // Stop Test Button
                OutlinedButton(
                    onClick = {
                        viewModel.stopCharacterTest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("Stop Test", modifier = Modifier.padding(start = 8.dp))
                }
            }

            // Linked Dimensions Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Linked Dimensions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = linkedDimensions,
                    onCheckedChange = { viewModel.setLinkedDimensions(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            // Size Controls
            Text(
                text = "Character Size",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            // Width control
            Text(
                text = "Width: $width px",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = width.toFloat(),
                onValueChange = { newWidth ->
                    if (linkedDimensions) {
                        viewModel.updateDimensions(newWidth.toInt(), newWidth.toInt())
                    } else {
                        viewModel.updateDimensions(newWidth.toInt(), height)
                    }
                },
                valueRange = 10f..50f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            // Height control (only show if not linked)
            if (!linkedDimensions) {
                Text(
                    text = "Height: $height px",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = height.toFloat(),
                    onValueChange = { newHeight ->
                        viewModel.updateDimensions(width, newHeight.toInt())
                    },
                    valueRange = 10f..50f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Position Controls
            Text(
                text = "Position Settings ${if (characterRunning) "(Live Updates)" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = if (characterRunning)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )

            // Y Position control
            Text(
                text = "Vertical Position: $yPosition px from top",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = yPosition.toFloat(),
                onValueChange = { viewModel.updateYPosition(it.toInt()) },
                valueRange = 0f..300f,
                steps = 30,
                colors = SliderDefaults.colors(
                    thumbColor = if (characterRunning)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (characterRunning)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary
                )
            )

            // Speed control
            Text(
                text = "Movement Speed: $speed px/frame",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { viewModel.updateSpeed(it.toInt()) },
                valueRange = 1f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            // Animation Speed Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Animation Speed: ${animationDelay}ms",
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(
                    onClick = { showPresetInfo = !showPresetInfo },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (showPresetInfo) {
                Text(
                    text = "Lower values = faster animation (more battery usage)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Preset chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                animationSpeedPresets.forEach { preset ->
                    FilterChip(
                        selected = animationDelay == preset,
                        onClick = { viewModel.updateAnimationDelay(preset) },
                        label = { Text("${preset}ms") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            selected = animationDelay == preset,
                            enabled = true
                        )
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    viewModel.saveSettings()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save Settings", style = MaterialTheme.typography.labelLarge)
            }

            // Reset to defaults button
            OutlinedButton(
                onClick = {
                    viewModel.resetToDefaults()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}