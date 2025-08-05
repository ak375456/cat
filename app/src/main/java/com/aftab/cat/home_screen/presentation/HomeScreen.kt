package com.aftab.cat.home_screen.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftab.cat.componenets.PermissionExplanationDialog
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
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()

    // Check permissions
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }



    // Initialize dialog state
    LaunchedEffect(Unit) {
        viewModel.initializeDialogState(context)
    }

    // Bind to service and sync state
    LaunchedEffect(Unit) {
        viewModel.bindToService(context)
    }

    LaunchedEffect(hasOverlayPermission, hasNotificationPermission) {
        if (hasOverlayPermission && hasNotificationPermission) {
            viewModel.syncWithService()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindFromService(context)
        }
    }

    // Permission explanation dialog
    PermissionExplanationDialog(
        showDialog = showPermissionDialog,
        onDismiss = { viewModel.dismissPermissionDialog() },
        onDontShowAgain = { viewModel.setDontShowPermissionDialogAgain() },
        onContinue = { viewModel.dismissPermissionDialog() }
    )

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
                            viewModel.startCharacter(context, character)
                        } else {
                            // Show a snackbar or navigate to settings
                            onNavigateToSettings()
                        }
                        expandedCharacterId = null
                    },
                    onCharacterSettings = {
                        onNavigateToCharacterSettings(character.id)
                        expandedCharacterId = null
                    },
                    isCharacterRunning = isRunning,
                    onStopCharacter = {
                        viewModel.stopCharacter(context, character.id)
                    },
                    canUseCharacter = hasOverlayPermission && hasNotificationPermission
                )
            }

            // Show a simple message if permissions are missing
            if (!hasOverlayPermission || !hasNotificationPermission) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "⚠️ Permissions Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "To use your pets, please grant the required permissions in Settings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Go to Settings")
                            }
                        }
                    }
                }
            }
        }
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
        true // Not required for older versions
    }
}