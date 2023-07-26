/*
 * Copyright 2021-2023 Jürgen Reichmann

, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import java.util.*

public actual object System {
	public actual object err {
		public actual fun println(line: String): Unit = java.lang.System.err.println(line)
		public actual fun print(text: String): Unit = java.lang.System.err.print(text)
	}


	public actual object out {
		public actual fun println(line: String): Unit = java.lang.System.out.println(line)
		public actual fun print(text: String): Unit = java.lang.System.out.print(text)
	}

	init {
		if (getProperty("app.name") == null) setProperty("app.name", computeAppPath())
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

public actual fun computeAppPath(mainObj: Any?): String {
	// if part of a KScript env KSCRIPT_FILE is set to path of script
	System.getenv("KSCRIPT_FILE")?.run {
		return this
	}

	// if part of a KScript getArgs is a subclass of predefined "script" class (e.g. "Fetch_hkg_adm556.getArgs")
	mainObj?.apply {
		javaClass.canonicalName.split('.', limit = 2)?.run {
			if (size == 2) return get(0).replaceFirstChar { it.lowercase(Locale.getDefault()) }
		}
	}

	// else get java app.name or first param of commandline
	val appPath = System.getProperty("sun.java.command")?.split(' ', limit = 2)?.get(0)	?: "_appName_"
	appPath.run {
		removeSurrounding("\"")
		removeSurrounding("'")
		removeSuffix(".jar")
	}
	return appPath
}
