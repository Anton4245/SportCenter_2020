package ru.sport_center.data.provider

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import ru.sport_center.App
import ru.sport_center.common.*
import ru.sport_center.data.entity.*
import ru.sport_center.data.errors.NoAuthException
import ru.sport_center.data.model.OrderResult
import java.security.InvalidAlgorithmParameterException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max
import kotlin.math.min

const val DATE_FORMAT_FOR_FIREBASE = "yyyyMMdd"


class FireStoreProvider(
    private val firebaseAuth: FirebaseAuth,
    private val store: FirebaseFirestore
) : RemoteDataProvider {

    companion object {
        private const val ORDER_COLLECTION_NAME = "orders"
        private const val ORDER_COLLECTION_NAME_DELETED = "deletedOrders"
        private const val USER_COLLECTION_NAME = "usersOrders"
        private const val USER_COLLECTION_NAME_DELETED = "deletedUsersOrders"
        private const val USER_DETAILS_COLLECTION_NAME = "userDetails"
        private const val DATE_COLLECTION_NAME = "dates"
        private const val INTERVALS_COLLECTION_NAME = "intervals"
        private const val BOOKING_COLLECTION_NAME = "booking"
        private const val INDEX_BEGIN_DATE = "indexBeginDate"
        private const val INDEX_DATE = "indexDate"

        private val myFormatter =
            SimpleDateFormat(DATE_FORMAT_FOR_FIREBASE, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT")
            }

    }

    private val currentUser
        get() = firebaseAuth.currentUser

    override fun retrieveCurrentUser(): FirebaseUser? = firebaseAuth.currentUser


    private fun AdministrativeUserOrdersCollection(
        choosedDate: Date?,
        choosedInterval: Interval?
    ): Query {
        var ref = store.collectionGroup(ORDER_COLLECTION_NAME)
        if (choosedInterval != null) {
//            ref = ref.whereGreaterThanOrEqualTo("beginDate", choosedInterval.beginDate).whereLessThanOrEqualTo("beginDate",choosedInterval.endDate)
            ref = ref.whereEqualTo(INDEX_BEGIN_DATE, choosedInterval.beginDate.time)
        } else if (choosedDate != null) {
            ref = ref.whereEqualTo(INDEX_DATE, choosedDate.time)
        }

        return ref
        //// //.whereEqualTo("type", "museum")
    }

    private fun userOrdersCollection(user: Any?): CollectionReference {
        return user?.let {
            store.collection(USER_COLLECTION_NAME).document(takeUserIdfromUser(user))
                .collection(ORDER_COLLECTION_NAME)
        } ?: throw NoAuthException()
    }

    private fun userOrdersCollection(userId: String): CollectionReference {
        return store.collection(USER_COLLECTION_NAME).document(userId)
            .collection(ORDER_COLLECTION_NAME)

    }

    private fun deletedUserOrdersCollection(user: Any?): CollectionReference {
        return user?.let {
            store.collection(USER_COLLECTION_NAME_DELETED).document(takeUserIdfromUser(user))
                .collection(ORDER_COLLECTION_NAME_DELETED)
        } ?: throw NoAuthException()
    }


    private fun intervalsCollection(date: Date): CollectionReference {
        val curDateString =
            myFormatter.format(date)

        return store.collection(DATE_COLLECTION_NAME).document(curDateString)
            .collection(INTERVALS_COLLECTION_NAME)
    }

    private fun intervalsCollectionFiltered(date: Date, beginDate: Date, endDate: Date): Query {
        val curDateString =
            myFormatter.format(date)

        return store.collection(DATE_COLLECTION_NAME).document(curDateString)
            .collection(INTERVALS_COLLECTION_NAME)
//            .whereEqualTo(INDEX_BEGIN_DATE, beginDate.time)
            .whereGreaterThanOrEqualTo(INDEX_BEGIN_DATE, beginDate.time)
            .whereLessThan(INDEX_BEGIN_DATE, endDate.time)
    }

    private fun intervalsDocument(date: Date, interval: Interval): DocumentReference {
        val curDateString =
            myFormatter.format(date)

        return intervalsCollection(date).document(interval.toString())
    }


    private fun IntervalDocument(
        date: Date,
        interval: Interval,
        orderId: String
    ): DocumentReference {
        val curDateString = myFormatter.format(date)
        return store.collection(DATE_COLLECTION_NAME).document(curDateString)
            .collection(INTERVALS_COLLECTION_NAME).document("$interval-$orderId")
    }


    private fun userDetailsDocument(user: Any?): DocumentReference {
        return user?.let {
            store.collection(USER_DETAILS_COLLECTION_NAME).document(takeUserIdfromUser(user))
        } ?: throw NoAuthException()
    }

    private fun takeUserIdfromUser(user: Any?): String {
        return when (val intUsers = user) {
            is User -> intUsers.id
            is FirebaseUser -> intUsers.uid
            else -> throw InvalidAlgorithmParameterException()
        }
    }

    override suspend fun LoadCurrentDatabesUser(): User? = suspendCoroutine { continuation ->
        currentUser?.let { fbUser ->
            try {
                userDetailsDocument(fbUser).get().addOnSuccessListener {
                    val user = it.toObject(User::class.java)
                    continuation.resume(user)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Throwable) {
                continuation.resume(null)
            }
        }
            ?: continuation.resume(null)
    }

    override suspend fun getCurrentLocalUser(): User? = suspendCoroutine { continuation ->
        val user = currentUser?.let { getDefaultUser(it) }
        continuation.resume(user)
    }

    private fun getDefaultUser(fbUser: FirebaseUser) =
        User(fbUser.uid, fbUser.displayName ?: "", fbUser.email ?: "")

    fun nonAdministrativeSubscribeToAllOrders(): ReceiveChannel<OrderResult> =
        Channel<OrderResult>(Channel.CONFLATED).apply {
            var registration: ListenerRegistration? = null

            try {
                registration =
                    userOrdersCollection(App.giveMeCurrentDatabaseUserSavedOrDefault()).addSnapshotListener { snapshot, e ->
                        val value = e?.let {
                            OrderResult.Error(it)
                        } ?: snapshot?.let { curSnapshot ->
                            val orders =
                                curSnapshot.documents.map { it.toObject(Order::class.java) }
                            OrderResult.Success(orders.sortedBy {
                                it?.date ?: (it?.interval?.beginDate ?: Date(0))
                            })
                        }

                        value?.let { offer(it) }
                    }

            } catch (e: Throwable) {
                offer(OrderResult.Error(e))
            }

            invokeOnClose {
                registration?.remove()
            }
        }

    override fun subscribeToAllOrders(): ReceiveChannel<OrderResult> {
        if (!App.administative) return nonAdministrativeSubscribeToAllOrders()
        else return AdministrativeSubscribeToAllOrders(null, null)
    }

    override fun subscribeToAllOrders(
        choosedDate: Date?,
        choosedInterval: Interval?
    ): ReceiveChannel<OrderResult> {
        if (!App.administative) return nonAdministrativeSubscribeToAllOrders()
        else return AdministrativeSubscribeToAllOrders(choosedDate, choosedInterval)
    }

    fun AdministrativeSubscribeToAllOrders(
        choosedDate: Date?,
        choosedInterval: Interval?
    ): ReceiveChannel<OrderResult> =
        Channel<OrderResult>(Channel.CONFLATED).apply {
            var registration: ListenerRegistration? = null

            try {
                registration = AdministrativeUserOrdersCollection(choosedDate, choosedInterval)
                    .addSnapshotListener { snapshot, e ->
                        val value = e?.let {
                            OrderResult.Error(it)
                        } ?: snapshot?.let { curSnapshot ->
                            val orders =
                                curSnapshot.documents.map { it.toObject(Order::class.java) }
                            OrderResult.Success(orders.sortedBy {
                                it?.date ?: (it?.interval?.beginDate ?: Date(0))
                            })
                        }

                        value?.let { offer(it) }
                    }

            } catch (e: Throwable) {
                offer(OrderResult.Error(e))
            }

            invokeOnClose {
                registration?.remove()
            }
        }

    override fun subscribeToAllIntervals(choosedDate: Date): ReceiveChannel<OrderResult> =

        Channel<OrderResult>(Channel.CONFLATED).apply {
            var registration: ListenerRegistration? = null
            try {
                registration =
                    intervalsCollection(choosedDate).addSnapshotListener { snapshot, e ->
                        val value = e?.let {
                            OrderResult.Error(it)
                        } ?: snapshot?.let { snapshot ->

                            //переводим в список
                            var intervals: List<Interval?> =
                                snapshot.documents.map {
                                    it.toObject(Interval::class.java)
                                }


                            var intervalsCompleted: MutableList<Interval> =
                                groupAndExpandEnquiry(intervals)
                            OrderResult.Success(intervalsCompleted)
                        }
                        value?.let { offer(it) }
                    }

            } catch (e: Throwable) {
                offer(OrderResult.Error(e))
            }

            invokeOnClose {
                registration?.remove()
            }
        }

    private fun groupAndExpandEnquiry(intervals: List<Interval?>): MutableList<Interval> {
        var intervals1 = intervals.filter { (it != null && it.beginDate != Date(0)) }.map { it!! }
            .toMutableList()

        val aMap = HashMap<String, Interval>()
        //проходим расписание
        intervals1.forEach { interval ->
            if (interval.booking == null) {

                //если не указано, сколько в расписании доступно услуг, устанавливаем по умолчанию
                SERVICES.forEach { service ->
                    if (interval.servicesFree.get(service.key) == null) interval.servicesFree.put( // была проверка  - в терии,
                        service.key,
                        SERVICES[service.value.name]?.capacity ?: 12
                    )
                }

                aMap.put(
                    interval.toString(),
                    interval.copy(booking = null, bookingList = ArrayList())
                )
            }
        }

        //проходим бронирование
        intervals1.forEach { interval ->
            if (interval.booking != null) {
                if (aMap.get(interval.toString()) == null) {

                    //если не найдено в расписании необходимых интервалов, то устанавливаем 0
                    SERVICES.forEach { service ->
                        interval.servicesFree.put(service.key, 0)
                    }

                    aMap.put(
                        interval.toString(),
                        interval.copy(booking = null, bookingList = ArrayList())
                    )
                }
                aMap.get(interval.toString())?.bookingList?.add(interval.booking)
            }
        }

        intervals1 = aMap.map { it.value }.toMutableList()
        intervals1.sortBy { it!!.beginDate }

        //для каждой услуги нужно подсчитать кол-во занятых
        intervals1.forEach { interval ->

            interval.bookingList.forEach { booking ->
                if (booking.id != App.orderIdForIntervalCount) {

                    when (booking.serviceName) {
                        JUMP -> {
                            interval.servicesFree[JUMP] =
                                (interval.servicesFree[JUMP] ?: 0) - booking.personNumber
                            interval.servicesFree[RENT_ZAL] =
                                (interval.servicesFree[RENT_ZAL] ?: 0) - booking.personNumber
                        }
                        INDIVIDUAL -> {
                            interval.servicesFree[INDIVIDUAL] =
                                (interval.servicesFree[INDIVIDUAL] ?: 0) - booking.personNumber
                            interval.servicesFree[RENT_ZAL] =
                                (interval.servicesFree[RENT_ZAL] ?: 0) - booking.personNumber
                            interval.servicesFree[JUMP] =
                                (interval.servicesFree[JUMP] ?: 0) - booking.personNumber
                        }
                        RENT_ZAL -> {
                            interval.servicesFree[RENT_ZAL] =
                                (interval.servicesFree[RENT_ZAL] ?: 0) - booking.personNumber
                            interval.servicesFree[JUMP] =
                                (interval.servicesFree[JUMP] ?: 0) - (SERVICES[JUMP]?.capacity
                                    ?: 12) * booking.personNumber
                            interval.servicesFree[INDIVIDUAL] =
                                (interval.servicesFree[INDIVIDUAL] ?: 0) - (SERVICES[INDIVIDUAL]?.capacity
                                    ?: 1) * booking.personNumber
                        }
                        RENT_TABLE -> {
                            interval.servicesFree[RENT_ZAL] =
                                (interval.servicesFree[RENT_ZAL]?: 0) - booking.personNumber
                        }
                        else -> {
                        }
                    }
                }
            }
            interval.servicesFree[INDIVIDUAL] = min(interval.servicesFree[INDIVIDUAL] ?: 0, interval.servicesFree[JUMP] ?: 0)
            interval.servicesFree[RENT_ZAL] = max(interval.servicesFree[RENT_ZAL] ?: 0, 0)
        }
        intervals1.sortBy { it.beginDate }
        return intervals1
    }

    override suspend fun getIntervalsByBeginDateAndEndDate(date: Date, beginDate: Date, endDate: Date ): MutableList<Interval> = suspendCoroutine { continuation ->
        var finished = false
        try {
            intervalsCollectionFiltered(date, beginDate, endDate).get()
                .addOnSuccessListener { documents ->
                    try {
                        //переводим в список
                        var intervals = documents.map {
                            it.toObject(Interval::class.java)
                        }

                        var intervalsCompleted: MutableList<Interval> =
                            groupAndExpandEnquiry(intervals)
                        finished = true
                        continuation.resume(intervalsCompleted)

                    } catch (e: Throwable) {
                        finished = true
                        continuation.resumeWithException(e)
                    }

                }.addOnFailureListener {
                    finished = true
                    continuation.resumeWithException(it)
                }.addOnCompleteListener {
                    if (!finished)
                    continuation.resume(ArrayList<Interval>().toMutableList())
                }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun getOrderById(id: String, userId: String): Order =
        suspendCoroutine { continuation ->
            try {
                userOrdersCollection(userId).document(id).get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val order = snapshot.toObject(Order::class.java)
                            order?.let {
                                continuation.resume(order)
                            } ?: run {
                                showError("Не найдена заявка с пользователем: $userId и id: $id")
                                continuation.resumeWithException(NullPointerException("Не найдена заявка,  пользователь: $userId, id: $id"))
                            }

                        } catch (e: Throwable) {
                            continuation.resumeWithException(e)
                        }

                    }.addOnFailureListener {
                        continuation.resumeWithException(it)
                    }
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }

    protected fun showError(error: String) {
        Toast.makeText(App.applicationContext(), error, Toast.LENGTH_LONG).show()
    }


    override suspend fun saveOrder(order: Order, intervalsForBooking: MutableList<Interval>): Order = suspendCoroutine { continuation ->
        order.indexBeginDate = order.interval?.beginDate?.time ?: 0
        order.indexEndDate = (order.interval?.beginDate?.time ?: 0)
            .apply { if (this == 0L) 0 else this + order.lengthInMin * 60 * 1000 }
        order.indexDate = order.date?.time ?: 0

        try {
            store.runBatch { batch ->

                deleteAccountingEntries(order, batch)

                order.bookedIntervals = intervalsForBooking
                order.bookedIntervals?.forEach { interval ->
                    batch.set(IntervalDocument(interval.beginDate, interval, order.id), interval)
                }
                userOrdersCollection(order.user).document(order.id).set(order)


            }.addOnSuccessListener {
                continuation.resume(order)
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun saveUserDetails(user: User): User = suspendCoroutine { continuation ->
        try {
            userDetailsDocument(user).set(user)
                .addOnSuccessListener {
                    continuation.resume(user)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    override suspend fun deleteOrder(order: Order): Unit = suspendCoroutine { continuation ->
        try {
            store.runBatch { batch ->
                batch.delete(userOrdersCollection(order.user).document(order.id))
                if (order.date != null && order.interval != null)
                    deleteAccountingEntries(order, batch)
                order.bookedIntervals = null
                deletedUserOrdersCollection(order.user).document(order.id).set(order)
            }
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        } catch (e: Throwable) {
            continuation.resumeWithException(e)
        }
    }

    private fun deleteAccountingEntries(
        order: Order,
        batch: WriteBatch
    ) {
        order.bookedIntervals?.forEach { interval ->
            batch.delete(IntervalDocument(interval.beginDate, interval, order.id))
        }
    }

    override fun setInitialIntervals(choosedDate: Date) {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = choosedDate.time
        cal.add(Calendar.HOUR, HOUR_BEGIN)

        for (addMinutes in HOUR_BEGIN * RENT_TABLE_LENGTH until HOUR_END * RENT_TABLE_LENGTH step APPOINTMENT_INTERVAL_IN_MINUTES) {
            val dateBegin = cal.time
            cal.add(Calendar.MINUTE, APPOINTMENT_INTERVAL_IN_MINUTES)

            val curInterval = Interval(
                "",
                dateBegin,
                cal.time,
                color = Interval.Color.WHITE,
                indexBeginDate = dateBegin.time
            )
            intervalsCollection(choosedDate).document(curInterval.toString()).set(curInterval)
        }
    }


}