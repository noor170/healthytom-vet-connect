# Firebase Integration Guide

This guide explains how to integrate Firebase with the Healthy Tom Vet Connect backend.

## Overview

Firebase integration provides:
- **Firestore**: NoSQL cloud database for flexible data storage
- **Firebase Storage**: Cloud storage for files and documents
- **Firebase Authentication**: User authentication and management
- **Real-time sync**: Automatic data synchronization across devices

## Setup Instructions

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: `healthytom-vet-connect`
4. Enable Google Analytics (optional)
5. Create the project

### 2. Set Up Firestore Database

1. In Firebase Console, go to **Build** → **Firestore Database**
2. Click **Create database**
3. Choose production mode (with security rules)
4. Select region closest to your users
5. Click **Enable**

### 3. Set Up Firebase Storage

1. In Firebase Console, go to **Build** → **Storage**
2. Click **Get started**
3. Accept security rules
4. Select storage region
5. Click **Done**

### 4. Set Up Firebase Authentication

1. In Firebase Console, go to **Build** → **Authentication**
2. Click **Get started**
3. Enable **Email/Password** provider
4. (Optional) Enable Google, GitHub, Facebook providers

### 5. Get Service Account Credentials

1. In Firebase Console, go to **Project Settings** (gear icon)
2. Click **Service accounts** tab
3. Click **Generate new private key**
4. Save the JSON file securely
5. Place it in your project or reference it via environment variable

## Configuration

### Enable Firebase Integration

Edit `application.properties`:

```properties
firebase.enabled=true
firebase.credentials.path=/path/to/serviceAccountKey.json
firebase.database-url=https://your-project.firebaseio.com
firebase.project-id=your-project-id
firebase.storage.bucket=your-project.appspot.com
```

### Environment Variables

Alternatively, set environment variables:

```bash
export FIREBASE_ENABLED=true
export FIREBASE_CREDENTIALS_PATH=/path/to/serviceAccountKey.json
export FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
export FIREBASE_PROJECT_ID=your-project-id
export FIREBASE_STORAGE_BUCKET=your-project.appspot.com
```

### Using Application Default Credentials

If running on Google Cloud or with ADC configured:

```properties
firebase.enabled=true
firebase.credentials.path=
firebase.database-url=https://your-project.firebaseio.com
firebase.project-id=your-project-id
firebase.storage.bucket=your-project.appspot.com
```

## Services Available

### 1. FirebaseService (Firestore)

Manages document operations in Firestore.

#### Methods:

```java
// Save/Update a document
firebaseService.saveDocument("users", userId, userData);

// Get a document
Map<String, Object> user = firebaseService.getDocument("users", userId);

// Delete a document
firebaseService.deleteDocument("users", userId);

// Query documents
List<Map<String, Object>> pets = firebaseService.queryDocuments("pets", "ownerId", userId);

// Get all documents in collection
firebaseService.getAllDocuments("consultations");

// Update specific fields
firebaseService.updateDocument("users", userId, updates);

// Batch write operations
firebaseService.batchWrite("logs", documentsMap);

// Check if document exists
boolean exists = firebaseService.documentExists("users", userId);

// Count documents
long count = firebaseService.countDocuments("pets");
```

### 2. FirebaseStorageService

Handles file uploads and downloads.

#### Methods:

```java
// Upload a file
String url = firebaseStorageService.uploadFile(
    "documents/prescription-123.pdf",
    fileContent,
    "application/pdf"
);

// Download a file
byte[] fileContent = firebaseStorageService.downloadFile("documents/prescription-123.pdf");

// Delete a file
firebaseStorageService.deleteFile("documents/prescription-123.pdf");

// Check if file exists
boolean exists = firebaseStorageService.fileExists("documents/prescription-123.pdf");

// Get file metadata
String metadata = firebaseStorageService.getFileMetadata("documents/prescription-123.pdf");

// Get public URL
String publicUrl = firebaseStorageService.getPublicUrl("documents/prescription-123.pdf");
```

### 3. FirebaseAuthService

Manages user authentication.

#### Methods:

```java
// Create a new user
UserRecord user = firebaseAuthService.createUser(
    "user@example.com",
    "password123",
    "John Doe"
);

// Get user by UID
UserRecord user = firebaseAuthService.getUserByUid(uid);

// Get user by email
UserRecord user = firebaseAuthService.getUserByEmail("user@example.com");

// Update user profile
firebaseAuthService.updateUserProfile(uid, "New Name", "https://avatar.url");

// Update user email
firebaseAuthService.updateUserEmail(uid, "newemail@example.com");

// Update user password
firebaseAuthService.updateUserPassword(uid, "newpassword123");

// Delete user
firebaseAuthService.deleteUser(uid);

// Set custom claims (roles, permissions)
firebaseAuthService.setCustomClaims(uid, Map.of("role", "veterinarian"));

// Disable user account
firebaseAuthService.disableUser(uid);

// Enable user account
firebaseAuthService.enableUser(uid);
```

## Using Firebase in Controllers

### Example: Save Pet Data to Firestore

```java
@PostMapping
public ResponseEntity<PetDto> createPet(@RequestBody PetDto petDto, @AuthenticationPrincipal UserDetails userDetails) {
    // Save to PostgreSQL
    Pet pet = petService.savePet(petDto);
    
    // Also save to Firestore for sync
    firebaseService.saveDocument("pets", pet.getId().toString(), petDto);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(petDto);
}
```

### Example: Upload Medical Records to Firebase Storage

