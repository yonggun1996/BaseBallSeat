package com.example.baseballseat.MyInfoRecyclerView

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseballseat.MyInfoData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_mydata_viewholder.view.*

class MyInfoDataViewHolder(v: View) : RecyclerView.ViewHolder(v){
    val view: View = v
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MyInfoAdapter

    fun bind(item: MyInfoData, itemList: ArrayList<MyInfoData>, position: Int){
        db = FirebaseFirestore.getInstance()
        val img_URL = item.img_URL
        Glide.with(view.context)
                .load(img_URL)
                .into(view.myInfo_IV)

        view.myInfo_stadium_TV.text = item.local
        view.myInfo_seat_TV.text = item.seat
        view.myInfo_area_TV.text = item.area
        view.myInfo_date_TV.text = item.date

        adapter = MyInfoAdapter(itemList)
        //삭제버튼 클릭시
        view.delete_Btn.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("삭제 확인")
            builder.setMessage("데이터를 삭제하시겠습니까?")
            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialogInterface, i ->
                //대화상자에서 "네"를 클릭을 할 시 삭제가 된다
                val dataRef = db.collection(item.local).document(item.document)
                dataRef.delete().addOnCompleteListener(OnCompleteListener {
                    if (it.isSuccessful) {//삭제 성공시
                        Log.d(ContentValues.TAG, "데이터를 삭제하는데 성공했습니다.")
                        Toast.makeText(view.context, "데이터를 삭제했습니다.", Toast.LENGTH_SHORT).show()
                    } else {//삭제 실패시
                        Log.d(ContentValues.TAG, "데이터를 삭제하는데 실패했습니다.")
                        Toast.makeText(view.context, "데이터를 삭제하는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                })
            })

            builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                Log.d(ContentValues.TAG, "데이터를 삭제하지 않습니다.")
            })

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}