package com.healthytom.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.CreateRequest;
import com.google.firebase.auth.UpdateRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Firebase Authentication Service
 * Manages user authentication through Firebase Authentication
 */
@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    /**
     * Create a new user with email and password
     */
    public UserRecord createUser(String email, String password, String displayName) {
        try {
            CreateRequest request = new CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(displayName)
                    .setEmailVerified(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            logger.info("User created successfully in Firebase Auth: {}", userRecord.getUid());
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Error creating user in Firebase Auth: {}", email, e);
            throw new RuntimeException("Failed to create user in Firebase Authentication", e);
        }
    }

    /**
     * Get user by UID
     */
    public UserRecord getUserByUid(String uid) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            logger.info("Retrieved user from Firebase Auth: {}", uid);
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Error retrieving user from Firebase Auth: {}", uid, e);
            return null;
        }
    }

    /**
     * Get user by email
     */
    public UserRecord getUserByEmail(String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            logger.info("Retrieved user by email from Firebase Auth: {}", email);
            return userRecord;
        } catch (FirebaseAuthException e) {
            logger.error("Error retrieving user by email from Firebase Auth: {}", email, e);
            return null;
        }
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(String uid, String displayName, String photoUrl) {
        try {
            UpdateRequest request = new UpdateRequest(uid)
                    .setDisplayName(displayName)
                    .setPhotoUrl(photoUrl);

            FirebaseAuth.getInstance().updateUser(request);
            logger.info("User profile updated in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error updating user profile in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to update user profile", e);
        }
    }

    /**
     * Update user email
     */
    public void updateUserEmail(String uid, String newEmail) {
        try {
            UpdateRequest request = new UpdateRequest(uid)
                    .setEmail(newEmail);

            FirebaseAuth.getInstance().updateUser(request);
            logger.info("User email updated in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error updating user email in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to update user email", e);
        }
    }

    /**
     * Update user password
     */
    public void updateUserPassword(String uid, String newPassword) {
        try {
            UpdateRequest request = new UpdateRequest(uid)
                    .setPassword(newPassword);

            FirebaseAuth.getInstance().updateUser(request);
            logger.info("User password updated in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error updating user password in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to update user password", e);
        }
    }

    /**
     * Delete user
     */
    public void deleteUser(String uid) {
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            logger.info("User deleted from Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error deleting user from Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Set custom claims for a user
     */
    public void setCustomClaims(String uid, java.util.Map<String, Object> claims) {
        try {
            FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
            logger.info("Custom claims set for user in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error setting custom claims for user in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to set custom claims", e);
        }
    }

    /**
     * Disable user account
     */
    public void disableUser(String uid) {
        try {
            UpdateRequest request = new UpdateRequest(uid)
                    .setDisabled(true);

            FirebaseAuth.getInstance().updateUser(request);
            logger.info("User disabled in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error disabling user in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to disable user", e);
        }
    }

    /**
     * Enable user account
     */
    public void enableUser(String uid) {
        try {
            UpdateRequest request = new UpdateRequest(uid)
                    .setDisabled(false);

            FirebaseAuth.getInstance().updateUser(request);
            logger.info("User enabled in Firebase Auth: {}", uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error enabling user in Firebase Auth: {}", uid, e);
            throw new RuntimeException("Failed to enable user", e);
        }
    }
}
