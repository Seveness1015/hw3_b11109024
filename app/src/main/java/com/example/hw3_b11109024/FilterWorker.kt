package com.example.hw3_b11109024

import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream

/**
 * Worker class to apply a black and white filter to an image.
 */
class FilterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Get the image URI from input data
        val imageUri = inputData.getString("imageUri")

        // Load Bitmap from URI
        val inputBitmap = imageUri?.let { uri ->
            val inputStream = applicationContext.contentResolver.openInputStream(Uri.parse(uri))
            BitmapFactory.decodeStream(inputStream)
        } ?: return Result.failure()

        // Apply black and white filter
        val filteredBitmap = applyBlackAndWhiteFilter(inputBitmap)

        // Save filtered image to a file
        val filteredBitmapFile = saveBitmapToFile(filteredBitmap)

        // Create output data with file path of the filtered image
        val outputData = workDataOf("filteredBitmapFilePath" to filteredBitmapFile.absolutePath)

        // Send notification about the filtered image
        sendNotification("Image Filtered", "The image has been filtered.", filteredBitmapFile.absolutePath)

        return Result.success(outputData)
    }

    /**
     * Apply a simple black and white filter to a Bitmap.
     */
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

    /**
     * Save a Bitmap to a file in internal storage.
     */
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(applicationContext.filesDir, "filtered_image.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

    /**
     * Send a notification to inform the user about the filtered image.
     */
    private fun sendNotification(title: String, message: String, filePath: String) {
        // Create notification channel for Android Oreo and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

        // Intent to open ImageViewActivity with the filtered image
        val intent = Intent(applicationContext, ImageViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("imagePath", filePath)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Send the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "image_filter_channel"
        private const val NOTIFICATION_ID = 12345

        /**
         * Create input data for FilterWorker.
         */
        fun createInputData(imageUri: String) =
            workDataOf("imageUri" to imageUri)
    }
}
