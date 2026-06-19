# Stream URL

Stream URL is a lightweight and modern Android video player designed to help you capture and share video stream URLs from other applications. 

Whether you're using modded social media apps or just need to grab a direct stream link, Stream URL provides a clean interface to play the media and instantly share or copy its source link.

## ✨ Features

- **URL Catcher**: Seamlessly intercepts video links from other apps via the "Open with" or "Share" menu.
- **Modern Video Player**: High-performance playback using **Jetpack Media3 (ExoPlayer)**, supporting a wide range of stream formats.
- **URL Shortening**: Built-in support to shorten long, messy stream URLs using `is.gd` before sharing.
- **Material You**: Beautiful, modern UI that supports Dynamic Colors (Android 12+) and full Dark Theme.
- **Fullscreen Mode**: Immersive viewing experience with aspect-ratio aware rotation.
- **Privacy Focused**: Lightweight with minimal permissions. No tracking, no history, just a simple utility.

## 📱 How to Use

1. **Capture**: When you find a video in another app, choose **Share** or **Open With** and select **Stream URL**.
2. **View & Play**: The app opens, loads the video, and displays the full stream URL at the bottom.
3. **Copy or Share**: 
    - Tap **Copy** to put the raw URL on your clipboard.
    - Tap **Share** to automatically shorten the link and send it to WhatsApp, Telegram, or any other app.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Material Design 3 (Views-based)
- **Video Engine**: Jetpack Media3 (ExoPlayer)
- **Network**: Native HttpURLConnection for lightweight link shortening.

## 🏗 Build Requirements

- Android Studio Jellyfish or newer
- JDK 17
- Android SDK 33+

## 🙏 Acknowledgments

- **is.gd**: For providing a simple and reliable URL shortening API.
- **Jetpack Media3**: For the high-performance ExoPlayer engine.

## 📄 License

Copyright 2026 pdklabs.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
