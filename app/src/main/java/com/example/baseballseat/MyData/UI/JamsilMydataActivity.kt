package com.example.baseballseat.MyData.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseballseat.MyData.MyDataViewModel.MyDataViewModel
import com.example.baseballseat.MyInfoData
import com.example.baseballseat.MyData.MyInfoRecyclerView.MyInfoAdapter
import com.example.baseballseat.databinding.ActivityJamsilMydataBinding
import java.util.*

class JamsilMydataActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "JamsilMydataActivity"
    }

    val mydataViewModel = MyDataViewModel()
    private lateinit var binding: ActivityJamsilMydataBinding
    private lateinit var adapter: MyInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_jamsil_mydata)

        Log.d(TAG, "JamsilMydataActivity")
        binding = ActivityJamsilMydataBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var info_List = ArrayList<MyInfoData>()
        mydataViewModel.get_MyData().observe(this, androidx.lifecycle.Observer {
            Log.d(TAG, "it.size = ${it.size}")
            info_List.clear()
            info_List.addAll(it)

            adapter = MyInfoAdapter(info_List)
            binding.myInfoRV.adapter = adapter
            binding.myInfoRV.layoutManager = LinearLayoutManager(this)
            adapter.notifyDataSetChanged()
        })

    }

}