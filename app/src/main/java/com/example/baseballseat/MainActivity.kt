package com.example.baseballseat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.baseballseat.board.ChangWonBoardActivity
import com.example.baseballseat.board.JamsilBoardActivity
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

/*
구장을 선택할 수 있는 MainActivity
 */

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"
    var userData = UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity 입니다.")

        val username = userData.username
        user_tv.text = "${username}님 환영합니다"

        //로그아웃 버튼 클릭시
        logout_Btn.setOnClickListener {
            signOut()
        }

        //창원 NC파크 선택
        NC_Btn.setOnClickListener {
            val NC_nextIntent = Intent(this, ChangWonBoardActivity::class.java)
            NC_nextIntent.putExtra("username",username)
            startActivity(NC_nextIntent)
        }

        Jamsil_Btn.setOnClickListener {
            val Jamsil_nextIntent = Intent(this, JamsilBoardActivity::class.java)
            Jamsil_nextIntent.putExtra("username",username)
            startActivity(Jamsil_nextIntent)
        }

    }

    private fun signOut(){
        Firebase.auth.signOut()
        LoginManager.getInstance().logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
        Log.d(TAG, "로그아웃 시도")
        userData.username = ""
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "onBackPressed: 시스템 종료")
        moveTaskToBack(true)
        finishAffinity()//앱을 완전히 종료 시키기 위한 함수
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}