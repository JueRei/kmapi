plugins {
	kotlin("multiplatform") version "1.4.21"
	`maven-publish`
}

group = "de.rdvsb"
version = "0.1-SNAPSHOT"

repositories {
	jcenter()
	mavenCentral()
}

println("Hi there")
println("kotlin.presets: ${kotlin.presets.names}")
println("kotlin.targets: ${kotlin.linuxX64()}")
println("kotlin.sourceSets: ${kotlin.sourceSets.names}")

kotlin {
	explicitApi()

	//println("kotlin: ${kotlin.presets}")

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
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
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

