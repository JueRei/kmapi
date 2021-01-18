package de.rdvsb.kmapi

public actual object System {
	public actual object err {
		public actual fun println(line: String): Unit = java.lang.System.err.println(line)
		public actual fun print(text: String): Unit = java.lang.System.err.print(text)
	}


	public actual object out {
		public actual fun println(line: String): Unit = java.lang.System.out.println(line)
		public actual fun print(text: String): Unit = java.lang.System.out.print(text)
	}

	public actual fun getenv(name: String): String? = java.lang.System.getenv(name)

	public actual fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()

	public actual val lineSeparator: String = java.lang.System.lineSeparator()

	public actual fun getProperty(name: String): String? = java.lang.System.getProperty(name)
	public actual fun setProperty(name: String, value: String): String? = java.lang.System.setProperty(name, value)

	public actual fun getProperties(): MutableMap<String, String> {
		val propHashOfAnyAny = java.lang.System.getProperties()
		var propMap: MutableMap<String, String> = mutableMapOf()

		propMap.putAll(propHashOfAnyAny.asSequence().map { it.key.toString() to it.value.toString() })

		return propMap
	}

	public actual fun exit(status: Int): Nothing {
		java.lang.System.exit(status)
		throw RuntimeException("System.exit returned normally, while it was supposed to halt JVM.")
	}

	private enum class OsType { UNKNOWN, UNIX, WINDOWS}
	private var osType = OsType.UNKNOWN
		get() {
			if (field == OsType.UNKNOWN) {
				field = System.getProperty("os.name").let {
					when {
						it == null               -> OsType.UNIX
						it.startsWith("Windows") -> OsType.WINDOWS
						else                     -> OsType.UNIX
					}
				}
			}
			return field
		}
	public actual val isUnix: Boolean
		get() = osType == OsType.UNIX
	public actual val isWindows: Boolean
		get() = osType == OsType.WINDOWS

}

public actual fun computeAppPath(): String {
	// if part of a KScript env KSCRIPT_FILE is set to path of script
	System.getenv("KSCRIPT_FILE")?.run {
		return this
	}

	// else get java app.name or first param of commandline
	return System.getProperty("app.name")
		?: System.getProperty("sun.java.command")?.split(' ', limit = 2)?.get(0)?.removeSurrounding("\"")?.removeSurrounding("'")
		?: "_appName_"
}
