# Firebase Integration - Demo Data Setup

## Overview
This guide demonstrates how to insert demo data into Firebase Firestore for the Healthy Tom Vet Connect application.

## Prerequisites
- Firebase project: `healthytom-vet-connect`
- Service account credentials configured
- Firestore database initialized
- Firebase Storage bucket configured

## Firebase Console Access

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `healthytom-vet-connect`
3. Navigate to **Build** → **Firestore Database**

## Demo Data Structure

### Collection: `prod_users`

#### Document: `user_demo_001`
```json
{
  "email": "demo@healthytom.com",
  "firstName": "Demo",
  "lastName": "User",
  "role": "OWNER",
  "phoneNumber": "+1234567890",
  "emailVerified": false,
  "profileImageUrl": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/avatars/demo-user.jpg",
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "isActive": true
}
```

#### Document: `user_vet_001`
```json
{
  "email": "vet@healthytom.com",
  "firstName": "Dr.",
  "lastName": "Veterinarian",
  "role": "VETERINARIAN",
  "phoneNumber": "+1234567891",
  "emailVerified": true,
  "profileImageUrl": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/avatars/vet-001.jpg",
  "specialization": "Small Animal Medicine",
  "licenseNumber": "VET2024001",
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "isActive": true
}
```

### Collection: `prod_pets`

#### Document: `pet_buddy_001`
```json
{
  "name": "Buddy",
  "species": "Dog",
  "breed": "Golden Retriever",
  "age": 3,
  "weight": 30.5,
  "dateOfBirth": "2023-01-15",
  "color": "Golden",
  "microchipId": "978097109876543",
  "medicalHistory": [
    {
      "date": "2025-12-15",
      "description": "Annual health check",
      "veterinarian": "Dr. Veterinarian",
      "notes": "All healthy, vaccines up to date"
    }
  ],
  "ownerId": "user_demo_001",
  "vaccinated": true,
  "lastVaccinationDate": "2025-12-01",
  "allergies": [],
  "medications": [],
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "profileImageUrl": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/pets/buddy.jpg"
}
```

#### Document: `pet_fluffy_001`
```json
{
  "name": "Fluffy",
  "species": "Cat",
  "breed": "Persian",
  "age": 5,
  "weight": 4.2,
  "dateOfBirth": "2021-02-10",
  "color": "White",
  "microchipId": "978097109876544",
  "medicalHistory": [
    {
      "date": "2025-11-20",
      "description": "Dental cleaning",
      "veterinarian": "Dr. Veterinarian",
      "notes": "Plaque removal completed successfully"
    }
  ],
  "ownerId": "user_demo_001",
  "vaccinated": true,
  "lastVaccinationDate": "2025-10-15",
  "allergies": ["Fish"],
  "medications": [
    {
      "name": "Allergy Medicine",
      "dosage": "5mg",
      "frequency": "Once daily",
      "prescribedDate": "2026-01-15"
    }
  ],
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "profileImageUrl": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/pets/fluffy.jpg"
}
```

### Collection: `prod_consultations`

#### Document: `consult_buddy_001`
```json
{
  "petId": "pet_buddy_001",
  "petName": "Buddy",
  "ownerId": "user_demo_001",
  "veterinarianId": "user_vet_001",
  "veterinarianName": "Dr. Veterinarian",
  "type": "GENERAL_CHECKUP",
  "status": "COMPLETED",
  "scheduledDate": "2026-03-10T14:00:00Z",
  "completedDate": "2026-03-10T14:45:00Z",
  "durationMinutes": 45,
  "notes": "Routine health check performed. Pet is in excellent health.",
  "diagnosis": "No health issues detected",
  "prescriptions": [],
  "nextFollowUp": "2027-03-10",
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "attachments": [
    {
      "type": "MEDICAL_REPORT",
      "url": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/reports/consult_buddy_001_report.pdf",
      "uploadedAt": "2026-03-10T14:45:00Z"
    }
  ]
}
```

### Collection: `prod_prescriptions`

#### Document: `rx_fluffy_001`
```json
{
  "petId": "pet_fluffy_001",
  "petName": "Fluffy",
  "ownerId": "user_demo_001",
  "veterinarianId": "user_vet_001",
  "medicationName": "Histamine Blocker",
  "dosage": "5mg",
  "frequency": "Once daily",
  "duration": "30 days",
  "instructions": "Give with food. May cause drowsiness.",
  "startDate": "2026-03-02",
  "endDate": "2026-04-01",
  "reason": "Allergy management",
  "sideEffects": [
    "Mild drowsiness",
    "Increased thirst"
  ],
  "dosesRemaining": 28,
  "refillsRemaining": 2,
  "pharmacyName": "Happy Paws Pharmacy",
  "status": "ACTIVE",
  "createdAt": "2026-03-02T15:57:35Z",
  "updatedAt": "2026-03-02T15:57:35Z",
  "prescriptionDocument": "https://storage.googleapis.com/healthytom-vet-connect.appspot.com/prod/prescriptions/rx_fluffy_001.pdf"
}
```

