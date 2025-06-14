package com.example.readeasy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val CAMERA_PERMISSION_CODE = 100

    // Camera result
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                imageView.setImageBitmap(it)
                val image = InputImage.fromBitmap(it, 0)
                processImage(image)
            }
        }
    }

    // Gallery result
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            try {
                val image = InputImage.fromFilePath(this, it)
                processImage(image)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val captureButton = findViewById<Button>(R.id.captureButton)
        val uploadButton = findViewById<Button>(R.id.uploadButton)

        // Load OpenDyslexic font
        val typeface = Typeface.createFromAsset(assets, "fonts/OpenDyslexic-Regular.ttf")
        textView.typeface = typeface

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                captureImage.launch(cameraIntent)
            }
        }

        uploadButton.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                captureImage.launch(cameraIntent)
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processImage(image: InputImage) {
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                textView.text = visionText.text
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
