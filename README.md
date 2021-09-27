# Kotlin multi-platform API for scripts and command line utilities
Provide some useful Java standard library API classes for Kotlin Native


## Preliminary version 0.1.6
### parts working

| Module | Common | JVM | NativeLinux | NativeMingw |
| :---   | :---:  | :---: | :---: | :---: |
| File | x | x | x | x |
| InputStream | x | x | - | - |
| OutputStream | x | x | - | - |
| KmProcess | x | x | - | - |


## Goals
 * Ability to write these utilities in a platform independent fashion
 * Facilitate the creation of script like utilities (e.g. alternative or replacement of Bash, Perl, Python scripts)
 * Develop using the JVM, deploy as native

## Non goals
 * Provide the full Java standard library for Native
 * Provide the full functionality of Java classes 
 * Create a replacement of the Kotlin standard library
 * Support mobile platforms
 * Support Web platforms

## Use for
 * Native
 * JVM
 * Server
 * Desktop

## Classes
 * File
 * System
 * Process
 * IO

## Note
 * This is a work in progress. Functionality will be added when needed
 * Prefer functionality of the Kotlin standard library, as long as it is implemented for the JVM and Native target

## Licence
Apache 2.0
