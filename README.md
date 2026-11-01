# Xenci Launcher

**Modern, Fast & Highly Customizable Android Launcher (Based on Neo Launcher & Launcher3)**

[![Base: Neo Launcher](https://img.shields.io/badge/Based%20On-Neo%20Launcher-blue?style=flat&logo=android)](https://github.com/NeoApplications/Neo-Launcher)
[![GitHub repo stars](https://img.shields.io/github/stars/Patr10on/Xenci-Launcher?style=flat)](https://github.com/Patr10on/Xenci-Launcher/stargazers)
[![GitHub License](https://img.shields.io/github/license/Patr10on/Xenci-Launcher)](https://github.com/Patr10on/Xenci-Launcher/blob/main/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/Patr10on/Xenci-Launcher?display_name=tag)](https://github.com/Patr10on/Xenci-Launcher/releases/latest)

---

## 🚀 Overview

> 📱 **Fork & Heritage:** **Xenci Launcher** is an enhanced, feature-rich edition built directly on top of the open-source **[Neo Launcher](https://github.com/NeoApplications/Neo-Launcher)** and AOSP **Launcher3** codebases, developed and maintained by **[Patr10on (Xenci)](https://github.com/Patr10on)**.

While preserving Neo Launcher\'s lightweight, clean design and extensive customizability, **Xenci Launcher** introduces brand-new privacy vaults, biometric app locking, iOS-style gesture navigation, and complete 16 KB page-size compatibility for Android 11 through Android 17.

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

## 📜 Credits & Acknowledgments

- **Enhanced & Maintained by:**
  - **[Patr10on (Xenci)](https://github.com/Patr10on)** — Xiaomi hidden apps vault, biometric/PIN security, custom app lock, iOS navigation bar, and 16 KB page-size zero-native engine.

- **Original Base Project & Developers:**
  - **[Neo Launcher](https://github.com/NeoApplications/Neo-Launcher)** by **[Neo Applications](https://github.com/NeoApplications)**
  - **[Saul Henriquez (saulhdev)](https://github.com/saulhdev)** — Creator & Lead Developer of Neo Launcher / Omega
  - **[Antonios Hazim (machiav3lli)](https://github.com/machiav3lli)** — Core Contributor & UI/Design
  - **[Rafael Venâncio (nonaybay)](https://github.com/nonaybay)** — Contributor & F-Droid repository maintainer
  - **[Lawnchair Team](https://github.com/LawnchairLauncher/lawnchair)** & **[AOSP Launcher3](https://android.googlesource.com/platform/packages/apps/Launcher3/)** — Foundational launcher architecture

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0 (GPLv3)**. See [LICENSE](LICENSE) for details.
