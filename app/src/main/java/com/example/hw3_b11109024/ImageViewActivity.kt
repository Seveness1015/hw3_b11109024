package com.example.hw3_b11109024

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import android.view.View
import android.provider.Settings

class ImageViewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnApplyFilter: Button
    private lateinit var btnReturnToHome: Button

    private var originalImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        imageView = findViewById(R.id.imageView)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        btnReturnToHome = findViewById(R.id.btnReturnToHome)

        // get path
        originalImagePath = intent.getStringExtra("imagePath")

        // load image
        originalImagePath?.let { path ->
            Glide.with(this).load(path).into(imageView)
        }

        // 設定黑白濾鏡按鈕的點擊行為
        btnApplyFilter.setOnClickListener {
            // touch button, run filter work
            val filterWork = OneTimeWorkRequestBuilder<FilterWorker>()
                .setInputData(FilterWorker.createInputData())
                .build()
            WorkManager.getInstance(applicationContext).enqueue(filterWork)
        }

        // back-to-homepage button
        btnReturnToHome.setOnClickListener {
            finish() // close page
        }
    }

    private fun hideButtonsIfNeeded() {
        originalImagePath?.let {
            btnApplyFilter.visibility = View.GONE
            btnReturnToHome.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // check permissions
        requestNotificationPermissionIfNeeded()
        // check hide or not
        hideButtonsIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        // permissions close, request permission
        if (!isNotificationPermissionGranted()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        // check permission
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        return notificationManager?.areNotificationsEnabled() ?: false
    }
}
