/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
//import platform.mingw_x64.ENOTFOUND
import platform.posix.*
import platform.windows.*
import kotlin.native.concurrent.AtomicReference

import kotlin.native.concurrent.ThreadLocal
import kotlin.native.concurrent.freeze
import kotlin.system.exitProcess
import kotlin.system.getTimeMillis

private val setLocale = setlocale(LC_ALL, "en_US.UTF-8"); // allow thousands separators

// share a common thread local C char buffer
public const val cstrBufLen: Int = 2048
@ThreadLocal // one C variable for each thread
public val cstrBuf: CArrayPointer<ByteVar> = nativeHeap.allocArray<ByteVar>(cstrBufLen)

// Windows
public fun nativeAppPath(): String {
	memScoped {
		val buffer = allocArray<UShortVar>(MAX_PATH)
		GetModuleFileNameW(null, buffer, MAX_PATH) // TODO: support Linux
		return buffer.toKString()
	}
	//return "_appName"
}

public fun getOSName(): String = platform.posix.getenv("OS")?.toKString()?:"Windows"

public fun posixErrorMessage(err: Int): String {
	strerror_s(cstrBuf, cstrBufLen.toULong(), err)
	return cstrBuf.toKString()
}

public fun winSysErrMessage(winErrorCode: UInt): String {
	memScoped {
		System.err.println("winSysErrMessage winErrorCode=\"$winErrorCode\"")
		val msgBufSize = 256
		val msgBuf = allocArray<WCHARVar>(msgBufSize)

		val rc = FormatMessageW((FORMAT_MESSAGE_FROM_SYSTEM + FORMAT_MESSAGE_IGNORE_INSERTS).toUInt(),
		                        null, winErrorCode, 0U, msgBuf, msgBufSize.toUInt(), null).toInt()

		rc == 0 && return "WinError $winErrorCode"

		return msgBuf.toKString().trimEnd() // ('\n', '\r')
	}
}


public actual object System {
	public actual object out {
		public actual fun println(line: String) {
			fputs(line, stdout)
			fputc(0x0a, stdout)
		}

		public actual fun print(text: String) {
			fputs(text, stdout)
		}
	}
	public actual object err {
		public actual fun println(line: String) {
			fputs(line, stderr)
			fputc(0x0a, stderr)
		}

		public actual fun print(text: String) {
			fputs(text, stderr)
		}
	}

	public actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()

	public actual fun currentTimeMillis(): Long = getTimeMillis()

	private val osName: String = getOSName()
	public actual val lineSeparator: String = "\r\n"

	private val propertyMap = AtomicReference(
		mapOf(
			"app.name" to nativeAppPath(),
			"os.name" to osName,
			"file.separator" to KmFile.separator,
			"line.separator" to lineSeparator,
			"path.separator" to ";",
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

	public actual val isUnix: Boolean
		get() = false
	public actual val isWindows: Boolean
		get() = true

	public actual fun exit(status: Int): Nothing {
		exitProcess(status)
	}
}

public actual fun computeAppPath(mainObj: Any?): String = nativeAppPath()
