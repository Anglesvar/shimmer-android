# Quick Start Guide

## Project Structure

```
shimmer-android/
├── shimmer/           # Main library module
│   ├── src/main/java/com/anglesvar/shimmer/
│   │   ├── Shimmer.kt              # Core configuration class
│   │   ├── ShimmerDrawable.kt      # Drawable implementation
│   │   └── ShimmerFrameLayout.kt   # FrameLayout wrapper
│   └── src/main/res/values/
│       └── attrs.xml                # XML attributes definition
│
├── sample/            # Sample application
│   ├── src/main/java/com/anglesvar/shimmer/sample/
│   │   └── MainActivity.kt
│   └── src/main/res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   └── placeholder_content.xml
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
│
├── README.md          # Full documentation
├── LICENSE            # BSD License
└── settings.gradle.kts
```

## Getting Started

### 1. Open in Android Studio

1. Open Android Studio
2. Select **File > Open**
3. Navigate to this project directory
4. Click **Open**
5. Wait for Gradle sync to complete

### 2. Build the Project

```bash
./gradlew clean build
```

Or use Android Studio:
- **Build > Make Project** (⌘F9 / Ctrl+F9)

### 3. Run the Sample App

1. Select `sample` run configuration
2. Click **Run** (⌃R / Shift+F10)
3. Choose an emulator or connected device

## Key Components

### Shimmer.kt
- Core configuration class with Builder pattern
- Two builder types: `AlphaHighlightBuilder` and `ColorHighlightBuilder`
- Configurable properties: direction, shape, colors, duration, intensity, etc.
- Shared configuration between XML/View and Compose implementations

### ShimmerDrawable.kt
- Custom Drawable that renders the shimmer effect (for XML/View usage)
- Uses `ValueAnimator` for animation
- Supports both linear and radial gradients
- Handles matrix transformations for rotation and translation

### ShimmerFrameLayout.kt
- Extends FrameLayout (for XML/View usage)
- Automatically manages shimmer lifecycle
- Handles visibility changes
- Provides simple API: `startShimmer()`, `stopShimmer()`

### ShimmerCompose.kt
- Jetpack Compose implementation
- `Modifier.shimmer()` extension for easy integration
- `rememberShimmerState()` for state management
- `ShimmerBox()` convenience composable
- Uses `InfiniteTransition` for smooth animations

## Basic Usage Examples

### Jetpack Compose (Recommended)

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anglesvar.shimmer.shimmer
import com.anglesvar.shimmer.Shimmer
import com.anglesvar.shimmer.ShimmerBox
import com.anglesvar.shimmer.rememberShimmerState

@Composable
fun LoadingScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        // Simple shimmer with default settings
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
                .shimmer()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom shimmer configuration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.Gray)
                .shimmer(
                    direction = Shimmer.Direction.TOP_TO_BOTTOM,
                    shape = Shimmer.Shape.LINEAR,
                    durationMillis = 1500,
                    intensity = 0.5f
                )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Using ShimmerBox convenience composable
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            backgroundColor = Color.LightGray
        ) {
            Text(
                text = "Loading...",
                modifier = Modifier.padding(16.dp),
                color = Color.Gray
            )
        }
    }
}
```

### XML Layout (Traditional View System):

```xml
<com.anglesvar.shimmer.ShimmerFrameLayout
    android:id="@+id/shimmer_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:shimmer_auto_start="true"
    app:shimmer_duration="1000">

    <!-- Your placeholder content -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Loading..." />

</com.anglesvar.shimmer.ShimmerFrameLayout>
```

### Kotlin Code (View System):

```kotlin
val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)

// Start/stop
shimmerLayout.startShimmer()
shimmerLayout.stopShimmer()

// Custom configuration
val shimmer = Shimmer.AlphaHighlightBuilder()
    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
    .setDuration(1500)
    .setIntensity(0.5f)
    .build()

shimmerLayout.setShimmer(shimmer)
```

## Technology Stack

- **Language**: Kotlin 2.0.21
- **Build System**: Gradle 8.7.3 with Kotlin DSL
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 15)
- **Compose**: 1.7.6 (for sample app)
- **Material Design**: 1.12.0

## Modern Features

✅ **100% Kotlin** - No Java code
✅ **Jetpack Compose Support** - First-class Compose integration with `Modifier.shimmer()`
✅ **Type-safe builders** - Fluent API with compile-time safety
✅ **Null-safe** - Leverages Kotlin's null safety
✅ **Lifecycle-aware** - Automatically handles view lifecycle (View system) and composition (Compose)
✅ **Hardware-accelerated** - Optimized rendering for both View and Compose
✅ **Clean architecture** - Separation of concerns
✅ **Well-documented** - KDoc comments throughout

## Compose vs View System

| Feature | Jetpack Compose | View System (XML) |
|---------|----------------|-------------------|
| **Integration** | `Modifier.shimmer()` | `ShimmerFrameLayout` |
| **Configuration** | Function parameters or `ShimmerConfig` | XML attributes or `Shimmer.Builder` |
| **Animation** | Automatic with `InfiniteTransition` | Manual `startShimmer()`/`stopShimmer()` |
| **State Management** | `rememberShimmerState()` | Instance methods |
| **Best For** | Modern Compose UIs | Legacy View-based UIs |
| **Performance** | Excellent | Excellent |

## Testing

The project includes test directories:
- `shimmer/src/test/` - Unit tests
- `shimmer/src/androidTest/` - Instrumentation tests

## Publishing the Library

To publish this library, you can:

1. **Maven Central**: Configure publishing in `shimmer/build.gradle.kts`
2. **JitPack**: Simply push to GitHub and use JitPack
3. **Local Maven**: Use `./gradlew publishToMavenLocal`

## Next Steps

1. Open the project in Android Studio
2. Run the sample app to see examples
3. Explore different XML attributes in `attrs.xml`
4. Check `MainActivity.kt` for programmatic usage
5. Read `README.md` for complete documentation

## Differences from Original Facebook Shimmer

### Improvements:
- ✨ Written in modern Kotlin
- ✨ **Full Jetpack Compose support with `Modifier.shimmer()`**
- ✨ Updated to latest Android APIs
- ✨ Better null safety
- ✨ More idiomatic Kotlin patterns
- ✨ Modern Gradle configuration
- ✨ Compose-first API design

### API Compatibility:
- ✅ Same XML attributes
- ✅ Similar Builder pattern
- ✅ Same core functionality
- ✅ Compatible configuration options

## Support

For issues or questions:
1. Check `README.md` for documentation
2. Review the sample app code
3. Examine XML attributes in `attrs.xml`

---

**Author**: anglesvar  
**License**: BSD  
**Inspired by**: Facebook's Shimmer Android
