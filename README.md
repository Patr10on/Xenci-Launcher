# Xenci Launcher

**Modern, Fast & Highly Customizable Android Launcher**

[![GitHub repo stars](https://img.shields.io/github/stars/Patr10on/Xenci-Launcher?style=flat)](https://github.com/Patr10on/Xenci-Launcher/stargazers)
[![GitHub License](https://img.shields.io/github/license/Patr10on/Xenci-Launcher)](https://github.com/Patr10on/Xenci-Launcher/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/Patr10on/Xenci-Launcher?display_name=tag)](https://github.com/Patr10on/Xenci-Launcher/releases/latest)

---

## 🚀 Overview

**Xenci Launcher** is an advanced Android launcher based on AOSP / Launcher3 and Neo Launcher, extensively enhanced and maintained by **[Patr10on (Xenci)](https://github.com/Patr10on)**. 

It combines smooth performance, deep personalization, and next-generation privacy features with full modern Android compatibility (Android 11 to Android 17, 32-bit and 64-bit).

---

## ✨ Features Added by Xenci (Patr10on)

- 🔒 **Xiaomi-Style Hidden Apps Vault:**
  - Fast, intuitive two-finger **Pinch-Out** gesture on the home screen to access hidden applications.
  - Dedicated private drawer with real-time StackBlur background effect.
  - Automatic filtering from the main app drawer.

- 🛡️ **Biometric & PIN Security Layer:**
  - Hardware-backed biometric authentication (`androidx.biometric:biometric` Fingerprint / Face Unlock).
  - Fallback to device PIN/Pattern or custom launcher PIN.
  - Search and filter bar in hidden and locked app settings.

- 🔐 **Per-App Custom App Lock:**
  - Lock individual sensitive apps with biometrics or a custom PIN before launch.

- 📱 **iPhone-Style Gesture Navigation Bar (iOS Pill):**
  - Fully customizable floating gesture bar at the bottom of the screen.
  - Adjustable thickness, width, corner radius, margin, and haptic feedback.
  - Configurable gesture triggers: Double Tap, Long Press, Swipe Up, Swipe Up & Hold, Swipe Left/Right.
  - Ability to hide the system Android navigation bar for an immersive experience.

- ⚡ **16 KB Page Size Alignment & Zero-Native DEX Engine:**
  - 100% compliant with Android 15+ 16 KB page size requirements.
  - Replaced native `.so` dependencies with a pure Kotlin integer-arithmetic `StackBlur` engine and pure Java `org.lsposed.hiddenapibypass`.
  - Zero `UnsatisfiedLinkError` crashes across all custom ROMs (crDroid, LineageOS, Pixel Experience, etc.).

- 🎨 **Deep Interface Opacity & Theming:**
  - Sliders for folder background opacity, search bar opacity, and navigation pill opacity.
  - Modern Xenci gradient branding and full Android 13+ Material You monochrome dynamic icon support.

---

## 📥 Downloads & Releases

Pre-compiled APK builds are available on GitHub Releases:

👉 **[Download Latest APK from GitHub Releases](https://github.com/Patr10on/Xenci-Launcher/releases)**

---

## 🛠️ Building from Source

### Prerequisites:
- JDK 17
- Android SDK (API 33, Build Tools 34.0.0)

### Build Command:
```bash
./gradlew assembleAospWithQuickstepOmegaDebug
```
The resulting APK will be generated at `build/outputs/apk/aospWithQuickstepOmega/debug/`.

---

## 📜 Credits & License

- Maintained and enhanced by **[Patr10on (Xenci)](https://github.com/Patr10on)**
- Based on AOSP Launcher3, Lawnchair, and Neo Launcher by Saul Henriquez and contributors.
- Licensed under the **GNU General Public License v3.0 (GPLv3)**.
