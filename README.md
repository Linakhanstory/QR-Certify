QR-Certify is a secure, QR-based digital certificate verification system designed for internship programs. It ensures the authenticity of issued certificates by providing a seamless, real-time validation process.

🚀 Key Features
Unique QR Generation: Each issued certificate is assigned a unique, tamper-proof QR code.

Instant Verification: Public users can verify the authenticity of a certificate instantly by scanning the QR code via the app.

Secure Backend: Built with Firebase, ensuring that certificate data is protected and verifiable in real-time.

Cloud Storage: Efficiently manages certificate images using Cloudinary for fast retrieval.

🛠 Tech Stack
Language: Kotlin (Android)

Backend & Auth: Firebase Firestore, Firebase Authentication, Firebase Functions

QR Code: ZXing Library

Media Storage: Cloudinary (Unsigned Uploads)

Image Loading: Coil

Camera: CameraX API

📋 How It Works
Issue: Admins authenticate and upload certificate details and images, generating a unique, verifiable record in Firestore.

Generate: The system generates a QR code linked to that specific database entry.

Verify: Users scan the QR code; the app queries Firebase to validate the document's authenticity instantly.

⚙️ Setup Instructions
Clone the Repository: git clone https://github.com/Linakhanstory/QR-Certify.git

Configuration:

Add your google-services.json file to the app/ folder.

Add your Cloudinary cloud_name in the MyApplication.kt file.

Build: Open the project in Android Studio (Giraffe or newer recommended) and sync with Gradle.
