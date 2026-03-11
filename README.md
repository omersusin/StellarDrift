# 🚀 STELLAR DRIFT

> **An endless space survival & shooter game — generated entirely by AI and written in pure Java using the Android Canvas API. No game engines. No bitmaps. No external audio files. Everything is produced programmatically.**

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Engine-None_(Pure_Canvas)-00E5FF?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Version-4.6.3-7C4DFF?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Min_SDK-21-FF6D00?style=for-the-badge" />
</p>

---

# 🎮 About The Game

**Stellar Drift** started as a simple asteroid-dodging prototype and evolved into a full space shooter experience featuring:

- in-game economy  
- ship progression  
- advanced game feel systems  
- procedural audio  
- physics-driven gameplay  

The entire game is generated purely through code.

Graphics are rendered in real time using the Android Canvas API (`Path`, `Paint`, gradients).

Audio is generated using `AudioTrack` and mathematical waveforms (sine, square, noise).

The architecture uses an optimized game loop with object pooling to maintain **60 FPS**.

---

# ✨ Gameplay Features

## 🛸 The Fleet (6 Unique Ships)

Unlock different ships with distinct stats and play styles:

1. **Striker** — Fast interceptor  
2. **Juggernaut** — Heavy bomber with dual cannons  
3. **Phantom** — Alien triple-spread weapon system  
4. **Swarm** — Twin-hull bullet carrier  
5. **Eclipse** — Fast stealth hunter with piercing shots  
6. **Zenith** — End-game divine vessel with massive penta-spread  

---

## 🛒 Meta Progression

Destroy asteroids and collect **StarDust** to earn **Cosmic Credits**.

Credits can be used in the **Hangar** to:

- unlock ships  
- upgrade ship statistics  

Upgradeable attributes:

- Speed  
- Fire Rate  
- Damage  

Each stat supports **5 upgrade levels**.

---

## ⛽ Fuel System

Your ship constantly consumes fuel.

If fuel reaches zero the ship slows dramatically.

Fuel can be restored by collecting **Golden StarDust**.

Low fuel (<20%) triggers:

- screen pulse warning  
- heartbeat vibration feedback  

---

## 💥 Combat System

- Auto fire based on Fire Rate stat  
- Destructible asteroids with size-based HP  
- Velocity-based movement physics  
- Ship banking up to 12° based on horizontal velocity  

---

## ⚡ Power-Ups

Temporary power-ups appear during gameplay:

🧲 **Magnet** — Pulls nearby StarDust toward the player.

🕐 **Slow-Mo** — Slows the universe except the player ship.

✖️ **Double** — Doubles score, credits and fuel pickups.

🛡 **Shield** — Blocks one fatal hit.

🔵 **Plasma Core** — Rare item triggering **OVERCHARGE**:

- double speed  
- triple fire rate  
- bullet-hell mode  

Duration: **4 seconds**

---

# 🎬 Game Feel

- Snap Collection (magnetic item pickup)
- Hit Stall micro freeze effect
- Chromatic aberration death glitch
- Graze chain system for near misses
- Particle ring burst effects
- Elastic UI animations
- Speed lines at high velocity
- Danger vignette when asteroids are close
- High score proximity line

These systems create a responsive arcade feel.

---

# 🔊 Procedural Audio & Haptics

All audio is synthesized in real time.

Examples:

Laser → "Piyuw"  
Explosion → "Boom"  
UI click → "Ding"

Generated using mathematical waveforms via `AudioTrack`.

Additional systems:

- adaptive ambient drone soundtrack
- pitch changes based on gameplay tempo
- vibration patterns for gameplay events

---

# ⚙️ Performance Techniques

- Zero allocations during gameplay  
- Object pooling for bullets and particles  
- Ring buffers for temporary effects  
- Cached vector paths for asteroid geometry  
- Isolated Paint objects for UI rendering  

---

# 📦 Build & Install

## Option A — GitHub Actions

1. Open the **Actions** tab  
2. Select the latest workflow  
3. Download the **Release APK** from Artifacts

## Option B — Local Build (Termux / Linux)

Clone and build the project:

git clone https://github.com/omersusin/StellarDrift.git  
cd StellarDrift  

gradle wrapper --gradle-version 8.2  
chmod +x gradlew  

./gradlew assembleRelease

---

# 🤖 Built with AI

This game was developed through collaboration between a human designer and **Anthropic Claude AI**.

The human provided:

- vision  
- gameplay direction  
- design feedback  

The AI generated the implementation and architecture.

---

<p align="center">
<b>⭐ If you enjoyed the project, consider starring the repository ⭐</b>
<br><br>
<i>A demonstration that AI-assisted development can produce complex and polished interactive systems.</i>
</p>

