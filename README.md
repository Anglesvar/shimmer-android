# Shimmer for Android

[![Maven Central](https://img.shields.io/maven-central/v/io.github.anglesvar/shimmer.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.anglesvar/shimmer)
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

A modern, Kotlin-based implementation of the shimmer effect for Android, inspired by Facebook's Shimmer library.

## Features

- **Easy Integration**: Simple XML attributes, programmatic API, or Jetpack Compose modifier
- **Jetpack Compose Support**: First-class Compose support with `Modifier.shimmer()`
- **Highly Customizable**: Control direction, shape, colors, duration, and more
- **Modern Architecture**: Built with Kotlin and modern Android best practices
- **Lightweight**: Minimal dependencies
- **Alpha and Color Modes**: Support for both alpha-based and color-based shimmer effects
- **Multiple Shapes**: Linear and radial shimmer patterns

## Requirements

- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 2.0.21+

## Installation

### Maven Central

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.anglesvar:shimmer:1.0.0")
}
```

Or in `build.gradle`:

```groovy
dependencies {
    implementation 'io.github.anglesvar:shimmer:1.0.0'
}
```

### Local Module

If you're using it as a local module:

```kotlin
dependencies {
    implementation(project(":shimmer"))
}
```

## Usage

### Jetpack Compose (Recommended)

Apply shimmer effect using the `Modifier.shimmer()` extension:

```kotlin
import com.anglesvar.shimmer.shimmer
import com.anglesvar.shimmer.Shimmer

@Composable
fun LoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.LightGray)
            .shimmer()
    )
}
```

#### Custom Configuration

```kotlin
@Composable
fun CustomShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.Gray)
            .shimmer(
                direction = Shimmer.Direction.LEFT_TO_RIGHT,
                shape = Shimmer.Shape.LINEAR,
                baseColor = Color.Gray.copy(alpha = 0.3f),
                highlightColor = Color.White,
                durationMillis = 1500,
                intensity = 0.5f,
                dropoff = 0.5f,
                tilt = 20f
            )
    )
}
```

#### Reusable Shimmer State

For better performance when multiple composables use the same shimmer configuration:

```kotlin
@Composable
fun ShimmerList() {
    val shimmerState = rememberShimmerState(
        config = ShimmerConfig(
            direction = Shimmer.Direction.TOP_TO_BOTTOM,
            baseColor = Color.Gray.copy(alpha = 0.3f),
            highlightColor = Color.White
        ),
        durationMillis = 1500
    )

    Column {
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.LightGray)
                    .shimmer(shimmerState)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
```

#### ShimmerBox Convenience Composable

```kotlin
@Composable
fun QuickPlaceholder() {
    ShimmerBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        backgroundColor = Color.LightGray
    ) {
        // Optional content inside
        Text("Loading...", modifier = Modifier.padding(16.dp))
    }
}
```

#### Radial Shimmer Effect

```kotlin
@Composable
fun RadialShimmerExample() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color.Gray)
            .shimmer(
                shape = Shimmer.Shape.RADIAL,
                highlightColor = Color.White,
                baseColor = Color.Gray.copy(alpha = 0.3f)
            )
    )
}
```

### XML (Traditional View System)

Add `ShimmerFrameLayout` to your layout:

```xml
<com.anglesvar.shimmer.ShimmerFrameLayout
    android:id="@+id/shimmer_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:shimmer_auto_start="true"
    app:shimmer_base_alpha="0.3"
    app:shimmer_highlight_alpha="1.0"
    app:shimmer_duration="1000"
    app:shimmer_direction="left_to_right"
    app:shimmer_shape="linear">

    <!-- Your content here -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Loading..." />

</com.anglesvar.shimmer.ShimmerFrameLayout>
```

### Kotlin

Control shimmer programmatically:

```kotlin
val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_layout)

// Start shimmer
shimmerLayout.startShimmer()

// Stop shimmer
shimmerLayout.stopShimmer()

// Custom configuration
val shimmer = Shimmer.AlphaHighlightBuilder()
    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
    .setDuration(1500)
    .setIntensity(0.5f)
    .setDropoff(0.5f)
    .setTilt(20f)
    .build()

shimmerLayout.setShimmer(shimmer)
```

### Color Shimmer

For a colored shimmer effect:

```xml
<com.anglesvar.shimmer.ShimmerFrameLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:shimmer_colored="true"
    app:shimmer_base_color="#E0E0E0"
    app:shimmer_highlight_color="#FFFFFF"
    app:shimmer_duration="1500">

    <!-- Your content -->

</com.anglesvar.shimmer.ShimmerFrameLayout>
```

Or programmatically:

```kotlin
val shimmer = Shimmer.ColorHighlightBuilder()
    .setBaseColor(Color.GRAY)
    .setHighlightColor(Color.WHITE)
    .setDuration(1500)
    .build()
```

## XML Attributes

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `shimmer_clip_to_children` | boolean | true | Clip shimmer to children bounds |
| `shimmer_colored` | boolean | false | Use color shimmer instead of alpha |
| `shimmer_auto_start` | boolean | true | Auto-start animation |
| `shimmer_base_color` | color | #4CFFFFFF | Base color for shimmer |
| `shimmer_highlight_color` | color | #FFFFFFFF | Highlight color |
| `shimmer_base_alpha` | float | 0.3 | Base alpha (0.0 - 1.0) |
| `shimmer_highlight_alpha` | float | 1.0 | Highlight alpha (0.0 - 1.0) |
| `shimmer_intensity` | float | 0.0 | Shimmer intensity |
| `shimmer_dropoff` | float | 0.5 | Gradient dropoff |
| `shimmer_tilt` | float | 20 | Tilt angle in degrees |
| `shimmer_duration` | integer | 1000 | Animation duration (ms) |
| `shimmer_repeat_count` | integer | -1 | Repeat count (-1 = infinite) |
| `shimmer_repeat_delay` | integer | 0 | Delay between repeats (ms) |
| `shimmer_repeat_mode` | enum | restart | `restart` or `reverse` |
| `shimmer_direction` | enum | left_to_right | Direction of shimmer effect |
| `shimmer_shape` | enum | linear | `linear` or `radial` |
| `shimmer_width_ratio` | float | 1.0 | Width ratio |
| `shimmer_height_ratio` | float | 1.0 | Height ratio |
| `shimmer_fixed_width` | dimension | 0 | Fixed width in pixels |
| `shimmer_fixed_height` | dimension | 0 | Fixed height in pixels |

## Direction Options

- `left_to_right`
- `top_to_bottom`
- `right_to_left`
- `bottom_to_top`

## Shape Options

- `linear`: Ray effect (default)
- `radial`: Spotlight effect

## Sample App

Run the `sample` module to see various shimmer configurations in action, including both XML and Jetpack Compose implementations.

## License

BSD License - See [LICENSE](LICENSE) file for details.

## Author

Created by anglesvar

## Acknowledgments

Inspired by [Facebook's Shimmer Android library](https://github.com/facebookarchive/shimmer-android)
