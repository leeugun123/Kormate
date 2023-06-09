package org.techtown.kormate.Profile

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.kakao.sdk.user.UserApiClient
import org.techtown.kormate.Fragment.Data.UserIntel
import org.techtown.kormate.Fragment.ViewModel.KakaoViewModel
import org.techtown.kormate.MainActivity
import org.techtown.kormate.R
import org.techtown.kormate.databinding.ActivityGenderBinding



class GenderActivity : AppCompatActivity() {

    private var binding : ActivityGenderBinding? = null

    private lateinit var kakaoViewModel : KakaoViewModel
    private lateinit var userId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        kakaoViewModel = ViewModelProvider(this).get(KakaoViewModel::class.java)
        kakaoViewModel.loadUserData()

        kakaoViewModel.userId.observe(this){ userId ->
            this.userId = userId.toString()
        }



        var receivedIntel  = intent.getParcelableExtra<UserIntel>("userIntel")

        var gender : String = ""

        binding!!.radioGroup.setOnCheckedChangeListener { group, checkId ->

            when(checkId) {

                binding!!.maleButton.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    gender = "남성"

                }//남성

                binding!!.FemaleButton.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    gender = "여성"

                }//여성

            }

        }

        binding!!.checkButton.setOnClickListener {

            if(gender == ""){
                Toast.makeText(this,"성별을 체크해주세요", Toast.LENGTH_SHORT).show()
            }
            else{

                Toast.makeText(this,"정보가 입력되었습니다.", Toast.LENGTH_SHORT).show()

                receivedIntel!!.gender = gender

                val intent = Intent(this, MainActivity::class.java)

                writeIntelFirebase(receivedIntel, userId)
                //파이베이스에 데이터 올리기


                startActivity(intent)
                finish()



            }


        }


    }


    fun writeIntelFirebase(userIntel: UserIntel , userId: String) {

        // Firebase Realtime Database의 레퍼런스를 가져옵니다.
        val database = FirebaseDatabase.getInstance()
        val reference = database.reference.child("usersIntel").child(userId)

        // UserIntel 객체를 Firebase에 저장합니다.
        reference.setValue(userIntel)

            .addOnSuccessListener {
                Toast.makeText(this, "정보가 입력되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {

            }


    }

}