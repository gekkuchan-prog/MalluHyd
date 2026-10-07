# Mallu Gang — Deployment, Build & GitHub Release Guide

## 1. Project Repository Structure

```
MalluGang/
├── app/                  # Android Native / Compose Applet Source
├── backend/              # Supabase / PostgreSQL schema & Edge Functions
├── windows/              # Windows cross-platform runner configs
├── docs/                 # System specs, privacy & moderation rules
├── ARCHITECTURE.md       # Full data & permission architecture
├── BUILD.md              # Build & CI/CD instructions
└── README.md             # Project introduction & feature breakdown
```

## 2. Android Build

Requirements:
- Android SDK 34+
- JDK 17 / 21
- Gradle 8.11+

Command:
```bash
gradle assembleRelease
```
Generates APK artifact at:
`app/build/outputs/apk/release/MalluGang-Android-v1.0.apk`

## 3. Windows Desktop Build

Cross-platform distribution for Windows is handled via Compose Multiplatform / Flutter desktop runner:
```bash
flutter build windows --release
# OR via Gradle Compose Desktop:
./gradlew packageMsi
```
Generates Windows installer:
`build/windows/runner/Release/MalluGang-Windows-v1.0.exe`

## 4. GitHub Releases & CI/CD Pipeline

When a tag (e.g. `v1.0.0`) is pushed to GitHub, GitHub Actions automates:
1. Matrix build for Android APK (`assembleRelease`) and Windows EXE
2. SHA-256 checksum generation for verified integrity
3. Automatic Draft/Release publish with Changelog to GitHub Releases
4. Secrets (SUPABASE_URL, API_KEY) managed through repository secrets or AI Studio Secrets panel.
