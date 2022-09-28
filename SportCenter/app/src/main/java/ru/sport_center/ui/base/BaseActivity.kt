package ru.sport_center.ui.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ru.sport_center.App
import ru.sport_center.R
import ru.sport_center.data.OrdersRepository
import ru.sport_center.data.errors.NoAuthException
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

abstract class BaseActivity<T> : AppCompatActivity(), CoroutineScope {

    companion object {
        const val RC_SIGN_IN = 4242
    }

    override val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Main + job
    }

    val job = Job()
    private lateinit var dataJob: Job
    private lateinit var errorJob: Job

    abstract val model: BaseViewModel<T>
    abstract val layoutRes: Int?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutRes?.let { setContentView(it) }
    }

    override fun onStart() {
        super.onStart()
        dataJob = launch {
            model.getViewState().consumeEach {
                renderData(it)
            }
        }

        errorJob = launch {
            model.getErrorChannel().consumeEach {
                renderError(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        dataJob.cancel()
        errorJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
        if (App.forFinish) exitProcess(0)
    }

    abstract fun renderData(data: T)

    private fun renderError(error: Throwable) {
        when (error) {
            is NoAuthException -> if (!App.forFinish) {
                //showMessage("Login to Firebase Auth: " + error)
                startLogin()
            }
            else -> error.message?.let { showError(it) }
        }
    }

    private fun startLogin() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
            //,AuthUI.IdpConfig.EmailBuilder().build()
        )

        showMessage("Begin authenticating")
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.ic_skysky6)
                .setTheme(R.style.LoginStyle)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode != Activity.RESULT_OK) {
            App.forFinish = true
            finishAndRemoveTask()
        }
    }

    protected fun showError(error: String) {
        System.out.println("ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR_ERROR " + error)
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    protected fun showMessage(message: String) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!_ " + message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}
