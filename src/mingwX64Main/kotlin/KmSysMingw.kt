package de.rdvsb.kmapi

import kotlinx.cinterop.*
//import platform.mingw_x64.ENOTFOUND
import platform.posix.*
import platform.windows.*
import kotlin.math.max

import kotlin.native.concurrent.ThreadLocal

private val setLocale = setlocale(LC_ALL, "en_US.UTF-8"); // allow thousands separators

// share a common thread local C char buffer
private const val cstrBufLen = 2048
@ThreadLocal // one C variable for each thread
private val cstrBuf = nativeHeap.allocArray<ByteVar>(cstrBufLen)

// Windows
private fun nativeAppPath(): String {
	memScoped {
		val buffer = allocArray<UShortVar>(MAX_PATH)
		GetModuleFileNameW(null, buffer, MAX_PATH) // TODO: support Linux
		return buffer.toKString()
	}
	//return "_appName"
}

public object System {
	public object err {
		public fun println(line: String) {
			fputs(line, stderr)
			fputc(0x0a, stderr)
		}
	}

	public fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()

	public fun currentTimeMillis(): Long = time(null) * 1000L // getTimeMillis()

	private val propertyNames = arrayOf("app.name", "os.name", "file.separator")
	private val osName: String? = getOSName()
	public val separatorChar: String = if (osName?.indexOf(string = "win", ignoreCase = true) ?: -1 >= 0) "\\" else "/"

	public fun getProperty(name: String): String? =
		when {
			name == "app.name"       -> nativeAppPath()
			name == "os.name"        -> osName
			name == "file.separator" -> separatorChar
			else                     -> null
		}

	public fun getProperties(): MutableMap<String, String> {
		val propMap = mutableMapOf<String, String>()
		propertyNames.forEach { name ->
			propMap[name] = getProperty(name) ?: "<null>"
		}
		return propMap
	}

	public actual val isUnix: Boolean
		get() = false
	public actual val isWindows: Boolean
		get() = true

}

public actual fun computeAppPath(): String = nativeAppPath()
