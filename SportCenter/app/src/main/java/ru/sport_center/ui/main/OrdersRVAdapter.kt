package ru.sport_center.ui.main

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.item_order.view.*
import ru.sport_center.R
import ru.sport_center.common.INDIVIDUAL
import ru.sport_center.common.getColorInt
import ru.sport_center.data.entity.Order
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class OrdersRVAdapter(val onItemViewClick : ((order: Order) -> Unit)? = null) : RecyclerView.Adapter<OrdersRVAdapter.ViewHolder>() {

    companion object {
        private const val DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm"
        val myFormatter: SimpleDateFormat =
            SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT")}

        private const val DATE_TIME_FORMAT_SHORT = "dd.MM.yyyy"
        val myFormatterShort: SimpleDateFormat =
            SimpleDateFormat(DATE_TIME_FORMAT_SHORT, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("GMT")}
    }

    fun digitalFormat(value: Int): String {
        return DecimalFormat("#0").format(value)
    }


    var orders: List<Order> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        )

    override fun getItemCount() = orders.size

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) = vh.bind(orders[pos])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(order: Order) = with(itemView) {
            tv_date_and_time.text = order.interval?.let {myFormatter.format(it.beginDate)} ?: (order.date?.let { myFormatterShort.format(it)} ?: "")
            tv_service.text = order.serviceItem.shortName
            itv_title.text = "${order.personNumber} чел. ${order.age}"  //TODO количество
            itv_name.text = order.user.name

            (this as CardView).setCardBackgroundColor(order.color.getColorInt(context))
            itemView.setOnClickListener {
                onItemViewClick?.invoke(order)
            }
        }
    }
}