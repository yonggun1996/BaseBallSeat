package com.example.baseballseat.board

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseballseat.BoardData
import com.example.baseballseat.BoardRecyclerView.BoardDataAdapter
import com.example.baseballseat.LoginActivity
import com.example.baseballseat.Post.GocheokPostActivity
import com.example.baseballseat.Post.GwangjuPostActivity
import com.example.baseballseat.UserData
import com.example.baseballseat.databinding.ActivityGwangjuBoardBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class GwangjuBoardActivity : AppCompatActivity() {

    /* 광주 기아챔피언스필드 게시판 */

    private val TAG = "GwangjuBoardActivity"
    val UPLOADSUCESSCODE = 9999
    private lateinit var binding: ActivityGwangjuBoardBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var adapter : BoardDataAdapter
    private var boardDataList = ArrayList<BoardData>()
    private lateinit var lastResult: DocumentSnapshot
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_gwangju_board)

        binding = ActivityGwangjuBoardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = FirebaseFirestore.getInstance()

        binding.boardprogressBar.visibility = View.VISIBLE
        username = UserData.user?.displayName.toString()

        binding.createBtn.setOnClickListener {
            val createpostIntent = Intent(this, GwangjuPostActivity::class.java)
            createpostIntent.putExtra("local", "Gwangju")
            startActivity(createpostIntent)
        }

        //로그아웃 버튼 클릭
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            LoginManager.getInstance()
                .logOut()//이 라인을 적지 않으면 Firebase에만 로그아웃이 되고, Facebook은 로그아웃이 안된다.
            Log.d(TAG, "로그아웃 시도")
            UserData.user = null
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        adapter = BoardDataAdapter(boardDataList)
        binding.gwangjuBoardRv.adapter = adapter
        binding.gwangjuBoardRv.layoutManager = LinearLayoutManager(this)

        update_RecyclerView()

        binding.gwangjuBoardRv.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastItemPosition =
                    (binding.gwangjuBoardRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                val itemTotalCount = binding.gwangjuBoardRv.adapter?.itemCount?.minus(1)
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

    private fun update_RecyclerView() {
        val docRef = db.collection("Gwangju")//창원 구장에 대한 정보만 추출
            .orderBy("date",com.google.firebase.firestore.Query.Direction.DESCENDING)//DB 역순으로 정렬
            .limit(5)
            .addSnapshotListener  { snapshot, e ->
                binding.boardprogressBar.visibility = View.VISIBLE
                Log.d(TAG, "sucess : $snapshot")
                boardDataList.clear()
                for(doc in snapshot!!){//저장해둔 데이터를 리스트에 담는 과정
                    var area = doc.get("area").toString()
                    var seat = doc.get("seat").toString()
                    var contents = doc.get("contents").toString()
                    var imageURI = doc.get("imageURI").toString()
                    var date = doc.get("date").toString()
                    var username = doc.get("username").toString()

                    boardDataList.add(BoardData(area, contents, seat, username, date, "Gwangju",imageURI))
                }

                if(snapshot.size() > 0){
                    lastResult = snapshot.documents.get(snapshot.size() - 1)
                }
                adapter.notifyDataSetChanged()//어댑터가 변경된 부분이 있다면 변경
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
    }

    private fun add_RecyclerView() {
        Log.d(TAG, "lastResult : $lastResult")
        val docRef = db.collection("Gwangju")//창원 구장에 대한 정보만 추출
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

                    boardDataList.add(BoardData(area, contents, seat, username, date, "Gwangju",imageURI))
                }

                if(snapshot.size() > 0){
                    lastResult = snapshot.documents.get(snapshot.size() - 1)
                }

                adapter.notifyDataSetChanged()//어댑터가 변경된 부분이 있다면 변경
                binding.boardprogressBar.visibility = View.INVISIBLE
            }
    }
}