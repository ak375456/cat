// Updated PermissionExplanationDialog.kt with privacy policy link

package com.aftab.cat.componenets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Cat
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.User
import com.composables.icons.lucide.View
import com.aftab.cat.ui.theme.* // Import your color palette

@Composable
fun PermissionExplanationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDontShowAgain: () -> Unit,
    onContinue: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = DialogBackground // Using custom dialog background
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Icon(
                        imageVector = Lucide.Cat,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Primary // Using custom primary color
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Welcome to Yumo!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = OnDialog // Using custom dialog text color
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To bring your cute characters to life on your status bar, we need a few permissions:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = OnDialog.copy(alpha = 0.8f) // Using custom dialog text with transparency
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Permission explanations
                    PermissionExplanationItem(
                        icon = Lucide.View,
                        title = "Overlay Permission",
                        description = "Allows your characters to appear on top of other apps and walk around your status bar cutely."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PermissionExplanationItem(
                        icon = Icons.Default.Notifications,
                        title = "Notification Permission",
                        description = "Keeps your characters running smoothly in the background, even when the app is closed."
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PermissionExplanationItem(
                        icon = Lucide.Shield,
                        title = "Foreground Service",
                        description = "Ensures your pets stay active and responsive while you use other apps."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Privacy assurance card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Container.copy(alpha = 0.6f) // Using custom container color
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Lucide.User,
                                contentDescription = null,
                                tint = Primary, // Using custom primary color
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Your Privacy is Safe",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary // Using custom primary color
                                )
                                Text(
                                    text = "We comply with Google Play policies and do not collect, store, or share any personal data from your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnContainer // Using custom text on container color
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Privacy Policy Link
                    val uriHandler = LocalUriHandler.current
                    val privacyPolicyText = buildAnnotatedString {
                        append("We respect your privacy and do not collect any type of data. Our app is designed with Google privacy policy. ")
                        pushStringAnnotation(
                            tag = "URL",
                            annotation = "https://ak375456.github.io/app-privacy-policy/"
                        )
                        withStyle(
                            style = SpanStyle(
                                color = Primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Read more here")
                        }
                        pop()
                    }

                    ClickableText(
                        text = privacyPolicyText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnDialog.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { offset ->
                            privacyPolicyText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDontShowAgain,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = IconSecondary // Using custom secondary icon color
                            )
                        ) {
                            Text("Don't show again")
                        }

                        Button(
                            onClick = onContinue,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButtonPrimary, // Using custom button color
                                contentColor = OnButtonPrimary // Using custom button text color
                            )
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionExplanationItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Primary // Using custom primary color
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = OnDialog // Using custom dialog text color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnDialog.copy(alpha = 0.7f) // Using custom dialog text with transparency
            )
        }
    }
}