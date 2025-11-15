# Contributing to SangeetMind Android

Thank you for your interest in contributing to SangeetMind! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for all contributors.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/yourusername/sangeetmind-android.git
   cd sangeetmind-android
   ```
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Workflow

### Before You Start

- Ensure you have the latest code from `main`:
  ```bash
  git checkout main
  git pull origin main
  ```
- Create a new branch for your work
- Check that the project builds successfully:
  ```bash
  ./gradlew build
  ```

### Making Changes

1. **Write clean, readable code** following Kotlin coding conventions
2. **Add tests** for new features or bug fixes
3. **Update documentation** if you change APIs or add features
4. **Follow the existing architecture** (MVVM, Clean Architecture, multi-module structure)
5. **Use Hilt for dependency injection**
6. **Keep commits atomic** and focused on a single change

### Commit Message Format

Use clear, descriptive commit messages following this format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, tooling, dependencies

**Examples:**
```
feat(raaglibrary): add filter by time of day

Implement filtering UI with chips for morning, evening, night.
Update ViewModel to handle filter state.

Closes #123
```

```
fix(audio): resolve ExoPlayer memory leak

Release player instance properly in onDestroy.
Add null checks for media session.
```

```
docs(readme): update Play Store name configuration

Add instructions for setting PLAY_STORE_DISPLAY_NAME property.
```

### Module Ownership

When contributing to a specific module, follow these guidelines:

| Module | Focus | Key Considerations |
|--------|-------|-------------------|
| `:core:*` | Shared infrastructure | Keep dependencies minimal, avoid feature-specific logic |
| `:features:*` | Feature implementation | Self-contained, communicate via navigation or shared state |
| `:libs:models` | Domain models | Pure Kotlin, no Android dependencies |
| `:integration:backend-stub` | Mock data | Keep in sync with real API contracts |

### Testing

- **Write unit tests** for ViewModels and business logic
- **Write Compose UI tests** for complex screens
- Run tests before submitting:
  ```bash
  ./gradlew test
  ./gradlew connectedAndroidTest
  ```

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Prefer immutability and functional patterns

### Linting

Run lint checks before submitting:
```bash
./gradlew lint
```

Fix any errors or warnings reported.

## Submitting a Pull Request

1. **Push your branch** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open a Pull Request** on GitHub:
   - Provide a clear title and description
   - Reference any related issues (e.g., "Closes #123")
   - Add screenshots or videos for UI changes
   - Ensure CI checks pass

3. **Respond to feedback**:
   - Address review comments promptly
   - Push additional commits to your branch as needed
   - Request re-review when ready

4. **Squash commits** if requested by maintainers

## Feature Requests and Bug Reports

### Reporting Bugs

Use the GitHub issue tracker and include:
- **Description**: Clear summary of the bug
- **Steps to reproduce**: Detailed steps
- **Expected behavior**: What should happen
- **Actual behavior**: What actually happens
- **Environment**: Device, Android version, app version
- **Logs**: Relevant logcat output or stack traces

### Requesting Features

Use the GitHub issue tracker and include:
- **Use case**: Why is this feature needed?
- **Proposed solution**: How should it work?
- **Alternatives**: Other approaches you've considered
- **Additional context**: Mockups, examples, references

## Development Tips

### Building Specific Modules

```bash
./gradlew :core:network:build
./gradlew :features:raaglibrary:test
```

### Running on Device

```bash
./gradlew :app:installDebug
adb shell am start -n com.sangeetmind.app.debug/.MainActivity
```

### Viewing Logs

```bash
adb logcat | grep -i sangeetmind
```

### Debugging Hilt Issues

If you encounter Hilt compilation errors:
1. Clean the project: `./gradlew clean`
2. Rebuild: `./gradlew build`
3. Invalidate caches in Android Studio

## Questions?

If you have questions about contributing:
- Check existing issues and discussions
- Open a new issue with the `question` label
- Reach out to maintainers

Thank you for contributing to SangeetMind! 🎵

