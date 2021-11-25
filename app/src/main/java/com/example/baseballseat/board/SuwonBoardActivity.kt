package com.example.baseballseat.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.BoardData
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.LoginActivity
import com.example.baseballseat.Post.CreateJamsilPostActivity
import com.example.baseballseat.Post.CreateSuwonPostActivity
import com.example.baseballseat.R
import com.example.baseballseat.UserData
import com.example.baseballseat.databinding.ActivityJamsilBoardBinding
import com.example.baseballseat.databinding.ActivitySuwonBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SuwonBoardActivity : AppCompatActivity() {
    val TAG = "SuwonBoardActivity"
    var userData = UserData
    private lateinit var username: String
    private lateinit var binding: ActivitySuwonBoardBinding
    private var boardDataList = ArrayList<BoardData>()
    private var database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suwon_board)

        binding = ActivitySuwonBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.boardprogressBar.visibility = View.VISIBLE
        username = userData.username.toString()
        Log.d(TAG, "수원 Ktwiz 파크 페이지")
        Log.d(TAG, "User : ${username}")

        binding.createBtn.setOnClickListener {
            val createpostIntent = Intent(this, CreateSuwonPostActivity::class.java)
            createpostIntent.putExtra("local", "Suwon")

            startActivity(createpostIntent)
        }

        //로그아웃 버튼 클릭시
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            LoginManager.getInstance()
                    .logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
            Log.d(TAG, "로그아웃 시도")
            userData.username = ""
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        readData(object : SuwonBoardActivity.FirebaseCallBack {
            override fun onCallback() {//callback 함수 구현
                Log.d(TAG, "boardlist -> ${boardDataList.size}")
                boardDataList.sortByDescending { it.date }//데아터 역순 정렬
                //RecyclerView 세팅
                val adapter = BoardDataAdapter(boardDataList)
                binding.suwonBoardRv.adapter = adapter
                binding.suwonBoardRv.layoutManager = LinearLayoutManager(this@SuwonBoardActivity)
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
        })
    }

    //데이터베이스를 차례대로 불러오는 메소드
    fun readData(firebaseCallBack: FirebaseCallBack){
        val query = database.getReference("Suwon")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val children = postSnapshot.getValue<BoardData>()
                    var key = postSnapshot.key
                    var area = children?.area
                    var contents = children?.contents
                    var seat = children?.seat
                    var user = children?.username
                    var imageURI = children?.imageURI
                    children?.date = key
                    children?.local = "jamsil"

                    Log.d(TAG, "key : ${key} / area : ${area} / seat : ${seat} / contents : ${contents} / imageURI : ${imageURI}")
                    boardDataList.add(BoardData(area, contents, seat, user, key, "jamsil", imageURI))
                }

                firebaseCallBack.onCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.toString()}")
            }
        })

    }

    //firebase realtimedatabase를 동기 방식으로 설정하기 위한 인터페이스
    interface FirebaseCallBack{
        fun onCallback()
    }

    //액티비티를 벗어나면 리스트에 있는 내용들을 지우고 다시 화면으로 돌아올 때 firebase의 데이터베이스에 데이터를 채운다.
    override fun onStop() {
        super.onStop()
        boardDataList.clear()
    }
}