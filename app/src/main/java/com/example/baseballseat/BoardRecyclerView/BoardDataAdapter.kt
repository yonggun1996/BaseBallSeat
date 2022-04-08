package com.example.baseballseat.BoardRecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.BoardData
import com.example.baseballseat.R
import java.util.*

class BoardDataAdapter(val itemList : ArrayList<BoardData>) :
    RecyclerView.Adapter<ChangwonBoardDataViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangwonBoardDataViewHolder {
        val inflateView = LayoutInflater.from(parent.context)
            .inflate(R.layout.boardviewholder,parent,false)
        return ChangwonBoardDataViewHolder(inflateView)
    }

    override fun onBindViewHolder(holder: ChangwonBoardDataViewHolder, position: Int) {
        val item = itemList[position]
        holder.apply {
            bind(item)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}