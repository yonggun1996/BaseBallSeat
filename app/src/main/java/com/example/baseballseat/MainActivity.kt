package com.example.baseballseat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.baseballseat.board.*
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseUser
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

        user_tv.text = "${userData.user?.displayName}님 환영합니다"

        //로그아웃 버튼 클릭시
        logout_Btn.setOnClickListener {
            signOut()
        }

        //창원 NC파크 선택
        NC_Btn.setOnClickListener {
            val ncnextIntent = Intent(this, ChangWonBoardActivity::class.java)
            startActivity(ncnextIntent)
        }

        Jamsil_Btn.setOnClickListener {
            val jamsil_nextIntent = Intent(this, JamsilBoardActivity::class.java)
            startActivity(jamsil_nextIntent)
        }

        KT_Btn.setOnClickListener {
            val suwon_nextIntent = Intent(this, SuwonBoardActivity::class.java)
            startActivity(suwon_nextIntent)
        }
        
        Kiwoom_Btn.setOnClickListener {
            val geocheok_nextIntent = Intent(this, GocheokBoardActivity::class.java)
            startActivity(geocheok_nextIntent)
        }

        KIA_Btn.setOnClickListener {
            val gwangju_nextIntent = Intent(this, GwangjuBoardActivity::class.java)
            startActivity(gwangju_nextIntent)
        }

        Lotte_Btn.setOnClickListener {
            val busan_nextIntent = Intent(this, BusanBoardActivity::class.java)
            startActivity(busan_nextIntent)
        }

    }

    private fun signOut(){
        Firebase.auth.signOut()
        LoginManager.getInstance().logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
        Log.d(TAG, "로그아웃 시도")
        userData.user = null
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