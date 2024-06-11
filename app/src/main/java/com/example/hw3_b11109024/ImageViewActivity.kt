package com.example.hw3_b11109024

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
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

        // Initialize UI elements
        imageView = findViewById(R.id.imageView)
        btnApplyFilter = findViewById(R.id.btnApplyFilter)
        btnReturnToHome = findViewById(R.id.btnReturnToHome)

        // get path(original picture)
        originalImageUri = intent.getStringExtra("imageUri")

        // load picture(original)
        originalImageUri?.let { uri ->
            // load pucture to ImageView(by Glide)
            Glide.with(this).load(uri).into(imageView)
        }

        // setting Filter-button's apply
        btnApplyFilter.setOnClickListener {
            applyFilter()
        }

        // setting home-page's button
        btnReturnToHome.setOnClickListener {
            finish()
        }
    }

    /**
     * hide button（if-need）
     */
    private fun hideButtonsIfNeeded() {
        originalImageUri?.let {
            btnApplyFilter.visibility = View.GONE
            btnReturnToHome.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        // 每次回到這個畫面時，檢查通知權限
        requestNotificationPermissionIfNeeded()
        // 檢查並隱藏按鈕（如果需要）
        hideButtonsIfNeeded()
    }

    /**
     * 檢查並請求通知權限（如果需要）
     */
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

    /**
     * 檢查通知權限是否已授權
     */
    private fun isNotificationPermissionGranted(): Boolean {
        val notificationManager =
            getSystemService(NotificationManager::class.java)
        return notificationManager?.areNotificationsEnabled() ?: false
    }

    /**
     * 開始應用圖片濾鏡
     */
    private fun applyFilter() {
        // 創建一個OneTimeWorkRequest來應用濾鏡
        val filterWork = originalImageUri?.let { FilterWorker.createInputData(it) }?.let {
            OneTimeWorkRequestBuilder<FilterWorker>()
                .setInputData(it)
                .build()
        }

        // 將工作提交到WorkManager
        filterWork?.let {
            WorkManager.getInstance(applicationContext).enqueue(it)
        }
    }

    companion object
}
