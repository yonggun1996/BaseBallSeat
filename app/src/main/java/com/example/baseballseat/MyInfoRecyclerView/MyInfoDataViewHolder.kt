package com.example.baseballseat.MyInfoRecyclerView

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseballseat.MyInfoData
import kotlinx.android.synthetic.main.activity_mydata_viewholder.view.*
import kotlinx.android.synthetic.main.boardviewholder.view.*

class MyInfoDataViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val view: View = v

    fun bind(item: MyInfoData){
        val img_URL = item.img_URL
        Glide.with(view.context)
            .load(img_URL)
            .into(view.myInfo_IV)

        view.myInfo_stadium_TV.text = item.area
        view.myInfo_seat_TV.text = item.seat
        view.myInfo_area_TV.text = item.area
        view.myInfo_date_TV.text = item.date
    }
}