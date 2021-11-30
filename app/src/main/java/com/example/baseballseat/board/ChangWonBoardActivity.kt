package com.example.baseballseat.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.*
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.Post.CreateChangwonPostActivity
import com.example.baseballseat.R
import com.example.baseballseat.databinding.ActivityChangWonBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.core.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chang_won_board.*
import kotlinx.android.synthetic.main.activity_main.*

/*
창원 NC파크 게시물 확인하는 페이지
 */
class ChangWonBoardActivity : AppCompatActivity() {
    val TAG = "ChangWonBoardActivity"
    var userData = UserData
    private lateinit var username: String
    private lateinit var binding: ActivityChangWonBoardBinding//뷰 바인딩
    private var boardDataList = ArrayList<BoardData>()
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_chang_won_board)
        //뷰바인딩 설정 코드
        binding = ActivityChangWonBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()

        binding.boardprogressBar.visibility = View.VISIBLE
        username = userData.username.toString()
        Log.d(TAG, "창원 NC 파크 페이지")
        Log.d(TAG, "User : ${username}")

        binding.createBtn.setOnClickListener {
            val createpostIntent = Intent(this, CreateChangwonPostActivity::class.java)
            createpostIntent.putExtra("local", "Changwon")

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

        val adapter = BoardDataAdapter(boardDataList)
        binding.NCBoardRv.adapter = adapter
        binding.NCBoardRv.layoutManager = LinearLayoutManager(this@ChangWonBoardActivity)

        val docRef = db.collection("Changwon")//창원 구장에 대한 정보만 추출
                .orderBy("date",com.google.firebase.firestore.Query.Direction.DESCENDING)//DB 역순으로 정렬
                .addSnapshotListener  { snapshot, e ->
                    binding.boardprogressBar.visibility = View.VISIBLE
                    boardDataList.clear()//리스너가 성공응답을 받으면 리스트를 지운다
                    Log.d(TAG, "sucess : $snapshot")
                    for(doc in snapshot!!){//저장해둔 데이터를 리스트에 담는 과정
                        var area = doc.get("area").toString()
                        var seat = doc.get("seat").toString()
                        var contents = doc.get("contents").toString()
                        var imageURI = doc.get("imageURI").toString()
                        var date = doc.get("date").toString()
                        var username = doc.get("username").toString()

                        boardDataList.add(BoardData(area, contents, seat, username, date, "Changwon",imageURI))
                    }

                    adapter.notifyDataSetChanged()//어댑터가 변경된 부분이 있다면 변경
                    binding.boardprogressBar.visibility = View.INVISIBLE
                }
    }

    //액티비티를 벗어나면 리스트에 있는 내용들을 지우고 다시 화면으로 돌아올 때 firebase의 데이터베이스에 데이터를 채운다.
    override fun onStop() {
        super.onStop()
        boardDataList.clear()
        Log.d(TAG, "boardDataList Clear")
    }
}