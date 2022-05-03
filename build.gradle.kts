
plugins {
	kotlin("multiplatform")
	`maven-publish`
}

group = "de.rdvsb"
version = "0.2.2-SNAPSHOT"

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kotlin_coroutines_version: String by project
val kotlin_serialization_version: String by project
val kotlin_date_version: String by project


repositories {
	//jcenter()
	mavenCentral()
}

println("Hi there")
println("kotlin.presets: ${kotlin.presets.names}")
println("kotlin.targets: ${kotlin.linuxX64()}")
println("kotlin.sourceSets: ${kotlin.sourceSets.names}")

kotlin {
	explicitApi()

	//println("kotlin: ${kotlin.presets}")

	targets.all {
		compilations.all {
			kotlinOptions {
				allWarningsAsErrors = false
				freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
				//freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
			}
		}
	}

	jvm {
		withJava() // Includes Java sources into the JVM target’s compilations.
		compilations.all {
			kotlinOptions.jvmTarget = "1.8"
		}
		testRuns["test"].executionTask.configure {
			useJUnit()
		}
	}

	linuxX64("linuxX64")
	mingwX64("mingwX64")

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_version")
			}

		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}
		val jvmMain by getting

		val jvmTest by getting {
			dependencies {
				implementation(kotlin("test-junit"))
			}
		}
		//val nativeTest by getting

		val nativeCommon by creating {
			dependsOn(commonMain)
		}
		val nativeTest by creating {
			dependsOn(commonTest)
		}
		println("kotlin.sourceSets.nativeCommon.srcDirs: ${nativeCommon.kotlin.srcDirs}")

		val linuxX64Main by getting {
			dependsOn(nativeCommon)
		}

		val linuxX64Test by getting {
			dependsOn(nativeTest)
		}

		val mingwX64Main by getting {
			dependsOn(nativeCommon)
		}

	}
}

//println("kotlin.sourceSets: ${kotlin.sourceSets.names}")

