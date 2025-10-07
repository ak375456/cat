package com.lexur.yumo.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lexur.yumo.ui.theme.*

@Composable
fun PremiumFeatureDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    isLoading: Boolean = false,
    productPrice: String = "",
    error: String? = null
) {
    if (!showDialog) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = DialogBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Primary,
                                    ButtonPrimary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Premium Character Creator",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = OnPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock unlimited creativity!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnPrimary.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create characters that truly feel yours! This app was lovingly crafted by an indie developer who believes everyone should have their perfect companion on screen.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnDialog,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Divider
                    )

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Error.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnError,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Text(
                        text = "What You'll Get:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnDialog
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.Person,
                        title = "Upload Your Own Characters",
                        description = "Bring your partner, friend, or any character to life"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.Image,
                        title = "Background Removal",
                        description = "Remove background with magnifier preview for cutouts"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.AutoFixHigh,
                        title = "Professional Edge Feathering",
                        description = "Smooth, natural-looking character edges"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.Brush,
                        title = "Custom Rope Selection",
                        description = "Choose, resize, and position ropes perfectly"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.ZoomIn,
                        title = "Live Preview",
                        description = "See exactly how your character will look before saving"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.AllInclusive,
                        title = "Unlimited Creations",
                        description = "Create as many custom characters as you want"
                    )

                    PremiumFeatureItem(
                        icon = Icons.Default.Upload,
                        title = "Pre-Cut Image Support",
                        description = "Skip background removal if you already have cutouts"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your support helps keep this app growing with new features and improvements!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = OnContainerVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Error.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnError,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                // Bottom action buttons
                Surface(
                    tonalElevation = 3.dp,
                    color = DialogSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OnButtonSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    colors = listOf(OutlinePrimary, OutlinePrimary)
                                )
                            )
                        ) {
                            Text("Maybe Later")
                        }

                        Button(
                            onClick = onPurchase,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = OnButtonPrimary,
                                disabledContainerColor = Disabled,
                                disabledContentColor = OnDisabled
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = OnButtonPrimary
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (productPrice.isNotEmpty()) {
                                            "Get Premium $productPrice"
                                        } else {
                                            "Get Premium"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = OnCard
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
        }
    }
}