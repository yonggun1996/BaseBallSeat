package com.example.baseballseat.BoardRecyclerView

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseballseat.BoardData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.boardviewholder.view.*

class ChangwonBoardDataViewHolder(v : View) : RecyclerView.ViewHolder(v){
    private val TAG = "ChangwonBoardDataViewHo"
    var view = v
    private var storage = Firebase.storage

    fun bind(item: BoardData){
        var storageRef = storage.reference.child("${item.local}/${item.date}.jpg")

        Glide.with(view.context)
            .load(item.imageURI)
            .into(view.stadium_Iv)

        view.username_Tv.text = item.username
        view.area_Tv.text = "구역 : " + item.area
        view.seat_Tv.text = "좌석 : " + item.seat
        view.content_Tv.text = item.contents
        Log.d(TAG, "contents : ${item.contents}")
        view.date_Tv.text = "날짜 : " + item.date
        Log.d(TAG, "지역 : ${item.local}")
    }
}