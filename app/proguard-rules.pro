# VYRO Video Player ProGuard / R8 Optimization Rules

# 1. AndroidX Media3 / ExoPlayer Keep Rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.datasource.** { *; }
-dontwarn androidx.media3.**

# 2. Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# 3. Hilt Dependency Injection Keep Rules
-keep class * extends dagger.hilt.internal.UnsafeCasts { *; }
-keep class com.vyro.player.di.** { *; }
-dontwarn dagger.hilt.**

# 4. Domain Models & Native Decoder Bridge
-keep class com.vyro.player.domain.model.** { *; }
-keep class com.vyro.player.decoder.** { *; }
