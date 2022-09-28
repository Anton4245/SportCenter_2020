package ru.sport_center

import android.app.Application
import android.content.Context
import org.koin.android.ext.android.startKoin
import ru.sport_center.common.JUMP
import ru.sport_center.data.entity.User
import ru.sport_center.di.*
import java.util.*

class App : Application(){

    init {
        instance = this
    }

    companion object {
        var currentDefaultDatabaseUser: User? = null
        var currentSavedDatabaseUser: User? = null
        fun giveMeCurrentDatabaseUserSavedOrDefault(): User? = currentSavedDatabaseUser ?: currentDefaultDatabaseUser

        var forFinish: Boolean = false
        var dateForIntervalGet: Date = Date(0)
        var orderIdForIntervalCount: String = ""
        var serviceNameForIntervalsDigits:String? = null

        private var instance: App? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
        val administative
        get() = currentSavedDatabaseUser?.administrative ?: false


    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule, splashModule, mainModule, orderModule, inervalsChooseModule))
    }
}