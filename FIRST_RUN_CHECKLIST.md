# SangeetMind Android - First Run Checklist

## ✅ Android Studio Installed!

Follow these steps to run the app for the first time:

---

## 📂 **Step 1: Open the Project**

1. **Launch Android Studio**
2. Click **"Open"** (NOT "New Project")
3. Navigate to: `C:\Users\PRANSHI\sangeetmind-android`
4. Click **"OK"**

---

## ⏳ **Step 2: Wait for Initial Sync (Important!)**

Android Studio will now:
- ✅ Download Gradle dependencies (~5-10 minutes first time)
- ✅ Index the project files
- ✅ Configure the build system

**Watch the bottom status bar** - it will show:
- "Gradle: Downloading dependencies..."
- "Indexing..."
- "Build: Syncing..."

**⚠️ DO NOT click anything until sync completes!**

---

## 🔧 **Step 3: Install Missing Components (If Prompted)**

You might see prompts like:

### "Install missing platforms and sync project"
- ✅ Click **"Install missing platforms"**
- Wait for Android SDK Platform 34 to download

### "Install Build Tools"
- ✅ Click **"Install Build Tools"**
- Wait for completion

### "Accept Android SDK License"
- ✅ Click **"Accept"** and then **"Next"**

---

## 📱 **Step 4: Set Up a Device**

### Option A: Create an Emulator (Recommended for First Run)

1. Click the **Device Manager** icon (phone icon in toolbar)
2. Click **"Create Device"**
3. Select **"Pixel 6"** or **"Pixel 7"**
4. Click **"Next"**
5. Select **"Tiramisu"** (API 34) or **"UpsideDownCake"** (API 34)
6. If not downloaded, click **"Download"** and wait
7. Click **"Next"** → **"Finish"**

### Option B: Use a Physical Device

1. Enable **Developer Options** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging**:
   - Settings → System → Developer Options → USB Debugging
3. Connect phone via USB
4. Accept the "Allow USB debugging" prompt on your phone

---

## 🚀 **Step 5: Run the App!**

1. **Select your device** from the dropdown (top toolbar)
2. Click the **green Run button (▶️)** or press `Shift + F10`
3. **Wait for build** (first build takes 2-5 minutes)
4. **App will launch automatically!** 🎉

---

## 📱 **What You'll See:**

### 1. **Onboarding Screen** (First Launch)
- 3 beautiful pages with raag meditation introduction
- Swipe left to navigate
- Accept Terms & Privacy checkboxes
- Click "Get Started"

### 2. **Authentication Screen**
- Login or Sign Up form
- Enter any email/password (mock auth)
- Or click "Continue with Google"

### 3. **Raag Library** (Home Screen)
- Search bar at top
- List of 5 sample raags with artwork
- Click any raag to see player
- Heart icon to favorite

### 4. **Player Screen** (Full-screen)
- Large raag artwork
- Play/pause controls
- Progress slider
- Shuffle & repeat buttons
- Back button to return

---

## 🐛 **Troubleshooting**

### Build Failed: "SDK location not found"
**Solution:**
1. Go to `File → Project Structure`
2. Click "SDK Location"
3. Ensure "Android SDK location" is set (usually `C:\Users\PRANSHI\AppData\Local\Android\Sdk`)
4. Click "Apply" → "OK"
5. Click "Sync Project with Gradle Files" (elephant icon)

### Build Failed: "Could not resolve dependencies"
**Solution:**
1. Check your internet connection
2. Click `File → Invalidate Caches → Invalidate and Restart`
3. After restart, click "Sync Project with Gradle Files"

### Emulator Won't Start
**Solution:**
1. Go to `Tools → AVD Manager`
2. Click the ▼ dropdown next to your device
3. Click "Cold Boot Now"

### App Crashes on Launch
**Solution:**
1. Check the **Logcat** tab at the bottom
2. Look for red error messages
3. Most common: Missing permissions (already added in manifest)

### Gradle Sync Takes Forever
**Solution:**
1. Wait at least 10 minutes (first sync downloads ~500MB)
2. Check bottom-right for progress
3. If stuck, click "Cancel" and retry

### "Unsupported Kotlin plugin version"
**Solution:**
1. Android Studio will prompt to update
2. Click "Update Kotlin Plugin"
3. Restart Android Studio

