package com.example.baseballseat.Post

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import com.example.baseballseat.R
import com.example.baseballseat.UserData
import com.example.baseballseat.board.ChangWonBoardActivity
import com.example.baseballseat.databinding.ActivityCreatePostBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/*
게시물을 업로드 하기 위한 Activity 코틀린 코드
 */
class CreateChangwonPostActivity : AppCompatActivity() {
    var TAG = "CreateChangwonPostActivity"
    private lateinit var binding: ActivityCreatePostBinding
    private var area = ""//선택한 구역
    private var seat = ""//선택한 좌석
    private var username = ""//사용자 이름
    private var local = ""//구장명
    private var userdate = UserData
    private var bitmapString = ""//사진을 Byte배열로 변환한 후 String으로 변환한 문자열
    private val REQUEST_IMAGE_CAPTURE = 1001//카메라 촬영에 성공했을 때 받는 코드
    private val REQUEST_GALLERY = 1002//갤러리를 호출할 때 받는 코드
    private var storage = Firebase.storage
    private lateinit var currentPhotoPath: String//문자열 형태의 파일 경로

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_create_post)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        username = userdate.username.toString()
        local = intent.getStringExtra("local").toString()

        setCameraPermissoin()

        //구장의 좌석 이름
        val seat_list : List<String> = mutableListOf("내야석","외야잔디석","미니테이블석","달&아자부 테라스석","프리미엄테이블석","불펜석","테이블석","외야석"
                                                ,"가족석","3층테라스석","스카이박스","포크벨리 바베큐석","라운드테이블석","3.4층 내야","프리미엄석","불펜가족석","피크닉테이블석")

        //좌석 선택
        binding.seatCHSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,seat_list)
        binding.seatCHSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                seat = binding.seatCHSpinner.selectedItem.toString()
                setAreaList(seat)//좌석이 선택되면 구역 스피너를 생성하는 메서드 호출
                Log.d(TAG, "seat : ${seat}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //카메라로 이미지를 설정하려는 경우
        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        //갤러리로 이미지를 설정하려는 경우
        binding.gallaryBtn.setOnClickListener {
            takeGallry()
        }

        //업로드 버튼을 클릭한 경우
        binding.UploadBtn.setOnClickListener {
            Log.d(TAG, "area : ${area}")
            Log.d(TAG, "seat : ${seat}")
            Log.d(TAG, "contents : ${binding.ContentEt.text}")
            Log.d(TAG, "user : ${username}")

            //이미지 업로드 기본 사진일 경우 토스트메시지를 띄운다
            //그렇지 않은 경우 업로드를 실행한다.
            //코드 출처 : https://stackoverflow.com/questions/6353170/android-how-to-compare-resource-of-an-image-with-r-drawable-imagename
            if(binding.StadiumIv.drawable.constantState == this.resources.getDrawable(R.drawable.uploadimage).constantState){
                Toast.makeText(this, "이미지를 업로드해 주세요.", Toast.LENGTH_SHORT).show()
            }else if(binding.ContentEt.text.toString() == ""){//내용을 입력하지 않은 경우
                Toast.makeText(this, "글 내용을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }else{
                addDataBase()
                uploadStorage()
            }
        }

    }

    private fun uploadStorage(){
        val storageRef = storage.reference
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var datetime = sdf.format(Calendar.getInstance().time)
        val link = storageRef.child("$local/$datetime.jpg")

        //비트맵을 byte배열 반환 후 문자열로 만드는 과정
        val drawable = binding.StadiumIv.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        val byte_Array = stream.toByteArray()

        var uploadTask = link.putBytes(byte_Array)
        uploadTask.addOnFailureListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 실패")
        }.addOnSuccessListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 성공")
        }

        //이미지가 저장소에 업로드 되면 액티비티를 벗어난다
        uploadTask.addOnSuccessListener {
            finish()
        }
    }

    private fun addDataBase(){
        //Firebase에 데이터를 삽입하는 과정
        //현재날짜를 설정하는 코드
        var database = Firebase.database
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var datetime = sdf.format(Calendar.getInstance().time)
        Log.d(TAG, "sdf : ${datetime}")
        val myRef = database.getReference(local).child(datetime)

        //HashMap 자료구조로 구역, 좌석, 사진 byte 문자열, 유저명을 담는다
        var hashmap = HashMap<String, String>()
        hashmap.put("area", area)
        hashmap.put("seat", seat)
        hashmap.put("username", username)
        hashmap.put("contents", binding.ContentEt.text.toString())

        //데이터를 실질적으로 삽입하는 코드
        myRef.setValue(hashmap)
    }

    //갤러리로 향하는 인텐트 설정정
   private fun takeGallry(){
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    //카메라촬영 기능
    private fun takeCapture(){
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

    //이미지 파일 생성 과정(임시적으로)
    @Throws(IOException::class)
    private fun createImageFile(): File {
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

    //카메라 권한을 체크하는 라이브러리
    //각종 권한을 허용하는 라이브러리
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun setCameraPermissoin() {
        val permission = object : PermissionListener{
            override fun onPermissionGranted() {//권한을 사용자가 허용했을 때
                Toast.makeText(this@CreateChangwonPostActivity, "권한을 수락하셨습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//권한을 사용자가 거부했을 때
                Toast.makeText(this@CreateChangwonPostActivity, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("게시물 업로드시 관련 사진이 꼭 있어야 합니다.")
            .setDeniedMessage("카메라 권한을 거부해 게시물을 업로드 할 수 없습니다.")
            .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)
            .check()
    }

    //startActivityForResult를 거친 후 카메라 앱, 갤러리 앱으로 받아온 결과 값
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap
            val file = File(currentPhotoPath)

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
            }
        }
    }

    //카메라로 찍은 이미지를 내장 메모리에 저장
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
        Toast.makeText(this, "사진이 갤러리에 저장했습니다.",Toast.LENGTH_SHORT).show()
    }

    fun setAreaList(seat : String){
        var list : List<String> = mutableListOf("101","102","103","104","105","106","107","108","109","110","119","120","121","122","123","124","125")
        when(seat){
            "내야석" -> list = mutableListOf("101","102","103","104","105","106","107","108","109","110","119","120","121","122","123","124","125")
            "미니테이블석" -> list = mutableListOf("111","118")
            "테이블석" -> list = mutableListOf("112","113","114","115","116","117")
            "외야석" -> list = mutableListOf("130","131","132","133","134","135","136","137","138")
            "스카이박스" -> list = mutableListOf("305","306","307","308","312","313","314","315","321","322","323","324","327","328","329","330"
                                        ,"405","406","407","408","412","413","414","416","420","422","423","424","427","428","429","430")
            "포크벨리 바베큐석" -> list = mutableListOf("126","127","131","132","134","135","136")
            "라운드 테이블석" -> list = mutableListOf("201","202","203","204","205","206","207","208","209","210","211","212","213","214","215","216","217","218","219","220","221","222","223")
            "3.4층 내야" -> list = mutableListOf("301","302","303","304","309","310","311","325","326","331","332","333","401","402","403","404","409","410","411","415","417","418","419","421","425","426","431","432","433")
            "프리미엄석" -> list = mutableListOf("112","113","114")
            "피크닉테이블석" -> list = mutableListOf("101","102","103","104","122","123","124","125")
            else -> list = mutableListOf("")//구역이 따로 없는 좌석
        }

        binding.areaCHSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        binding.areaCHSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                area = binding.areaCHSpinner.selectedItem.toString()//스피너로 구역을 선택하면 area 변수 초기화
                Log.d(TAG, "area : ${area}")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
}