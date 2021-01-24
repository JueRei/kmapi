package de.rdvsb.kmapi


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public expect object System {
	public object err {
		public fun println(line: String): Unit
		public fun print(text: String): Unit
	}
	public object out {
		public fun println(line: String): Unit
		public fun print(text: String): Unit
	}

	public fun getenv(name: String):String?
	public fun currentTimeMillis(): Long
	public val lineSeparator: String
	public fun getProperty(name: String): String?
	public fun setProperty(name: String, value: String): String?
	public fun getProperties(): MutableMap<String, String>
	public fun exit(status: Int): Nothing

	public val isUnix: Boolean
	public val isWindows: Boolean
}

//* mainObj: any object in the main module (e.g. getArgs)
public expect fun computeAppPath(mainObj: Any? = null): String