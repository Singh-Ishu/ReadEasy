package com.example.readeasy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private lateinit var tts: TextToSpeech

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val CAMERA_PERMISSION_CODE = 100
    private var isSpeaking = false
    private var isTranslated = false
    private var isReadabilityMode = false
    private var originalText: String = ""
    private var translatedText: String = ""

    private val captureImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val preprocessed = preprocessBitmap(bitmap)
            imageView.setImageBitmap(preprocessed)
            processImage(InputImage.fromBitmap(preprocessed, 0))
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageView.setImageURI(it)
            try {
                val image = InputImage.fromFilePath(this, it)
                processImage(image)
            } catch (e: Exception) {
                showToast("Failed to load image: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        textView.typeface = Typeface.createFromAsset(assets, "fonts/OpenDyslexic-Regular.ttf")

        initializeTextToSpeech()
        setupButtons()
    }

    private fun initializeTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            } else {
                showToast("TTS initialization failed")
            }
        }
    }

    private fun setupButtons() {
        val captureButton = findViewById<Button>(R.id.captureButton)
        val uploadButton = findViewById<Button>(R.id.uploadButton)
        val speakButton = findViewById<ImageButton>(R.id.speakButton)
        val translateButton = findViewById<ImageButton>(R.id.translateButton)
        val readabilityButton = findViewById<Button>(R.id.readabilityToggleButton)
        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        val historyButton = findViewById<ImageButton>(R.id.historyButton)

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

        speakButton.setOnClickListener {
            val text = textView.text.toString()
            if (text.isNotBlank()) {
                if (isSpeaking) {
                    tts.stop()
                    isSpeaking = false
                } else {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    isSpeaking = true
                }
            }
        }

        translateButton.setOnClickListener {
            if (textView.text.isBlank()) {
                showToast("No text to translate")
                return@setOnClickListener
            }

            if (!isTranslated) {
                originalText = textView.text.toString()
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.HINDI)
                    .build()

                val translator = Translation.getClient(options)
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(originalText)
                            .addOnSuccessListener { result ->
                                translatedText = result
                                textView.text = translatedText
                                isTranslated = true
                            }
                            .addOnFailureListener { e ->
                                showToast("Translation failed: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        showToast("Model download failed: ${e.message}")
                    }
            } else {
                textView.text = originalText
                isTranslated = false
            }
        }

        readabilityButton.setOnClickListener {
            textView.textSize = if (!isReadabilityMode) 24f else 18f
            isReadabilityMode = !isReadabilityMode
        }

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "saved_texts_db").build()

        saveButton.setOnClickListener {
            val content = textView.text.toString()
            if (content.isNotBlank()) {
                lifecycleScope.launch {
                    db.savedTextDao().insert(SavedText(content = content))
                    runOnUiThread {
                        showToast("Saved!")
                    }
                }
            }
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, SavedTextsActivity::class.java))
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
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()

        val grayscaleMatrix = ColorMatrix().apply { setSaturation(0f) }
        val contrast = 1.5f
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        grayscaleMatrix.postConcat(contrastMatrix)

        paint.colorFilter = ColorMatrixColorFilter(grayscaleMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun processImage(image: InputImage) {
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                textView.text = visionText.text
            }
            .addOnFailureListener { e ->
                showToast("Text recognition failed: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            dispatchTakePictureIntent()
        } else {
            showToast("Camera permission denied")
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
