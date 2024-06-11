package com.example.hw3_b11109024

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnSelectImage: Button // button read gallery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // set page

        btnSelectImage = findViewById(R.id.btnSelectImage) // button

        // button's activity
        btnSelectImage.setOnClickListener {
            // (start)chose an image
            val intent = Intent(this, ImageViewActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // get picture path
            val selectedImageUri: Uri? = data.data
            val imagePath = selectedImageUri?.toString()

            // run ImageViewActivity & transport picture
            val intent = Intent(this, ImageViewActivity::class.java).apply {
                putExtra("imagePath", imagePath)
            }
            startActivity(intent)
        }
    }
}
