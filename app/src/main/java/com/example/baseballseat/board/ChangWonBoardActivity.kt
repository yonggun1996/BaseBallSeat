package com.example.baseballseat.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.*
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.Post.CreateChangwonPostActivity
import com.example.baseballseat.databinding.ActivityChangWonBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_chang_won_board)
        //뷰바인딩 설정 코드
        binding = ActivityChangWonBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        username = userData.username.toString()
        Log.d(TAG, "창원 NC 파크 페이지")
        Log.d(TAG, "User : ${username}")

        binding.createBtn.setOnClickListener {
            val createpostIntent = Intent(this, CreateChangwonPostActivity::class.java)
            createpostIntent.putExtra("local", "changwon")

            startActivity(createpostIntent)
        }

        //로그아웃 버튼 클릭시
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            LoginManager.getInstance()
                .logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
            Log.d(TAG, "로그아웃 시도")
            userData.username = ""
            startActivity(Intent(this, LoginActivity::class.java))
        }


        var database = FirebaseDatabase.getInstance()
        val adapter = BoardDataAdapter(boardDataList)
        //데이터베이스에 있는 데이터를 읽어오는 코드
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                val boarddata = dataSnapshot.getValue<BoardData>()
                var bdataKey = dataSnapshot.key
                var area = boarddata?.area
                var bitmapString = boarddata?.bitmapString
                var contents = boarddata?.contents
                var seat = boarddata?.seat
                boarddata?.date = dataSnapshot.key

                Log.d(TAG, "key : ${bdataKey} / area : ${area} / seat : ${seat} / contents : ${contents}")
                boardDataList.add(BoardData(area, bitmapString, contents, seat, username, bdataKey))
                boardDataList.sortByDescending { it.date }//최신 게시물일수록 먼저 나와야 하기 때문에 날짜 순대로 내림차순 정렬
                adapter.notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                Toast.makeText(
                    this@ChangWonBoardActivity, "Failed to load comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val ref = database.getReference("changwon").orderByKey()
        ref.addChildEventListener(childEventListener)


        Log.d(TAG, "list size : ${boardDataList.size}")
        binding.NCBoardRv.adapter = adapter
        binding.NCBoardRv.layoutManager = LinearLayoutManager(this)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val backIntent = Intent(this, MainActivity::class.java)
        startActivity(backIntent)
    }
}