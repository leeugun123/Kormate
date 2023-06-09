package org.techtown.kormate.Fragment

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.techtown.kormate.CurrentDateTime
import org.techtown.kormate.Fragment.Adapter.GalaryAdapter
import org.techtown.kormate.Fragment.Data.BoardDetail
import org.techtown.kormate.Fragment.ViewModel.BoardPostViewModel
import org.techtown.kormate.Fragment.ViewModel.KakaoViewModel
import org.techtown.kormate.databinding.ActivityBoardPostBinding
import java.text.SimpleDateFormat
import java.util.*

class BoardPostActivity : AppCompatActivity() {

    private var binding : ActivityBoardPostBinding? = null
    private lateinit var kakaoViewModel : KakaoViewModel
    private lateinit var boardPostViewModel: BoardPostViewModel

    private val REQUEST_CODE_PICK_IMAGES = 1
    private val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

    private var imageUris = mutableListOf<String>()

    private var adapter : GalaryAdapter? = null



    private var userName : String? = null
    private var userImg: String? = null
    private var userId : Long? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardPostBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        kakaoViewModel = ViewModelProvider(this).get(KakaoViewModel::class.java)
        boardPostViewModel = ViewModelProvider(this).get(BoardPostViewModel::class.java)

        kakaoViewModel.loadUserData()
        observeKakaoModel()
        //카카오 유저 정보 가져오기

        val postsRef = Firebase.database.reference.child("posts")
        val postId = postsRef.push().key

        binding!!.backBtn.setOnClickListener {
            finish()
        }//뒤로가기

        binding!!.uploadImgButton.setOnClickListener {

            TedPermission.create()
                .setPermissionListener(object : PermissionListener {

                    override fun onPermissionGranted() {
                        // 권한이 허용되면 갤러리에서 이미지를 선택합니다.
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                        startActivityForResult(Intent.createChooser(intent, "Select images"), REQUEST_CODE_PICK_IMAGES)
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        // 권한이 거부되면 처리합니다.
                        // ...
                    }

                })
                .setPermissions(PERMISSION_READ_EXTERNAL_STORAGE)
                .check()

        }//사진 올리기


        binding!!.updateButton.setOnClickListener {

            val post = binding!!.post.text.toString()

            if (post.isEmpty() && imageUris.isEmpty()) {
                Toast.makeText(this, "내용이 없습니다. 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }//사진하고 글 둘다 없는 경우

            if (post.isEmpty()) {
                Toast.makeText(this, "글의 내용이 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }//글의 내용이 없는 경우

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("업로드 중")
            progressDialog.setCancelable(false) // 사용자가 대화 상자를 닫을 수 없도록 설정
            progressDialog.show()

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            var picUri: MutableList<String> = mutableListOf()

            //사진이 1장 이상인 경우
            if (imageUris.size > 0) {

                val imageFileNames = mutableListOf<String>()

                for (i in 0 until imageUris.size) {

                    val imageFileName = "IMG_${
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                            Date()
                        )}_${UUID.randomUUID()}"
                    val imageRef = storageRef.child("images/$imageFileName")

                    imageRef.putFile(imageUris[i].toUri())
                        .addOnSuccessListener {
                            imageRef.downloadUrl
                                .addOnSuccessListener { uri ->

                                    Log.e("TAG",uri.toString())

                                    imageFileNames.add(uri.toString())

                                    if (imageFileNames.size == imageUris.size) {

                                        boardPostViewModel.uploadPost(postsRef, BoardDetail(postId, userId, userName, userImg, post, imageFileNames, CurrentDateTime.getPostTime()))

                                        progressDialog.dismiss()

                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }


                }


            }
            else {
                boardPostViewModel.uploadPost(postsRef, BoardDetail(postId, userId, userName, userImg, post, picUri, CurrentDateTime.getPostTime()))
                progressDialog.dismiss()

            }//글만 올리는 경우


        }

        boardPostViewModel.postLiveData.observe(this) { success ->

            if (success) {
                complete()
            }

        }


    }

    private fun observeKakaoModel() {

        kakaoViewModel.userName.observe(this) { userName ->
            this.userName = userName
        }

        kakaoViewModel.userProfileImageUrl.observe(this) { imageUrl ->
            this.userImg = imageUrl
        }

        kakaoViewModel.userId.observe(this){ userId ->
            this.userId = userId
        }


    }

    private fun complete(){

        finish()
        Toast.makeText(this, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()

    }


    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == Activity.RESULT_OK) {

            if (data?.clipData != null) {
                // 다중 이미지를 선택한 경우
                val clipData = data.clipData

                if (clipData != null) {

                    for (i in 0 until clipData.itemCount) {

                        if(imageUris.size == 3){
                            Toast.makeText(this, "사진은 최대 3장까지 업로드 가능합니다.", Toast.LENGTH_SHORT).show()
                            break
                        }

                        //사진 개수 제한

                        val uri = clipData.getItemAt(i).uri
                        imageUris.add(uri.toString())

                    }
                }

            }

            //정상적으로 사진을 골랐을때
            binding!!.uploadImgButton.text = "사진 올리기(" + imageUris.size.toString() + "/3)"
            handleSelectedImages(imageUris, binding!!)


        }

    }//갤러리로 이동했을때

    private fun handleSelectedImages(imageUris: MutableList<String>, acBinding : ActivityBoardPostBinding) {

        adapter = GalaryAdapter(imageUris,acBinding)
        adapter!!.notifyDataSetChanged()

        binding!!.uploadImgButton.text = "사진 올리기(" + imageUris.size.toString() + "/3)"


        binding!!.ImgRecyclerView.layoutManager = GridLayoutManager(this,3)
        binding!!.ImgRecyclerView.adapter = adapter


    }// 선택한 이미지들을 처리하는 코드를 작성




}