package com.example.baseballseat.DataUpdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.baseballseat.R
import com.example.baseballseat.databinding.ActivityCreatePostBinding

/*
잠실야구장 수정 페이지
 */

class UpdateJamsilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    var area = ""
    val TAG = "UpdateJamsilActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_update_jamsil)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent
        val map = intent.getSerializableExtra("map") as HashMap<String, String>//내 정보 페이지에서 intent를 통해 받아온 map

        var seat = map["seat"].toString()//좌석명
        area = map["area"].toString()//구역명

        val seat_list : List<String> = listOf("프리미엄석","블루석","네이비석","1루 테이블석","3루 테이블석","오렌지석","익사이팅석","레드석","외야그린석")
        Log.d(TAG, "seat index : ${seat_list.indexOf(seat)}")

        binding.seatSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,seat_list)
        binding.seatSpinner.setSelection(seat_list.indexOf(seat))//초기 스피너에 데이터 세팅
        binding.seatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                seat = binding.seatSpinner.selectedItem.toString()
                setAreaList(seat)
                Log.d(TAG, "seat : ${seat}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }//OnCreate()

    private fun setAreaList(seat: String) {
        var list : List<String> = mutableListOf()
        when(seat){
            "1루 테이블석" -> list = mutableListOf("110","111","212","213")
            "3루 테이블석" -> list = mutableListOf("112","113","214","215")
            "블루석" -> list = mutableListOf("107","108","109","114","115","116","209","210","211","216","217","218")
            "오렌지석" -> list = mutableListOf("205","206","207","208","219","220","221","222")
            "레드석" -> list = mutableListOf("101","102","103","104","105","106","117","118","119","120","121","122","201","202","203","204","223","224","225","226")
            "네이비석" -> list = mutableListOf("301","302","303","304","305","306","307","308","309","310","311","312","313","314","315","316","317","318",
                "319","320","321","322","323","324","325","326","327","328","329","330","331","332","333","334")
            "외야그린석" -> list = mutableListOf("401","402","403","404","405","406","407","408","409","410","411","412","413","414","415","416","417","418","419","420","421","422")
        }

        Log.d(TAG, "area index : ${list.indexOf(area)}")
        var idx = list.indexOf(area)

        binding.areaSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)

        //초기 구역 스피너의 초기값을 세팅하기 위한 코드
        if(idx == -1){
            binding.areaSpinner.setSelection(0)
        }else{
            //초기 area에 따른 아이템을 지정한다
            binding.areaSpinner.setSelection(idx)
        }

        binding.areaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                area = binding.areaSpinner.selectedItem.toString()//스피너로 구역을 선택하면 area 변수 초기화
                Log.d(TAG, "area : ${area}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
}