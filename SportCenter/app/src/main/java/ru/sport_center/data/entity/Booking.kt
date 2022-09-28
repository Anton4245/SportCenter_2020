package ru.sport_center.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*


@Parcelize
data class Booking(
    val id: String = "",
    val personNumber: Int = 0,
    val serviceName: String = ""
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as Booking

        if(id != other.id) return false
        return true
    }
}