package com.example.hw3_b11109024

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

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