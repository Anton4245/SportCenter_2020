package ru.sport_center.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val INTERVAL_DISPLAY_TIME_FORMAT = "HH:mm"

@Parcelize
data class Interval(
    val id: String = "",
    val beginDate: Date = Date(0),
    val endDate: Date = Date (0),
    val color: Interval.Color = Interval.Color.WHITE,
    var bookingList: ArrayList<Booking> = ArrayList(),
    val booking: Booking? = null,
    var servicesFree: HashMap<String, Int> = HashMap(),
    val uid: String = "",
    val indexBeginDate: Long? = null
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Interval

        if(id != other.id) return false
        return true
    }

    override fun toString(): String {
        val myFormatter = SimpleDateFormat(INTERVAL_DISPLAY_TIME_FORMAT, Locale.getDefault())
        myFormatter.timeZone = TimeZone.getTimeZone("GMT")
        myFormatter.format(beginDate)
        return "${myFormatter.format(beginDate)} - ${myFormatter.format(endDate)}"
    }

    enum class Color {
        WHITE,
        YELLOW,
        GREEN,
        BLUE,
        RED,
        VIOLET,
        PINK
    }

}