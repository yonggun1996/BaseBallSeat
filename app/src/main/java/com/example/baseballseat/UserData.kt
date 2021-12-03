package com.example.baseballseat

import com.google.firebase.auth.FirebaseUser

//뒤로가기를 했을 때 유저이름이 변경되지 않도록 싱글톤 객체로 생성
object UserData {
    var user : FirebaseUser? = null
}