package ru.sport_center.ui.order

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.koin.android.viewmodel.ext.android.viewModel
import ru.sport_center.App
import ru.sport_center.R
import ru.sport_center.common.SERVICES
import ru.sport_center.common.SERVICES_NAMES
import ru.sport_center.common.getColorInt
import ru.sport_center.data.entity.Interval
import ru.sport_center.data.entity.Order
import ru.sport_center.data.entity.ServiceItem
import ru.sport_center.data.entity.User
import ru.sport_center.data.errors.NoAuthException
import ru.sport_center.ui.base.BaseActivity
import ru.sport_center.ui.intervals_сhoose.IntervalsChooseActivity
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 4141

class OrderActivity : BaseActivity<OrderData>() {

    //работаем со следующими сущностями:
    //order - объект заказа для данного активити, в него можем записывать данные формы в любой момент через saveFormFieldsToOrder()
    //ViewModel.pendingOrder - объект заказа при старте активити считывается из базы данных, меняем через saveOrderToViewState(), saveFormFieldsToOrder() запускать перед этим не нужно
    //при выходе проверяем заказ на изменения, сравниваем текущий Хэш с  model.HashOfLoadedOrder

    companion object {
        private val EXTRA_ORDER = OrderActivity::class.java.name + "extra.NOTE"
        private val EXTRA_USER = OrderActivity::class.java.name + "extra.USER"

        private const val DATE_TIME_FORMAT = "dd.MM.yy HH:mm"
        val myFormatter: SimpleDateFormat =
            SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())

        private const val DATE_TIME_FORMAT_LONG = "dd MMM yyyy"
        val myFormatterLong: SimpleDateFormat =
            SimpleDateFormat(DATE_TIME_FORMAT_LONG, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT")
            }

