package ru.sport_center.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ServiceItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val defaultLengthInMin: Int = 0,
    val capacity: Int = 1,
    val shortName: String = name,
    val veryShortName: String = name
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServiceItem

        if (name != other.name) return false
        return true
    }

}
