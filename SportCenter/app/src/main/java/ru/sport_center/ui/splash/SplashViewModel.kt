package ru.sport_center.ui.splash

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sport_center.App
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.errors.NoAuthException
import ru.sport_center.ui.base.BaseViewModel

class SplashViewModel(private val ordersRepository: OrdersRepository) : BaseViewModel<Boolean?>() {

    fun requestUser() {
        launch {
            ordersRepository.getCurrentLocalUser()?.let {
                App.currentDefaultDatabaseUser = it
//
//                delay(300);
                ordersRepository.LoadCurrentDatabesUser()?.let {
                    App.currentSavedDatabaseUser = it
                    setData(true)
                } ?: setData(true)

            } ?: setError(NoAuthException())

        }
    }
}