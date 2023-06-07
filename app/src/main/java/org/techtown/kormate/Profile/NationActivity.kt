package org.techtown.kormate.Profile

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import org.techtown.kormate.LoginActivity
import org.techtown.kormate.MainActivity
import org.techtown.kormate.R
import org.techtown.kormate.databinding.ActivityNationBinding


class NationActivity : AppCompatActivity() {

    private var binding : ActivityNationBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        var nation : String = ""

        binding!!.backBtn.setOnClickListener {
            finish()
        }

        binding!!.radioGroup.setOnCheckedChangeListener { group, checkId ->

            when(checkId) {

                binding!!.radio1.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    nation = "한국"
                    Log.e(TAG,"한국 선택")

                }//한국

                binding!!.radioButton2.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    nation = "중국"

                }//중국

                binding!!.radioButton3.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    nation = "베트남"

                }//베트남

                binding!!.radioButton4.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    nation = "몽골"

                }//몽골

                binding!!.radioButton5.id -> {

                    binding!!.checkButton.setBackgroundResource(R.color.blue)
                    nation = "우즈벡"

                }//우즈벡

            }

        }

        binding!!.checkButton.setOnClickListener {

            if(nation == ""){
                Toast.makeText(this,"국가를 체크해주세요",Toast.LENGTH_SHORT).show()
            }
            else{

                //서버 입력 작업 처리
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }


        }


    }

}