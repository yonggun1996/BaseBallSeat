package com.example.baseballseat.DataUpdate

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.baseballseat.R
import com.example.baseballseat.databinding.ActivityCreatePostBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/*
잠실야구장 수정 페이지
 */

class UpdateJamsilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private val REQUEST_IMAGE_CAPTURE = 1001//카메라 촬영에 성공했을 때 받는 코드
    private val REQUEST_GALLERY = 1002//갤러리를 호출할 때 받는 코드
    private lateinit var currentPhotoPath: String//문자열 형태의 파일 경로
    private var photoURI : Uri? = null//전역변수로 이미지의 uri를 담을 변수를 선언
    var area = ""
    var seat = ""
    var imageURI = ""
    var username = ""
    var user = ""
    private var storage = Firebase.storage
    private val db = Firebase.firestore
    val SUCESS = 9999
    val TAG = "UpdateJamsilActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_update_jamsil)

        setCameraPermissoin()//퍼미션을 확인하는 메서드 호출
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent
        val map = intent.getSerializableExtra("map") as HashMap<String, String>//내 정보 페이지에서 intent를 통해 받아온 map

        var seat = map["seat"].toString()//좌석명
        area = map["area"].toString()//구역명
        var contents = map["contents"].toString()//텍스트 내용
        imageURI = map["imageURI"].toString()//이미지 저장소 URI
        username = map["username"].toString()//유저 이름
        user = map["User"].toString()
        val doc = intent.getStringExtra("doc").toString()//update할 문서의 document
        val local = intent.getStringExtra("local").toString()//update할 문서
        photoURI = imageURI.toUri()

        val seat_list : List<String> = listOf("프리미엄석","블루석","네이비석","1루 테이블석","3루 테이블석","오렌지석","익사이팅석","레드석","외야그린석")
        Log.d(TAG, "seat index : ${seat_list.indexOf(seat)}")

        //좌석 스피너 초기 세팅
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

        //EditText 초기 세팅
        binding.ContentEt.setText(contents)

        //imageview 초기 세팅
        Glide.with(this)
            .load(imageURI)
            .into(binding.StadiumIv)

        //카메라로 이미지를 설정하려는 경우
        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        //갤러리로 이미지를 설정하려는 경우
        binding.gallaryBtn.setOnClickListener {
            takeGallry()
        }

        //업로드 이미지를 클릭한 경우
        binding.UploadBtn.setOnClickListener {
            Log.d(TAG, "area : ${area}")
            Log.d(TAG, "seat : ${seat}")
            Log.d(TAG, "contents : ${binding.ContentEt.text}")
            Log.d(TAG, "user : ${username}")

            if(binding.ContentEt.text.toString() == ""){//내용을 입력하지 않은 경우
                Toast.makeText(this, "글 내용을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }else{
                binding.postprogressBar.visibility = View.VISIBLE
                var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var datetime = sdf.format(Calendar.getInstance().time)
                if(photoURI.toString() != imageURI){//이미지를 변경했으면 저장소에 새로운 이미지를 업로드한다
                    uploadStorage(local, doc, datetime)//이미지를 저장소에 업로드 성공 여부를 담는 변수
                }else{//이미지를 변경하지 않았으면 바로 DB에 업로드 한다
                    uploadDB(local, doc, datetime)
                }
            }
        }

    }//OnCreate()

    //저장소에 이미지를 업로드 하는 메소드
    private fun uploadStorage(local: String, doc: String, datetime: String){
        val storageRef = storage.reference
        val link = storageRef.child("$local/$datetime.jpg")

        var uploadTask = link.putFile(photoURI!!)
        uploadTask.addOnFailureListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 실패")
        }.addOnSuccessListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 성공")
            link.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "uploadStorage: $it")
                imageURI = it.toString()//이미지 URI 변경
                //저장소에 업로드 했으면 DB에 업로드
                uploadDB(local, doc, datetime)
            }
        }

    }

    //데이터베이스에 데이터를 수정하고 해당 액티비티를 빠져 나오는 메서드
    private fun uploadDB(local: String, doc: String, datetime: String){
        var hashmap = HashMap<String, String>()
        hashmap.put("area", binding.areaSpinner.selectedItem.toString())
        hashmap.put("seat", binding.seatSpinner.selectedItem.toString())
        hashmap.put("username", username)
        hashmap.put("User", user)
        hashmap.put("contents", binding.ContentEt.text.toString())
        hashmap.put("date", datetime)
        hashmap.put("imageURI", imageURI)

        Log.d(TAG, "imageURI: ${imageURI}")
        //Cloud FireStore에 수정
        db.collection(local).document(doc)
            .set(hashmap)
            .addOnSuccessListener {
                Log.d(TAG, "DB 수정 성공")
                binding.postprogressBar.visibility = View.INVISIBLE
                var intent = Intent()
                setResult(SUCESS, intent)
                finish()
            }.addOnFailureListener {
                binding.postprogressBar.visibility = View.INVISIBLE
                Log.d(TAG, "DB 수정 실패")
                Toast.makeText(this, "DB에 수정 실패", Toast.LENGTH_SHORT).show()
            }
    }

    //갤러리를 살펴보틑 코드
    private fun takeGallry() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun takeCapture() {
        //기본 카메라 앱 실행
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.d(TAG, "${ex}")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.baseballseat.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)//사진 결과물을 다시 가져와야 하기 때문에 startActivityForResult 사용
                }
            }
        }
    }

    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())//날짜의 형태로 저장하기 위해 설정
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!//변수가 null일 수 있기에 Nullable check
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Log.d(TAG, "이미지 경로 생성 : ${currentPhotoPath}")
        }
    }

    //카메라로 찍은 이미지를 내장 메모리에 저장
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

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

    //startActivityForResult를 거친 후 카메라 앱, 갤러리 앱으로 받아온 결과 값
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap
            val file = File(currentPhotoPath)
            photoURI = file.toUri()

            if (Build.VERSION.SDK_INT < 28) {//안드로이드 9.0보다 낮은 경우
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                binding.StadiumIv.setImageBitmap(bitmap)
            } else {
                val decode = ImageDecoder.createSource(this.contentResolver, Uri.fromFile(file))
                bitmap = ImageDecoder.decodeBitmap(decode)
                binding.StadiumIv.setImageBitmap(bitmap)
            }
            Log.d(TAG, "Bitmap : ${bitmap}")
            galleryAddPic()
        }else if(requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK){
            Log.d(TAG, "갤러리 오픈 코드 : ${REQUEST_GALLERY}")
            data?.data.let { uri ->
                binding.StadiumIv.setImageURI(uri)
                photoURI = uri
            }
        }
    }

    //카메라 권한을 체크하는 라이브러리
    //각종 권한을 허용하는 라이브러리
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun setCameraPermissoin() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//권한을 사용자가 허용했을 때
                Toast.makeText(this@UpdateJamsilActivity, "권한을 수락하셨습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//권한을 사용자가 거부했을 때
                Toast.makeText(this@UpdateJamsilActivity, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("게시물 업로드시 관련 사진이 꼭 있어야 합니다.")
            .setDeniedMessage("카메라 권한을 거부해 게시물을 업로드 할 수 없습니다.")
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }
}