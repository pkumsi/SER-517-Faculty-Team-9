# CARMA Android App & Backend

This repository contains the CARMA system, which provides context-aware auto-responses for incoming messages using a Go backend and an Android mobile app. The backend uses sensor/context data and an LLM to generate polite, situation-appropriate replies. The Android app collects context, previews responses, and manages user feedback.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Architecture](#architecture)
- [Backend Setup](#backend-setup)
- [Android App Setup](#android-app-setup)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Monitoring & Logging](#monitoring--logging)
- [Development Notes](#development-notes)

---

## Project Overview

- **Backend:** Go REST API (Gin) for generating auto-responses using OpenRouter LLM.
- **Frontend:** Android app for previewing, editing, and sending auto-responses, with local Room database for message/feedback history.

## Features
- Context-aware auto-response generation
- Mobile context collection (activity, sender, urgency, etc.)
- Message preview, edit, and feedback (thumbs up/down)
- Local message/feedback history (Room DB)
- REST API for integration
- Prometheus metrics and logging

---

## Architecture

```
[Android App] <----> [Go Backend API] <----> [OpenRouter LLM]
```
- Android app collects context, displays preview, and sends feedback.
- Backend receives context, infers user state, builds prompt, and calls LLM.
- LLM generates a response, which is returned to the app.

---

## Backend Setup

### Prerequisites
- Go 1.21+
- (Recommended) [Homebrew](https://brew.sh) for macOS

### Install Go (macOS)
```zsh
brew install go
```
Or download from [go.dev/dl](https://go.dev/dl/).

### Configure Environment
```zsh
cp configs/.env.example configs/.env
# Edit configs/.env and set your OpenRouter API key
```

### Run the Server
```zsh
cd backend
go run ./cmd/api
```
Server runs at `http://localhost:8080`.

---

## Android App Setup

### Prerequisites
- Java 21 JDK (e.g., Temurin 21)
- Android Studio (latest recommended)
- Gradle (wrapper included)

### Install Java 21 (macOS)
```zsh
brew install --cask temurin@21
java -version # Should show version 21
```

### Build the App
```zsh
cd CARMA_android_app
./gradlew clean :app:assembleDebug
```
The APK will be in `CARMA_android_app/app/build/outputs/apk/debug/`.

### Run in Android Studio
- Open `CARMA_android_app` in Android Studio.
- Sync Gradle and build the project.
- Run on an emulator or device (minSdk 21).

---

## Database Schema

- Local Room DB with two tables: `messages` and `feedback`.
- See [`Database_Schema_Design.md`](CARMA_android_app/app/src/main/Database_Schema_Design.md) for full schema, ERD, and rationale.

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET    | `/` | Health check |
| GET    | `/health` | Health check |
| POST   | `/api/v1/response` | Generate auto-response |
| POST   | `/api/v1/feedback` | Submit feedback |
| GET    | `/api/v1/statistics/messages` | Message stats |

See [README.md](backend/README.md) for full request/response examples.

---

## Monitoring & Logging
- Prometheus metrics at `/metrics` (see [Monitoring.md](backend/cmd/api/Monitoring.md))
- Log file: `api.log` (see [Logging.md](backend/cmd/api/Logging.md))

---

## Development Notes
- See [`DEV_SETUP.md`](DEV_SETUP.md) for Java/Gradle/CI setup.
- Android app uses Room for local storage, Material UI, and modern Android best practices.
- Backend is modular (handlers, services, models) and easy to extend.

---

## How to Run the Full System

1. **Start the Backend:**
   ```zsh
   cd backend
   go run ./cmd/api
   ```
2. **Build & Run the Android App:**
   - Open `CARMA_android_app` in Android Studio, or
   - Build APK with Gradle and install on device/emulator.
3. **Test End-to-End:**
   - Send a message to the device (simulate notification or use test UI).
   - Preview, edit, and send auto-response in the app.
   - Give feedback (thumbs up/down) in the app.
   - Check backend logs and Prometheus metrics.

---

## Contact & Contribution
- For questions, open an issue or contact the maintainers.
- Contributions welcome! See `CONTRIBUTING.md` if available.
