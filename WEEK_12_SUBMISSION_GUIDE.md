# SafeStride - Week 12 Submission Guide

## ✅ What's Already Complete

Your SafeStride project has the following components ready:

### 1. ✅ Project Initiation Documentation
- **Project Title**: SafeStride – Personal Safety Monitoring App
- **Description**: SafeStride monitors users' motion and location during walks, automatically detecting falls and sending SOS alerts with location data to emergency contacts.
- **Advanced Android Area**: Geolocation (Fused Location Provider API) + Motion Sensors (Accelerometer & Gyroscope) + Foreground Services
- **100-word description**: Ready (see README.md)

### 2. ✅ Development Environment
- **Package Name**: `week11.stn2002.safestride`
- **Kotlin + Jetpack Compose**: Fully set up
- **MVVM Architecture**: Complete
- **All dependencies**: Added and configured

### 3. ✅ Firebase Authentication
- Login Screen ✓
- Register Screen ✓
- Forgot Password Screen ✓
- Input validation ✓
- Error handling ✓
- Loading states ✓

### 4. ✅ Navigation System
- All routes defined
- Auth flow implemented
- Main app screens created
- Access control working

### 5. ✅ Firestore Integration
- Repository classes created
- CRUD operations for alerts
- Real-time data with Flow
- Security rules written

### 6. ✅ UI Components
- Custom themed components
- Reusable buttons and text fields
- SafeStride brand colors applied

---

## 📋 REMAINING TASKS - DO THESE NOW

### Task 1: Create Figma Prototype (REQUIRED)

#### What You Need to Design:

**Authentication Flow:**
1. **Splash/Welcome Screen**
   - SafeStride logo
   - Tagline: "Your Personal Safety Companion"
   - "Get Started" button

2. **Login Screen**
   - Email input field
   - Password input field
   - "Login" button
   - "Forgot Password?" link
   - "Sign Up" link

3. **Register Screen**
   - Email input
   - Emergency Contact Phone input
   - Password input
   - Confirm Password input
   - "Sign Up" button
   - "Already have account? Login" link

4. **Forgot Password Screen**
   - Email input
   - "Send Reset Email" button
   - Success message area

**Main App Screens:**
5. **Dashboard/Home Screen**
   - Top app bar with "SafeStride" title and profile icon
   - Large circular "Start Safe Walk" button (blue/green)
   - HUGE red "SOS ALERT" button (emergency red)
   - "View Alert History" button
   - Bottom navigation or menu

6. **Active Safe Walk Screen**
   - Timer showing walk duration
   - Live location indicator/map placeholder
   - "Monitoring your location" status
   - Red "Stop Safe Walk" button
   - Manual "SOS" button still visible

7. **Fall Detection Alert Dialog**
   - Large "Are you okay?" text
   - Warning icon
   - "I'm OK" button (green)
   - Countdown timer (e.g., "Auto-alert in 30 seconds")
   - "Send SOS Now" button (red)

8. **Alert History Screen**
   - List of past alerts with cards showing:
     - Alert type (Manual/Automatic)
     - Date and time
     - Location/address
     - Resolved status badge
     - Delete icon

9. **Profile/Settings Screen**
   - User email display
   - Emergency contact information
   - Edit emergency contact button
   - Logout button

#### Figma Design Tips:
- Use **Material 3 Design** components
- **Colors**:
  - Primary Blue: #1976D2
  - Primary Green: #388E3C
  - Alert Red: #D32F2F
  - Background: White/Light Gray
- **Typography**: Use Roboto font
- Add realistic sample data (fake names, dates, locations)
- Show proper spacing and padding
- Include status bar and navigation elements

