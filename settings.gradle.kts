rootProject.name = "kmapi"

pluginManagement {
	val kotlin_version: String by settings
	plugins {
		id("org.jetbrains.kotlin.jvm").version(kotlin_version)
		id("org.jetbrains.kotlin.kapt").version(kotlin_version)
		id("org.jetbrains.kotlin.plugin.serialization").version(kotlin_version)
		id("org.jetbrains.kotlin.multiplatform").version(kotlin_version)
	}
}


