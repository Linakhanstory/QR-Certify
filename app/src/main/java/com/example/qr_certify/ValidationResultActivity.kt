package com.example.qr_certify

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.qr_certify.databinding.ActivityValidationResultBinding
import com.google.firebase.Timestamp
import com.google.firebase.functions.FirebaseFunctions
import java.text.SimpleDateFormat
import java.util.*

class ValidationResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityValidationResultBinding
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidationResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        functions = FirebaseFunctions.getInstance()

        val certificateId = intent.getStringExtra("CERTIFICATE_ID")
        if (certificateId != null) {
            verifyCertificate(certificateId)
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun verifyCertificate(id: String) {
        binding.tvStatus.text = "Verifying ID: $id"
        binding.progressBar.visibility = View.VISIBLE

        val data = hashMapOf("certificateId" to id)

        functions.getHttpsCallable("verifyCertificate")
            .call(data)
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                val resultData = result.data as? Map<String, Any>
                if (resultData != null && resultData["success"] == true) {
                    val certData = resultData["certificate"] as? Map<String, Any>
                    if (certData != null) {
                        displayCertificate(certData)
                    } else {
                        showError("Data format error")
                    }
                } else {
                    showError(resultData?.get("message") as? String ?: "Certificate not found")
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                showError("Verification failed: ${e.message}")
            }
    }

    private fun displayCertificate(data: Map<String, Any>) {
        binding.tvStatus.text = "VERIFIED CERTIFICATE"
        binding.tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        binding.cvDetails.visibility = View.VISIBLE

        binding.tvRecipient.text = data["recipientName"] as? String
        binding.tvCourse.text = data["courseName"] as? String
        binding.tvCertId.text = "ID: ${data["certificateId"]}"

        // Handle date
        val dateMap = data["issueDate"] as? Map<String, Any>
        val seconds = (dateMap?.get("_seconds") as? Number)?.toLong() ?: 0
        val date = Date(seconds * 1000)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvDate.text = "Issued on: ${sdf.format(date)}"

        val imageUrl = data["imageUrl"] as? String
        if (!imageUrl.isNullOrEmpty()) {
            binding.ivCertificateImage.load(imageUrl)
        }
    }

    private fun showError(message: String) {
        binding.tvStatus.text = "INVALID CERTIFICATE"
        binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
