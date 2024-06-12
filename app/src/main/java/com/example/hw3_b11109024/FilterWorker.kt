package com.example.hw3_b11109024

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream

/**
 * Worker 類，用於應用黑白濾鏡到圖像。
 */
class FilterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 檢查並請求權限
        if (!hasRequiredPermissions()) {
            // 如果權限未被授予，打開設置頁面讓用戶授予權限
            requestNotificationPermission()
            return Result.failure()
        }

        // 獲取輸入數據中的圖像 URI
        val imageUri = inputData.getString("imageUri")

        // 從 URI 加載 Bitmap
        val inputBitmap = imageUri?.let { uri ->
            val inputStream = applicationContext.contentResolver.openInputStream(Uri.parse(uri))
            BitmapFactory.decodeStream(inputStream)
        } ?: return Result.failure()

        // 應用黑白濾鏡
        val filteredBitmap = applyBlackAndWhiteFilter(inputBitmap)

        // 保存濾鏡處理過的圖像到文件
        val filteredBitmapFile = saveBitmapToFile(filteredBitmap)

        // 創建輸出數據，包括濾鏡處理過的圖像的文件路徑
        val outputData = workDataOf("filteredBitmapFilePath" to filteredBitmapFile.absolutePath)

        // 發送有關濾鏡處理過圖像的通知
        sendNotification("Image Filtered", "The image has been filtered.", filteredBitmapFile.absolutePath)

        return Result.success(outputData)
    }

    /**
     * 應用簡單的黑白濾鏡到 Bitmap。
     */
    private fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val grayValues = IntArray(width * height)

        // 使用加權平均值轉換為灰度圖
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF

                val gray = (0.3 * red + 0.59 * green + 0.11 * blue).toInt()
                grayValues[y * width + x] = gray
                val newPixel = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                grayBitmap.setPixel(x, y, newPixel)
            }
        }

        // 應用直方圖均衡化
        return applyHistogramEqualization(grayBitmap, grayValues)
    }

    private fun applyHistogramEqualization(bitmap: Bitmap, grayValues: IntArray): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val size = width * height

        // 計算直方圖
        val histogram = IntArray(256)
        for (gray in grayValues) {
            histogram[gray]++
        }

        // 計算累積分佈函數（CDF）
        val cdf = IntArray(256)
        cdf[0] = histogram[0]
        for (i in 1 until 256) {
            cdf[i] = cdf[i - 1] + histogram[i]
        }

        // 正規化 CDF
        val cdfMin = cdf.first { it > 0 }
        val cdfScale = 255.0 / (size - cdfMin).toDouble()
        val equalizedValues = cdf.map { ((it - cdfMin) * cdfScale).toInt() }

        // 使用均衡化的值創建新的 Bitmap
        val equalizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val gray = grayValues[y * width + x]
                val equalizedGray = equalizedValues[gray]
                val newPixel = (0xFF shl 24) or (equalizedGray shl 16) or (equalizedGray shl 8) or equalizedGray
                equalizedBitmap.setPixel(x, y, newPixel)
            }
        }

        return equalizedBitmap
    }

    /**
     * 保存 Bitmap 到內部存儲中的文件。
     */
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(applicationContext.filesDir, "filtered_image.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

    /**
     * 發送通知，告知用戶有關濾鏡處理過的圖像。
     */
    private fun sendNotification(title: String, message: String, filePath: String) {
        // 創建通知通道（適用於 Android Oreo 及更高版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Filtered Image Notification"
            val descriptionText = "Notification for filtered image"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 意圖打開帶有濾鏡處理過圖像的 ImageViewActivity
        val intent = Intent(applicationContext, ImageViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("imagePath", filePath)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 構建通知
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 發送通知，檢查並請求權限
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    /**
     * 檢查是否具有所需的權限。
     */
    private fun hasRequiredPermissions(): Boolean {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    /**
     * 請求通知權限。
     */
    private fun requestNotificationPermission() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        applicationContext.startActivity(intent)
    }

    companion object {
        private const val CHANNEL_ID = "image_filter_channel"
        private const val NOTIFICATION_ID = 12345

        /**
         * 創建 FilterWorker 的輸入數據。
         */
        fun createInputData(imageUri: String) =
            workDataOf("imageUri" to imageUri)
    }
}
