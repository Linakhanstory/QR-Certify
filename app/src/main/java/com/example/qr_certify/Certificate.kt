package com.example.qr_certify

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Certificate(
    val certificateId: String? = null,
    val recipientName: String? = null,
    val courseName: String? = null,
    val issueDate: Timestamp? = null,
    val imageUrl: String? = null,
    val isVerified: Boolean = true
)
