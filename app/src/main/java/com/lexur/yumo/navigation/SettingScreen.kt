package com.lexur.yumo.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.lexur.yumo.componenets.PermissionExplanationDialog
import com.lexur.yumo.home_screen.presentation.HomeViewModel
import com.lexur.yumo.ui.theme.Background
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.CardBackground
import com.lexur.yumo.ui.theme.ContainerHigh
import com.lexur.yumo.ui.theme.Error
import com.lexur.yumo.ui.theme.IconSecondary
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnCard
import com.lexur.yumo.ui.theme.OnContainer
import com.lexur.yumo.ui.theme.OnSurface
import com.lexur.yumo.ui.theme.OnSurfaceVariant
import com.lexur.yumo.ui.theme.OnTopBar
import com.lexur.yumo.ui.theme.OutlinePrimary
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.Success
import com.lexur.yumo.ui.theme.TopBarBackground
import com.composables.icons.lucide.Cat
import com.composables.icons.lucide.CircleStop
import com.composables.icons.lucide.Cross
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.composables.icons.lucide.View
import com.lexur.yumo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showPermissionDialog by viewModel.showPermissionDialog.collectAsState()

    // Permission states
    var hasOverlayPermission by remember { mutableStateOf(checkOverlayPermission(context)) }
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    // Permission launchers
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { hasOverlayPermission = checkOverlayPermission(context) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    // REMOVED: Don't initialize dialog state here anymore
    // This was causing the dialog to show every time we navigate to SettingsScreen

    // Permission explanation dialog
    PermissionExplanationDialog(
        showDialog = showPermissionDialog,
        onDismiss = { viewModel.dismissPermissionDialog() },
        onDontShowAgain = { viewModel.setDontShowPermissionDialogAgain() },
        onContinue = { viewModel.dismissPermissionDialog() }
    )

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = OnTopBar) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnTopBar
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopBarBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasOverlayPermission && hasNotificationPermission)
                        ContainerHigh
                    else Error.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = if (hasOverlayPermission && hasNotificationPermission)
                                Icons.Default.CheckCircle
                            else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasOverlayPermission && hasNotificationPermission)
                                Success
                            else Error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Permission Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnContainer
                        )
                    }

                    PermissionStatusRow(
                        "Overlay Permission",
                        hasOverlayPermission,
                        Lucide.View
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PermissionStatusRow(
                        "Notification Permission",
                        hasNotificationPermission,
                        Icons.Default.Notifications
                    )

                    if (!hasOverlayPermission || !hasNotificationPermission) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "⚠️ Some permissions are missing. Your characters may not work properly without all required permissions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }

            // Permission Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permission Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnCard,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Overlay Permission Button
                    if (!hasOverlayPermission) {
                        Button(
                            onClick = { requestOverlayPermission(context, overlayPermissionLauncher) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary,
                                contentColor = OnButtonPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Lucide.View,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Overlay Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        OutlinedButton(
                            onClick = { openOverlaySettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnSurface
                            ),
                            border = BorderStroke(1.dp, OutlinePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage Overlay Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Notification Permission Button
                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary,
                                contentColor = OnButtonPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Notification Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // General App Settings Button
                    OutlinedButton(
                        onClick = { openAppSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OnSurface
                        ),
                        border = BorderStroke(1.dp, OutlinePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open App Settings")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Permission Explanation Button
                    TextButton(
                        onClick = { viewModel.showPermissionDialogManually() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Why do we need these permissions?")
                    }
                }
            }

            // App Control Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Controls",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnCard,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.clearAllRunningCharacters(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Error
                        ),
                        border = BorderStroke(1.dp, Error)
                    ) {
                        Icon(
                            imageVector = Lucide.CircleStop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop All Overlays")
                    }
                }
            }

            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Cat,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About Yumo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnCard
                        )
                    }
                    Text(
                        text = "Yumo brings cute animated characters to your status bar that walk around while you use other apps. We prioritize your privacy and comply with all Google Play policies.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Lucide.User,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Privacy-focused • No data collection • Google Play compliant",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
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
    isGranted: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = IconSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = permissionName,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Lucide.Cross,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isGranted) Success else Error
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "Granted" else "Required",
                style = MaterialTheme.typography.bodySmall,
                color = if (isGranted) Success else Error
            )
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

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    context.startActivity(intent)
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}