        fun start(context: Context, orderId: String? = null, userId: String? = null) {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra(EXTRA_ORDER, orderId)
            intent.putExtra(EXTRA_USER, userId)
            context.startActivity(intent)
        }
    }

    override val layoutRes = R.layout.activity_order
    override val model: OrderViewModel by viewModel()

    private var order: Order? = null
    var color = Order.Color.WHITE
    private lateinit var picker: MaterialDatePicker<Long>
    var spinnerActivated: Boolean = false


    // INIT FORM <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initServicesSpinner()
        et_calendar_button.setOnClickListener { InitDatepicker() }
        initIntervalChoose()

        val orderId = intent.getStringExtra(EXTRA_ORDER)
        val userId = intent.getStringExtra(EXTRA_USER)
        if (orderId != null && userId != null)
            model.loadOrder(orderId, userId)
        else {
            supportActionBar?.title = getString(R.string.new_order_title)
            initView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveOrderToViewState()
    }

    override fun renderData(data: OrderData) {
        if (data.isDeleted) finish()
        else if (data.doNotSave) finish()
        else {
            this.order = data.order
            initView()
        }
    }

    fun initView() {
        colorPicker.onColorClickListener = {
            toolbar.setBackgroundColor(color.getColorInt(this))
            color = it
            saveOrderToViewState()

        }

        spinnerActivated = true

        order?.let { order ->

            // при изменении существующего заказа
            if (App.administative) order.color = Order.Color.WHITE
            fillFields(order)
            toolbar.setBackgroundColor(order.color.getColorInt(this))

            supportActionBar?.title =
                myFormatter.format(order.lastChanged)
        } ?: let {

            // при создании нового заказа
            if (App.administative) color = Order.Color.WHITE else color = Order.Color.GREEN
            toolbar.setBackgroundColor(color.getColorInt(this))

            supportActionBar?.title = getString(R.string.new_order_title)
            App.giveMeCurrentDatabaseUserSavedOrDefault()
                ?.let { user -> if (!user.administrative) fillUserFields(user) else cleanUserFields() } //остаются в форме в некоторых случаях
                ?: showMessage(getString(R.string.no_registration1))
        }
    }

    private fun initServicesSpinner() {
        val spinner = findViewById<Spinner>(R.id.et_spinner)
        val spinnerArrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            SERVICES_NAMES
        )
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerArrayAdapter

        et_spinner.onItemSelectedListener = onItemSelectedListener
    }

    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (!spinnerActivated) return
            val serviceItem = (SERVICES.get(SERVICES_NAMES[position]))
            if (order?.serviceItem?.equals(serviceItem) ?: false) return

            saveFormFieldsToOrder() //это всегда создает order
            order = order!!.copy(
                serviceItem = (serviceItem ?: ServiceItem()),
                date = null,
                interval = null,
                lengthInMin = 0
            )
            model.saveToViewState(order!!) //перезапустим initView() через channel и render(it)
            //fillFields(order!!)
        }
    }

    private fun InitDatepicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        val titleText = builder.setTitleText(getString(R.string.picker_date_title))
        picker = builder.build()
        picker.show(supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            saveFormFieldsToOrder()
            order = order!!.copy(date = Date(it), interval = null, lengthInMin = 0)
            model.saveToViewState(order!!) //перезапустим initView() через channel и render(it)
            //fillFields(order!!)

        }
    }

    private fun initIntervalChoose() {
        et_interval_button.setOnClickListener {
            order?.date?.let { choosedDate ->

                App.dateForIntervalGet = choosedDate
                App.serviceNameForIntervalsDigits = order!!.serviceItem.name
                App.orderIdForIntervalCount = order!!.id
                val intent = Intent(this, IntervalsChooseActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
                //IntervalsChooseActivity.start(this)

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
                    saveFormFieldsToOrder()
                    order = order!!.copy(
                        interval = it,
                        lengthInMin = ((interval.endDate.time - interval.beginDate.time) / 1000 / 60).toInt()
                    )
                    model.saveToViewState(order!!) //перезапустим initView() через channel и render(it)
                    //fillFields(order!!)
                }
            }
        }
    }

    private fun fillFields(order: Order) {
        if (et_age.text.toString() != order.age) et_age.setText(order.age)
        if (et_body.text.toString() != order.text) et_body.setText(order.text)
        et_call_for_info.isChecked = order.callForInfo

        spinnerActivated = false
        et_spinner.setSelection(
            when (val index = SERVICES_NAMES.indexOf(order.serviceItem.name)) {
                -1 -> 0
                else -> index
            }
        )
        spinnerActivated = true

        if (order.serviceItem.capacity == 1) {
            et_persons_number.setText(1.toString())
            et_persons_number_TIL.visibility = View.GONE
        } else {
            et_persons_number.setText(order.personNumber.toString())
            et_persons_number_TIL.visibility = View.VISIBLE
        }

        color = order.color

        et_calendar_button.text = order.date?.let {
            myFormatterLong.format(it)
        } ?: getString(R.string.calendar_button_text)
        et_interval_button.text = order.interval?.let {
            order.interval.toString()
        } ?: getString(R.string.interval_button_text)
        fillUserFields(order.user)
    }

    private fun fillUserFields(user: User) {
        et_name.setText(user.name)
        et_phone.setText(user.phone)
        et_email.setText(user.email)
    }

    private fun cleanUserFields() {
        et_name.setText("")
        et_phone.setText("")
        et_email.setText("")
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> INIT form

    // MENU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    override fun onCreateOptionsMenu(menu: Menu?) =
        menuInflater.inflate(R.menu.order, menu).let {
            menu?.getItem(3)?.isVisible = App.administative
            true }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> onBackPressed().let { true }
        R.id.palette -> togglePalette().let { true }
        R.id.delete -> deleteOrder().let { true }
        R.id.save -> saveOrderToDatabase().let { true }
        R.id.init_intervals -> setInitialIntervals().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun setInitialIntervals() {

        order?.date?.let { choosedDate ->
            model.setInitialIntervals(choosedDate)
        } ?: showMessage(getString(R.string.interval_choose_attention))
    }

    override fun onBackPressed() {
        if (order != null) {

            saveFormFieldsToOrder()

            if (model.checkIfOrderChanged(order!!))
                showExitDialog()?.let { true }
            else
                model.cancelOrder()
        } else
        //если заказ еще не сохранен, то точно - в диалог
            showExitDialog()?.let { true }
    }

    private fun showExitDialog() {
        alert {
            titleResource = R.string.order_exit_title
            messageResource = R.string.order_exit_message
            positiveButton(R.string.logout_dialog_ok) { saveOrderToDatabase() }
            negativeButton(R.string.logout_dialog_cancel) { cancelAndExit() }
        }.show()
    }

    private fun cancelAndExit() {
        order?.let {
            model.cancelOrder()
        } ?: finish()
    }

    private fun saveOrderToDatabase() {
        saveOrderToViewState()
        launch (Dispatchers.Main) {
            delay(100)
            val result = model.checkIfBookingIsPossible()
                if (result.first) {
                    if (result.second != "")
                        showMessage(result.second)
                    finish()
                } else
                    showMessage(result.second)
        }
    }

    private fun togglePalette() {
        if (colorPicker.isOpen) {
            colorPicker.close()
        } else {
            colorPicker.open()
        }
    }

    private fun deleteOrder() {
        alert {
            messageResource = R.string.order_delete_message
            negativeButton(R.string.order_delete_cancel) { dialog -> dialog.dismiss() }
            positiveButton(R.string.order_delete_ok) { model.deleteOrder() }
        }.show()
    }
    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> MENU

    // SAVE FORM to ViewState <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    fun saveOrderToViewState() {
        saveFormFieldsToOrder()
        order?.let {
            model.saveToViewState(it)
        }
    }

    private fun saveFormFieldsToOrder() {

        lateinit var user: User
        order?.let {
            user = it.user
        } ?: kotlin.run {
            App.giveMeCurrentDatabaseUserSavedOrDefault()?.let {
                if (it.administrative) user = it.copy(name = "", email = "", phone = "")
                else user = it.copy()
            }
                ?: run {
                    showMessage("Нет регистрации в сервисе. Документ не запишется")
                    throw NoAuthException()
                }
            order = Order(UUID.randomUUID().toString())
        }

        if (et_name.text.toString() != "" || et_email.text.toString() != "" || et_phone.text.toString() != "")
            user = user.copy(
                name = et_name.text.toString(),
                email = et_email.text.toString(),
                phone = et_phone.text.toString()
            )

        val serviceItem = saveSpinnerData()

        order = order?.copy(
            age = et_age.text.toString(),
            text = et_body.text.toString(),
            lastChanged = Date(),
            color = color,
            user = user,
            serviceItem = serviceItem,
            lengthInMin = SERVICES[et_spinner.selectedItem.toString()]!!.defaultLengthInMin,
            callForInfo = et_call_for_info.isChecked,
            personNumber = try {
                if (serviceItem.capacity == 1) 1 else
                    et_persons_number.text.toString().toInt()
            } catch (e: Exception) {
                1
            }
        )
    }

    private fun saveSpinnerData(): ServiceItem {
        val serviceItem = when (val serviceItem = SERVICES[et_spinner.selectedItem.toString()]) {
            null -> ServiceItem()
            else -> serviceItem.copy()
        }
        return serviceItem
    }
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< SAVE FORM to ViewState
}
