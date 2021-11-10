package com.example.baseballseat.BoardRecyclerView

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.BoardData
import kotlinx.android.synthetic.main.activity_create_post.view.*
import kotlinx.android.synthetic.main.boardviewholder.view.*
import java.io.ByteArrayInputStream
import kotlin.experimental.or

class ChangwonBoardDataViewHolder(v : View) : RecyclerView.ViewHolder(v){
    private val TAG = "ChangwonBoardDataViewHo"
    var view = v

    fun bind(item: BoardData){
        view.username_Tv.text = item.username
        view.area_Tv.text = "구역 : " + item.area
        view.seat_Tv.text = "좌석 : " + item.seat
        view.content_Tv.text = item.contents
        view.date_Tv.text = "날짜 : " + item.date

        var bitmapString = item.bitmapString

        Log.d(TAG, "bitmapString : ${bitmapString}")
        var byteArray = binaryStringToByteArray(bitmapString.toString()).toByteArray()
        Log.d(TAG, "byteArraySize : ${byteArray.size}")
        var byteIs = ByteArrayInputStream(byteArray)
        Log.d(TAG, "byteIs : ${byteIs}")
        var image = Drawable.createFromStream(byteIs, "")
        Log.d(TAG, "Image : ${image.javaClass.name}")
        view.stadium_Iv.setImageDrawable(image)
    }

    fun binaryStringToByteArray(s : String) : Array<Byte> {
        var count : Int = s.length / 8
        var basic : Byte = 0
        var bytearray : Array<Byte> = Array(count,{basic})
        for(i in 1 until count){
            var t = s.substring((i - 1) * 8, i * 8)
            bytearray[i - 1] = binaryStringToByte(t)
        }

        return bytearray
    }

    fun binaryStringToByte(s : String) : Byte{
        var ret : Byte = 0
        var total : Byte = 0

        for(i in 0..7){
            if(s[7 - i] == '1'){
                ret = 1.shl(i).toByte()
            }else{
                ret = 0
            }

            total = ret.or(total).toByte()
        }
        return total
    }
}