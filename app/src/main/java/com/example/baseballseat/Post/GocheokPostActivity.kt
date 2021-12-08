package com.example.baseballseat.Post

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
import com.example.baseballseat.R
import com.example.baseballseat.UserData
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

class GocheokPostActivity : AppCompatActivity() {

    private val TAG = "GocheokPostActivity"
    val SUCESS = 9999
    val FAIL = 9998
    private lateinit var binding: ActivityCreatePostBinding
    private val REQUEST_IMAGE_CAPTURE = 1001//카메라 촬영에 성공했을 때 받는 코드
    private val REQUEST_GALLERY = 1002//갤러리를 호출할 때 받는 코드
    private var seat = ""
    private var area = ""
    private var local = ""
    private var photoURI : Uri? = null//전역변수로 이미지의 uri를 담을 변수를 선언
    private var storage = Firebase.storage
    private val db = Firebase.firestore
    private lateinit var currentPhotoPath: String//문자열 형태의 파일 경로

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_gocheok_post)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        local = intent.getStringExtra("local").toString()
        setCameraPermissoin()

        val seat_list : List<String> = mutableListOf("로얄다이아몬드클럽","스카이박스","1층 테이블석","2층 테이블석","내야커플석","외야커플석","다크버건디석","버건디석","3층지정석",
                                                        "4층지정석","외야일반석")

        binding.seatSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, seat_list)
        binding.seatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                seat = binding.seatSpinner.selectedItem.toString()
                setAreaList(seat)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        binding.gallaryBtn.setOnClickListener {
            takeGallry()
        }

        binding.UploadBtn.setOnClickListener {
            Log.d(TAG, "area : ${area}")
            Log.d(TAG, "seat : ${seat}")
            Log.d(TAG, "contents : ${binding.ContentEt.text}")
            Log.d(TAG, "user : ${UserData.user?.displayName}")

            //이미지 업로드 기본 사진일 경우 토스트메시지를 띄운다
            //그렇지 않은 경우 업로드를 실행한다.
            //코드 출처 : https://stackoverflow.com/questions/6353170/android-how-to-compare-resource-of-an-image-with-r-drawable-imagename
            if(binding.StadiumIv.drawable.constantState == this.resources.getDrawable(R.drawable.uploadimage).constantState){
                Toast.makeText(this, "이미지를 업로드해 주세요.", Toast.LENGTH_SHORT).show()
            }else if(binding.ContentEt.text.toString() == ""){//내용을 입력하지 않은 경우
                Toast.makeText(this, "글 내용을 작성해 주세요.", Toast.LENGTH_SHORT).show()
            }else{
                binding.postprogressBar.visibility = View.VISIBLE
                upload()
            }
        }
    }

    private fun upload() {
        val storageRef = storage.reference
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var datetime = sdf.format(Calendar.getInstance().time)
        val link = storageRef.child("$local/$datetime.jpg")

        var uploadTask = link.putFile(photoURI!!)
        uploadTask.addOnFailureListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 실패")
        }.addOnSuccessListener {
            Log.d(TAG, "uploadStorage: 이미지 업로드 성공")
            link.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "uploadStorage: $it")
                //Firebase에 데이터를 삽입하는 과정
                //현재날짜를 설정하는 코드
                //HashMap 자료구조로 구역, 좌석, 사진 byte 문자열, 유저명을 담는다
                var hashmap = HashMap<String, String>()
                hashmap.put("area", area)
                hashmap.put("seat", seat)
                hashmap.put("username", UserData.user?.displayName.toString())
                hashmap.put("User", UserData.user?.uid.toString())
                hashmap.put("contents", binding.ContentEt.text.toString())
                hashmap.put("date", datetime)
                hashmap.put("imageURI", it.toString())

                //Cloud FireStore에 저장
                db.collection(local).document()
                    .set(hashmap)
                    .addOnSuccessListener {
                        Log.d(TAG, "DB 업로드 성공")
                        binding.postprogressBar.visibility = View.INVISIBLE
                        var intent = Intent()
                        setResult(SUCESS, intent)
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "DB에 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        //이미지가 저장소에 업로드 되면 액티비티를 벗어난다
        uploadTask.addOnSuccessListener {
            binding.postprogressBar.visibility = View.INVISIBLE
            finish()
        }
    }

    //갤러리로 향하는 인텐트 설정정
    private fun takeGallry(){
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

    //카메라로 찍은 이미지를 내장 메모리에 저장
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
        Toast.makeText(this, "사진이 갤러리에 저장했습니다.",Toast.LENGTH_SHORT).show()
    }

    //카메라 권한을 체크하는 라이브러리
    //각종 권한을 허용하는 라이브러리
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun setCameraPermissoin() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//권한을 사용자가 허용했을 때
                Toast.makeText(this@GocheokPostActivity, "권한을 수락하셨습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//권한을 사용자가 거부했을 때
                Toast.makeText(this@GocheokPostActivity, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
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

    fun setAreaList(seat : String){
        var list : List<String> = mutableListOf()
        when(seat){
            "로얄다이아몬드클럽" -> list = mutableListOf("D01","D02","D03","D04","D05","D06","D07")
            "스카이박스" -> list = mutableListOf("S01","S02","S03","S04","S05","S06","S07","S08","S09","S10","S11","S12","S13","S14","S15","S16")
            "1층 테이블석" -> list = mutableListOf("T01","T02","T03","T04","T05","T06","T07")
            "2층 테이블석" -> list = mutableListOf("T11","T12","T13","T15","T16","T17")
            "다크버건디석" -> list = mutableListOf("105","106","108","109","204","205","206","207")
            "버건디석" -> list = mutableListOf("101","102","103","104","105","110","111","112","113","114","201","202","203","208","209","210")
            "3층지정석" -> list = mutableListOf("301","302","303","304","305","306","307","308","309","310","311","312","313","314","315","316","317","318","319","320","321","322")
            "4층지정석" -> list = mutableListOf("401","402","403","404","405","406","407","408","409","410","411","412",
                                                "413","414","415","416","417","418","419","420","421","422","423","424")
            "외야일반석" -> list = mutableListOf("115","116","117","118","119","120","121","122","123","124","125","126","127","128","129","130","131","132",
                                                "211","212","213","214","215","216","217","218","219","220","221","222",
                                                "323","324","325","326","327","328","329","330","331","332","333","334",
                                                "425","426","427","428","429","430","431","432","433","434","435")
        }

        binding.areaSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
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