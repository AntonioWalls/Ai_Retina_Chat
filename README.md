# 👁️ Retina AI Chat - Android App

Retina AI Chat is an advanced Android application built to assist ophthalmologists in diagnosing retinal images. It provides a secure, modern chat interface where medical professionals can upload retinal scans and receive instant analysis powered by a custom fine-tuned LLaMA 3 Vision AI model.

- Note: This repository contains the Android (Frontend) codebase. The backend API (Python/Colab/Ngrok) and the fine-tuned LLM model are kept in a separate, private repository.

✨ Features

- **Modern UI/UX:** Built entirely with Jetpack Compose, following a sleek, futuristic dark-mode design ("Obsidian" style).

- **Secure Authentication:** User registration and login handled via Firebase Authentication (Email/Password & Google Sign-In ready).

- **Clean Architecture:** Strictly adheres to the MVVM (Model-View-ViewModel) pattern, ensuring separation of concerns and highly maintainable code.

- **Reactive State Management:** Utilizes Kotlin Coroutines and StateFlow for smooth, crash-free, asynchronous operations.

- **Multimodal API Integration:** Connects via Retrofit to a custom Python backend, handling multipart form data (Images + Text).

## 🛠️ Tech Stack

- **Language:** Kotlin

- **UI Framework:** Jetpack Compose

- **Architecture:** MVVM, Single Source of Truth

- **Network:** Retrofit2 & Gson

- **Image Loading:** Coil

- **Backend as a Service (BaaS):** Firebase (Auth)

- **Asynchronous Programming:** Kotlin Coroutines & Flow

## 🚀 How to Run Locally
If you wish to clone and build this project:

1. Clone the repository: git clone https://github.com/YourUsername/Retina-AI-Android.git

2. Open the project in Android Studio.

3. **Important:** You must add your own google-services.json file inside the app/ directory. You can generate one by creating a project in the Firebase Console.

3. Sync Gradle and Run on an emulator or physical device (API 26+).
