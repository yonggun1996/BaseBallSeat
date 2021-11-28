package com.example.baseballseat.BoardRecyclerView

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.example.baseballseat.BoardData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_create_post.view.*
import kotlinx.android.synthetic.main.boardviewholder.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.experimental.or

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
        view.date_Tv.text = "날짜 : " + item.date
        Log.d(TAG, "지역 : ${item.local}")
    }
}