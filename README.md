# kmApi - Kotlin Multi-platform API for Scripts and Command Line Utilities

A lightweight library providing essential Java standard library API functionality for Kotlin Multiplatform projects, focusing on script-like utilities and command-line applications.

## Overview

kmApi enables you to write platform-independent utilities that can run on both JVM and Native platforms. It's particularly useful for creating script-like utilities that can be developed using the JVM and deployed as native applications.

## Features

### Supported Platforms
- JVM
- Native Linux (x64)
- Native Windows (MinGW x64)

### Core Components
| Module       | Description | Common | JVM | NativeLinux | NativeMingw |
|:-------------| :--- | :---: | :---: | :---: | :---: |
| KmFile       | File system operations similar to Java's File class | ✓ | ✓ | ✓ | ✓ |
| InputStream  | Input stream operations | ✓ | ✓ | - | - |
| OutputStream | Output stream operations | ✓ | ✓ | - | - |
| KmProcess    | Process management | ✓ | ✓ | - | - |
| System       | System-level operations | ✓ | ✓ | ✓ | ✓ |

## Requirements
- Kotlin 1.8+
- JDK 1.8 or higher (for JVM target)
- Gradle 7.0+ (for building)

## Installation

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("de.rdvsb:kmapi:0.2.5")
}
```

For Kotlin Multiplatform projects:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("de.rdvsb:kmapi:0.2.5")
            }
        }
    }
}
```

## Usage Examples

### File Operations
```kotlin
// Create and write to a file
val file = KmFile("example.txt")
file.writeText("Hello, World!")

// Read from a file
val content = file.readText()
println(content)

// Check if file exists and delete it
if (file.exists()) {
    file.delete()
}
```

### System Operations
```kotlin
// Get environment variables
val path = System.getenv("PATH")

// Get system properties
val osName = System.getProperty("os.name")
val userHome = System.getProperty("user.home")

// Check OS type and perform platform-specific operations
if (System.isUnix) {
    println("Running on Unix-like system")
} else if (System.isWindows) {
    println("Running on Windows system")
}
```

## Version History

### 0.2.5 (Current)
- Current stable release

### 0.2.00 (Breaking changes)
- Renamed `de.rdvsb.kmapi.File` to `de.rdvsb.kmapi.KmFile` to avoid confusion with Java File class
- Migration: Use `typealias File = de.rdvsb.kmapi.KmFile` to quickly fix existing code

## Project Goals
- Enable platform-independent utility development
- Facilitate creation of script-like utilities
- Support development on JVM with native deployment capability

## Non-Goals
- Full Java standard library implementation for Native
- Complete Java class functionality replication
- Kotlin standard library replacement
- Mobile platform support
- Web platform support

## Notes
- This is an active work in progress
- Functionality is added based on needs
- Prefer Kotlin standard library functionality when available for both JVM and Native targets

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE.txt](LICENSE.txt) file for details.
