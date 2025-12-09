package com.intern002.locketapp.core.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.InputStream

fun initFirebaseAdmin() {
    if (FirebaseApp.getApps().isEmpty()) {
        val serviceAccount: InputStream? = 
            Thread.currentThread().contextClassLoader.getResourceAsStream("locket-app-server-firebase-adminsdk-fbsvc-dec249bb6d.json")

        if (serviceAccount == null) {
            println("Firebase Admin SDK file not found in resources. Push notifications will not work.")
            return
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)
        println("Firebase Admin SDK initialized successfully.")
    }
}
