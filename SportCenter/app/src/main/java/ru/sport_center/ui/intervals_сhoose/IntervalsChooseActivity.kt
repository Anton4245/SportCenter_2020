package ru.sport_center.ui.intervals_сhoose

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.intervals_choose.*
import org.koin.android.ext.android.setProperty
import org.koin.android.viewmodel.ext.android.getViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import ru.sport_center.R
import ru.sport_center.data.entity.Interval
import ru.sport_center.ui.base.BaseActivity
import ru.sport_center.ui.order.OrderActivity
import ru.sport_center.ui.order.REQUEST_CODE
import java.util.*

class IntervalsChooseActivity : BaseActivity<List<Interval>?>() {

    companion object {
        fun start(context: Context) = Intent(context, IntervalsChooseActivity::class.java).apply {
            context.startActivity(this)
        }
    }

    override val model: IntervalsChooseModel by viewModel()
    override val layoutRes = R.layout.activity_main
    lateinit var adapter: IntervalsRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.intervals_choose)
        setSupportActionBar(toolbar)

        //model = ViewModelProvider(this).get(IntervalsChooseModel::class.java)
        rv_intervals.layoutManager = GridLayoutManager(this, 2)
        adapter = IntervalsRVAdapter { interval ->
            val intent = Intent(this, IntervalsChooseActivity::class.java)
            intent.putExtra("Interval", interval)
            setResult(Activity.RESULT_OK, intent)
            model.clean()
            finish()
        }

        rv_intervals.adapter = adapter
    }

    override fun renderData(data: List<Interval>?) {
        data?.let {
            adapter.intervals = it
        }
    }
}
