package com.example.hw3_b11109024

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream

class FilterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "filter_channel" // notify channel "filter_channel"

        // data input
        fun createInputData() =
            workDataOf()
    }

    override suspend fun doWork(): Result {
        // chose picture
        val inputBitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.pic_01)
        val filteredBitmap = applyBlackAndWhiteFilter(inputBitmap) // 使用黑白濾鏡

        // store processed picture
        val filteredBitmapFile = saveBitmapToFile(filteredBitmap)
        val outputData = workDataOf("filteredBitmapFilePath" to filteredBitmapFile.absolutePath) // rotatedBitmapFilePath 改為 filteredBitmapFilePath

        // notify user already processed
        sendNotification("Image Filtered", "The image has been filtered.", filteredBitmapFile.absolutePath) // Image Rotated 改為 Image Filtered

        // return output
        return Result.success(outputData)
    }

    // Filter(Black And White)
    private fun applyBlackAndWhiteFilter(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val blackAndWhiteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                val gray = (red + green + blue) / 3
                val newPixel = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
                blackAndWhiteBitmap.setPixel(x, y, newPixel)
            }
        }

        return blackAndWhiteBitmap
    }

    // store picture
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(applicationContext.filesDir, "filtered_image.jpg")
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

    // sent notify
    @SuppressLint("MissingPermission")
    private fun sendNotification(title: String, message: String, filePath: String) {
        // check Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Filter Notification"
            val descriptionText = "Notifications for image filter"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // notify to output
        val intent = Intent(applicationContext, ImageViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("imagePath", filePath)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // notify
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // sent notify
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(12345, builder.build())
        }
    }
}
