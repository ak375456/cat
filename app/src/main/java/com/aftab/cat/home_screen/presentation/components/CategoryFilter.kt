package com.aftab.cat.home_screen.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aftab.cat.home_screen.data.model.CharacterCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterSection(
    selectedCategory: CharacterCategory?,
    onCategorySelected: (CharacterCategory) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedCategory != null) {
                TextButton(
                    onClick = onClearFilter,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear filter",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(CharacterCategory.entries.toTypedArray()) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    category: CharacterCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderWidth = 1.dp,
            borderWidth = 1.dp,
            enabled = true,
            selected = true
        )
    )
}
