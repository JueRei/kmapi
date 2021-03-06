/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package de.rdvsb.kmapi

public expect class File(pathName: String) {

	public companion object {
		public val separatorChar: Char
		public val separator: String
		public fun createTempDirectory(prefix: String): File
	}

	override fun toString(): String

	public val name: String
	public val path: String

	public val absolutePath: String
	public val absoluteFile: File

	public val canonicalPath: String
	public val canonicalFile: File

	public fun exists(): Boolean
	public val isFile: Boolean
	public val isDirectory: Boolean
	public val isDevice: Boolean // not in Java.io.File
	public val isSymbolicLink: Boolean // not in Java.io.File
	public val isHidden: Boolean

	public fun canWrite():Boolean

	public fun canRead(): Boolean
	public fun lastModified(): Long
	public fun creationTime(): Long // not in Java.io.File
	public fun length(): Long

	public fun renameTo(newFile: File): Boolean

	// some extras not found in java.io.File:
	public enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	public enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, TERMINATE, ABORT }

	public fun walkDir(callBack: (callBackFor: CallBackFor, file: File, errorStr: String?) -> CallBackResult): CallBackResult

	public fun delete(retries: UInt = 0U): Boolean
}

public fun File.deleteDir(filesOnly: Boolean, retries: UInt = 0U): Boolean { // not in Java.io.File
	// recursively delete dir
	if (name.isEmpty() || !isDirectory) return false

	val walkRC = walkDir callBack@{ callBackFor, foundFile, _ ->
		when (callBackFor) {
			File.CallBackFor.ENTERDIR -> File.CallBackResult.ENTER

			File.CallBackFor.LEAVEDIR,
			File.CallBackFor.FILE     -> {
				filesOnly && foundFile.isDirectory && return@callBack File.CallBackResult.OK
				foundFile.delete(retries)
				File.CallBackResult.OK
			}
			else                      -> File.CallBackResult.OK
		}
	}

	return true
}



/**
 * Reads this file line by line  calls [action] for each line.
 * charset is UTF-8.
 *
 * @param action function to process file lines.
 */
public expect fun File.forEachLine(action: (line: String) -> Unit): Unit

/**
 * Gets the entire content of this file as a String using UTF-8
 *
 * This method is not recommended on huge files. It has an internal limitation of 2 GB file size.
 *
 * @return the entire content of this file as a String.
 */
public expect fun File.readText(): String