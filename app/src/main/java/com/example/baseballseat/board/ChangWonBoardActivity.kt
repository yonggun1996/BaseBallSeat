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
    private var database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_chang_won_board)
        //뷰바인딩 설정 코드
        binding = ActivityChangWonBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.boardprogressBar.visibility = View.VISIBLE
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
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        readData(object : FirebaseCallBack {
            override fun onCallback() {//callback 함수 구현
                Log.d(TAG, "boardlist -> ${boardDataList.size}")
                boardDataList.sortByDescending { it.date }//데아터 역순 정렬
                //RecyclerView 세팅
                val adapter = BoardDataAdapter(boardDataList)
                binding.NCBoardRv.adapter = adapter
                binding.NCBoardRv.layoutManager = LinearLayoutManager(this@ChangWonBoardActivity)
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
        })
    }

    //데이터베이스를 차례대로 불러오는 메소드
    fun readData(firebaseCallBack: FirebaseCallBack){
        val query = database.getReference("changwon")
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
                    children?.local = "changwon"

                    Log.d(TAG, "key : ${key} / area : ${area} / seat : ${seat} / contents : ${contents} / imageURI : ${imageURI}")
                    boardDataList.add(BoardData(area, contents, seat, user, key, "changwon", imageURI))
                }

                firebaseCallBack.onCallback()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.toString()}")
            }
        })

    }

    override fun onStart() {
        super.onStart()
    }

    //firebase realtimedatabase를 동기 방식으로 설정하기 위한 인터페이스
    interface FirebaseCallBack{
        fun onCallback()
    }

    //액티비티를 벗어나면 리스트에 있는 내용들을 지우고 다시 화면으로 돌아올 때 firebase의 데이터베이스에 데이터를 채운다.
    override fun onStop() {
        super.onStop()
        boardDataList.clear()
        Log.d(TAG, "boardDataList Clear")
    }
}