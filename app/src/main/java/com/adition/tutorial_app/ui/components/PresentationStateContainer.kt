package com.adition.tutorial_app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.adition.tutorial_app.presentation.entities.PresentationState

@Composable
fun <Data> PresentationStateContainer(
    state: PresentationState<Data>,
    modifier: Modifier = Modifier,
    content: @Composable (data: Data) -> Unit
) {
    when (state) {
        is PresentationState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
            ) {
                CircularProgressIndicator()
            }
        }
        is PresentationState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
            ) {
                Text(text = state.description, color = Color.Red)
            }
        }
        is PresentationState.Loaded -> content(state.data)
    }
}
