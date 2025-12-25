# Fitness Tracker App 🏋️‍♂️📈

A modern, native Android application designed to help users track their daily fitness activities and monitor their progress towards weekly calorie goals. This project was developed as a school assignment to demonstrate proficiency in Android development using modern architectural patterns and libraries.

## 🚀 Key Features

- **User Authentication**: Secure Login and Registration flow connected to a REST API.
- **Dynamic Dashboard**: A central home screen displaying weekly calorie goals with a real-time progress bar.
- **Activity Tracking**: Log various activities including Walking, Running, Swimming, Jumping Rope, and Cycling.
- **Goal Management**: Set and update weekly calorie targets with visual feedback (ProgressBar turns green when the goal is met! 🎉).
- **Activity History**: View a detailed list of past exercises with the ability to filter by date range.
- **Profile Management**: Manage personal data such as Weight, Height, Phone, and Gender to ensure accurate tracking.

## 🏗 Architecture

The app follows the **MVVM (Model-View-ViewModel)** architectural pattern, ensuring a clean separation of concerns:
- **UI Layer**: Fragments and XML layouts for a responsive user interface.
- **ViewModel Layer**: Manages UI state and business logic, leveraging LiveData for data observation.
- **Data Layer**: Repositories abstracting the data source (Remote API via Retrofit).

## 🛠 Tech Stack

- **Language**: Kotlin
- **Networking**: Retrofit 2 & OkHttp (with Logging Interceptor)
- **Async Processing**: Kotlin Coroutines (Suspend functions)
- **UI Components**: XML Layouts, Material Design, Navigation Component, ViewBinding
- **Backend**: PHP REST API (MySQL Database)

## ⚙️ Setup & Configuration

1. **Clone the Project**: Open the project in Android Studio.
2. **API Configuration**:
   - Locate `data/remote/RetrofitClient.kt`.
   - Update the `BASE_URL` constant with your server's local IP address (e.g., `http://192.168.x.x:8000/api/`).
3. **Local Server**: Ensure your PHP server is running and accessible from the same network as your Android device/emulator.
4. **Build & Run**: Use the standard Android Studio build tools to install the app on your device.

## 🎓 Academic Note

This project was built for educational purposes as part of a school assignment. It focuses on implementing robust networking, session management, and a clean MVVM architecture in a professional Android environment.
