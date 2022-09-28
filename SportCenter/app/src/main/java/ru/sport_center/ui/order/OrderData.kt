package ru.sport_center.ui.order

import ru.sport_center.data.entity.Order

data class OrderData(val isDeleted: Boolean = false, val order: Order? = null, val doNotSave: Boolean = false)