package com.lexur.yumo.home_screen.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Lucide
import com.lexur.yumo.ui.theme.ButtonPrimary
import com.lexur.yumo.ui.theme.CardBackground
import com.lexur.yumo.ui.theme.OnButtonPrimary
import com.lexur.yumo.ui.theme.OnCard
import com.lexur.yumo.ui.theme.Primary
import com.lexur.yumo.ui.theme.SecondaryVariant

@Composable
fun HangingCharacterInfoDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    characterName: String = "character"
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Hanging Character",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = OnCard
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "$characterName is a hanging character that can be positioned at various locations on your screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnCard.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "You can hang it from:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = OnCard
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HangingLocationItem(
                            title = "Camera Notch",
                            description = "Position around the front camera"
                        )

                        HangingLocationItem(
                            title = "Status Bar",
                            description = "Hang from the top status bar area"
                        )

                        HangingLocationItem(
                            title = "Battery Icon",
                            description = "Position near the battery indicator"
                        )

                        HangingLocationItem(
                            title = "WiFi Icon",
                            description = "Hang from the WiFi signal indicator"
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = SecondaryVariant.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Tip: You can adjust the exact position in character settings after starting the character.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnCard.copy(alpha = 0.7f),
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
                        contentColor = OnButtonPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got it!")
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HangingLocationItem(
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = OnCard
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnCard.copy(alpha = 0.7f)
            )
        }
    }
}