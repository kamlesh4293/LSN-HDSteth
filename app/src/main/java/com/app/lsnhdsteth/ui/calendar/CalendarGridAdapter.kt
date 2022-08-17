package com.app.lsnhdsteth.ui.calendar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.app.lsnhdsteth.R

class CalendarGridAdapter(
    private var date_list: MutableList<String>,
    private var date: String,
    private val context: Context,
    private var month: Int,
    private var sele_month: Int,
) : BaseAdapter(){

    private var layoutInflater: LayoutInflater? = null
    private lateinit var date_tv: TextView
    private lateinit var day_tv: TextView

    val days = arrayOf("MON", "TUE", "WED", "THUR", "FRI", "SAT", "SUN")

    fun clear(new_date_list: MutableList<String>,day:String,month1: Int,sele_month1: Int){
        date_list.clear()
        date_list = new_date_list
        date = day
        month = month1
        sele_month = sele_month1
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return date_list.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    // below function is use to return item id of grid view.
    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.calendar_date_item, null)
        }
        date_tv = convertView!!.findViewById(R.id.tv_calgrid_date)
        day_tv = convertView!!.findViewById(R.id.tv_calgrid_day)

        date_tv.setText(date_list[position])
        // selected background
        if(date.equals(date_list[position]) && month==sele_month ){
            date_tv.setTextColor(ContextCompat.getColor(context, R.color.bg_color));
            date_tv.setTypeface(null,Typeface.BOLD)
        }else{
            date_tv.setTextColor(ContextCompat.getColor(context, R.color.black));
            date_tv.setTypeface(null,Typeface.NORMAL)
        }

        // set days on header
        if(position<7) day_tv.setText(days[position])
        else day_tv.setText("")
        return convertView
    }

}