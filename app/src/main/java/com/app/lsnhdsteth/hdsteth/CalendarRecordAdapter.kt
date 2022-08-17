package com.app.lsnhdsteth.hdsteth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.app.lsnhdsteth.R
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.app.lsnhdsteth.utils.Utility
import com.example.example.Rec

class CalendarRecordAdapter(
    var context: Context,
    var rec: java.util.ArrayList<Rec>,
    var storage:String
) : RecyclerView.Adapter<CalendarRecordAdapter.MyViewHolder>()  {


    var pref : MySharePrefernce? = null
    var time_format ="";

    init {
        pref = MySharePrefernce(context)
        time_format = pref?.getStringData(MySharePrefernce.KEY_TIME_FORMAT)!!
    }

    private var listener: ListItemClickListener? = null

    fun setData(records: ArrayList<Rec>,storage:String){
        this.rec.clear()
        this.rec = records
        this.storage = storage
        notifyDataSetChanged()
        notifyItemChanged(0)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.tv_adprecord_name)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.calendar_record_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if(time_format.equals("12")){
            holder.title.text = Utility.getFormatedTime(rec!![position]?.time!!)
        }else holder.title.text = rec!![position]?.time
        holder.itemView.setOnClickListener({
            listener?.onItemClick(rec!![position],storage)
        });

    }

    override fun getItemCount(): Int {
        return rec!!.size
    }

    fun setListerner(lister : ListItemClickListener){
        this.listener = lister
    }

    interface ListItemClickListener{
        fun onItemClick(rec: Rec,storage: String)
    }

}


