# SangeetMind Android - Development Environment Setup Guide

This guide will help you set up your development environment to build and run the SangeetMind Android app.

## Prerequisites

Before you begin, ensure you have:
- Windows 10/11 (64-bit)
- At least 8GB RAM (16GB recommended)
- 10GB free disk space
- Stable internet connection

## Step 1: Install Java Development Kit (JDK 17)

### Option A: Using Chocolatey (Recommended)

1. Open PowerShell as Administrator
2. Install Chocolatey if not already installed:
   ```powershell
   Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
   ```

3. Install JDK 17:
   ```powershell
   choco install openjdk17 -y
   ```

4. Verify installation:
   ```powershell
   java -version
   ```
   Expected output: `openjdk version "17.x.x"`

### Option B: Manual Installation

1. Download JDK 17 from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
   - Choose: Windows x64, JDK, .msi installer

2. Run the installer
   - Check "Set JAVA_HOME variable"
   - Check "Add to PATH"

3. Verify installation:
   ```cmd
   java -version
   javac -version
   ```

### Set JAVA_HOME (if not set automatically)

1. Open System Properties:
   - Press `Win + X` → System → Advanced system settings
   - Click "Environment Variables"

2. Under "System variables":
   - Click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` (adjust path)

3. Edit "Path" variable:
   - Add: `%JAVA_HOME%\bin`

4. Restart your terminal and verify:
   ```cmd
   echo %JAVA_HOME%
   java -version
   ```

## Step 2: Install Android Studio

### Download and Install

1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)

2. Run the installer:
   - Choose "Standard" installation
   - Accept licenses
   - Wait for SDK components to download

3. On first launch:
   - Complete the setup wizard
   - Install Android SDK, SDK Platform, and Android Virtual Device

### Configure Android SDK

1. Open Android Studio
2. Go to: `File → Settings → Appearance & Behavior → System Settings → Android SDK`

3. **SDK Platforms** tab:
   - ✅ Android 14.0 (API 34) - Required for this project
   - ✅ Android 13.0 (API 33)
   - ✅ Android 12.0 (API 31)

4. **SDK Tools** tab:
   - ✅ Android SDK Build-Tools 34
   - ✅ Android SDK Command-line Tools
   - ✅ Android Emulator
   - ✅ Android SDK Platform-Tools
   - ✅ Google Play services

5. Click "Apply" and wait for downloads to complete

### Set ANDROID_HOME Environment Variable

1. Open Environment Variables (as above)

2. Under "System variables":
   - Click "New"
   - Variable name: `ANDROID_HOME`
   - Variable value: `C:\Users\YourUsername\AppData\Local\Android\Sdk`

3. Edit "Path" variable, add:
   - `%ANDROID_HOME%\platform-tools`
   - `%ANDROID_HOME%\tools`
   - `%ANDROID_HOME%\tools\bin`

4. Verify:
   ```cmd
   echo %ANDROID_HOME%
   adb --version
   ```

## Step 3: Configure Android Studio for the Project

### Import the Project

1. Open Android Studio
2. Click "Open" (not "New Project")
3. Navigate to `C:\Users\PRANSHI\sangeetmind-android`
4. Click "OK"

### First-Time Setup

Android Studio will:
- Detect the Gradle wrapper
- Download Gradle 8.3
- Sync dependencies (this may take 5-10 minutes)

If you see errors:
- Click "Sync Project with Gradle Files" (elephant icon)
- Click "Build → Clean Project"
- Click "Build → Rebuild Project"

### Configure JDK in Android Studio

1. Go to: `File → Project Structure → SDK Location`
2. Ensure "JDK location" points to your JDK 17 installation
3. If not, click "..." and select: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`

## Step 4: Create an Android Virtual Device (AVD)

### Using Device Manager

1. In Android Studio, click the "Device Manager" icon (phone icon)
2. Click "Create Device"
3. Select a device:
   - **Recommended**: Pixel 6 or Pixel 7
4. Select a system image:
   - **API Level**: 34 (Android 14.0)
   - **ABI**: x86_64
   - Click "Download" if not already downloaded
5. Click "Next" → "Finish"

### Alternative: Use a Physical Device

1. Enable Developer Options on your Android phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → System → Developer Options

2. Enable USB Debugging

3. Connect phone via USB

4. On your PC, verify:
   ```cmd
   adb devices
   ```
   You should see your device listed

## Step 5: Build and Run the Project

### Using Android Studio

