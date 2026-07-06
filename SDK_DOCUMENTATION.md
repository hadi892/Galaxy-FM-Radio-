# Galaxy FM SDK Specification & Hardware Integration Guide

## 1. Overview
The **Galaxy FM SDK** (`:galaxyfmsdk`) is a modular, standalone Kotlin and C++ Android library optimized for the **Samsung Galaxy Tab A9+ 5G (SM-X216B)** powered by the **Qualcomm Snapdragon 695 5G (SM6375)** SoC.

It provides direct Linux V4L2 kernel driver access to the Qualcomm integrated FM radio hardware subsystem (`WCN3990 / WCN6855`) via JNI (`galaxy_fm_jni.so`), exposing a clean, type-safe reactive Kotlin API (`GalaxyFmManager`).

---

## 2. Linux V4L2 Architecture (Snapdragon 695)

```
[ Kotlin Application Layer (:app) ]
               │
               ▼  (Reactive StateFlows & Coroutines)
[ Galaxy FM SDK (:galaxyfmsdk) - GalaxyFmManager.kt ]
               │
               ▼  (JNI C++ Bridge - galaxy_fm_jni.cpp)
[ Native C++ Tuner - v4l2_fm_tuner.cpp ]
               │
               ▼  (ioctl: VIDIOC_S_TUNER, VIDIOC_S_FREQUENCY, VIDIOC_S_HW_FREQ_SEEK)
[ Linux Kernel Driver - /dev/radio0 (WCN3990 FM HAL) ]
```

### Supported Kernel IOCTLs:
- `VIDIOC_S_TUNER` / `VIDIOC_G_TUNER`: Configures FM receiver mode, queries signal level (`0..65535`), checks stereo subcarrier status (`V4L2_TUNER_SUB_STEREO`), and verifies RDS capability (`V4L2_TUNER_CAP_RDS`).
- `VIDIOC_S_FREQUENCY` / `VIDIOC_G_FREQUENCY`: Sets frequency in units of 62.5 Hz (`freq_mhz * 16000`).
- `VIDIOC_S_HW_FREQ_SEEK`: Hardware autonomous scanning up/down across the band.
- `VIDIOC_S_CTRL` / `VIDIOC_G_CTRL`: Private Qualcomm WCN controls (`V4L2_CID_PRIVATE_BASE`, mute, de-emphasis 50µs/75µs, audio routing).

---

## 3. Public API Summary (`GalaxyFmManager`)

```kotlin
val fmManager = GalaxyFmManager.getInstance()

// Reactive StateFlows
val powerState: StateFlow<Boolean> = fmManager.isPowerOn
val freqMhz: StateFlow<Float> = fmManager.currentFrequency
val rssiDb: StateFlow<Int> = fmManager.rssiDb
val isStereo: StateFlow<Boolean> = fmManager.isStereo
val rds: StateFlow<FmRdsData> = fmManager.rdsData

// Core Operations
fmManager.powerOn(98.5f)
fmManager.tune(101.1f)
fmManager.seekUp()
fmManager.scanAllStations(onStationFound = { ... }, onScanComplete = { ... })
fmManager.powerOff()
```

---

## 4. Hardware Requirements & Offline Operation
The Galaxy FM SDK strictly operates with real physical FM hardware circuitry via direct Linux kernel V4L2 character device nodes (`/dev/radio0`, `/dev/v4l2-radio0`, `/dev/wcn3990_fm`).
- **No Internet Required**: All demodulation, stereo decoding, and RDS Program Service (PS) / Radio Text (RT) extraction are executed entirely locally inside the Snapdragon 695 WCN3990 radio subsystem.
- **No Dongles Required**: Uses the integrated internal FM transceiver. Wired 3.5mm headphones or USB-C analog headset adapter must be connected to act as the receiving RF antenna.
- **Production-Only Codebase**: Contains zero simulation loops, zero mock stations, and zero placeholder responses. If physical FM hardware nodes cannot be opened or accessed, the manager reports clear offline failure states (`-120 dBm` noise floor, unpowered status).
