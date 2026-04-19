package com.nsnm.herenow.api.home.service

import com.nsnm.herenow.api.home.dto.HomeResponse
import com.nsnm.herenow.api.item.service.ItemService
import com.nsnm.herenow.api.room.service.RoomService
import com.nsnm.herenow.api.storage.dto.StorageResponse
import com.nsnm.herenow.domain.room.repository.RoomRepository
import com.nsnm.herenow.domain.storage.repository.StorageRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class HomeService(
    private val roomService: RoomService,
    private val itemService: ItemService,
    private val roomRepository: RoomRepository,
    private val storageRepository: StorageRepository
) {

    fun getHomeData(userId: UUID, spaceId: UUID): HomeResponse {
        val rooms = roomService.getRooms(userId, spaceId)
        val roomIds = rooms.map { it.id }
        val storages = storageRepository.findByRoomIdIn(roomIds).map { s ->
            StorageResponse(
                id = s.id, roomId = s.roomId, name = s.name,
                x = s.x, y = s.y, w = s.w, h = s.h,
                color = s.color, topColor = s.topColor, design = s.design,
                gridRows = s.gridRows, gridCols = s.gridCols, layout = s.layout,
                createdAt = s.createdAt
            )
        }
        val items = itemService.getItems(userId, spaceId)
        val today = LocalDate.now()
        val sevenDaysLater = today.plusDays(7)

        val imminentItems = items.filter { item ->
            item.expiryDate != null && !item.expiryDate.isBefore(today) && !item.expiryDate.isAfter(sevenDaysLater)
        }
        val lowStockItems = items.filter { item ->
            item.minQuantity > 0 && item.quantity <= item.minQuantity
        }

        return HomeResponse(
            rooms = rooms,
            storages = storages,
            items = items,
            imminentItems = imminentItems,
            lowStockItems = lowStockItems,
            totalItemCount = items.size
        )
    }
}
