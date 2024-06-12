package com.example.hw3_b11109024

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide

class ImageViewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnApplyFilter: Button
    private lateinit var btnReturnToHome: Button

    private var originalImageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        imageView = findViewById(R.id.imageView)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        btnReturnToHome = findViewById(R.id.btnReturnToHome)

        originalImageUri = intent.getStringExtra("imageUri")
        val imagePath = intent.getStringExtra("imagePath") // Get flipped image path

        // Load original or flipped image
        if (imagePath != null) {
            Glide.with(this).load(imagePath).into(imageView)
        } else {
            originalImageUri?.let { uri ->
                Glide.with(this).load(uri).into(imageView)
            }
        }

        btnApplyFilter.setOnClickListener {
            applyFilter()
        }

        btnReturnToHome.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        requestNotificationPermissionIfNeeded()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestNotificationPermissionIfNeeded() {
        if (!isNotificationPermissionGranted()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        val notificationManager = getSystemService(NotificationManager::class.java)
        return notificationManager?.areNotificationsEnabled() ?: false
    }

    private fun applyFilter() {
        val filterWork = FilterWorker.createInputData()

        originalImageUri?.let {
            val workRequest = OneTimeWorkRequestBuilder<FilterWorker>()
                .setInputData(filterWork)
                .build()

            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }
    }

}
