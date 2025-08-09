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
import com.aftab.cat.ui.theme.* // Import your color palette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CharacterSettingsScreen(
    characterId: String,
    onNavigateBack: () -> Unit,
    isCharacterRunning: Boolean = false,
    viewModel: CharacterSettingsViewModel = hiltViewModel()
) {
    val character by viewModel.character.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val width by viewModel.width.collectAsState()
    val height by viewModel.height.collectAsState()
    val animationDelay by viewModel.animationDelay.collectAsState()
    val yPosition by viewModel.yPosition.collectAsState()
    val xPosition by viewModel.xPosition.collectAsState()
    val linkedDimensions by viewModel.linkedDimensions.collectAsState()
    val characterRunning by viewModel.isCharacterRunning.collectAsState()

    // UI state
    val animationSpeedPresets = listOf(50L, 80L, 100L, 140L, 200L, 300L, 500L)
    var showPresetInfo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Check if character is hanging (static)
    val isHangingCharacter = character?.isHanging == true

    // Load character on startup and set running state
    LaunchedEffect(characterId, isCharacterRunning) {
        viewModel.loadCharacter(characterId)
        viewModel.setCharacterRunning(isCharacterRunning)
    }

    Scaffold(
        containerColor = Background, // Using custom background color
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${character?.name} Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnTopBar // Using custom topbar text color
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconOnPrimary // Using custom icon color
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground // Using custom topbar background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        containerColor = Container.copy(alpha = 0.4f) // Using custom container with transparency
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Primary, // Using custom primary color
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isHangingCharacter)
                                "Hanging character is active - changes will be applied instantly!"
                            else
                                "Character is running - changes will be applied instantly!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary, // Using custom primary color
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Character Type Info Card
            if (isHangingCharacter) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Container.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = "Static Hanging Character",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = OnBackground
                            )
                            Text(
                                text = "This character stays in one position and doesn't move",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Test Controls (only show when character is NOT running)
            if (!characterRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryVariant.copy(alpha = 0.4f) // Using custom secondary variant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Your Settings",
                            style = MaterialTheme.typography.titleSmall,
                            color = OnSecondary // Using custom secondary text color
                        )
                        Text(
                            text = if (isHangingCharacter)
                                "Activate the character to see it positioned on screen"
                            else
                                "Start the character to see changes in real-time",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSecondary.copy(alpha = 0.8f), // Using custom secondary text with transparency
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.saveSettings()
                                viewModel.startCharacterTest()
                            },
                            enabled = character != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary, // Using custom button color
                                contentColor = OnButtonPrimary // Using custom button text color
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (isHangingCharacter) "Activate Character" else "Start Test",
                                modifier = Modifier.padding(start = 8.dp)
                            )
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
                        contentColor = Error // Using custom error color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (isHangingCharacter) "Deactivate Character" else "Stop Test",
                        modifier = Modifier.padding(start = 8.dp)
                    )
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
                    color = OnBackground // Using custom text on background color
                )
                Switch(
                    checked = linkedDimensions,
                    onCheckedChange = { viewModel.setLinkedDimensions(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary, // Using custom primary color
                        checkedTrackColor = Container, // Using custom container color
                        uncheckedThumbColor = IconSecondary, // Using custom secondary icon color
                        uncheckedTrackColor = SurfaceVariant // Using custom surface variant
                    )
                )
            }

            // Size Controls
            Text(
                text = "Character Size",
                style = MaterialTheme.typography.titleSmall,
                color = Primary.copy(alpha = 0.9f) // Using custom primary color with transparency
            )

            // Width control
            Text(
                text = "Width: $width px",
                style = MaterialTheme.typography.bodyLarge,
                color = OnBackground // Using custom text on background
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
                colors = SliderDefaults.colors(
                    thumbColor = Primary, // Using custom primary color
                    activeTrackColor = Primary.copy(alpha = 0.7f), // Using custom primary with transparency
                    inactiveTrackColor = OutlineVariant // Using custom outline variant
                )
            )

            // Height control (only show if not linked)
            if (!linkedDimensions) {
                Text(
                    text = "Height: $height px",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnBackground // Using custom text on background
                )
                Slider(
                    value = height.toFloat(),
                    onValueChange = { newHeight ->
                        viewModel.updateDimensions(width, newHeight.toInt())
                    },
                    valueRange = 10f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary, // Using custom primary color
                        activeTrackColor = Primary.copy(alpha = 0.7f), // Using custom primary with transparency
                        inactiveTrackColor = OutlineVariant // Using custom outline variant
                    )
                )
            }

            // Position Controls
            Text(
                text = "Position Settings ${if (characterRunning) "(Live Updates)" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = if (characterRunning) Primary else Primary.copy(alpha = 0.9f) // Using custom primary color
            )

            // X Position control (only for hanging characters)
            if (isHangingCharacter) {
                Text(
                    text = "Horizontal Position: $xPosition px from left",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnBackground // Using custom text on background
                )
                Slider(
                    value = xPosition.toFloat(),
                    onValueChange = { viewModel.updateXPosition(it.toInt()) },
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary, // Using custom primary color
                        activeTrackColor = Primary.copy(alpha = 0.7f), // Using custom primary with transparency
                        inactiveTrackColor = OutlineVariant // Using custom outline variant
                    )
                )
            }

            // Y Position control
            Text(
                text = if (isHangingCharacter)
                    "Vertical Position: $yPosition px from top"
                else
                    "Vertical Position: $yPosition px from top",
                style = MaterialTheme.typography.bodyLarge,
                color = OnBackground // Using custom text on background
            )
            Slider(
                value = yPosition.toFloat(),
                onValueChange = { viewModel.updateYPosition(it.toInt()) },
                valueRange = 0f..300f,
                colors = SliderDefaults.colors(
                    thumbColor = Primary, // Using custom primary color
                    activeTrackColor = Primary.copy(alpha = 0.7f), // Using custom primary with transparency
                    inactiveTrackColor = OutlineVariant // Using custom outline variant
                )
            )

            // Speed control (disabled for hanging characters)
            Text(
                text = if (isHangingCharacter)
                    "Movement Speed: Static (no movement)"
                else
                    "Movement Speed: $speed px/frame",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isHangingCharacter)
                    OnBackground.copy(alpha = 0.5f)
                else
                    OnBackground // Using custom text on background
            )
            Slider(
                value = speed.toFloat(),
                onValueChange = { viewModel.updateSpeed(it.toInt()) },
                valueRange = 1f..10f,
                enabled = !isHangingCharacter, // Disable for hanging characters
                colors = SliderDefaults.colors(
                    thumbColor = Primary, // Using custom primary color
                    activeTrackColor = Primary.copy(alpha = 0.7f), // Using custom primary with transparency
                    inactiveTrackColor = OutlineVariant // Using custom outline variant
                )
            )

            // Animation Speed Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isHangingCharacter)
                        "Animation: ${animationDelay}ms (static)"
                    else
                        "Animation Speed: ${animationDelay}ms",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHangingCharacter)
                        OnBackground.copy(alpha = 0.5f)
                    else
                        OnBackground // Using custom text on background
                )
                IconButton(
                    onClick = { showPresetInfo = !showPresetInfo },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = IconSecondary, // Using custom secondary icon color
                    )
                }
            }

            if (showPresetInfo) {
                Text(
                    text = if (isHangingCharacter)
                        "Hanging characters display static images, animation delay doesn't affect them"
                    else
                        "Lower values = faster animation (more battery usage)",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnBackground.copy(alpha = 0.7f) // Using custom text with transparency
                )
            }

            // Preset chips (disabled for hanging characters)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                animationSpeedPresets.forEach { preset ->
                    FilterChip(
                        selected = animationDelay == preset,
                        onClick = { viewModel.updateAnimationDelay(preset) },
                        enabled = !isHangingCharacter, // Disable for hanging characters
                        label = { Text("${preset}ms") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Container, // Using custom container color
                            selectedLabelColor = OnContainer, // Using custom text on container
                            containerColor = SurfaceVariant, // Using custom surface variant
                            labelColor = OnSurfaceVariant // Using custom text on surface variant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selectedBorderColor = Primary, // Using custom primary color
                            borderColor = OutlineSecondary, // Using custom secondary outline
                            selected = animationDelay == preset,
                            enabled = !isHangingCharacter
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
                    containerColor = ButtonPrimary, // Using custom button color
                    contentColor = OnButtonPrimary // Using custom button text color
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
                    contentColor = IconPrimary // Using custom primary icon color
                )
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}