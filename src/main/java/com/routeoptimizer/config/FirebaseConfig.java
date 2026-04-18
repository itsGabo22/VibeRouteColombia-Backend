package com.routeoptimizer.config;
 
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
 
/**
 * Configuration to initialize Firebase Admin SDK.
 * Note: Requires serviceAccountKey.json in the project root or specified path.
 */
@Configuration
public class FirebaseConfig {
 
    @PostConstruct
    public void initialize() {
        try {
            // Path to your service account key file
            FileInputStream serviceAccount =
                new FileInputStream("serviceAccountKey.json");
 
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                // Bucket name will be set in properties, but can be set here too
                .build();
 
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            // Log error but don't prevent app startup unless strictly necessary
            System.err.println("Firebase could not be initialized. Manifest uploads will fail. " + e.getMessage());
        }
    }
}
