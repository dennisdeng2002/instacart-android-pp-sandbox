package com.instacart.android.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.instacart.android.challenges.network.DeliveryItem
import com.instacart.android.challenges.network.NetworkService
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    interface UpdateListener {
        fun onUpdate(state: ItemListViewState)
    }

    private var itemListViewState: ItemListViewState
    private var listener: UpdateListener? = null
    private val networkService = NetworkService()

    init {
        itemListViewState = ItemListViewState("Delivery Items", emptyList())
        fetchAllItems()
    }

    private fun fetchAllItems() {
        viewModelScope.launch {
            val api = networkService.api
            val ordersResponse = api.fetchOrdersCoroutine()
            val orderIds = ordersResponse.orders
            val linkedHashMap = LinkedHashMap<String, DeliveryItem>()
            for (orderId in orderIds) {
                val orderResponse = api.fetchOrderByIdCoroutine(orderId)
                val items = orderResponse.items
                for (item in items) {
                    val name = item.name
                    if (name !in linkedHashMap) {
                        linkedHashMap[name] = item
                    } else {
                        linkedHashMap.getValue(name).count += item.count
                    }
                }
            }

            val itemRows = linkedHashMap.values.map { deliveryItem ->
                ItemRow(
                    name = deliveryItem.name,
                    count = deliveryItem.count
                )
            }

            itemListViewState = itemListViewState.copy(items = itemRows)
            listener?.onUpdate(itemListViewState)
        }
    }

    fun setStateUpdateListener(listener: UpdateListener?) {
        this.listener = listener

        listener?.onUpdate(itemListViewState)
    }
}