1. Wait for Gradle sync to complete
2. Select your device/emulator from the dropdown
3. Click the green "Run" button (▶️) or press `Shift + F10`

### Using Command Line

1. Open PowerShell in the project directory:
   ```powershell
   cd C:\Users\PRANSHI\sangeetmind-android
   ```

2. Build the project:
   ```powershell
   .\gradlew.bat build
   ```

3. Install on device:
   ```powershell
   .\gradlew.bat :app:installDebug
   ```

4. Launch the app manually on your device

## Step 6: Run Tests

### Unit Tests

```powershell
.\gradlew.bat test
```

View results in: `app/build/reports/tests/testDebugUnitTest/index.html`

### Instrumentation Tests

1. Ensure device/emulator is running
2. Run:
   ```powershell
   .\gradlew.bat connectedAndroidTest
   ```

## Troubleshooting

### "JAVA_HOME is not set"

**Solution**:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
.\gradlew.bat build
```

Or set permanently via Environment Variables (see Step 1).

### "SDK location not found"

**Solution**:
1. Create `local.properties` in project root:
   ```properties
   sdk.dir=C\:\\Users\\PRANSHI\\AppData\\Local\\Android\\Sdk
   ```
2. Adjust path to your actual SDK location

### "Gradle sync failed"

**Solution**:
1. Click "File → Invalidate Caches → Invalidate and Restart"
2. Delete `.gradle` folder in project root
3. Run: `.\gradlew.bat clean build`

### "Unable to find target with hash string 'android-34'"

**Solution**:
1. Open SDK Manager in Android Studio
2. Install Android 14.0 (API 34)
3. Sync project again

### "Execution failed for task ':app:compileDebugKotlin'"

**Solution**:
1. Ensure JDK 17 is being used
2. Check `File → Project Structure → SDK Location`
3. Clean and rebuild: `.\gradlew.bat clean build`

### Emulator is slow

**Solution**:
1. Ensure Intel HAXM or AMD Hypervisor is installed
2. In BIOS, enable virtualization (VT-x/AMD-V)
3. Use x86_64 system images (not ARM)
4. Allocate more RAM to emulator in AVD settings

## Quick Start Script

Save this as `setup.ps1` and run in PowerShell:

```powershell
# Check Java
Write-Host "Checking Java installation..." -ForegroundColor Cyan
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java not found. Please install JDK 17." -ForegroundColor Red
    exit 1
}

# Check Android SDK
Write-Host "`nChecking Android SDK..." -ForegroundColor Cyan
if (Test-Path $env:ANDROID_HOME) {
    Write-Host "Android SDK found at: $env:ANDROID_HOME" -ForegroundColor Green
} else {
    Write-Host "ANDROID_HOME not set. Please set it to your SDK location." -ForegroundColor Red
    exit 1
}

# Check ADB
Write-Host "`nChecking ADB..." -ForegroundColor Cyan
adb version
if ($LASTEXITCODE -ne 0) {
    Write-Host "ADB not found. Please add Android SDK platform-tools to PATH." -ForegroundColor Red
    exit 1
}

# Build project
Write-Host "`nBuilding project..." -ForegroundColor Cyan
.\gradlew.bat clean build

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Setup complete! Project built successfully." -ForegroundColor Green
} else {
    Write-Host "`n❌ Build failed. Check errors above." -ForegroundColor Red
}
```

## Recommended IDE Plugins

Install these in Android Studio for better development experience:

1. **Kotlin** (pre-installed)
2. **Compose Multiplatform IDE Support**
3. **Rainbow Brackets** - Color-matched brackets
4. **Key Promoter X** - Learn keyboard shortcuts
5. **GitToolBox** - Enhanced Git integration

Install via: `File → Settings → Plugins → Marketplace`

## Next Steps

Once your environment is set up:

1. ✅ Verify the app builds: `.\gradlew.bat build`
2. ✅ Run unit tests: `.\gradlew.bat test`
3. ✅ Launch the app on emulator/device
4. 📝 Start implementing remaining features (see TODO comments in code)
5. 🔗 Integrate with your backend API
6. 🚀 Prepare for Play Store release

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Material Design 3](https://m3.material.io/)

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review error messages in Android Studio's "Build" tab
3. Check `CONTRIBUTING.md` for development guidelines
4. Open an issue on GitHub with error details

---

**Last Updated**: 2025-11-15  
**Tested On**: Windows 11, Android Studio Hedgehog

