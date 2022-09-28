package ru.sport_center.ui.base

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ru.sport_center.data.OrdersRepository
import kotlin.coroutines.CoroutineContext

open class BaseViewModel<S>() : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + Job()
    }


    private val errorChannel = Channel<Throwable>()
    private val viewStateChannel = BroadcastChannel<S>(Channel.CONFLATED)

    fun getViewState(): ReceiveChannel<S> = viewStateChannel.openSubscription()
    fun getErrorChannel(): ReceiveChannel<Throwable> = errorChannel

    protected fun setError(e: Throwable){
        launch {
            errorChannel.send(e)
        }
    }

    protected fun setData(data: S) {
        launch {
            viewStateChannel.send(data)
        }
    }

    override fun onCleared() {
        viewStateChannel.close()
        errorChannel.close()
        coroutineContext.cancel()
        super.onCleared()
    }
}


