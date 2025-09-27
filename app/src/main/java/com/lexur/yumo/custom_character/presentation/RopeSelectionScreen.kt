package com.lexur.yumo.custom_character.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lexur.yumo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopeSelectionScreen(
    onRopeSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val ropes = listOf(
        R.drawable.rope_1,
        R.drawable.rope_2,
        R.drawable.rope_3,
        R.drawable.rope_4
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Rope") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ropes) { ropeResId ->
                Card(
                    modifier = Modifier.clickable { onRopeSelected(ropeResId) }
                ) {
                    Image(
                        painter = painterResource(id = ropeResId),
                        contentDescription = "Rope",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
