package ru.sport_center.data.model

sealed class OrderResult {
    data class Success<out T>(val data: T): OrderResult()
    data class Error(val error: Throwable) : OrderResult()
}