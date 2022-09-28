package ru.sport_center.ui.order

import androidx.annotation.VisibleForTesting
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.runBlocking
import ru.sport_center.App
import ru.sport_center.R
import ru.sport_center.common.RENT_TABLE
import ru.sport_center.common.RENT_ZAL
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order
import ru.sport_center.ui.base.BaseViewModel
import java.lang.Exception
import java.util.*
import kotlin.NoSuchElementException

class OrderViewModel(private val ordersRepository: OrdersRepository) : BaseViewModel<OrderData>() {

    private val pendingOrder: Order?
        get() = getViewState().poll()?.order

    private var HashOfLoadedOrder: String = ""
    private var savedBookedIntervals: MutableList<Interval>? = null

    fun checkIfOrderChanged(order: Order): Boolean {
        return order.changed(HashOfLoadedOrder)
    }

    fun saveToViewState(order: Order) {
        setData(OrderData(order = order))
    }

    fun loadOrder(orderId: String, userId: String) {
        launch {
            try {
                ordersRepository.getOrderById(orderId, userId).let {
                    HashOfLoadedOrder = it.stringToCompare()
                    savedBookedIntervals = it.bookedIntervals
                    setData(OrderData(order = it))
                }
            } catch (e: Throwable) {
                setError(e)
            }
        }
    }

    fun deleteOrder() {
        pendingOrder?.let { order ->
            launch {
                try {
                    order.color = Order.Color.PINK
                    order.bookedIntervals = savedBookedIntervals
                    ordersRepository.deleteOrder(order)
                    setData(OrderData(isDeleted = true))
                } catch (e: Throwable) {
                    setError(e)
                }
            }
        }
    }

    fun cancelOrder() {
        pendingOrder?.let { order ->
            setData(OrderData(doNotSave = true))
        }
    }

    @VisibleForTesting
    public override fun onCleared() {
        launch {
            pendingOrder?.let {
                try {
                    if (!App.administative) {
                        App.currentSavedDatabaseUser = it.user
                    }

                    it.bookedIntervals = savedBookedIntervals
                    if (HashOfLoadedOrder != "" && !App.administative) it.color = Order.Color.YELLOW
                    ordersRepository.saveOrder(it)

                    if (!it.user.administrative && !App.administative) {
                        ordersRepository.saveUsereDetails(it.user)
                    }

                } catch (e: Throwable) {
                    setError(e)
                }
            }
            super.onCleared()
        }
    }

    fun setInitialIntervals(choosedDate: Date) = ordersRepository.setInitialIntervals(choosedDate)

    suspend fun checkIfBookingIsPossible(): Pair<Boolean, String> {
        val order = pendingOrder ?: return Pair(true, App.applicationContext().getString(R.string.check_booking_problem1))
        if (order.date == null) return Pair(true, App.applicationContext().getString(R.string.check_booking_problem2))
        if (order.interval == null) return Pair(true, App.applicationContext().getString(R.string.check_booking_problem3))
        if (order.lengthInMin == 0) return Pair(false, App.applicationContext().getString(R.string.check_booking_problem4))

        App.orderIdForIntervalCount = order.id //TODO переделать

        var intervals: MutableList<Interval> = ArrayList()
        var result = Pair(true, App.applicationContext().getString(R.string.check_booking_ok))
        try {
            val endDate = Date(order.interval.beginDate.time + order.lengthInMin * 60 * 1000)
            intervals = ordersRepository.getIntervalsByBeginDateAndEndDate(
                order.date,
                order.interval.beginDate,
                endDate
            )

        } catch (e: Throwable) {
            result = Pair(false, App.applicationContext().getString(R.string.check_booking_problem5) + e.message)
        }
        if (!result.first) return result

        val intervalsForBooking = ordersRepository.GetIntervalsForBookingService(order)
        intervalsForBooking.forEach { intervalForBooking ->

            if  (intervalForBooking.booking!!.serviceName == RENT_TABLE) {
                //continue
            } else {
                try {
                    val foundInterval =
                        intervals.first { it.beginDate == intervalForBooking.beginDate && it.endDate == intervalForBooking.endDate }
                    val free =
                        foundInterval.servicesFree[intervalForBooking.booking!!.serviceName] ?: 0
                    if (free < intervalForBooking.booking!!.personNumber)
                        return Pair(
                            false,
                            App.applicationContext().getString(R.string.check_booking_not_enouph_part1) + free
                                    + App.applicationContext().getString(R.string.check_booking_not_enouph_part2) + intervalForBooking.booking!!.serviceName
                                    + App.applicationContext().getString(R.string.check_booking_not_enouph_part3) + intervalForBooking.booking!!.personNumber
                        )

                } catch (e: NoSuchElementException) {
                    return Pair(
                        false,
                        App.applicationContext().getString(R.string.check_booking_not_possible_part1) + intervalForBooking.toString()
                                + App.applicationContext().getString(R.string.check_booking_not_possible_part2)
                    )
                }

            }

        }
        return result
    }
}