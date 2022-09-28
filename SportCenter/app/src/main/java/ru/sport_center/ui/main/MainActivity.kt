package ru.sport_center.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.koin.android.viewmodel.ext.android.viewModel
import ru.sport_center.App
import ru.sport_center.R
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order
import ru.sport_center.ui.base.BaseActivity
import ru.sport_center.ui.intervals_сhoose.IntervalsChooseActivity
import ru.sport_center.ui.order.OrderActivity
import ru.sport_center.ui.splash.SplashActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : BaseActivity<List<Order>?>() {

    val REQUEST_CODE = 4567

    companion object {
        fun start(context: Context) = Intent(context, MainActivity::class.java).apply {
            context.startActivity(this)
        }

        private const val DATE_TIME_FORMAT_LONG = "dd MMM yyyy"
        val myFormatterLong: SimpleDateFormat =
            SimpleDateFormat(DATE_TIME_FORMAT_LONG, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT")
            }

    }

    override val model: MainViewModel by viewModel()
    override val layoutRes = R.layout.activity_main
    lateinit var adapter: OrdersRVAdapter
    private lateinit var picker: MaterialDatePicker<Long>

    // INIT FORM <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        rv_orders.layoutManager = GridLayoutManager(this, 2)
        adapter = OrdersRVAdapter { order ->
            OrderActivity.start(this, order.id, order.user.id)
        }

        rv_orders.adapter = adapter
        fab.setOnClickListener {
            OrderActivity.start(this)
        }

        if (App.administative) {
            et_calendar_button.setOnClickListener { InitDatepicker()}
            InitDateClear()
            initIntervalChoose()
            initIntervalClear()
        } else {
            et_filters.visibility = View.GONE
        }
    }

    private fun InitDatepicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val titleText = builder.setTitleText(getString(R.string.picker_date_title))
        picker = builder.build()
        picker.show(supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            model.choosedDate = Date(it)
            model.choosedInterval = null
            UpdateFormAndFilter()
        }
    }

    private fun InitDateClear() {
        et_calendar_button_clear.setOnClickListener {
            model.choosedDate = null
            model.choosedInterval = null
            UpdateFormAndFilter()
        }
    }

    private fun UpdateFormAndFilter() {
        et_calendar_button.text = model.choosedDate?.let {date ->
            myFormatterLong.format(date)
        } ?: getString(R.string.calendar_button_text)
        et_calendar_button_clear.visibility = model.choosedDate?.let { View.VISIBLE } ?: View.GONE
        et_interval_button.text = model.choosedInterval?.let {interval ->
            interval.toString()
        } ?: getString(R.string.interval_button_text)
        et_interval_button_clear.visibility = model.choosedInterval?.let { View.VISIBLE } ?: View.GONE
        model.updateFilterForChannel()
    }

    private fun initIntervalChoose() {
        et_interval_button.setOnClickListener {
            model.choosedDate?.let { choosedDate ->

                App.dateForIntervalGet = choosedDate
                App.serviceNameForIntervalsDigits = null
                App.orderIdForIntervalCount = ""
                val intent = Intent(this, IntervalsChooseActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
                //IntervalsChooseActivity.start(this)

            } ?: showMessage(getString(R.string.interval_choose_attention))
        }
    }

    private fun initIntervalClear() {
        et_interval_button_clear.setOnClickListener {
            model.choosedDate?.let { choosedDate ->
                model.choosedInterval = null
                UpdateFormAndFilter()
            } ?: showMessage(getString(R.string.interval_choose_attention))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val interval = data.getParcelableExtra<Interval>("Interval")
                showMessage("Получили $interval")
                interval?.let {
                    model.choosedInterval = it
                    UpdateFormAndFilter()
                }
            }
        }
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> INIT FORM

    // OTHER FUNS <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    override fun onCreateOptionsMenu(menu: Menu?) =
        MenuInflater(this).inflate(R.menu.main, menu).let {
            menu?.getItem(2)?.isVisible = App.administative
            true }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.logout -> showLogoutDialog()?.let { true }
        R.id.exit -> showExitDialog()?.let { true }
        R.id.init_intervals_main -> setInitialIntervals().let { true }
        else -> false
    }



    private fun setInitialIntervals() {
        model.choosedDate?.let {model.setInitialIntervals(it)} ?: showMessage(getString(ru.sport_center.R.string.interval_choose_attention))

    }

    private fun showLogoutDialog() {
        alert {
            titleResource = R.string.logout_dialog_title
            messageResource = R.string.logout_dialog_message
            positiveButton(R.string.logout_dialog_ok) { onLogout() }
            negativeButton(R.string.logout_dialog_cancel) { dialog -> dialog.dismiss() }
        }.show()
    }

    private fun onLogout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                App.currentDefaultDatabaseUser = null
                App.currentSavedDatabaseUser = null
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
    }

    private fun showExitDialog() {
        alert {
            titleResource = R.string.logout_dialog_title
            messageResource = R.string.logout_dialog_message
            positiveButton(R.string.logout_dialog_ok) { onExit() }
            negativeButton(R.string.logout_dialog_cancel) { dialog -> dialog.dismiss() }
        }.show()

    }

    private fun onExit() {
        App.forFinish = true
        finishAndRemoveTask()
    }

    override fun renderData(data: List<Order>?) {
        data?.let {
            adapter.orders = it
        }
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OTHER FUNS
}
