package com.example.baseballseat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

/*
로그인이 안되있을 경우 로그인을 하는 코틀린 코드
Google 로그인과 FaceBook 로그인 지원
 */

class LoginActivity : AppCompatActivity(){

    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth : FirebaseAuth//firebase의 인증 객체에 대한 인스턴스
    private val RC_SIGN_IN = 1000//결과코드
    private val TAG = "GoogleLoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()//파이어베이스 인증 객체 초기화
        //구글 로그인 옵션 객체 생성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //구글 로그인 API 클라이언트 생성
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()

        //로그인 버튼을 늘렀을 시
        google_Btn.setOnClickListener {
            signIn()
        }

        facebook_Btn.setReadPermissions("email","public_profile")
        facebook_Btn.registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "facebook:onSuccess: ${result.accessToken}")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "facebook:onError", error)
            }

        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    move_MainActivity(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    move_MainActivity(null)
                }
            }
    }

    //앱이 화면에 나타날 때 호출하는 메소드
    //근데 만약 로그인 한 이력이 있다면 메인 액티비티로
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        val userData = UserData
        userData.username = currentUser?.displayName.toString()
        Log.d(TAG, "유저 정보 : ${currentUser?.displayName}")
        move_MainActivity(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent//API로 부터 인텐트를 불러와 구글 로그인 액티비티를 실행
        startActivityForResult(signInIntent, RC_SIGN_IN)//구글 인증 절차를 거친 후 결과값을 받는 구문
    }

    //로그인을 요청했을 때 결과를 받는 메소드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //로그인에 성공한 경우 OAuth2.0 토큰을 읽어들여 클라이언트 ID를 넘긴다
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        Log.d(TAG, "로그인 성공, 유저 정보 : ${user?.displayName}")
                        move_MainActivity(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "로그인 실패", task.exception)
                        Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    //MainActivity로 화면을 전환하는 메소드
    private fun move_MainActivity(user : FirebaseUser?){
        if(user != null){
            val userData = UserData
            userData.username = user.displayName.toString()
            val intent = Intent(this, MainActivity::class.java)
            //intent.putExtra("Username", user.displayName)//로그인한 계정의 이름도 인텐트에 묶어 보낸다
            finish()
            startActivity(intent)
        }
    }

    //뒤로가기 버튼을 누르면 바로 종료되게끔 메서드를 생성
    //메인에서 로그아웃 버튼을 누른 후 뒤로가기를 누리면 Main으로 가는 것을 방지
    override fun onBackPressed() {
        super.onBackPressed()
        Log.d(TAG, "onBackPressed: 시스템 종료")
        moveTaskToBack(true)
        finishAffinity()//앱을 완전히 종료시키기 위한 함수
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}