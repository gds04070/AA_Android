package com.gds.aa_android.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gds.aa_android.R
import com.gds.aa_android.db.ChartData
import kotlinx.android.synthetic.main.rv_item_chart_data.view.*

class ChartDataRecyclerViewAdapter(val ctx:Context, val dataList : ArrayList<ChartData>) : RecyclerView.Adapter<ChartDataRecyclerViewAdapter.Holder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view : View = LayoutInflater.from(ctx).inflate(R.layout.rv_item_chart_data, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.number.text = dataList[position].number.toString()
        holder.average.text = dataList[position].average
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val number : TextView = itemView.findViewById(R.id.iv_rv_item_chart_number)as TextView
        val average : TextView = itemView.findViewById(R.id.iv_rv_item_chart_average)as TextView
    }
}