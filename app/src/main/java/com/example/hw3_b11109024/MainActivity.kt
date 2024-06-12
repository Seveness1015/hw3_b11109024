package com.example.hw3_b11109024

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnSelectImage: Button
    private lateinit var btnApplyFilter: Button
    private lateinit var imageView: ImageView

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        imageView = findViewById(R.id.imageView)

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnApplyFilter.setOnClickListener {
            selectedImageUri?.let { uri ->
                performImageLoading(uri)
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                imageView.setImageURI(uri)
                btnApplyFilter.isEnabled = true
            }
        }
    }

    private fun performImageLoading(imageUri: Uri) {
        val intent = Intent(this, ImageViewActivity::class.java).apply {
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }
}