```java
@PostMapping("/{petId}/upload-records")
public ResponseEntity<String> uploadMedicalRecords(
        @PathVariable Long petId,
        @RequestParam("file") MultipartFile file) throws IOException {
    
    String fileName = "pets/" + petId + "/records/" + file.getOriginalFilename();
    String url = firebaseStorageService.uploadFile(
        fileName,
        file.getBytes(),
        file.getContentType()
    );
    
    return ResponseEntity.ok(url);
}
```

### Example: Sync Users to Firebase Auth

```java
@PostMapping("/register")
public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
    // Create local user
    User user = authService.registerUser(request);
    
    // Also create in Firebase Auth for optional use
    firebaseAuthService.createUser(
        user.getEmail(),
        request.getPassword(),
        user.getFullName()
    );
    
    return ResponseEntity.ok(authResponse);
}
```

## Database Schema Design

### Recommended Firestore Collections:

```
firestore/
├── users/
│   ├── {userId}/
│   │   ├── email
│   │   ├── fullName
│   │   ├── phone
│   │   ├── role
│   │   ├── createdAt
│   │   └── updatedAt
│
├── pets/
│   ├── {petId}/
│   │   ├── name
│   │   ├── ownerId
│   │   ├── species
│   │   ├── breed
│   │   ├── vaccinations
│   │   └── medicalHistory
│
├── consultations/
│   ├── {consultationId}/
│   │   ├── petId
│   │   ├── veterinarianId
│   │   ├── status
│   │   ├── diagnosis
│   │   └── timestamp
│
├── prescriptions/
│   ├── {prescriptionId}/
│   │   ├── consultationId
│   │   ├── medication
│   │   ├── dosage
│   │   └── duration
│
└── activity_logs/
    ├── {logId}/
    │   ├── userId
    │   ├── action
    │   ├── timestamp
    │   └── details
```

## Security Rules (Firestore)

Place these rules in Firebase Console under **Firestore Database** → **Rules**:

```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own documents
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Pets are readable by owner and assigned veterinarian
    match /pets/{petId} {
      allow read, write: if 
        request.auth.uid == resource.data.ownerId ||
        request.auth.uid in resource.data.allowedVets;
      allow create: if request.auth.uid == request.resource.data.ownerId;
    }
    
    // Consultations visible to involved parties
    match /consultations/{consultationId} {
      allow read: if 
        request.auth.uid == resource.data.petOwnerId ||
        request.auth.uid == resource.data.veterinarianId;
      allow write: if request.auth.uid == resource.data.veterinarianId;
    }
  }
}
```

## Storage Rules (Firebase Storage)

Set in Firebase Console under **Storage** → **Rules**:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Allow authenticated users to read/write their own files
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Allow public read of medical records
    match /pets/{petId}/records/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == request.auth.uid; // Change logic as needed
    }
  }
}
```

## Running the Application

### With Firebase Enabled:

```bash
# Using environment variables
export FIREBASE_ENABLED=true
export FIREBASE_CREDENTIALS_PATH=/path/to/serviceAccountKey.json
export FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
export FIREBASE_PROJECT_ID=your-project-id
export FIREBASE_STORAGE_BUCKET=your-project.appspot.com

java -jar target/vet-connect-1.0.0.jar
```

### With Docker Compose:

Update `docker-compose.yml`:

```yaml
services:
  api:
    environment:
      - FIREBASE_ENABLED=true
      - FIREBASE_CREDENTIALS_PATH=/app/serviceAccountKey.json
      - FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
      - FIREBASE_PROJECT_ID=your-project-id
      - FIREBASE_STORAGE_BUCKET=your-project.appspot.com
    volumes:
      - /path/to/serviceAccountKey.json:/app/serviceAccountKey.json:ro
```

## Monitoring and Logs

Monitor Firebase usage in Console:
- **Firestore**: **Data** tab shows operations and usage
- **Storage**: **Bucket** shows file operations
- **Authentication**: **Users** tab shows all user accounts

Backend logs will show Firebase operations:
```
INFO c.h.c.FirebaseConfig: Initializing Firebase Admin SDK...
INFO c.h.c.FirebaseConfig: Firebase credentials loaded from: /path/to/serviceAccountKey.json
INFO c.h.c.FirebaseConfig: Firebase Admin SDK initialized successfully
```

## Troubleshooting

### Credentials Not Found
```
Error: No credentials file found
Solution: Ensure FIREBASE_CREDENTIALS_PATH is set correctly or use Application Default Credentials
```

### Project ID Mismatch
```
Error: Project ID does not match
Solution: Verify firebase.project-id matches Firebase Console project ID
```

### Storage Bucket Not Found
```
Error: Storage bucket not found
Solution: Ensure FIREBASE_STORAGE_BUCKET is in format: project-id.appspot.com
```

### Firestore Permission Denied
```
Error: Permission denied: Read failed
Solution: Update Firestore security rules to allow your app access
```

## Cost Considerations

**Firestore Pricing:**
- 50k read/write operations: FREE
- 20k delete operations: FREE
- $0.06 per 100k read operations (beyond free tier)
- $0.18 per 100k write operations (beyond free tier)

**Storage Pricing:**
- 5GB: FREE
- $0.18 per GB (beyond free tier)

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Firebase Storage Guide](https://firebase.google.com/docs/storage)
- [Firebase Admin SDK](https://firebase.google.com/docs/admin/setup)
- [Firebase Security Rules](https://firebase.google.com/docs/rules)

## Support

For issues with Firebase integration:
1. Check Firebase Console for errors
2. Review application logs
3. Verify credentials and configuration
4. Check Firestore security rules
5. Test individual Firebase services separately
