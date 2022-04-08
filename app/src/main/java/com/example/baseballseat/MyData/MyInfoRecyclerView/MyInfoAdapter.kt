package com.example.baseballseat.MyData.MyInfoRecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.MyInfoData
import com.example.baseballseat.R
import kotlinx.android.synthetic.main.activity_mydata_viewholder.view.*

class MyInfoAdapter(var itemList: ArrayList<MyInfoData>) : RecyclerView.Adapter<MyInfoDataViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyInfoDataViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_mydata_viewholder, parent, false)

        return MyInfoDataViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: MyInfoDataViewHolder, position: Int) {
        val item = itemList[position]
        holder.apply {
            bind(item, itemList, position)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}