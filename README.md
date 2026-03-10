# 🚀 STELLAR DRIFT

> **An endless space survival game — made entirely by AI, coded in pure Java + Android Canvas API. No game engine. No bitmaps. Every pixel is drawn programmatically.**

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Engine-None_(Pure_Canvas)-00E5FF?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Version-2.0-7C4DFF?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Min_SDK-21-FF6D00?style=for-the-badge" />
</p>

---

# 🎮 About

**Stellar Drift** is an infinite space dodging game where you pilot a neon-lit spacecraft through asteroid fields, collect stardust, and survive as long as possible.

The entire game — graphics, sound effects, and UI — is created using nothing but code.

No external assets.  
No sprites.  
No audio files.

Every visual element is rendered in real time using Android's **Canvas API** (`Path`, `Paint`, `LinearGradient`, `RadialGradient`) and mathematical functions.

Every sound effect is synthesized programmatically using **AudioTrack** and sine waves.

---

# ✨ Features

## 🕹️ Controls

- **Virtual Joystick** — Touch anywhere to spawn a joystick
- **Full freedom** — Move in all directions
- **Ship banking** — Ship tilts up to 12° based on direction
- **Dead zone** — Prevents accidental small movements

---

## 🌌 Gameplay

- Endless survival gameplay
- Combo system (x2, x3, x4...)
- Near-miss bonus system
- Risk window multiplier (1.5x)
- Overdrive mode (combo x8)
- Dynamic tempo system:

| Phase | Description |
|------|-------------|
| 🔵 CALM | Normal pace |
| 🔴 PRESSURE | Fast asteroid waves |
| 🟡 REWARD | Stardust rain |

Additional mechanics:

- 3-second warmup
- Score milestones
- Dynamic asteroid spawning

---

## ⚡ Power-Ups

| Power-Up | Effect | Color |
|----------|--------|-------|
| 🧲 MAGNET | Pulls stardust | Gold |
| 🕐 SLOW-MO | Slows time | Blue |
| ✖️ DOUBLE | Double points | Green |
| 🛡️ SHIELD | Absorb one hit | Cyan |

---

## 🎨 Visual System (100% Procedural)

- Procedural spaceship rendering
- Neon glow trails
- 3-layer parallax starfield
- Nebula clouds
- Random polygon asteroids
- Particle explosion system
- Screen shake on impact
- Death shockwave
- Spawn warning indicators
- Risk window golden border
- Floating score popups
- Neon virtual joystick UI

---

## 🔊 Audio (Procedural Synthesis)

Generated entirely using **AudioTrack**.

Sound effects include:

- Stardust pickup
- Explosion rumble
- Button click
- Game over melody

No `.mp3` or `.wav` files exist in the project.

---

## 📳 Haptic Feedback

- Collection vibration
- Explosion vibration pattern
- Button micro-tick

Compatible with modern and legacy Android devices.

---

## ⚙️ Settings

| Setting | Options |
|-------|--------|
| Difficulty | EASY / NORMAL / HARD |
| Game Speed | SLOW / NORMAL / FAST |
| Sound | ON / OFF |
| Vibration | ON / OFF |

All settings persist between sessions.

---

## 📊 Game Over Screen

Includes:

- Score count-up animation
- Restart delay (anti-misclick)
- Game statistics
- New record celebration

---

# 📦 Build & Install

## Option A — GitHub Actions

1. Fork the repository
2. Open **Actions**
3. Run **Build Stellar Drift APK**
4. Download the APK from **Artifacts**

---

## Option B — Local Build (Termux)

    git clone https://github.com/omersusin/StellarDrift.git
    cd StellarDrift
    gradle wrapper --gradle-version 8.2
    chmod +x gradlew
    ./gradlew assembleDebug

---

## Option C — Android Studio

1. Clone the repo
2. Open in Android Studio
3. Build APK

---

# 🎯 How to Play

1. Tap **PLAY**
2. Touch screen to spawn joystick
3. Drag to move
4. Dodge asteroids
5. Collect stardust
6. Use power-ups
7. Build combos
8. Survive as long as possible

---

# 🤖 Made with AI

This game was designed and coded with the assistance of **Claude AI**.

Human provided the vision and design direction.  
AI implemented the entire codebase.

---

# 📜 License

MIT License

---

<p align="center">
<b>⭐ If you enjoyed the project, consider starring the repo! ⭐</b>

<br><br>

<i>AI is not just assisting development — it is creating.</i>
</p>

