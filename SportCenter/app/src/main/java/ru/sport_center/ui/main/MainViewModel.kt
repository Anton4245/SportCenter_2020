package ru.sport_center.ui.main

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order
import ru.sport_center.data.model.OrderResult
import ru.sport_center.ui.base.BaseViewModel
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(val ordersRepository: OrdersRepository) : BaseViewModel<List<Order>?>() {

    private var ordersChannel :ReceiveChannel<OrderResult>? = null
    var choosedDate: Date? = null
    var choosedInterval: Interval? = null

    init {
        newChannel()
    }

    private fun newChannel() {
        ordersChannel = ordersRepository.getOrders(choosedDate, choosedInterval)
        launch {
            ordersChannel!!.consumeEach {
                when (it) {
                    is OrderResult.Success<*> -> setData(it.data as? List<Order>)
                    is OrderResult.Error -> setError(it.error)
                }
            }
        }
    }

    fun updateFilterForChannel() {
        ordersChannel?.cancel()
        setData(ArrayList())
        newChannel()
    }

    @VisibleForTesting
    public override fun onCleared() {
        ordersChannel?.cancel()
        super.onCleared()
    }

    fun setInitialIntervals(date: Date) = ordersRepository.setInitialIntervals(date)

}