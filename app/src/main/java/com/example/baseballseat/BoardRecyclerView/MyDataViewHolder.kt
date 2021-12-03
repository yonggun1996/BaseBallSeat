package com.example.baseballseat.BoardRecyclerView

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baseballseat.MyUploadData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.mydataviewholder.view.*

class MyDataViewHolder(v : View) : RecyclerView.ViewHolder(v){
    private val TAG = "MyDataViewHolder"
    var view = v
    private lateinit var db : FirebaseFirestore

    fun bind(item: MyUploadData){
        var city = item.local
        var doc = item.document

        var area = ""
        var contents = ""
        var date = ""
        var seat = ""
        var imageURI = ""

        db = FirebaseFirestore.getInstance()
        val docRef = db.collection(city).document(doc)
        docRef.get()
            .addOnSuccessListener {
                area = it.get("area").toString()
                contents = it.get("contents").toString()
                date = it.get("date").toString()
                seat = it.get("seat").toString()
                imageURI = it.get("imageURI").toString()

                Glide.with(view.context)
                    .load(imageURI)
                    .into(view.md_stadiumIv)

                view.md_areaTv.text = area
                view.md_seatTv.text = seat
                view.md_dateTv.text = date
            }.addOnFailureListener {
                Toast.makeText(view.context,"데이터를 불러오지 못했습니다.",Toast.LENGTH_SHORT).show()
            }



        view.update_Btn.setOnClickListener {

        }

        //삭제버튼을 클릭했을 때
        view.delete_Btn.setOnClickListener {
            Log.d(TAG, "click DeleteBtn")
            //대화상자를 먼저 띄운다
            val activity: Activity = view.context as Activity
            val alterdialog: AlertDialog.Builder? = activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton("네",
                        DialogInterface.OnClickListener { dialog, id ->
                            deleteDB(city, doc)
                        })
                    setNegativeButton("아니오",
                        DialogInterface.OnClickListener { dialog, id ->

                        }
                    )

                    builder.create()
                }
            }

            alterdialog?.setTitle("삭제 여부")
            alterdialog?.setMessage("정말 삭제하시겠습니까?")
            alterdialog?.show()
        }
    }

    private fun deleteDB(city: String, doc: String){
        db.collection(city).document(doc)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(view.context, "데이터를 삭제했습니다.",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(view.context, "데이터를 삭제하는데 실패했습니다..",Toast.LENGTH_SHORT).show()
                Log.d(TAG, "데이터 삭제 실패 : $it")
            }
    }
}