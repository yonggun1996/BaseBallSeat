package com.example.baseballseat.BoardRecyclerView

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baseballseat.BoardData
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_create_post.view.*
import kotlinx.android.synthetic.main.boardviewholder.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import kotlin.experimental.or

class ChangwonBoardDataViewHolder(v : View) : RecyclerView.ViewHolder(v){
    private val TAG = "ChangwonBoardDataViewHo"
    var view = v
    private var storage = Firebase.storage

    fun bind(item: BoardData){
        var storageRef = storage.reference

        Log.d(TAG, "경로 : ${item.local}/${item.date}.jpg")
        val pathReference = storageRef.child("${item.local}/${item.date}.jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024 * 5
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            var byteIs = ByteArrayInputStream(it)
            var image = Drawable.createFromStream(byteIs,"")
            Log.d(TAG, "image : ${image}")
            Glide.with(view.context)
                    .load(image)
                    .into(view.stadium_Iv)
        }.addOnFailureListener {
            Log.d(TAG, "bind: 이미지 뷰에 로드 실패")
        }

        view.username_Tv.text = item.username
        view.area_Tv.text = "구역 : " + item.area
        view.seat_Tv.text = "좌석 : " + item.seat
        view.content_Tv.text = item.contents
        view.date_Tv.text = "날짜 : " + item.date
        Log.d(TAG, "지역 : ${item.local}")
    }
}