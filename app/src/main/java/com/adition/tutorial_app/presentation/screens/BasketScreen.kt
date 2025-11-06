package com.adition.tutorial_app.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.adition.ad_sdk.api.core.AdService
import com.adition.ad_sdk.api.entities.request.TrackingRequest
import com.adition.tutorial_app.di.ServiceLocator
import com.adition.tutorial_app.ui.components.AppTopBarContainer
import com.adition.tutorial_app.ui.components.LabeledContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class BasketRoute(val id: Int, val price: Int)

@Composable
fun BasketScreen(
    route: BasketRoute,
    viewModel: BasketViewModel = viewModel { BasketViewModel(route.id, route.price) },
    navController: NavController
) {
    val quantity by viewModel.quantity.collectAsState()
    val total by viewModel.total.collectAsState()
    val error by viewModel.error.collectAsState()

    AppTopBarContainer(
        title = "Basket",
        onNavigateBack = { navController.navigateUp() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabeledContent(label = "Item id") { Text(viewModel.id.toString()) }
            LabeledContent(label = "Price") { Text("€${viewModel.price}") }
            LabeledContent(label = "Quantity") { Text(quantity.toString()) }
            IconButton(onClick = { viewModel.onIncreaseQuantity() }) {
                Icon(Icons.Default.Add, contentDescription = "Increase quantity")
            }

            IconButton(onClick = { viewModel.onDecreaseQuantity() }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease quantity")
            }

            LabeledContent(label = "Total") { Text("€$total") }
            Button(
                onClick = { viewModel.onPurchase() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Purchase")
            }

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = Color.Red
                )
            }
        }
    }
}

class BasketViewModel(
    val id: Int,
    val price: Int,
    private val adService: AdService = ServiceLocator.adService
) : ViewModel() {
    private var _quantity = MutableStateFlow(1)
    private var _error = MutableStateFlow<String?>(null)

    val quantity = _quantity.asStateFlow()
    val error = _error.asStateFlow()
    val total: StateFlow<Int> = quantity
        .map { it * price }
        .stateIn(viewModelScope, SharingStarted.Eagerly, price)

    fun onIncreaseQuantity() { _quantity.value += 1 }
    fun onDecreaseQuantity() { if (quantity.value > 1) _quantity.value -= 1 }
    fun onPurchase() {
        val request = TrackingRequest(
            landingPageId = 0,
            trackingSpotId = 0,
            orderId = "My purchase id", // Can be skipped
            price = price.toFloat(), // Can be skipped
            total = total.value.toFloat(), // Can be skipped
            quantity = quantity.value, // Can be skipped
            itemNumber = "$id", // Can be skipped
            description = null, // Can be skipped
            timeout = null // Can be skipped
        )

        viewModelScope.launch {
            adService.trackingRequest(request)
                .get(
                    onSuccess = {
                        Log.d("BasketViewModel", "Successfully tracked purchase with id: $id")
                    },
                    onError = { _error.value = it.description }
                )
        }
    }
}
