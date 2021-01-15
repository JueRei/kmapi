package de.rdvsb.kmapi

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.system.exitProcess

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

	public actual fun currentTimeMillis(): Long = time(null) * 1000L // getTimeMillis()

	private val propertyNames = arrayOf("app.name", "os.name", "file.separator")
	private val osName: String = getOSName()
	public actual val lineSeparator: String = if (osName.indexOf(string = "win", ignoreCase = true) >= 0) "\r\n" else "\n"

	public actual fun getProperty(name: String): String? =
		when {
			name == "app.name"       -> nativeAppPath()
			name == "os.name"        -> osName
			name == "file.separator" -> "/" // TODO: File.separator
			else                     -> null
		}

	public actual fun getProperties(): MutableMap<String, String> {
		val propMap = mutableMapOf<String, String>()
		propertyNames.forEach { name ->
			propMap[name] = getProperty(name) ?: "<null>"
		}
		return propMap
	}

	public actual fun exit(status: Int): Nothing {
		exitProcess(status)
	}

	public actual val isUnix: Boolean
		get() = true
	public actual val isWindows: Boolean
		get() = false
}

public actual fun computeAppPath(): String = nativeAppPath()
