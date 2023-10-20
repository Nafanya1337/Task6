package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var URL: String
    private var ImageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            URL = binding.editTextText.text.toString()
            downloadPhoto()
        }
    }

    private fun downloadPhoto() {
        CoroutineScope(Dispatchers.IO).launch {
            val handler = Handler(Looper.getMainLooper())
            try {
                ImageBitmap = Glide.with(this@MainActivity)
                    .asBitmap()
                    .load(URL)
                    .submit()
                    .get()

                handler.post {
                    Toast.makeText(
                        this@MainActivity,
                        "Картинка успешно загружена",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.imageContainer.setImageBitmap(ImageBitmap)
                }
                saveImage()
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    Toast.makeText(
                        this@MainActivity,
                        "Картинку не удалось загрузить",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun saveImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val imageFileName = "JPEG_FILE_NAME.jpg"
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/YOUR_FOLDER_NAME"
            )

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val imageFile = File(storageDir, imageFileName)
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                ImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()

                galleryAddPic(imageFile.path)
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(this@MainActivity, "Картинка сохранена в память устройства", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(path)
            val contentUri: Uri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)
        }
    }
}
