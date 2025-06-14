package com.example.readeasy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.speech.tts.TextToSpeech
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import java.util.Locale
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var tts: TextToSpeech
    private var isSpeaking = false

    // Handle camera result
    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val rawBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            if (rawBitmap != null) {
                val preprocessed = preprocessBitmap(rawBitmap)
                imageView.setImageBitmap(preprocessed)
                val image = InputImage.fromBitmap(preprocessed, 0)
                processImage(image)
            } else {
                Toast.makeText(this, "Failed to load captured image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle gallery selection
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

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US  // or use Locale.UK, Locale("en", "IN"), etc.
            } else {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        val speakButton = findViewById<Button>(R.id.speakButton)
        speakButton.setOnClickListener {
            val text = textView.text.toString()
            if (text.isNotEmpty()) {
                if (isSpeaking) {
                    tts.stop()
                    isSpeaking = false
                    speakButton.text = getString(R.string.speak)
                } else {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    isSpeaking = true
                    speakButton.text = getString(R.string.stop)
                }
            }
        }


        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val captureButton = findViewById<Button>(R.id.captureButton)
        val uploadButton = findViewById<Button>(R.id.uploadButton)

        // Load dyslexia-friendly font
        val typeface = Typeface.createFromAsset(assets, "fonts/OpenDyslexic-Regular.ttf")
        textView.typeface = typeface

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                dispatchTakePictureIntent()
            }
        }

        uploadButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        val translateButton = findViewById<Button>(R.id.translateButton)
        translateButton.setOnClickListener {
            val originalText = textView.text.toString()

            if (originalText.isNotBlank()) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.HINDI) // or any other language code
                    .build()

                val translator = Translation.getClient(options)

                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(originalText)
                            .addOnSuccessListener { translatedText ->
                                textView.text = translatedText
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Translation failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Model download failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "No text to translate", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun dispatchTakePictureIntent() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = externalCacheDir
        photoFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        captureImage.launch(takePictureIntent)
    }

    private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(result)
        val paint = android.graphics.Paint()
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f) // Grayscale

        // Increase contrast
        val contrast = 1.5f
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return result
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

}
