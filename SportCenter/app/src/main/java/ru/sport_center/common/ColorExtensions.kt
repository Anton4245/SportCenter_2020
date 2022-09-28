package ru.sport_center.common

import android.content.Context
import androidx.core.content.ContextCompat
import ru.sport_center.R
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order

fun Order.Color.getColorInt(context: Context): Int =
    ContextCompat.getColor(
        context, when (this) {
            Order.Color.WHITE -> R.color.white
            Order.Color.VIOLET -> R.color.violet
            Order.Color.YELLOW -> R.color.yellow
            Order.Color.RED -> R.color.red
            Order.Color.PINK -> R.color.pink
            Order.Color.GREEN -> R.color.green
            Order.Color.BLUE -> R.color.blue
        }
    )


fun Order.Color.getColorRes(): Int = when (this) {
    Order.Color.WHITE -> R.color.white
    Order.Color.VIOLET -> R.color.violet
    Order.Color.YELLOW -> R.color.yellow
    Order.Color.RED -> R.color.red
    Order.Color.PINK -> R.color.pink
    Order.Color.GREEN -> R.color.green
    Order.Color.BLUE -> R.color.blue
}

fun Interval.Color.getColorInt(context: Context): Int =
    ContextCompat.getColor(
        context, when (this) {
            Interval.Color.WHITE -> R.color.white
            Interval.Color.VIOLET -> R.color.violet
            Interval.Color.YELLOW -> R.color.yellow
            Interval.Color.RED -> R.color.red
            Interval.Color.PINK -> R.color.pink
            Interval.Color.GREEN -> R.color.green
            Interval.Color.BLUE -> R.color.blue
        }
    )


fun Interval.Color.getColorRes(): Int = when (this) {
    Interval.Color.WHITE -> R.color.white
    Interval.Color.VIOLET -> R.color.violet
    Interval.Color.YELLOW -> R.color.yellow
    Interval.Color.RED -> R.color.red
    Interval.Color.PINK -> R.color.pink
    Interval.Color.GREEN -> R.color.green
    Interval.Color.BLUE -> R.color.blue
}