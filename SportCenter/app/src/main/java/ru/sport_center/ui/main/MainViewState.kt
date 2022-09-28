package ru.sport_center.ui.main

import ru.sport_center.data.entity.Order
import ru.sport_center.ui.base.BaseViewState

class MainViewState(val orders: List<Order>? = null, error: Throwable? = null): BaseViewState<List<Order>?>(orders, error)