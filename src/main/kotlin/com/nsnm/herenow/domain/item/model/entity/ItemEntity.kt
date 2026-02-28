package com.nsnm.herenow.domain.item.model.entity

import com.nsnm.herenow.lib.model.entity.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

import jakarta.persistence.EntityListeners

/**
 * 아이템 (재고 특성 내포)
 */
@Entity
@Table(name = "items")
class ItemEntity(
    @Id
    var itemId: String = UUID.randomUUID().toString(),
    
    var groupId: String,
    
    var categoryId: String? = null,
    
    var locationId: String? = null,
    
    var itemName: String,
    
    var quantity: Int = 1,
    
    var minQuantity: Int = 0,
    
    var purchaseDate: LocalDate? = null,
    
    var purchasePlace: String? = null,
    
    var price: BigDecimal? = null,
    
    var expiryDate: LocalDate? = null,
    
    var memo: String? = null,

    var shortcutNumber: Int? = null
) : BaseEntity()
