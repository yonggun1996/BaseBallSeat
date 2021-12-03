package com.example.baseballseat.BoardRecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.MyUploadData
import com.example.baseballseat.R

class MyDataAdapter(var itemList : ArrayList<MyUploadData>) :
    RecyclerView.Adapter<MyDataViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDataViewHolder {
        val inflateView = LayoutInflater.from(parent.context)
            .inflate(R.layout.mydataviewholder, parent, false)
        return MyDataViewHolder(inflateView)
    }

    override fun onBindViewHolder(holder: MyDataViewHolder, position: Int) {
        val item = itemList[position]
        holder.apply {
            bind(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}