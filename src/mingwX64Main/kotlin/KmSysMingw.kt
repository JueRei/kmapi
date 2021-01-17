package de.rdvsb.kmapi

import kotlinx.cinterop.*
//import platform.mingw_x64.ENOTFOUND
import platform.posix.*
import platform.windows.*
import kotlin.math.max

import kotlin.native.concurrent.ThreadLocal
import kotlin.system.exitProcess

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

	public actual fun currentTimeMillis(): Long = time(null) * 1000L // getTimeMillis()

	public actual val lineSeparator: String = "\\"

	private val propertyNames = arrayOf("app.name", "os.name", "file.separator")
	private val osName: String? = getOSName()
	public val separatorChar: String = if (osName?.indexOf(string = "win", ignoreCase = true) ?: -1 >= 0) "\\" else "/"

	public actual fun getProperty(name: String): String? =
		when {
			name == "app.name"       -> nativeAppPath()
			name == "os.name"        -> osName
			name == "file.separator" -> separatorChar
			else                     -> null
		}

	public actual fun getProperties(): MutableMap<String, String> {
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

	public actual fun exit(status: Int): Nothing {
		exitProcess(status)
	}
}

public actual fun computeAppPath(): String = nativeAppPath()
