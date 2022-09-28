package ru.sport_center.data

import ru.sport_center.common.APPOINTMENT_INTERVAL_IN_MINUTES
import ru.sport_center.common.RENT_TABLE
import ru.sport_center.common.RENT_ZAL
import ru.sport_center.common.RENT_TABLE_LENGTH
import ru.sport_center.data.entity.*
import ru.sport_center.data.provider.RemoteDataProvider
import java.util.*

class OrdersRepository(val remoteProvider: RemoteDataProvider) {
    fun getOrders() = remoteProvider.subscribeToAllOrders()
    fun getOrders(choosedDate: Date?, choosedInterval: Interval?) = remoteProvider.subscribeToAllOrders(choosedDate, choosedInterval)
    fun getIntervals(choosedDate: Date) = remoteProvider.subscribeToAllIntervals(choosedDate)
    suspend fun saveOrder(order: Order) = remoteProvider.saveOrder(order, GetIntervalsForBookingService(order))
    suspend fun saveUsereDetails(user: User) = remoteProvider.saveUserDetails(user)
    suspend fun getOrderById(id: String, userId: String) = remoteProvider.getOrderById(id, userId)
    suspend fun getCurrentLocalUser() = remoteProvider.getCurrentLocalUser()
    suspend fun deleteOrder(order: Order) = remoteProvider.deleteOrder(order)
    fun retrieveCurrentUser() = remoteProvider.retrieveCurrentUser()
    suspend fun LoadCurrentDatabesUser() = remoteProvider.LoadCurrentDatabesUser()
    fun setInitialIntervals(choosedDate: Date) = remoteProvider.setInitialIntervals(choosedDate)
    suspend fun getIntervalsByBeginDateAndEndDate(date: Date, beginDate: Date, endDate: Date): MutableList<Interval> = remoteProvider.getIntervalsByBeginDateAndEndDate(date, beginDate, endDate)

    fun GetIntervalsForBookingService(
        order: Order
    ): MutableList<Interval> {
        val intervals: MutableList<Interval> = ArrayList()
        if (order.date != null && order.interval != null && order.serviceItem.name != "") {

            when (order.serviceItem.name) {
                RENT_ZAL -> {
                    addsIntervalsFromLength(
                        intervals,
                        order,
                        order.serviceItem,
                        order.lengthInMin - RENT_TABLE_LENGTH,
                        0
                    )
                    addsIntervalsFromLength(
                        intervals, order, ServiceItem("", RENT_TABLE),
                        RENT_TABLE_LENGTH, order.lengthInMin - RENT_TABLE_LENGTH
                    )
                }
                else -> addsIntervalsFromLength(
                    intervals,
                    order,
                    order.serviceItem,
                    order.lengthInMin
                )
            }
        }
        return intervals
    }

    private fun addsIntervalsFromLength(
        intervals: MutableList<Interval>,
        order: Order,
        newService: ServiceItem,
        newLengthInMin: Int,
        addToStartMin: Int = 0
    ) {


        if (!(order.date != null && order.interval != null && order.serviceItem.name != "")) {
            return
        }

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.time = order.interval.beginDate
        cal.add(Calendar.MINUTE, addToStartMin)

        for (addMinutes in 0 until newLengthInMin step APPOINTMENT_INTERVAL_IN_MINUTES) {
            val dateBegin = cal.time
            cal.add(Calendar.MINUTE, APPOINTMENT_INTERVAL_IN_MINUTES)

            val newInterval = order.interval.copy(
                beginDate = dateBegin,
                endDate = cal.time,
                booking = Booking(
                    order.id,
                    personNumber = order.personNumber,
                    serviceName = newService.name
                ),
                uid = order.user.id, servicesFree = HashMap(),
                indexBeginDate = dateBegin.time
            )
            intervals.add(newInterval)
        }
    }
}