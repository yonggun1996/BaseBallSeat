package com.example.baseballseat.MyData.MyDataViewModel

import androidx.lifecycle.MutableLiveData
import com.example.baseballseat.MyInfoData
import com.example.baseballseat.UserData
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MyDataRepositoryImp : MyDataRepository {

    private val db = FirebaseFirestore.getInstance()
    private val user = UserData

    override fun get_MyJamsilInfoData(): MutableLiveData<ArrayList<MyInfoData>> {
        //Firebase 데이터베이스를 통해서 MyInfoData가 담긴 리스트를 반환하는 동작을 오버라이딩
        var result = MutableLiveData<ArrayList<MyInfoData>>()
        var list = ArrayList<MyInfoData>()
        db.collection("Jamsil").whereEqualTo("User",user.user?.uid.toString())
            .addSnapshotListener { value, error ->
                list.clear()
                for(doc in value!!){
                    val stadium = "Jamsil"
                    val seat = doc.get("seat").toString()
                    val area = doc.get("area").toString()
                    val date = doc.get("date").toString()
                    val imageURI = doc.get("imageURI").toString()
                    list.add(MyInfoData(imageURI, stadium, date, seat, area, doc.id))
                }

                result.postValue(list)
            }

        return result
    }
}