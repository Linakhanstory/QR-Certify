package com.example.qr_certify

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.qr_certify.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedImageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivCertificatePreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verifier Side: Start Scanner
        binding.btnScan.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        // Admin Side: Select Certificate Image
        binding.btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Admin Side: Upload and Generate
        binding.btnGenerate.setOnClickListener {
            val name = binding.etRecipientName.text.toString().trim()
            val course = binding.etCourseName.text.toString().trim()

            if (name.isEmpty() || course.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadToCloudinary(name, course)
        }
    }

    private fun uploadToCloudinary(name: String, course: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGenerate.isEnabled = false

        // MediaManager is initialized in CertifyApp
        MediaManager.get().upload(selectedImageUri)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    saveToFirestore(name, course, imageUrl)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnGenerate.isEnabled = true
                        Toast.makeText(this@MainActivity, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveToFirestore(name: String, course: String, imageUrl: String) {
        val certificateId = "CERT-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val certificate = Certificate(
            certificateId = certificateId,
            recipientName = name,
            courseName = course,
            issueDate = Timestamp.now(),
            imageUrl = imageUrl,
            isVerified = true
        )

        db.collection("certificates")
            .document(certificateId)
            .set(certificate)
            .addOnSuccessListener {
                generateQrCode(certificateId)
                binding.progressBar.visibility = View.GONE
                binding.btnGenerate.isEnabled = true
                Toast.makeText(this, "Certificate saved and QR generated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnGenerate.isEnabled = true
                Toast.makeText(this, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateQrCode(certificateId: String) {
        try {
            // Using BarcodeEncoder as requested to convert ID to Bitmap
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(certificateId, BarcodeFormat.QR_CODE, 400, 400)
            binding.ivQrCode.setImageBitmap(bitmap)
            binding.ivQrCode.visibility = View.VISIBLE
            binding.tvQrInstruction.visibility = View.VISIBLE
            binding.tvQrInstruction.text = "Certificate ID: $certificateId"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
