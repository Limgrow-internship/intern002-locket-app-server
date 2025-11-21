package com.intern002.locketapp.core.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.github.cdimascio.dotenv.dotenv
import java.io.ByteArrayInputStream
import java.util.Base64

fun initFirebaseAdmin() {
    val env = dotenv {
        ignoreIfMissing = true
    }

    val firebaseServiceAccount = env["FIREBASE_SERVICE_ACCOUNT_BASE64"]
        ?: System.getenv("FIREBASE_SERVICE_ACCOUNT_BASE64")
        ?: error("FIREBASE_SERVICE_ACCOUNT_BASE64 environment variable not found.")

    val decodedServiceAccount = Base64.getDecoder().decode(firebaseServiceAccount)

    val serviceAccountStream = ByteArrayInputStream(decodedServiceAccount)

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }
}
