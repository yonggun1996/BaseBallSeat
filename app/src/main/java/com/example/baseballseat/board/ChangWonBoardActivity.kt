package com.example.baseballseat.board

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.*
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.Post.CreateChangwonPostActivity
import com.example.baseballseat.databinding.ActivityChangWonBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/*
창원 NC파크 게시물 확인하는 페이지
 */
class ChangWonBoardActivity : AppCompatActivity() {
    companion object{
        const val LOCAL = "Changwon"
    }
    val TAG = "ChangWonBoardActivity"
    val UPLOADSUCESSCODE = 9999
    var userData = UserData
    private lateinit var username: String
    private lateinit var binding: ActivityChangWonBoardBinding//뷰 바인딩
    private var boardDataList = ArrayList<BoardData>()
    private lateinit var adapter : BoardDataAdapter
    private lateinit var db : FirebaseFirestore
    private lateinit var lastResult : DocumentSnapshot

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_chang_won_board)
        //뷰바인딩 설정 코드
        binding = ActivityChangWonBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        window.statusBarColor = ContextCompat.getColor(this, R.color.NC)
        db = FirebaseFirestore.getInstance()

        binding.boardprogressBar.visibility = View.VISIBLE
        username = userData.user?.displayName.toString()
        Log.d(TAG, "창원 NC 파크 페이지")
        Log.d(TAG, "User : ${username}")

        binding.createBtn.setOnClickListener {
            val createpostIntent = Intent(this, CreateChangwonPostActivity::class.java)
            createpostIntent.putExtra("local", LOCAL)

            startActivityForResult(createpostIntent, UPLOADSUCESSCODE)
        }

        //로그아웃 버튼 클릭시
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            LoginManager.getInstance()
                .logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
            Log.d(TAG, "로그아웃 시도")
            userData.user = null
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        adapter = BoardDataAdapter(boardDataList)
        binding.NCBoardRv.adapter = adapter
        binding.NCBoardRv.layoutManager = LinearLayoutManager(this)
        update_RecyclerView()

        //스크롤 했을 때 RecyclerView가 아래까지 오면 이벤트 호출
        //코드 출처 : https://start1a.tistory.com/51
        binding.NCBoardRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastItemPosition =
                    (binding.NCBoardRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                val itemTotalCount = binding.NCBoardRv.adapter?.itemCount?.minus(1)

                Log.d(TAG, "lastItemPosition: $lastItemPosition")
                Log.d(TAG, "itemTotalCount: $itemTotalCount")
                if (lastItemPosition == itemTotalCount) {
                    Log.d(TAG, "RecyclerView Last")
                    add_RecyclerView()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "RESULT_OK: $RESULT_OK")
        Log.d(TAG, "requestCode: $requestCode")
        if(resultCode == RESULT_OK && requestCode == UPLOADSUCESSCODE){
            update_RecyclerView()
        }
    }

    //RecyclerView가 아래까지 와서 FireStore의 데이터를 5개 더 확인하는 메서드
    //코드 출처 : https://www.youtube.com/watch?v=HQgJvHXsNOQ
    private fun add_RecyclerView(){
        Log.d(TAG, "lastResult : $lastResult")
        val docRef = db.collection(LOCAL)//창원 구장에 대한 정보만 추출
            .orderBy("date",com.google.firebase.firestore.Query.Direction.DESCENDING)//DB 역순으로 정렬
            .startAfter(lastResult)
            .limit(5)
            .addSnapshotListener  { snapshot, e ->
                binding.boardprogressBar.visibility = View.VISIBLE
                Log.d(TAG, "sucess : $snapshot")
                for(doc in snapshot!!){//저장해둔 데이터를 리스트에 담는 과정
                    var area = doc.get("area").toString()
                    var seat = doc.get("seat").toString()
                    var contents = doc.get("contents").toString()
                    var imageURI = doc.get("imageURI").toString()
                    var date = doc.get("date").toString()
                    var username = doc.get("username").toString()

                    boardDataList.add(BoardData(area, contents, seat, username, date, LOCAL, imageURI))
                }

                if(snapshot.size() > 0){
                    lastResult = snapshot.documents.get(snapshot.size() - 1)
                }

                adapter.notifyDataSetChanged()//어댑터가 변경된 부분이 있다면 변경
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
    }

    private fun update_RecyclerView(){
        val docRef = db.collection(LOCAL)//창원 구장에 대한 정보만 추출
            .orderBy("date",com.google.firebase.firestore.Query.Direction.DESCENDING)//DB 역순으로 정렬
            .limit(5)
            .addSnapshotListener  { snapshot, e ->
                binding.boardprogressBar.visibility = View.VISIBLE
                Log.d(TAG, "sucess : $snapshot")
                boardDataList.clear()
                for(doc in snapshot!!){//저장해둔 데이터를 리스트에 담는 과정
                    Log.d(TAG, "doc : ${doc.id}")
                    var area = doc.get("area").toString()
                    var seat = doc.get("seat").toString()
                    var contents = doc.get("contents").toString()
                    var imageURI = doc.get("imageURI").toString()
                    var date = doc.get("date").toString()
                    var username = doc.get("username").toString()
                    //Log.d(TAG, "area : $area / seat : $seat / contents : $contents / imageURI : $imageURI / date : $date / username : $username")

                    boardDataList.add(BoardData(area, contents, seat, username, date, LOCAL, imageURI))
                }

                lastResult = snapshot.documents.get(snapshot.size() - 1)
                adapter.notifyDataSetChanged()//어댑터가 변경된 부분이 있다면 변경
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
    }

    //액티비티를 벗어나면 리스트에 있는 내용들을 지우고 다시 화면으로 돌아올 때 firebase의 데이터베이스에 데이터를 채운다.
    override fun onStop() {
        super.onStop()

        Log.d(TAG, "boardDataList Clear")
    }
}