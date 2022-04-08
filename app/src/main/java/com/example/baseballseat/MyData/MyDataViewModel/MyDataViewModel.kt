package com.example.baseballseat.MyData.MyDataViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.baseballseat.MyInfoData
import java.util.*

class MyDataViewModel : ViewModel(){
    private val repository: MyDataRepository = MyDataRepositoryImp()
    private var myData = MutableLiveData<ArrayList<MyInfoData>>()

    fun get_MyData(): LiveData<ArrayList<MyInfoData>>{
        myData = repository.get_MyJamsilInfoData()
        return myData
    }
}