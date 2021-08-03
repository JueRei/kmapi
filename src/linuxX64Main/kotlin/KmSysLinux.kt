/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.posix.*
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze
import kotlin.system.exitProcess
import kotlin.system.getTimeMillis


private fun epochMillis(): Long = memScoped {
	val timeVal = alloc<timeval>()
	gettimeofday(timeVal.ptr, null)
	(timeVal.tv_sec * 1000) + (timeVal.tv_usec / 1000)
}

private fun getOSName(): String {
	memScoped {
		val utsName = alloc<utsname>()
		uname(utsName.ptr)
		return utsName.sysname.toKString()
	}
}

private fun nativeAppPath(): String {
	memScoped {
		val buffer = allocArray<ByteVar>(PATH_MAX)
		val len = readlink("/proc/self/exe", buffer, PATH_MAX)
		if (len <= 0) return "app"
		return buffer.toKString()
	}
}

public actual object System {
	public actual object err {
		public actual fun println(line: String) {
			fputs(line, stderr)
			fputc(0x0a, stderr)
		}

		public actual  fun print(text: String) {
			fputs(text, stderr)
		}
	}

	public actual object out {
		public actual  fun println(line: String) {
			fputs(line, stdout)
			fputc(0x0a, stdout)
		}

		public actual  fun print(text: String) {
			fputs(text, stdout)
		}
	}


	public actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()

	public actual fun currentTimeMillis(): Long = epochMillis()

	private val osName: String = getOSName()
	public actual val lineSeparator: String = "\n"

	private val propertyMap = AtomicReference(
		mapOf(
			"app.name" to nativeAppPath(),
			"os.name" to osName,
			"file.separator" to File.separator,
			"line.separator" to lineSeparator,
			"path.separator" to ":",
		).freeze()
	)

	public actual fun getProperty(name: String): String? = propertyMap.value[name]

	public actual fun setProperty(name: String, value: String): String? {
		return runBlocking {
			Mutex().withLock {
				val oldMap = propertyMap.value
				val oldValue = oldMap[name]
				if (oldValue == value) return@runBlocking value

				// AtomicReference is always frozen. Replace ref with a new map
				val newMap = oldMap.toMutableMap()
				newMap[name] = value
				newMap.freeze()
				propertyMap.value = newMap
				return@runBlocking oldValue
			}
		}
	}

	public actual fun getProperties(): MutableMap<String, String> {
		return propertyMap.value.toMutableMap()
	}

	public actual fun exit(status: Int): Nothing {
		exitProcess(status)
	}

	public actual val isUnix: Boolean
		get() = true
	public actual val isWindows: Boolean
		get() = false
}

public actual fun computeAppPath(mainObj: Any?): String = nativeAppPath()