---

## 📊 **Build Progress Indicators**

Watch the bottom status bar:

```
✅ "Gradle: Resolving dependencies..." - Downloading libraries
✅ "Gradle: Build Running..." - Compiling code
✅ "Installing APK..." - Deploying to device
✅ "Launching app..." - Starting the app
```

**First build**: 2-5 minutes  
**Subsequent builds**: 30-60 seconds  
**Hot reload (UI changes)**: Instant!

---

## 🎯 **Expected First Build Time**

- **Gradle Sync**: 5-10 minutes (first time only)
- **Build**: 2-5 minutes (first time)
- **Install**: 30 seconds
- **Total**: ~10-15 minutes first time

**Subsequent builds**: 30-60 seconds! 🚀

---

## ✨ **After Successful Launch**

### Test These Features:

1. **Onboarding Flow**
   - ✅ Swipe through 3 pages
   - ✅ Check Terms & Privacy boxes
   - ✅ Click "Get Started"

2. **Authentication**
   - ✅ Enter email: `test@sangeetmind.com`
   - ✅ Enter password: `password123`
   - ✅ Click "Log In"

3. **Raag Library**
   - ✅ Search for "Bhairav"
   - ✅ Click heart icon to favorite
   - ✅ Click a raag card

4. **Player**
   - ✅ Click play button
   - ✅ Drag progress slider
   - ✅ Try shuffle/repeat buttons
   - ✅ Click back arrow

---

## 🔍 **Useful Android Studio Features**

### Logcat (View App Logs)
- Bottom tab → "Logcat"
- Filter by "sangeetmind" to see your app logs

### Layout Inspector (View UI Hierarchy)
- `Tools → Layout Inspector`
- Select your device
- See live UI structure

### Profiler (Monitor Performance)
- `View → Tool Windows → Profiler`
- Monitor CPU, Memory, Network

### Device File Explorer
- `View → Tool Windows → Device File Explorer`
- Browse app files on device

---

## 📝 **Quick Commands**

| Action | Shortcut |
|--------|----------|
| Run app | `Shift + F10` |
| Stop app | `Ctrl + F2` |
| Build project | `Ctrl + F9` |
| Sync Gradle | `Ctrl + Shift + O` |
| Find file | `Ctrl + Shift + N` |
| Search everywhere | `Shift + Shift` |
| Logcat | `Alt + 6` |

---

## 🎨 **Project Structure in Android Studio**

```
Project view (left sidebar):
├── app/                    ← Main application
│   ├── manifests/         ← AndroidManifest.xml
│   ├── java/              ← Kotlin source files
│   └── res/               ← Resources (layouts, strings)
├── core/                   ← Core modules
│   ├── audio/             ← ExoPlayer service
│   ├── network/           ← Retrofit API
│   └── ui/                ← Theme & components
├── features/              ← Feature modules
│   ├── onboarding/
│   ├── auth/
│   ├── raaglibrary/
│   ├── player/
│   ├── meditation/
│   ├── astrology/
│   └── settings/
└── Gradle Scripts/        ← Build configuration
```

---

## 🎉 **Success Indicators**

You'll know it's working when:

✅ No red errors in "Build" tab  
✅ "BUILD SUCCESSFUL" message appears  
✅ App icon appears on emulator/device  
✅ Onboarding screen shows up  
✅ You can interact with the UI  

---

## 📞 **Need Help?**

If you encounter issues:

1. Check **Logcat** for error messages
2. Review **Build** tab for compilation errors
3. Check `SETUP_GUIDE.md` for detailed troubleshooting
4. Verify all SDK components are installed (`Tools → SDK Manager`)

---

## 🚀 **You're Ready!**

Once the app launches successfully:
- ✅ Explore all 7 features
- ✅ Test the navigation flow
- ✅ Check out the beautiful Material3 UI
- ✅ See the bilingual content (EN/HI)
- ✅ Try the mock audio player

**Happy coding!** 🎊

---

**Next Steps:**
- Integrate real backend API
- Add Google Sign-In
- Implement voice search
- Connect to actual audio streaming
- Add analytics
- Prepare for Play Store release

**The foundation is complete - now build something amazing!** ✨

