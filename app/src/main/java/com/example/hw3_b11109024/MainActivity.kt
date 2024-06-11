package com.example.hw3_b11109024

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenGallery: Button // button read gallery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // set page

        btnOpenGallery = findViewById(R.id.btnOpenGallery) //button

        // button's acvtivity
        btnOpenGallery.setOnClickListener {
            // (start)open a image
            val intent = Intent(this, ImageViewActivity::class.java)
            startActivity(intent)
        }
    }
}

// 1.定義了一個變數 btnOpenGallery 來表示開啟相簿的按鈕。
// 2.在 onCreate 方法中，設定這個活動的畫面是 activity_main。
// 3.使用 findViewById 找到佈局中的按鈕 btnOpenGallery。
// 4.設置按鈕的點擊行為：當按鈕被按下時，建立一個意圖來打開 ImageViewActivity，然後啟動這個活動。