#### How to Create:
1. Go to [figma.com](https://figma.com) and sign up/login
2. Create new design file named "SafeStride"
3. Create frames for each screen (use Android phone template)
4. Design all 9 screens listed above
5. Add navigation arrows between screens to show flow
6. Make it look professional and polished

#### Submission Requirements:
- [ ] Take a **full screenshot** of all screens in Figma
- [ ] Click "Share" → Set to "Anyone with link can view"
- [ ] Copy the Figma link
- [ ] Save both for submission

---

### Task 2: Firebase Setup (REQUIRED - DO THIS BEFORE TESTING)

#### Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click **"Add project"**
3. Project name: **SafeStride** (or any name you prefer)
4. **Disable** Google Analytics (not needed for this project)
5. Click **"Create project"**
6. Wait for it to finish, then click **"Continue"**

#### Step 2: Add Android App to Firebase
1. On Firebase Console homepage, click the **Android icon** (</>) to add Android app
2. Fill in the form:
   - **Android package name**: `week11.stn2002.safestride`
   - **App nickname**: SafeStride
   - **Debug signing certificate**: Leave blank (optional)
3. Click **"Register app"**
4. **Download `google-services.json`** file
5. **IMPORTANT**: Place this file in `/Users/dannyelzein/AndroidStudioProjects/SafeStride/app/` folder
   - Open Finder
   - Navigate to your SafeStride project
   - Drag `google-services.json` into the `app` folder (same level as build.gradle.kts)
6. Click **"Next"** → **"Next"** → **"Continue to console"**

#### Step 3: Enable Firebase Authentication
1. In Firebase Console, click **"Authentication"** in left sidebar
2. Click **"Get started"**
3. Click on **"Email/Password"** under Sign-in providers
4. **Enable** the first option (Email/Password)
5. Leave "Email link" disabled
6. Click **"Save"**

#### Step 4: Create Firestore Database
1. In Firebase Console, click **"Firestore Database"** in left sidebar
2. Click **"Create database"**
3. Choose **"Start in test mode"** (we'll add security rules next)
4. Click **"Next"**
5. Select location: **us-central** (or closest to you)
6. Click **"Enable"**
7. Wait for database to be created

#### Step 5: Deploy Firestore Security Rules
1. Once Firestore is created, click on **"Rules"** tab
2. You'll see default test mode rules
3. **REPLACE ALL** the text with the contents from your `firestore.rules` file:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }

    // Helper function to check if user owns the resource
    function isOwner(userId) {
      return request.auth.uid == userId;
    }

    // Users collection - users can only read/write their own profile
    match /users/{userId} {
      allow read: if isAuthenticated() && isOwner(userId);
      allow create: if isAuthenticated() && isOwner(userId);
      allow update: if isAuthenticated() && isOwner(userId);
      allow delete: if false; // Prevent user deletion
    }

    // Alerts collection - users can only read/write their own alerts
    match /alerts/{alertId} {
      allow read: if isAuthenticated() && isOwner(resource.data.userId);
      allow create: if isAuthenticated() && isOwner(request.resource.data.userId);
      allow update: if isAuthenticated() && isOwner(resource.data.userId);
      allow delete: if isAuthenticated() && isOwner(resource.data.userId);
    }

    // Location updates collection - users can only read/write their own location
    match /locationUpdates/{locationId} {
      allow read: if isAuthenticated() && isOwner(resource.data.userId);
      allow create: if isAuthenticated() && isOwner(request.resource.data.userId);
      allow update: if false; // Location updates should not be modified
      allow delete: if isAuthenticated() && isOwner(resource.data.userId);
    }

    // Deny all other access by default
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

4. Click **"Publish"**
5. Confirm by clicking **"Publish"** again

#### Step 6: Add Omar as Collaborator (Firebase)
1. In Firebase Console, click the **gear icon** (⚙️) next to "Project Overview"
2. Click **"Users and permissions"**
3. Click **"Add member"**
4. Enter Omar's email address
5. Select role: **"Editor"**
6. Click **"Add member"**

---

### Task 3: Build and Test the App

#### Step 1: Sync Gradle Files
1. Open Android Studio
2. File → Sync Project with Gradle Files
3. Wait for sync to complete
4. Fix any errors if they appear

#### Step 2: Run the App
1. Connect Android device OR start emulator
2. Click the **Run** button (green play icon)
3. Select your device/emulator
4. Wait for app to build and install

#### Step 3: Test Authentication
1. **Test Registration:**
   - Open the app (should show Login screen)
   - Click "Sign Up"
   - Enter test email: `test@safestride.com`
   - Enter emergency contact: `416-555-1234`
   - Enter password: `test123456`
   - Confirm password: `test123456`
   - Click "Sign Up"
   - Should navigate to Dashboard

2. **Verify in Firebase Console:**
   - Go to Firebase Console → Authentication
   - You should see the test user listed
   - Go to Firestore Database
   - You should see a `users` collection with your user document

3. **Test Login:**
   - Logout from the app
   - Try logging in with same credentials
   - Should work and go to Dashboard

4. **Test Forgot Password:**
   - Logout
   - Click "Forgot Password?"
   - Enter your test email
   - Click "Send Reset Email"
   - Check your email for reset link

#### Step 4: Test SOS Alert Feature
1. Login to the app
2. On Dashboard, click the big red **"SOS ALERT"** button
3. This should create a manual alert
4. Click **"View Alert History"**
5. You should see your alert in the list
6. Verify in Firebase Console:
   - Go to Firestore Database
   - Check `alerts` collection
   - You should see your alert document

#### Step 5: Test Alert History
1. Create 2-3 more test alerts
2. Go to Alert History screen
3. Verify all alerts appear
4. Try deleting an alert
5. Verify it's removed from both app and Firestore

---

### Task 4: Create GitHub Repository

#### Step 1: Create .gitignore File
First, make sure you have a proper .gitignore. Check if it already exists:

```bash
cat .gitignore
```

It should include:
```
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
google-services.json
```

**IMPORTANT**: Make sure `google-services.json` is in .gitignore so you don't commit it!

#### Step 2: Create GitHub Repository
1. Go to https://github.com
2. Click **"New repository"** (green button)
3. Repository name: **SafeStride**
4. Description: "Personal Safety Monitoring Android App for PROG 39402"
5. Select **"Private"** (unless instructor wants public)
6. **Do NOT** check "Initialize with README" (you already have one)
7. Click **"Create repository"**

#### Step 3: Add Collaborators
1. In your new GitHub repo, click **"Settings"**
2. Click **"Collaborators"** in left sidebar
3. Click **"Add people"**
4. Add **Omar Yoness** (get his GitHub username)
5. Add **your instructor** (get their GitHub username)

#### Step 4: Push Code to GitHub
Open Terminal in your SafeStride project folder and run:

```bash
cd /Users/dannyelzein/AndroidStudioProjects/SafeStride
git init
git add .
git commit -m "Initial SafeStride project with Firebase Auth and Firestore integration"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/SafeStride.git
git push -u origin main
```

**Replace `YOUR_USERNAME`** with your actual GitHub username!

#### Step 5: Verify Push
1. Refresh your GitHub repository page
2. You should see all your code files
3. **Verify** that `google-services.json` is **NOT** there (should be ignored)

---

### Task 5: Prepare Submission

#### Create a Submission Document
Create a document (Word/PDF) with the following:

**PROG 39402 - Final Project Step 2 Submission**

**Team Members:**
- Danny Elzein (991635120)
- Omar Yoness (991695302)

**Project Title:** SafeStride – Personal Safety Monitoring App

**Advanced Android Area:** Geolocation + Motion Sensors + Foreground Services

**Description:**
SafeStride enhances personal safety for students, night-shift workers, and commuters walking alone. Users activate "Safe Walk" mode, which continuously monitors motion using device sensors. If abnormal motion like a fall is detected, the app prompts "Are you okay?" If there's no response within a set timeframe, it automatically sends an SOS alert to emergency contacts with the user's last known GPS location. Users can also manually trigger SOS alerts. All events are stored in Firestore, allowing users to review their safety history. The app uses Firebase Authentication to secure personal data and ensure privacy.

**Figma Prototype:**
- Screenshot: [Paste your Figma screenshot here]
- Link: [Paste your Figma link here]

**GitHub Repository:**
- Link: [Paste your GitHub repo link here]

**Firebase Project:**
- Project ID: [Your Firebase project ID]
- Authentication: ✓ Enabled (Email/Password)
- Firestore: ✓ Configured with security rules

**Features Implemented:**
- ✓ Firebase Authentication (Login, Register, Forgot Password)
- ✓ MVVM Architecture with Repository Pattern
- ✓ Jetpack Compose UI
- ✓ Navigation System
- ✓ Firestore Database Integration
- ✓ SOS Alert CRUD Operations
- ✓ Alert History with Real-time Updates
- ✓ Security Rules Implemented
- ✓ Custom UI Components with Theme

**Package Name:** week11.stn2002.safestride

---

## 📋 FINAL CHECKLIST

Before submitting, make sure you've completed ALL of these:

### Figma
- [ ] Created all 9 screens (Splash, Login, Register, Forgot Password, Dashboard, Active Walk, Fall Alert, History, Profile)
- [ ] Used Material 3 design principles
- [ ] Applied SafeStride brand colors
- [ ] Added realistic sample data
- [ ] Took full screenshot
- [ ] Got shareable link
- [ ] Link is set to "Anyone can view"

### Firebase
- [ ] Created Firebase project
- [ ] Downloaded google-services.json
- [ ] Placed google-services.json in app/ folder
- [ ] Enabled Email/Password authentication
- [ ] Created Firestore database
- [ ] Deployed security rules
- [ ] Added Omar as collaborator

### App Testing
- [ ] App builds successfully
- [ ] Can register new account
- [ ] User appears in Firebase Authentication
- [ ] User profile created in Firestore
- [ ] Can login with credentials
- [ ] Can reset password
- [ ] Can create SOS alert
- [ ] Alert appears in Firestore
- [ ] Can view alert history
- [ ] Can delete alerts
- [ ] No crashes or errors

### GitHub
- [ ] Created GitHub repository
- [ ] .gitignore includes google-services.json
- [ ] Added Omar as collaborator
- [ ] Added instructor as collaborator
- [ ] Pushed all code to GitHub
- [ ] Verified google-services.json NOT in repo
- [ ] Repository link works

### Documentation
- [ ] Created submission document
- [ ] Included all team member info
- [ ] Added Figma screenshot and link
- [ ] Added GitHub repository link
- [ ] Listed all implemented features

---

## 🚨 IMPORTANT REMINDERS

1. **DO NOT** commit `google-services.json` to GitHub
2. **DO** share the file with Omar separately (Google Drive, Slack, etc.)
3. **VERIFY** all links work before submitting
4. **TEST** the app thoroughly
5. **BACKUP** your project

---

## 📞 If You Run Into Issues

### App won't build?
- Make sure google-services.json is in app/ folder
- File → Invalidate Caches and Restart
- Clean and rebuild project

### Firebase authentication not working?
- Check google-services.json is correct file for your project
- Verify Email/Password is enabled in Firebase Console
- Check internet connection

### Firestore errors?
- Make sure security rules are published
- Check user is authenticated
- Verify userId in alerts matches authenticated user

### Can't push to GitHub?
- Make sure you created the repo first
- Verify you're using correct repo URL
- Check GitHub authentication (may need personal access token)

---

## ✅ You're Ready!

Once you've completed all the tasks in this guide:
1. Double-check the final checklist
2. Submit your document to the Dropbox on SLATE
3. Ensure your instructor has access to both GitHub and Firebase

**Good luck with your submission!** 🎉

---

**Next Steps After Week 12:**
- Implement Safe Walk foreground service (Omar)
- Add motion sensor fall detection (Omar)
- Implement location tracking during Safe Walk (Omar)
- Add emergency contact notifications
- Request and handle runtime permissions
- Test on physical device with actual sensors
