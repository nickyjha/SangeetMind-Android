# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in SangeetMind Android, please report it responsibly:

1. **Do not** open a public GitHub issue
2. **Email** the maintainers directly at: security@sangeetmind.com (or create a private security advisory on GitHub)
3. **Include**:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will respond within **48 hours** and work with you to address the issue.

## Security Best Practices

### API Keys and Secrets

**Never commit sensitive data to the repository:**

- API keys
- OAuth client secrets
- Signing keys or keystores
- Database credentials
- Authentication tokens

### Storing Secrets Locally

Use `local.properties` (gitignored) for local development:

```properties
# local.properties
API_KEY=your_api_key_here
GOOGLE_CLIENT_ID=your_client_id_here
```

Access in `build.gradle.kts`:

```kotlin
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    defaultConfig {
        buildConfigField("String", "API_KEY", "\"${localProperties.getProperty("API_KEY", "")}\"")
    }
}
```

### CI/CD Secrets

For GitHub Actions, use **GitHub Secrets**:

1. Go to repository Settings → Secrets and variables → Actions
2. Add secrets (e.g., `API_KEY`, `KEYSTORE_PASSWORD`)
3. Reference in workflows:

```yaml
- name: Build release
  env:
    API_KEY: ${{ secrets.API_KEY }}
  run: ./gradlew assembleRelease
```

### Network Security

- **Use HTTPS only** for API calls
- **Validate SSL certificates** (do not disable certificate validation)
- **Implement certificate pinning** for production (optional but recommended)

### Authentication

- **Store tokens securely** using EncryptedSharedPreferences or DataStore with encryption
- **Implement token refresh** logic to avoid storing long-lived tokens
- **Use OAuth 2.0** for third-party authentication (Google Sign-In)
- **Never log sensitive data** (tokens, passwords, PII)

### Data Privacy

- **Request permissions at runtime** (RECORD_AUDIO, POST_NOTIFICATIONS)
- **Provide clear privacy policy** explaining data collection and usage
- **Implement GDPR/CCPA compliance** (consent screens, data deletion)
- **Encrypt local databases** if storing sensitive user data

### ProGuard/R8

For release builds, enable code obfuscation:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

Keep rules for serialization libraries (Moshi, Retrofit) in `proguard-rules.pro`.

### Dependencies

- **Keep dependencies up to date** to patch known vulnerabilities
- **Review dependency changes** in PRs
- **Use Dependabot** or Renovate for automated updates
- **Audit dependencies** periodically:
  ```bash
  ./gradlew dependencies
  ```

### Code Review

- **Review all PRs** for security issues before merging
- **Check for hardcoded secrets** or credentials
- **Validate input** from user or network sources
- **Sanitize data** before displaying in UI (prevent injection attacks)

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

We provide security updates for the latest stable release only.

## Acknowledgments

We appreciate responsible disclosure and will credit security researchers (with permission) in release notes.

---

**Last updated**: 2025-11-15

