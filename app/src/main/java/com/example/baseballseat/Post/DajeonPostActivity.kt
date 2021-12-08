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

class DajeonPostActivity : AppCompatActivity() {

    private val TAG = "DajeonPostActivity"
    val SUCESS = 9999
    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var currentPhotoPath: String//문자열 형태의 파일 경로
    private val REQUEST_IMAGE_CAPTURE = 1001//카메라 촬영에 성공했을 때 받는 코드
    private val REQUEST_GALLERY = 1002//갤러리를 호출할 때 받는 코드
    private var photoURI : Uri? = null//전역변수로 이미지의 uri를 담을 변수를 선언
    private var local = ""
    private var seat = ""
    private var area = ""
    private var storage = Firebase.storage
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_dajeon_post)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        local = intent.getStringExtra("local").toString()

        val seat_list : List<String> = mutableListOf("한화다이렉트존","중앙탁자석","내야탁자석","중앙가족석","VIP커플석","덕아웃지정석",
                                                    "미니박스","내야지정석(1층)","내야응원석","내야커플석","내야하단탁자석","익사이팅석","익사이팅커플석","스카이박스",
                                                    "내야지정석(3층)","1루외야커플석","3루외야불펜지정석","외야지정석","잔디석","외야FIELDBOX","외야라운지석")

        setCameraPermissoin()

        binding.seatSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, seat_list)
        binding.seatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                seat = binding.seatSpinner.selectedItem.toString()
                setAreaList()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        binding.gallaryBtn.setOnClickListener {
            takeGallry()
        }

        binding.UploadBtn.setOnClickListener {
            binding.postprogressBar.visibility = View.VISIBLE
            upload()
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

    //이미지 파일 생성 과정(임시적으로)
    @Throws(IOException::class)
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

    //카메라 권한을 체크하는 라이브러리
    //각종 권한을 허용하는 라이브러리
    //출처 : https://github.com/ParkSangGwon/TedPermission
    private fun setCameraPermissoin() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {//권한을 사용자가 허용했을 때
                Toast.makeText(this@DajeonPostActivity, "권한을 수락하셨습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {//권한을 사용자가 거부했을 때
                Toast.makeText(this@DajeonPostActivity, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
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

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
        Toast.makeText(this, "사진이 갤러리에 저장했습니다.",Toast.LENGTH_SHORT).show()
    }

    private fun setAreaList() {
        var list : List<String> = mutableListOf()
        when(seat){
            "한화다이렉트존" -> list = mutableListOf("S01","S02","S03","S04","S05")
            "중앙탁자석" -> list = mutableListOf("A01","A02","A03","A04","A05")
            "내야탁자석" -> list = mutableListOf("1루","3루")
            "중앙가족석" -> list = mutableListOf("A06","A07")
            "VIP커플석" -> list = mutableListOf("1루","3루")
            "덕아웃지정석" -> list = mutableListOf("1루","3루")
            "미니박스" -> list = mutableListOf("M01","M02","M03","M04","M05","M06","M07","M08","M09","M10")
            "내야지정석(1층)" -> list = mutableListOf("101","102","103","104","105","111","112","117","118","119","120","301","302","303",
                                                    "304","305","311","312","313","314","315","316","317","318","319","320")
            "내야응원석" -> list = mutableListOf("113","114","115","116")
            "내야커플석" -> list = mutableListOf("112","113","114","115","116","117","312","313","314","315","316","317")
            "익사이팅석" -> list = mutableListOf("106","107","108","109","306","307","308","309")
            "스카이박스" -> list = mutableListOf("SKY1","SKY2","SKY3","SKY4","SKY5","SKY6","SKY7","SKY8","SKY9","SKY10","SKY11")
            "내야지정석(3층)" -> list = mutableListOf("201","202","203","204","205","206","207","208","209","401","402","403","404","405","406","407","408","409")
            "1루외야커플석" -> list = mutableListOf("122T","123T","124T","125T","126T","127T")
            "외야지정석" -> list = mutableListOf("121","122","123","124","125","126","127","321","322","323","324","325")
            "잔디석" -> list = mutableListOf("128","326")
            "외야FIELDBOX" -> list = mutableListOf("FIELD01","FIELD02","FIELD03","FIELD04","FIELD05","FIELD06","FIELD07")
            "익사이팅커플석" -> list = mutableListOf("1루","3루")
        }

        binding.areaSpinner.adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)
        binding.areaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                area = binding.areaSpinner.selectedItem.toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }
}