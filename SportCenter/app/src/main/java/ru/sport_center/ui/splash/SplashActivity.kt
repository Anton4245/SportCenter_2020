package ru.sport_center.ui.splash

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import org.koin.android.viewmodel.ext.android.viewModel
import ru.sport_center.App
import ru.sport_center.ui.base.BaseActivity
import ru.sport_center.ui.main.MainActivity
import kotlin.system.exitProcess

class SplashActivity : BaseActivity<Boolean?>() {

    override val model: SplashViewModel by viewModel()

    override val layoutRes: Int? = null

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ model.requestUser() }, 1000)
    }

    override fun renderData(data: Boolean?) {
        data?.takeIf { it }?.let {
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        MainActivity.start(this)
        finish()
    }
}
