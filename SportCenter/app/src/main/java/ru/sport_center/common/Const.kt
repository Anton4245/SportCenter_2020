package ru.sport_center.common

import ru.sport_center.data.entity.ServiceItem
import java.util.ArrayList


const val HOUR_BEGIN = 11
const val HOUR_END = 22
const val APPOINTMENT_INTERVAL_IN_MINUTES = 60

const val RENT_TABLE_LENGTH = 60


const val JUMP = "Прыжки на батуте"
const val INDIVIDUAL = "Индивидуальная тренировка"
const val RENT_ZAL = "Аренда зала"
const val RENT_TABLE = "Аренда стола"

val SERVICES = hashMapOf<String, ServiceItem>(
    JUMP to ServiceItem(
        "",
        JUMP,
        "Тренировка в общем зале без тренера, 1 час",
        60, 12, "Прыжки", "батуты"
    ),
    INDIVIDUAL to ServiceItem(
        "",
        INDIVIDUAL,
        "Тренировка в общем зале с тренером, 1 час",
        60,1, "Тренировка", "тренера"
    ),
    RENT_ZAL to ServiceItem(
        "",
        RENT_ZAL,
        "Аренда всего зала, 1.5 часа в зале (зал бронируется на 2 часа) + 1 час за столом",
        180,1, "Аренда", "залы"
    )
)

val SERVICES_NAMES: ArrayList<String> = SERVICES.flatMap {
    listOf(it.key)
} as ArrayList<String>

enum class ServiceEnum{
    JUMP,
    INDIVIDUAL,
    RATE_ZAL
}


