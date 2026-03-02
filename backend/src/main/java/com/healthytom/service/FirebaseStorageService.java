package com.healthytom.service;

import com.google.firebase.cloud.StorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * Firebase Storage Service
 * Handles file uploads and downloads from Firebase Cloud Storage
 */
@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);

    private final String storageBucket;

    public FirebaseStorageService() {
        // Will be properly configured through environment
        this.storageBucket = System.getenv("FIREBASE_STORAGE_BUCKET");
    }

    /**
     * Upload a file to Firebase Storage
     */
    public String uploadFile(String fileName, byte[] fileContent, String contentType) {
        try {
            var bucket = StorageClient.getInstance().bucket(storageBucket);
            var blob = bucket.create(fileName, fileContent, contentType);
            
            String publicUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(),
                    blob.getName()
            );
            
            logger.info("File uploaded successfully: {}", fileName);
            return publicUrl;
        } catch (Exception e) {
            logger.error("Error uploading file: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to Firebase Storage", e);
        }
    }

    /**
     * Download a file from Firebase Storage
     */
    public byte[] downloadFile(String fileName) {
        try {
            var bucket = StorageClient.getInstance().bucket(storageBucket);
            var blob = bucket.get(fileName);
            
            if (blob == null) {
                logger.warn("File not found in Firebase Storage: {}", fileName);
                return new byte[0];
            }
            
            logger.info("File downloaded successfully: {}", fileName);
            return blob.getContent();
        } catch (Exception e) {
            logger.error("Error downloading file: {}", fileName, e);
            throw new RuntimeException("Failed to download file from Firebase Storage", e);
        }
    }

    /**
     * Delete a file from Firebase Storage
     */
    public void deleteFile(String fileName) {
        try {
            var bucket = StorageClient.getInstance().bucket(storageBucket);
            bucket.delete(fileName);
            logger.info("File deleted successfully: {}", fileName);
        } catch (Exception e) {
            logger.error("Error deleting file: {}", fileName, e);
            throw new RuntimeException("Failed to delete file from Firebase Storage", e);
        }
    }

    /**
     * Check if a file exists in Firebase Storage
     */
    public boolean fileExists(String fileName) {
        try {
            var bucket = StorageClient.getInstance().bucket(storageBucket);
            return bucket.get(fileName) != null;
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", fileName, e);
            return false;
        }
    }

    /**
     * Get file metadata
     */
    public String getFileMetadata(String fileName) {
        try {
            var bucket = StorageClient.getInstance().bucket(storageBucket);
            var blob = bucket.get(fileName);
            
            if (blob == null) {
                return null;
            }
            
            return String.format(
                    "File: %s, Size: %d bytes, Content-Type: %s, Created: %s",
                    blob.getName(),
                    blob.getSize(),
                    blob.getContentType(),
                    blob.getCreateTime()
            );
        } catch (Exception e) {
            logger.error("Error retrieving file metadata: {}", fileName, e);
            return null;
        }
    }

    /**
     * Get public download URL for a file
     */
    public String getPublicUrl(String fileName) {
        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                storageBucket,
                fileName
        );
    }
}
