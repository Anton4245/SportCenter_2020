package ru.sport_center.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
data class Order(
    val id: String = "",
    val age: String = "",
    val text: String = "",
    var color: Color = Color.WHITE,
    val lastChanged: Date = Date(),
    val date: Date? = null,
    val lengthInMin: Int = 0,
    val serviceItem: ServiceItem = ServiceItem(),
    val personNumber: Int = 1,
    val user: User = User(),
    val callForInfo: Boolean = false,
    val interval: Interval? = null,
    var bookedIntervals: MutableList<Interval>? = null,
    var indexDate: Long? = null,
    var indexBeginDate: Long? = null,
    var indexEndDate: Long? = null
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Order

        if(id != other.id) return false
        return true
    }

    enum class Color{
        WHITE,
        YELLOW,
        GREEN,
        BLUE,
        RED,
        VIOLET,
        PINK
    }

    fun changed(otherString: String) : Boolean{
        return (stringToCompare() != otherString)
    }

    fun stringToCompare(): String{
        return this.copy(lastChanged = Date(0), bookedIntervals = null).toString()
    }
}