package com.healthytom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase Configuration
 * Initializes Firebase Admin SDK with Firestore and Storage
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:#{null}}")
    private String firebaseCredentialsPath;

    @Value("${firebase.database-url:#{null}}")
    private String databaseUrl;

    @Value("${firebase.project-id:#{null}}")
    private String projectId;

    /**
     * Initialize Firebase Admin SDK
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        logger.info("Initializing Firebase Admin SDK...");

        if (FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials;

            if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isEmpty()) {
                // Load credentials from file
                credentials = GoogleCredentials.fromStream(
                        new FileInputStream(firebaseCredentialsPath)
                );
                logger.info("Firebase credentials loaded from: {}", firebaseCredentialsPath);
            } else {
                // Use Application Default Credentials
                credentials = GoogleCredentials.getApplicationDefault();
                logger.info("Firebase credentials loaded from Application Default Credentials");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setDatabaseUrl(databaseUrl)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("Firebase Admin SDK initialized successfully");
        }

        return FirebaseApp.getInstance();
    }

    /**
     * Firestore database bean
     */
    @Bean
    public Firestore firestore() throws IOException {
        firebaseApp(); // Ensure Firebase is initialized
        return FirestoreClient.getFirestore();
    }
}
