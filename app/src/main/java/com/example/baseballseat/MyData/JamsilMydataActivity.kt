package com.example.baseballseat.MyData

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.MyInfoData
import com.example.baseballseat.MyInfoRecyclerView.MyInfoAdapter
import com.example.baseballseat.R
import com.example.baseballseat.UserData
import com.example.baseballseat.databinding.ActivityJamsilMydataBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_jamsil_mydata.*

class JamsilMydataActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "JamsilMydataActivity"
    }

    private val LOCAL = "Jamsil"
    private val userData = UserData
    private lateinit var binding: ActivityJamsilMydataBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MyInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_jamsil_mydata)

        Log.d(TAG, "JamsilMydataActivity")
        binding = ActivityJamsilMydataBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()
        val info_List = ArrayList<MyInfoData>()
        //같은 유저가 올린 게시물만 확인하는 코드
        val docRef = db.collection(LOCAL)
                .whereEqualTo("User", userData.user?.uid.toString())
                .addSnapshotListener { value, error ->
                    info_List.clear()//데이터가 변경된 후 리스트가 다시 합쳐지는 과정을 거치기 때문에 for문을 돌기 전에 리스트를 비운다
                    for(doc in value!!){
                        val stadium = LOCAL
                        val seat = doc.get("seat").toString()
                        val area = doc.get("area").toString()
                        val date = doc.get("date").toString()
                        val imageURI = doc.get("imageURI").toString()
                        Log.d(TAG, "Data : ${stadium} / ${seat} / ${area} / ${date} / ${imageURI} / ${doc.id}")

                        info_List.add(MyInfoData(imageURI, stadium, date, seat, area, doc.id))//문서의 documentID도 포함시킨다
                    }

                    //어댑터 변경 감지
                    adapter.notifyDataSetChanged()
                }

        adapter = MyInfoAdapter(info_List)

        binding.myInfoRV.adapter = adapter
        binding.myInfoRV.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.VERTICAL }

    }

}