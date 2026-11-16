
[Descargar KMPlayer, Reproductor de video APK para Android - Última Versión](https://images.openai.com/static-rsc-4/MnWrvzO2XW1ZnljXGb9F_syOb1CDJRccFbSbI7ur-4lsYyYk64OtShU7Xz3pThgMmLYxB5sgtPZa2vA47U8ApNF8QgYJDXTNc4TJh2u3ajCHmeoIe3C1i_1mWuKVkAI0Xzh0lB8tS9isuEkJcUKELBH4RHOt6_fPRa2Jji0yA1w?purpose=inline)

[OPPO Community](https://images.openai.com/static-rsc-4/I_77Wn1X1nKIw_Z3oo_12qpYBrIIaexK20EGFlc_BgfJ-FHws_EMh51rChkXrQaNu5QTaXKaKLD5t3DjEOzuoKH8dFVxAdnWR1diAVgN-_wSJyjE6VQg9H7qYOFPhRt3XCcUR85LedH9KAruLiEdGFVMxGglD5m0iHrOEXZdubQ?purpose=inline)

[Sun Player Reviews in 2026](https://images.openai.com/static-rsc-4/V8gV2Wg4J3q9nhjJk7gGhXJ2KqTopOG6mTqgnTIFMfH9cOQEiJdz667aOXyZt6aBpV8XLWady4jpGZ9_X8iUEVFnu5j2QB45RbgKXfzRnysdmDTmAtlzgnko75Q716GUtWA-DWld0PRZiPW7mLA-hA6vRhvSTpekansruGal4FI?purpose=inline)

11

# **VYRO Video Player**

Ultimate Video Playback Experience

4K / 8K Ready

HDR Support

Hardware Decoding

Advanced Subtitles

Offline Player

একটি গুরুত্বপূর্ণ বিষয়: কোনো অ্যাপ সব ডিভাইসে সব Codec, 8K বা HDR চালানোর নিশ্চয়তা দিতে পারে না। এটি নির্ভর করে ফোনের Hardware Decoder, Android Version, Codec Profile, Bit Depth, HDR Display এবং Video Bitrate-এর ওপর। আমাদের লক্ষ্য হবে ডিভাইসের সর্বোচ্চ সক্ষমতা ব্যবহার করা এবং Unsupported ফাইলের জন্য উপযুক্ত fallback রাখা।

# **১. সম্পূর্ণ Feature List**

## **A. Video Format & Codec Support**

| Format / Codec | Support লক্ষ্য             |
| -------------- | -------------------------- |
| MP4            | H.264, H.265, AV1          |
| MKV            | H.264, HEVC, AV1           |
| WebM           | VP8, VP9, AV1              |
| MOV            | H.264, HEVC                |
| AVI            | Device/Extractor dependent |
| FLV            | Compatible files           |
| TS / MTS       | Supported streams          |
| 3GP            | Supported streams          |
| MPEG / MPG     | Compatible files           |
| WMV / ASF      | Fallback dependent         |

Audio codecs: AAC, MP3, AC3, E-AC3, FLAC, Opus, Vorbis, DTS (device/decoder dependent)।

## **B. Resolution & HDR**

- 144p থেকে 8K পর্যন্ত।
- 480p, 720p, 1080p, 2K, 4K, 8K।
- HDR10
- HDR10+
- Dolby Vision (compatible devices and profiles)
- HLG
- 10-bit Video
- High Bitrate Video
- Hardware / Software Decoder Selection
- Automatic Decoder Capability Detection

## **C. Advanced Playback Controls**

- 0.25x থেকে 4x Speed
- 90°, 180°, 270° Rotation
- Auto Rotation
- Landscape / Portrait Lock
- Zoom In / Out
- Fit / Fill / Crop / Stretch
- Picture-in-Picture
- Background Audio
- Frame Seeking
- A-B Repeat
- Playback Resume
- Sleep Timer
- Double Tap Seek
- Frame-by-Frame Seeking

## **D. Professional Subtitle System**

- SRT
- ASS / SSA
- VTT
- SUB / IDX (compatible formats)
- Embedded Subtitle
- Multiple Subtitle Tracks
- Subtitle Delay
- Font Size / Color
- Subtitle Position
- External Subtitle Import
- UTF-8 / Unicode Support

## **E. Gesture Control**

- Left side swipe: Brightness
- Right side swipe: Volume
- Horizontal swipe: Seek
- Double tap left/right: 10-second seek
- Pinch: Zoom
- Long press: Temporary speed boost
- Screen lock

## **F. Audio Control**

- Audio Track Selection
- Audio Delay
- Stereo / Surround (device dependent)
- Audio Passthrough (supported devices)
- Volume Boost (safe limits)
- Audio Language Selection

# **২. Android Studio Project Configuration**

আগের পরিকল্পনার নাম ও Package ধরে এগোব:

| Setting         | Configuration             |
| --------------- | ------------------------- |
| App Name        | VYRO Video Player         |
| Package         | `com.vyro.player`         |
| Language        | Kotlin                    |
| UI              | Jetpack Compose           |
| Architecture    | MVVM + Clean Architecture |
| Playback Engine | AndroidX Media3 ExoPlayer |
| Minimum SDK     | API 24                    |
| Media3 Stable   | 1.11.1                    |
| Database        | Room                      |
| Image Loading   | Coil                      |

Media3-এর বর্তমান Stable Release 1.11.1।&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1

**একটি বিষয় মনে রাখবে:** 8K, Dolby Vision, HDR10+ এবং সব Codec চালানোর ক্ষমতা অ্যাপ একা তৈরি করতে পারে না। অ্যাপকে ফোনের Decoder ও Display capability শনাক্ত করে সর্বোচ্চ সমর্থিত Playback দিতে হবে। Android-এর অফিসিয়াল Supported Formats তালিকাও ডিভাইসভেদে পার্থক্য দেখায়।&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1

# **৩. Gemini / AI Coding-এর জন্য ১৪টি ধাপে Prompt**

নিচের Prompt-গুলো একটার পর একটা Gemini-তে দেবে। প্রতিটি ধাপের কাজ সম্পূর্ণ ও Build Test হওয়ার পর পরবর্তী Prompt দেবে।

## **Prompt 01 — Complete Project Architecture**

প্রথমে এটি দেবে

**PROJECT SETUP PROMPT**

01

সম্পূর্ণ Project structure, Gradle, architecture ও dependency setup।

&#x20;Copy Prompt 01

## **Prompt 02 — Premium UI Design**

```
Design a premium, modern Android video player UI
for VYRO Video Player using Jetpack Compose and Material 3.

Design requirements:
- Professional dark theme with optional light theme
- Black/charcoal background
- Elegant green or blue accent
- Modern rounded cards
- Smooth transitions and animations
- Responsive phone and tablet layout

Screens:
1. Home Dashboard
2. All Videos
3. Folders
4. Recent Videos
5. Favorites
6. Video Player
7. Playback Settings
8. Subtitle Settings
9. Decoder Information
10. App Settings

Home screen:
- App logo and search
- Recently played with thumbnail(video)
- Continue watching
- Video folders
- view(grid column/row)
- Video thumbnails
- Sort and filter controls
- Bottom navigation

Use reusable Compose components.
Do not use placeholder navigation.
Implement functional UI and state management.
Provide complete code and file paths.
```

## **Prompt 03 — Automatic Video Scanner**

```
Implement a complete local video library scanner.

Requirements:
- Scan internal storage and accessible external storage
- Use Android MediaStore API
- Support Android 7 through latest Android
- Handle Android 13+ READ_MEDIA_VIDEO permission
- Handle Android 14+ selected photo/video access correctly
- Use SAF for user-selected folders
- Read video metadata:
  filename, duration, resolution, size, date,
  MIME type, folder, bitrate and thumbnail
- Sort by name, date, size, duration and resolution
- Search videos
- Filter videos by resolution and folder
- Detect duplicate entries
- Generate thumbnails
- Update library when files change
- Use Room database for metadata caching
- Do not scan inaccessible private app folders
- Handle permission denial and empty libraries

Implement production-quality code.
```

## **Prompt 04 — Advanced Video Playback Engine**

```
Build the core VYRO Video Playback Engine.

Use AndroidX Media3 ExoPlayer stable 1.11.1.

Requirements:

- Play local video files through Content URI
- Support progressive media playback
- Matroska/MKV, MP4, WebM and other
  Media3-supported containers
- Support network URLs when explicitly opened
- Reliable seeking and buffering
- Automatic track selection
- Audio/video synchronization
- Error handling and retry
- Playback state management
- Release player resources correctly
- Handle Activity and Compose lifecycle
- Support multiple video playback sessions safely
- Restore playback position
- Resume previously watched video

Create reusable PlayerManager and ViewModel.
Use Media3 PlayerView or AndroidView integration
where appropriate.
Provide complete working code.
```

## **Prompt 05 — Maximum Codec & Format Support**

বিশেষ গুরুত্বপূর্ণ

```
Implement advanced video codec and container
compatibility for VYRO Video Player.

Investigate and implement support for:

CONTAINERS:
MP4, MKV, WebM, MOV, AVI, TS, MTS, M2TS,
3GP, MPG, MPEG, FLV, WMV, ASF.

VIDEO CODECS:
H.264/AVC, H.265/HEVC, VP8, VP9, AV1,
MPEG-4 Part 2 and device-supported codecs.

AUDIO CODECS:
AAC, MP3, AC3, E-AC3, FLAC, Opus, Vorbis
and device-supported formats.

Requirements:
- Detect container and codec information
- Inspect MediaCodecList decoder capabilities
- Select hardware decoder when supported
- Handle unsupported codec/profile gracefully
- Provide software decoder fallback only
  when a compatible and maintained decoder exists
- Investigate FFmpeg/libVLC integration for
  unsupported containers and formats
- Keep optional native decoder dependencies isolated
- Display exact unsupported codec error
- Never claim support without testing

IMPORTANT:
A file extension does not guarantee that its
internal codec can be decoded.
Do not force unsupported codecs.
Create a compatibility report for each file.
```

## **Prompt 06 — 4K / 8K Ultra HD Playback**

```
Implement maximum resolution playback for VYRO.

Target:
144p, 240p, 360p, 480p, 720p, 1080p,
1440p, 2K, 4K, 5K and 8K.

Requirements:
- Background play (play as music for video)
- timer
- brightness (left side)
- sound right side scrolling up bottom
- Read actual video width and height
- Read frame rate and bitrate
- Inspect decoder-supported profile and level
- Check hardware decoder maximum capabilities
- Check device performance limitations
- Optimize Media3 load control
- Handle high bitrate files
- Avoid unnecessary memory allocation
- Optimize video thumbnail loading
- Handle dropped frames
- Monitor playback performance
- Avoid overheating where possible
- Show real-time resolution and decoder information

Never promise 8K on unsupported devices.
Use the best supported hardware decoding path.
Provide graceful fallback and clear error messages.
```

## **Prompt 07 — HDR & Dolby Vision**

```
Implement HDR video playback capability detection.

Target formats:
- HDR10
- HDR10+
- HLG
- Dolby Vision
- 10-bit HEVC
- HDR-capable VP9 and AV1 profiles

Requirements:
- Detect HDR video metadata
- Detect HDR-capable display
- Inspect codec HDR profile support
- Preserve HDR metadata and color information
- Use supported SurfaceView playback path
- Avoid unnecessary SDR conversion
- Detect Dolby Vision profile compatibility
- Handle HDR to SDR fallback if needed
- Show HDR badge in player UI
- Display HDR format and bit depth
- Handle unsupported HDR gracefully

Do not fake HDR using color filters.
Do not claim Dolby Vision support unless
the actual decoder and display support it.
Use Android official HDR APIs and Media3.
```

## **Prompt 08 — Advanced Playback Controls**

```
Implement professional playback controls.

Features:
- Play / Pause
- Previous / Next
- Seek forward / backward
- Timeline scrubbing
- Playback speed: 0.25x to 4x
- Rotation: 0, 90, 180, 270 degrees
- Auto rotation
- Portrait and landscape lock
- Aspect ratio: Fit, Fill, Crop, Stretch
- Zoom in / out
- Frame-by-frame seeking
- A-B repeat
- Sleep timer
- Resume playback
- Double-tap seeking
- Long press speed boost
- Screen lock
- Hide controls automatically
- Customizable seek interval

All controls must actually work.
Save user preferences.
```

## **Prompt 09 — Advanced Gesture System**

```
Create a professional video player gesture system.

Gestures:
- Swipe left side vertically: Brightness
- Swipe right side vertically: Volume
- Swipe horizontally: Video seeking
- Double tap left: Seek backward
- Double tap right: Seek forward
- Pinch: Zoom video
- Long press: Temporary playback speed boost
- Single tap: Toggle controls
- Two-finger gesture: Toggle screen lock

Requirements:
- Prevent accidental gesture conflicts
- Display animated brightness and volume indicators
- Use system brightness safely
- Do not modify system volume unexpectedly
- Allow gestures to be disabled in settings
- Smooth touch response
- Work in fullscreen mode
```

## **Prompt 10 — Professional Subtitle Engine**

```
Build an advanced subtitle system.

Support:
- SRT
- VTT
- SSA / ASS
- Embedded subtitle tracks
- External subtitle files
- Multiple subtitle tracks
- Subtitle language selection
- Subtitle delay adjustment
- Font size
- Font family
- Font color
- Background opacity
- Subtitle position
- Bold and outline
- Unicode and UTF-8
- Subtitle synchronization

Use Media3-supported subtitle formats.
For unsupported formats investigate a reliable
subtitle parser or conversion solution.

Provide a subtitle file picker using SAF.
Make all subtitle settings functional.
```

## **Prompt 11 — Audio Track & Video Sound**

```
Implement advanced audio track controls.

Features:
- Multiple audio track selection
- Audio language selection
- Audio delay synchronization
- Stereo and surround support where available
- Device-supported audio passthrough
- Audio track information
- Volume control
- Optional audio focus handling
- Bluetooth headset support
- Headphone button controls

Detect actual device audio capabilities.
Do not promise unsupported DTS or Dolby decoding.
Handle audio track switching without crashing.
```

## **Prompt 12 — Picture-in-Picture & Background**

```
Implement advanced playback multitasking.

Features:
- Android Picture-in-Picture
- Floating video window
- Background audio playback
- Notification playback controls
- Lock screen controls
- MediaSession integration
- MediaSessionService
- Play, Pause, Next and Previous actions
- Audio focus handling
- Headphone disconnection handling
- Restore playback after app reopen
- Correct foreground service handling
- Android 13+ notification behavior
- Android 14+ foreground service type requirements

Use official Android lifecycle and permission rules.
Do not leak player instances.
```

## **Prompt 13 — Settings, Privacy & Performance**

```
Build complete professional settings for VYRO.

Settings:
- Dark / Light / System theme
- Default playback speed
- Default rotation
- Default aspect ratio
- Default decoder preference
- Hardware decoding information
- Gesture controls
- Seek interval
- Subtitle preferences
- Resume playback
- Screen lock preference
- Sleep timer
- Hide file size or duration if desired
- Clear thumbnail cache
- Clear playback history
- Privacy settings
- App language
- Storage access management

Performance:
- Lazy video thumbnail loading
- Pagination
- Background metadata scanning
- Efficient Room database queries
- Lifecycle-safe player handling
- Low memory optimization
- Battery and thermal awareness
```

## **Prompt 14 — Final Testing & APK Release**

```
Perform a complete production-readiness audit
for VYRO Video Player.

Test:
- Android 7 to latest supported Android
- Different screen sizes
- 720p, 1080p, 4K and 8K samples
- H.264, HEVC, VP9 and AV1
- HDR10, HLG and Dolby Vision samples
- MKV, MP4, WebM and other containers
- External subtitles
- Multiple audio tracks
- Rotation
- PiP
- Background playback
- Permission handling
- Low memory
- App lifecycle
- Corrupted video files
- Unsupported codecs

Check:
- Compilation errors
- Runtime crashes
- Memory leaks
- Decoder initialization failures
- Audio/video synchronization
- Dropped frames
- Battery consumption
- UI responsiveness

Fix all reproducible bugs.
Do not hide unsupported capabilities.
Generate a final build report.

Prepare:
1. Debug APK
2. Release APK
3. Signed Android App Bundle (AAB)
4. ProGuard/R8 configuration
5. Final build instructions
```

# **৪. বিশেষভাবে আরও ৫টি Pro Feature যোগ করার Prompt**

এইগুলো সাধারণ Video Player-কে আরও Advanced করবে।

| Feature          | কাজ                               |
| ---------------- | --------------------------------- |
| Video Equalizer  | Picture color adjustment          |
| Video Screenshot | নির্দিষ্ট Frame Capture           |
| Video Crop       | Custom crop ও zoom                |
| Playback History | কোথায় দেখা বন্ধ করেছিলে           |
| Private Folder   | PIN / Biometric দিয়ে ভিডিও লুকানো |

## **Prompt 15 — Video Enhancement**

```
Add advanced video enhancement features:

- Brightness adjustment
- Contrast adjustment
- Saturation adjustment
- Hue adjustment
- Gamma adjustment
- Sharpness where supported
- Video color presets
- Grayscale
- Sepia
- Custom video filters
- Save custom presets
- Reset to original

Use GPU-based processing only when appropriate.
Do not compromise native HDR playback.
Keep video rendering efficient.
```

## **Prompt 16 — Screenshot & Private Folder**

```
Implement:

1. Capture current video frame
2. Save screenshot to user-selected folder
3. Preserve image quality
4. Private video folder
5. PIN protection
6. Biometric authentication
7. Hide videos from normal library
8. Restore hidden videos
9. Secure app lock
10. Privacy-friendly local storage

Use Android Keystore for secure key management.
Do not claim that ordinary hidden folders
provide encryption.
```

# **৫. গুরুত্বপূর্ণ Testing**

এই ভিডিওগুলো দিয়ে বাস্তবে পরীক্ষা করবে:

- H.264 1080p
- HEVC 4K
- HEVC 10-bit 4K HDR
- VP9 4K
- AV1 4K
- 8K HEVC
- Dolby Vision sample
- MKV with multiple audio tracks
- MKV with external subtitles

প্রতিটি ফাইলের ক্ষেত্রে পরীক্ষা করবে: Play, Seek, Pause, Rotation, Audio Sync, Decoder এবং dropped frames।

# **৬. আমার বিশেষ পরামর্শ**

## **VYRO Player — Development Strategy**

Recommended

**প্রথম ভার্সনে এই পাঁচটি বিষয় সবচেয়ে বেশি গুরুত্ব দেবে:**

1. Stable playback engine
2. Hardware decoder optimization
3. HDR compatibility
4. Advanced gesture controls
5. Subtitle engine

এরপর FFmpeg / libVLC fallback নিয়ে কাজ করবে। কারণ সব Container ও Codec Media3 একা সমর্থন করে না।

**Prompt ব্যবহারের নিয়ম:**

1. প্রথমে Prompt 01 দেবে।
2. AI যে ফাইল তৈরি করবে সেগুলো Android Studio-তে বসাবে।
3. Gradle Sync ও Build করবে।
4. কোনো Error থাকলে পরবর্তী Prompt দেওয়ার আগে Error Fix করবে।
5. তারপর একে একে পরবর্তী Prompt দেবে।
6. সবশেষে Real Android Device-এ Test করবে।

Official documentation:

- [Android Media3 Documentation](https://developer.android.com/media/media3?utm_source=chatgpt.com)
- [Android Supported Formats](https://developer.android.com/media/platform/supported-formats?utm_source=chatgpt.com)
- [Android HDR Guide](https://developer.android.com/media/platform/hdr?utm_source=chatgpt.com)
