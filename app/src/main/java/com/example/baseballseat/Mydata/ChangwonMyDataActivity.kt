package com.example.baseballseat.Mydata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.BoardRecyclerView.MyDataAdapter
import com.example.baseballseat.MyUploadData
import com.example.baseballseat.R
import com.example.baseballseat.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_changwon_my_data.*

class ChangwonMyDataActivity : AppCompatActivity() {
    val userdata = UserData
    private val TAG = "ChangwonMyDataActivity"
    private var myData_List = ArrayList<MyUploadData>()
    private lateinit var adapter : MyDataAdapter
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changwon_my_data)
        Log.d(TAG, "MyDataActivity")

        progressBar.visibility = View.VISIBLE
        myName_Tv.setText("${userdata.user?.displayName}님의 업로드 정보 입니다.")
        db = FirebaseFirestore.getInstance()

        adapter = MyDataAdapter(myData_List)
        changwon_Mydata_Rv.adapter = adapter
        changwon_Mydata_Rv.layoutManager = LinearLayoutManager(this)
        upload_RecyclerView()
    }

    private fun upload_RecyclerView(){
        //Log.d(TAG, "upload_RecyclerView: ${userdata.user?.uid.toString()}")
        val docRef = db.collection("Changwon")
            .whereEqualTo("User",userdata.user?.uid.toString())
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                myData_List.clear()
                for(doc in snapshot!!){
                    myData_List.add(MyUploadData("Changwon",doc.id))
                    Log.d(TAG, "upload_RecyclerView: ${doc.id}")
                }

                adapter.notifyDataSetChanged()
            }

        progressBar.visibility = View.INVISIBLE
    }
}