## Firebase Storage Structure

### Folder: `prod/avatars/`
- `demo-user.jpg` - Profile image for demo user
- `vet-001.jpg` - Profile image for veterinarian

### Folder: `prod/pets/`
- `buddy.jpg` - Pet profile image
- `fluffy.jpg` - Pet profile image

### Folder: `prod/reports/`
- `consult_buddy_001_report.pdf` - Medical consultation report

### Folder: `prod/prescriptions/`
- `rx_fluffy_001.pdf` - Prescription document

## Inserting Demo Data

### Method 1: Firebase Console (Manual)

1. Go to Firestore Database
2. Create new collection: `prod_users`
3. Click **Add Document**
4. Paste the JSON data from above
5. Repeat for other collections

### Method 2: Firestore Emulator (Local Testing)

```bash
# Install Firebase Tools
npm install -g firebase-tools

# Initialize Firebase in project
firebase init emulators

# Start emulator
firebase emulators:start
```

### Method 3: Firebase Admin SDK (Programmatic)

Create `insert-demo-data.js`:

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./firebase-credentials.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function insertDemoData() {
  try {
    // Insert user
    await db.collection('prod_users').doc('user_demo_001').set({
      email: 'demo@healthytom.com',
      firstName: 'Demo',
      lastName: 'User',
      role: 'OWNER',
      phoneNumber: '+1234567890',
      emailVerified: false,
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now()
    });

    // Insert pet
    await db.collection('prod_pets').doc('pet_buddy_001').set({
      name: 'Buddy',
      species: 'Dog',
      breed: 'Golden Retriever',
      age: 3,
      weight: 30.5,
      ownerId: 'user_demo_001',
      createdAt: admin.firestore.Timestamp.now(),
      updatedAt: admin.firestore.Timestamp.now()
    });

    console.log('✓ Demo data inserted successfully');
  } catch (error) {
    console.error('✗ Error inserting demo data:', error);
  }
}

insertDemoData();
```

Run:
```bash
npm install firebase-admin
node insert-demo-data.js
```

## Firestore Security Rules (Production)

Place these rules in Firestore Rules Editor:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read/write their own documents
    match /prod_users/{userId} {
      allow read, write: if request.auth.uid == userId;
      allow read: if request.auth.token.role in ['VETERINARIAN', 'ADMIN'];
    }
    
    // Allow users to read/write their own pets
    match /prod_pets/{petId} {
      allow read, write: if request.auth.uid == resource.data.ownerId;
      allow read: if request.auth.token.role in ['VETERINARIAN', 'ADMIN'];
    }
    
    // Allow users to read consultations for their pets
    match /prod_consultations/{consultationId} {
      allow read: if request.auth.uid == resource.data.ownerId ||
                     request.auth.uid == resource.data.veterinarianId;
      allow write: if request.auth.uid == resource.data.veterinarianId ||
                      request.auth.token.role == 'ADMIN';
    }
    
    // Allow users to read their prescriptions
    match /prod_prescriptions/{prescriptionId} {
      allow read: if request.auth.uid == resource.data.ownerId;
      allow write: if request.auth.uid == resource.data.veterinarianId;
    }
  }
}
```

## Firebase Storage Security Rules

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /prod/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                      request.auth.token.role in ['VETERINARIAN', 'ADMIN'];
      allow delete: if request.auth.token.role == 'ADMIN';
    }
  }
}
```

## Testing Firebase Connection

### Via cURL (with Custom Claims)

```bash
# Get access token
firebase auth:export credentials.json --project=healthytom-vet-connect

# Make authenticated request
curl -H "Authorization: Bearer ACCESS_TOKEN" \
  https://firestore.googleapis.com/v1/projects/healthytom-vet-connect/databases/(default)/documents/prod_users
```

### Via Backend Integration

The Spring Boot backend automatically connects to Firebase using:
- `firebase-credentials.json` for authentication
- Firestore for data storage
- Firebase Storage for file uploads

## Next Steps

1. Insert demo data using Firebase Console or Admin SDK
2. Test API endpoints with curl commands from [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md)
3. Verify Firebase collections and documents in Console
4. Deploy production rules to Firestore and Storage
5. Monitor usage in Firebase Console

## Firebase Dashboard Metrics

Monitor in Firebase Console:
- **Database**: Track reads/writes
- **Storage**: Monitor file uploads/downloads
- **Authentication**: User authentication events
- **Functions**: Cloud function execution logs
- **Firestore Backups**: Automatic daily backups

## Troubleshooting

### Missing Permissions
- Check service account has "Editor" role in Firebase Console
- Verify `firebase-credentials.json` path is correct

### Data Not Appearing
- Check Firestore Rules allow your auth method
- Verify collection names match exactly (case-sensitive)
- Check timestamp format is ISO 8601

### Connection Timeout
- Verify internet connection
- Check Firebase project ID matches
- Ensure credentials file has not expired
