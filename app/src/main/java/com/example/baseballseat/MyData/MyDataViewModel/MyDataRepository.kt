package com.example.baseballseat.MyData.MyDataViewModel

import androidx.lifecycle.MutableLiveData
import com.example.baseballseat.MyInfoData
import java.util.*

interface MyDataRepository {
    fun get_MyJamsilInfoData(): MutableLiveData<ArrayList<MyInfoData>>
}