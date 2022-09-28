package ru.sport_center.ui.intervals_сhoose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_interval.view.*
import ru.sport_center.App
import ru.sport_center.R
import ru.sport_center.common.*
import ru.sport_center.data.entity.Interval
import java.lang.StringBuilder
import java.text.DecimalFormat
import kotlin.math.max

class IntervalsRVAdapter(val onItemViewClick : ((interval: Interval) -> Unit)? = null) : RecyclerView.Adapter<IntervalsRVAdapter.ViewHolder>() {

    var intervals: List<Interval> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_interval, parent, false)
        )

    override fun getItemCount() = intervals.size

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) = vh.bind(intervals[pos])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(interval: Interval) = with(itemView) {
            itv_title.text = interval.toString()

            var color = Interval.Color.WHITE
            App.serviceNameForIntervalsDigits?.let{serviceName ->

                var freeServices = getFreeCapacityText(interval, serviceName)
                if (freeServices < 0) {
                    when {
                        serviceName == RENT_ZAL -> freeServices = 0
                        App.administative -> color = Interval.Color.RED
                        else -> freeServices = 0
                    }

                }   else if (freeServices == 0) color = Interval.Color.PINK
                    else if (freeServices < (SERVICES[serviceName]?.capacity ?: 0)) color = Interval.Color.GREEN

                itv_text.text = StringBuilder().append(String.format("%-3d", freeServices))
                    .append(App.applicationContext().getString(R.string.free_of_services))

            } ?: run{

                val result = StringBuilder()
                var sum = 0
                interval.servicesFree.forEach {element ->
                    SERVICES[element.key]?.let {serviceItem ->

                        var curFreeServices = getFreeCapacityText(interval, serviceItem.name)
                        if (curFreeServices <= 0 && element.key == RENT_ZAL)  {
                            curFreeServices = 0
                        } else if (curFreeServices == 0 ) color = Interval.Color.YELLOW
                            else if (curFreeServices < serviceItem.capacity ) color = Interval.Color.GREEN

                        if (curFreeServices < 0) color = Interval.Color.RED //пометим

                        result.append(String.format("%-4d",(curFreeServices)))
                            .append(" - ")
                            .append(serviceItem.veryShortName)
                            .append(",")
                            .appendln()

                        if (curFreeServices > 0) sum += curFreeServices
                    }
                }
                result.deleteCharAt(result.lastIndex-1).append(App.applicationContext().getString(R.string.free_of_services))
                itv_text.text = result
                if (sum == 0) color = Interval.Color.PINK
            }
            if (color != Interval.Color.WHITE) (this as CardView).setCardBackgroundColor(color.getColorInt(context))

            itemView.setOnClickListener {
                onItemViewClick?.invoke(interval)
            }
        }

        private fun getFreeCapacityText(interval: Interval, serviceName: String): Int {
            return (interval.servicesFree[serviceName]
                ?: (SERVICES[serviceName]?.let { it.capacity }
                    ?: 0))
        }
    }
}