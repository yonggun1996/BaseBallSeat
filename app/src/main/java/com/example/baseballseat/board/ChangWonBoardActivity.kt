package com.example.baseballseat.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.baseballseat.Post.CreateChangwonPostActivity
import com.example.baseballseat.LoginActivity
import com.example.baseballseat.MainActivity
import com.example.baseballseat.R
import com.example.baseballseat.UserData
import com.example.baseballseat.databinding.ActivityChangWonBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chang_won_board.*
import kotlinx.android.synthetic.main.activity_main.*

/*
창원 NC파크 게시물 확인하는 페이지
 */
class ChangWonBoardActivity : AppCompatActivity() {
    val TAG = "ChangWonBoardActivity"
    var userData = UserData
    private lateinit var username : String
    private lateinit var binding : ActivityChangWonBoardBinding//뷰 바인딩

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
            createpostIntent.putExtra("local","changwon")

            startActivity(createpostIntent)
        }

        //로그아웃 버튼 클릭시
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            LoginManager.getInstance().logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
            Log.d(TAG, "로그아웃 시도")
            userData.username = ""
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val backIntent = Intent(this, MainActivity::class.java)
        startActivity(backIntent)
    }
}