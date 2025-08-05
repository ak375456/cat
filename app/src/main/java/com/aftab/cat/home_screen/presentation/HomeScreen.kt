package com.aftab.cat.home_screen.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftab.cat.UniversalOverlayService
import com.aftab.cat.home_screen.presentation.components.AnimatedCharacterPreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCharacterSettings: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val characters by viewModel.allCharacters.collectAsState()
    var expandedCharacterId by remember { mutableStateOf<String?>(null) }

    val runningCharacters by viewModel.runningCharacters.collectAsState()

    // Permission states and launchers
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { hasOverlayPermission = checkOverlayPermission(context) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasNotificationPermission = isGranted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overlay Pets") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = characters, key = { it.id }) { character ->
                val isRunning = runningCharacters.contains(character.id)
                AnimatedCharacterPreviewCard(
                    character = character,
                    isExpanded = expandedCharacterId == character.id,
                    onCardClick = {
                        expandedCharacterId = if (expandedCharacterId == character.id) null else character.id
                    },
                    onUseCharacter = {
                        if (hasOverlayPermission && hasNotificationPermission) {
                            startOverlayService(context, character.id)
                            // Update the ViewModel state to track this character as running
                            viewModel.startCharacter(character.id)
                        }
                        expandedCharacterId = null
                    },
                    onCharacterSettings = {
                        onNavigateToCharacterSettings(character.id)
                        expandedCharacterId = null
                    },
                    isCharacterRunning = isRunning,
                    onStopCharacter = {
                        // Stop the character service
                        stopCharacterOverlay(context, character.id)
                        // Update the ViewModel state
                        viewModel.stopCharacter(character.id)
                    },
                    canUseCharacter = hasOverlayPermission && hasNotificationPermission
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Permission status card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Permissions",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            PermissionStatusRow("Overlay Permission", hasOverlayPermission)
                            PermissionStatusRow("Notification Permission", hasNotificationPermission)
                        }
                    }

                    // Permission buttons
                    if (!hasOverlayPermission) {
                        Button(
                            onClick = { requestOverlayPermission(context, overlayPermissionLauncher) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Grant Overlay Permission") }
                    }

                    if (!hasNotificationPermission) {
                        Button(
                            onClick = { requestNotificationPermission(notificationPermissionLauncher) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Grant Notification Permission") }
                    }

                    OutlinedButton(
                        onClick = {
                            stopOverlayService(context)
                            // Clear all running characters from ViewModel state
                            viewModel.clearAllRunningCharacters()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stop All Overlays")
                    }

                    if (!hasOverlayPermission || !hasNotificationPermission) {
                        Text(
                            text = "Please grant all permissions to use the overlay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    permissionName: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = permissionName)
        Text(
            text = if (isGranted) "✅ Granted" else "❌ Required",
            color = if (isGranted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

private fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun requestOverlayPermission(
    context: Context,
    launcher: ActivityResultLauncher<Intent>
) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    launcher.launch(intent)
}

private fun requestNotificationPermission(
    launcher: ActivityResultLauncher<String>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun startOverlayService(context: Context, characterId: String) {
    val serviceIntent = Intent(context, UniversalOverlayService::class.java).apply {
        putExtra("character_id", characterId)
    }
    ContextCompat.startForegroundService(context, serviceIntent)
}

private fun stopCharacterOverlay(context: Context, characterId: String) {
    val serviceIntent = Intent(context, UniversalOverlayService::class.java).apply {
        action = "STOP"
        putExtra("character_id", characterId)
    }
    ContextCompat.startForegroundService(context, serviceIntent)
}

private fun stopOverlayService(context: Context) {
    val serviceIntent = Intent(context, UniversalOverlayService::class.java)
    context.stopService(serviceIntent)
}