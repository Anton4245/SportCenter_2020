package ru.sport_center.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.viewmodel.ext.koin.viewModel

import org.koin.dsl.module.module
import ru.sport_center.App
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.provider.FireStoreProvider
import ru.sport_center.data.provider.RemoteDataProvider
import ru.sport_center.ui.intervals_сhoose.IntervalsChooseModel
import ru.sport_center.ui.main.MainViewModel
import ru.sport_center.ui.order.OrderViewModel
import ru.sport_center.ui.splash.SplashViewModel
import java.util.*

val appModule = module {
    single { FirebaseAuth.getInstance()}
    single { FirebaseFirestore.getInstance() }
    single { FireStoreProvider(get(), get()) } bind RemoteDataProvider::class
    single { OrdersRepository(get()) } bind OrdersRepository::class
}

val splashModule = module {
    viewModel { SplashViewModel(get()) }
}

val mainModule = module {
    viewModel { MainViewModel(get()) }
}

val orderModule = module {
    viewModel { OrderViewModel(get()) }
}

val inervalsChooseModule = module {
    viewModel { IntervalsChooseModel(get()) }
}
