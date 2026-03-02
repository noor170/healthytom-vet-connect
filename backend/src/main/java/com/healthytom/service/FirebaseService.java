package com.healthytom.service;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Firebase Service
 * Provides operations for Firestore database
 */
@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    private final Firestore firestore;

    public FirebaseService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Save or update a document in Firestore
     */
    public <T> void saveDocument(String collection, String documentId, T data) {
        try {
            firestore.collection(collection)
                    .document(documentId)
                    .set(data)
                    .get();
            logger.info("Document saved successfully in collection: {}, documentId: {}", collection, documentId);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error saving document in collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get a document from Firestore
     */
    public Map<String, Object> getDocument(String collection, String documentId) {
        try {
            DocumentSnapshot document = firestore.collection(collection)
                    .document(documentId)
                    .get()
                    .get();

            if (document.exists()) {
                logger.info("Document retrieved from collection: {}, documentId: {}", collection, documentId);
                return document.getData();
            } else {
                logger.warn("Document not found in collection: {}, documentId: {}", collection, documentId);
                return new HashMap<>();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving document from collection: {}", collection, e);
            Thread.currentThread().interrupt();
            return new HashMap<>();
        }
    }

    /**
     * Delete a document from Firestore
     */
    public void deleteDocument(String collection, String documentId) {
        try {
            firestore.collection(collection)
                    .document(documentId)
                    .delete()
                    .get();
            logger.info("Document deleted from collection: {}, documentId: {}", collection, documentId);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting document from collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Query documents from Firestore
     */
    public List<Map<String, Object>> queryDocuments(String collection, String field, Object value) {
        List<Map<String, Object>> documents = new ArrayList<>();
        try {
            Query query = firestore.collection(collection)
                    .whereEqualTo(field, value);

            QuerySnapshot querySnapshot = query.get().get();
            querySnapshot.getDocuments().forEach(doc -> documents.add(doc.getData()));

            logger.info("Retrieved {} documents from collection: {}", documents.size(), collection);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error querying documents from collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
        return documents;
    }

    /**
     * Get all documents from a collection
     */
    public List<Map<String, Object>> getAllDocuments(String collection) {
        List<Map<String, Object>> documents = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = firestore.collection(collection)
                    .get()
                    .get();

            querySnapshot.getDocuments().forEach(doc -> documents.add(doc.getData()));
            logger.info("Retrieved {} documents from collection: {}", documents.size(), collection);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error retrieving all documents from collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
        return documents;
    }

    /**
     * Update specific fields in a document
     */
    public void updateDocument(String collection, String documentId, Map<String, Object> updates) {
        try {
            firestore.collection(collection)
                    .document(documentId)
                    .update(updates)
                    .get();
            logger.info("Document updated in collection: {}, documentId: {}", collection, documentId);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating document in collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Batch write operations
     */
    public void batchWrite(String collection, Map<String, Object> documentsData) {
        try {
            var batch = firestore.batch();
            for (Map.Entry<String, Object> entry : documentsData.entrySet()) {
                DocumentReference docRef = firestore.collection(collection).document(entry.getKey());
                batch.set(docRef, entry.getValue());
            }
            batch.commit().get();
            logger.info("Batch write completed for collection: {}", collection);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error performing batch write in collection: {}", collection, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if document exists
     */
    public boolean documentExists(String collection, String documentId) {
        try {
            return firestore.collection(collection)
                    .document(documentId)
                    .get()
                    .get()
                    .exists();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking document existence in collection: {}", collection, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Count documents in a collection
     */
    public long countDocuments(String collection) {
        try {
            return firestore.collection(collection)
                    .get()
                    .get()
                    .size();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error counting documents in collection: {}", collection, e);
            Thread.currentThread().interrupt();
            return 0;
        }
    }
}
