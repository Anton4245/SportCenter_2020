package ru.sport_center.ui.intervals_сhoose

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.sport_center.App
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.model.OrderResult
import ru.sport_center.ui.base.BaseViewModel
import kotlin.collections.ArrayList

class IntervalsChooseModel(ordersRepository: OrdersRepository) : BaseViewModel<List<Interval>?>() {

    private val intervalsChannel = ordersRepository.getIntervals(App.dateForIntervalGet)

    init {
        launch {
            intervalsChannel.consumeEach {
                when(it){
                    is OrderResult.Success<*> -> setData(it.data as? List<Interval>)
                    is OrderResult.Error -> setError(it.error)
                }
            }
        }
    }

    @VisibleForTesting
    public override fun onCleared() {
        intervalsChannel.cancel()
        super.onCleared()
    }

    fun clean() {
        setData(ArrayList())
    }
}