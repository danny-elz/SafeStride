# SafeStride - Personal Safety Monitoring App

## Team Members
- Danny Elzein (991635120)
- Omar Yoness (991695302)

## Project Overview
SafeStride is an Android application designed to enhance personal safety by monitoring the user's motion and location during walks. If abnormal motion is detected (such as a fall), the app will check on the user and automatically send an SOS alert if no response is provided.

## Advanced Android Areas
- **Geolocation**: Fused Location Provider API for real-time location tracking
- **Motion Sensors**: Accelerometer and Gyroscope for fall detection
- **Foreground Services**: Continuous monitoring while app is in background

## Features
- Firebase Authentication (Email + Password)
- Safe Walk mode with continuous monitoring
- Motion detection using accelerometer and gyroscope
- Location tracking using Fused Location Provider
- Automatic and manual SOS alerts
- Alert history with timestamps and locations
- MVVM architecture with Repository pattern
- Jetpack Compose UI
- Real-time Firestore integration

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM with Repository Pattern
- **Authentication**: Firebase Authentication
- **Database**: Cloud Firestore
- **Location**: Google Play Services Location
- **Navigation**: Jetpack Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36

## Project Structure
```
app/src/main/java/week11/stn991635120/safestride/
├── data/
│   ├── model/
│   │   ├── User.kt
│   │   ├── SOSAlert.kt
│   │   └── LocationUpdate.kt
│   └── repository/
│       ├── AuthRepository.kt
│       └── AlertRepository.kt
├── ui/
│   ├── components/
│   │   ├── CustomButton.kt
│   │   ├── CustomTextField.kt
│   │   └── LoadingScreen.kt
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   ├── RegisterScreen.kt
│   │   │   └── ForgotPasswordScreen.kt
│   │   └── main/
│   │       ├── DashboardScreen.kt
│   │       ├── AlertHistoryScreen.kt
│   │       └── ProfileScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       ├── AuthViewModel.kt
│       └── AlertViewModel.kt
├── navigation/
│   ├── Screen.kt
│   └── NavGraph.kt
├── util/
│   └── Resource.kt
└── MainActivity.kt
```

## Firebase Setup Instructions

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and name it "SafeStride"
3. Follow the setup wizard

### 2. Add Android App
1. Click "Add app" and select Android
2. Package name: `week11.stn2002.safestride`
3. Download `google-services.json`
4. Place it in `app/` directory

### 3. Enable Authentication
1. In Firebase Console, go to Authentication
2. Click "Get Started"
3. Enable "Email/Password" sign-in method

### 4. Set Up Firestore
1. In Firebase Console, go to Firestore Database
2. Click "Create database"
3. Start in **Test mode** (we'll add security rules later)
4. Choose a location close to your users
5. After database is created, go to "Rules" tab
6. Copy the contents of `firestore.rules` from this project
7. Click "Publish"

### 5. Firestore Collections Structure

The app uses three main collections:

#### users
```json
{
  "uid": "string",
  "email": "string",
  "emergencyContact": "string",
  "createdAt": "timestamp"
}
```

#### alerts
```json
{
  "id": "string",
  "userId": "string",
  "timestamp": "timestamp",
  "latitude": "number",
  "longitude": "number",
  "isAutomatic": "boolean",
  "resolved": "boolean",
  "address": "string"
}
```

#### locationUpdates
```json
{
  "userId": "string",
  "latitude": "number",
  "longitude": "number",
  "timestamp": "timestamp",
  "accuracy": "number"
}
```

## Building and Running

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 36
- Google Play Services

### Steps
1. Clone this repository
2. Open project in Android Studio
3. Add `google-services.json` to `app/` directory
4. Sync Gradle files
5. Run on emulator or physical device

### Important Notes
- Location permissions will be requested at runtime
- For best results, test on a physical device with GPS
- Emulator location can be set via Extended Controls

## Work Distribution

### Danny Elzein (991635120)
- Firebase Authentication (login/registration/logout)
- UI screens (Login, Register, Dashboard, Alert History)
- Navigation flow (Auth to Main)
- Manual & automatic SOS logic
- Firestore writes for SOS alerts

### Omar Yoness (991695302)
- Motion sensor integration (accelerometer + gyroscope)
- Fall and abnormal motion detection logic
- Safe Walk foreground service (continuous monitoring)
- Location tracking using FusedLocationProviderClient
- Firestore writes for live location updates

## Security Rules
The Firestore security rules ensure:
- Only authenticated users can access data
- Users can only read/write their own data
- No public writes allowed
- Field-level validation for data integrity

See `firestore.rules` for complete implementation.

## Future Enhancements (Post-Step 2)
- Implement Safe Walk foreground service
- Add motion sensor fall detection algorithm
- Real-time location tracking during Safe Walk
- Emergency contact notifications via SMS/Call
- Geofencing for safe zones
- Share live location with trusted contacts
- Voice-activated SOS
- Integration with local emergency services

## License
This is a student project for PROG 39402 - Fall 2025.

## Contact
For questions or issues, contact the team members via their Sheridan email addresses.
# SafeStride
