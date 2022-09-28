package ru.sport_center.data.provider

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.ReceiveChannel
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order
import ru.sport_center.data.entity.User
import ru.sport_center.data.model.OrderResult
import java.util.*

interface RemoteDataProvider {
    fun subscribeToAllOrders(): ReceiveChannel<OrderResult>
    fun subscribeToAllOrders(choosedDate: Date?, choosedInterval: Interval?): ReceiveChannel<OrderResult>
    fun subscribeToAllIntervals(choosedDate: Date): ReceiveChannel<OrderResult>
    suspend fun getOrderById(id: String, userId: String): Order
    suspend fun saveOrder(order: Order, intervalsForBooking: MutableList<Interval>): Order
    suspend fun saveUserDetails(user: User): User
    suspend fun getCurrentLocalUser(): User?
    suspend fun deleteOrder(order: Order)
    fun retrieveCurrentUser() : FirebaseUser?
    suspend fun LoadCurrentDatabesUser(): User?
    fun setInitialIntervals(choosedDate: Date)
    suspend fun getIntervalsByBeginDateAndEndDate(date: Date, beginDate: Date, endDate: Date): MutableList<Interval>
}