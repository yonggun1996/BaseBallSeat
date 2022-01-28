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
        val info_List = get_Infodata()
        adapter = MyInfoAdapter(info_List)
        binding.myInfoRV.adapter = adapter
        binding.myInfoRV.layoutManager = LinearLayoutManager(this).also { it.orientation = LinearLayoutManager.VERTICAL }

    }

    //현재 로그인된 유저가 업로드한 정보를 확인시켜주는 메서드
    fun get_Infodata(): ArrayList<MyInfoData>{
        val infoData_List = ArrayList<MyInfoData>()

        Log.d(Companion.TAG, "get_Infodata")
        //같은 유저가 올린 게시물만 확인하는 코드
        val docRef = db.collection(LOCAL)
            .whereEqualTo("User", userData.user?.uid.toString())
            .addSnapshotListener { value, error ->
                for(doc in value!!){
                    val stadium = "잠실야구장"
                    val seat = doc.get("seat").toString()
                    val area = doc.get("area").toString()
                    val date = doc.get("date").toString()
                    val imageURI = doc.get("imageURI").toString()

                    Log.d(TAG, "Data : ${stadium} / ${seat} / ${area} / ${date} / ${imageURI}")

                    infoData_List.add(MyInfoData(imageURI, stadium, date, seat, area))
                }

                //어댑터 변경 감지
                adapter.notifyDataSetChanged()
            }

        return infoData_List
    }

}