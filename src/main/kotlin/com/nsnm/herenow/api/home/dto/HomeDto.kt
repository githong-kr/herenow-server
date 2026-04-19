package com.nsnm.herenow.api.home.dto

import com.nsnm.herenow.api.item.dto.ItemResponse
import com.nsnm.herenow.api.room.dto.RoomResponse
import com.nsnm.herenow.api.storage.dto.StorageResponse

data class HomeResponse(
    val rooms: List<RoomResponse>,
    val storages: List<StorageResponse>,
    val items: List<ItemResponse>,
    val imminentItems: List<ItemResponse>,
    val lowStockItems: List<ItemResponse>,
    val totalItemCount: Int
)
