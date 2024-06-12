package com.example.hw3_b11109024

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Worker class to apply a vertical flip filter to an image.
 */
class FilterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "rotate_channel" // Notification channel ID
        fun createInputData() = workDataOf()
    }

    override suspend fun doWork(): Result {
        val imageUriString = inputData.getString("imageUri")
        val imageUri = Uri.parse(imageUriString)

        // Load Bitmap from URI
        val inputBitmap = loadBitmapFromUri(imageUri) ?: return Result.failure()

        // Apply horizontal flip
        val flippedBitmap = applyHorizontalFlip(inputBitmap)

        // Save flipped image to a file
        val flippedBitmapFile = saveBitmapToFile(flippedBitmap)

        // Send notification about the flipped image
        sendNotification("Image Flipped", "The image has been flipped horizontally.", flippedBitmapFile.absolutePath)

        return Result.success()
    }

    /**
     * Load Bitmap from URI.
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = applicationContext.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

    /**
     * Apply a horizontal flip to Bitmap.
     */
    private fun applyHorizontalFlip(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val flippedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                flippedBitmap.setPixel(width - x - 1, y, bitmap.getPixel(x, y))
            }
        }

        return flippedBitmap
    }

    /**
     * Save a Bitmap to a file in internal storage.
     */
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(applicationContext.filesDir, "rotated_image.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
    }

    /**
     * Send a notification to inform the user about the flipped image.
     */
    @SuppressLint("MissingPermission")
    private fun sendNotification(title: String, message: String, filePath: String) {
        // Create notification channel if device is running Android O or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rotate Notification"
            val descriptionText = "Notifications for image rotation"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open ImageViewActivity with flipped image path
        val intent = Intent(applicationContext, ImageViewActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("imagePath", filePath)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build notification
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Send notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(12345, builder.build())
        }
    }
}
