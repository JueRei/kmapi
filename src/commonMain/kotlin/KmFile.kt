/*
 * Copyright 2021-2022 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package de.rdvsb.kmapi

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public expect class KmFile(pathName: String) {

	public companion object {
		public val separatorChar: Char
		public val separator: String
		public fun createTempDirectory(prefix: String): KmFile
	}

	override fun toString(): String

	public val name: String
	public val path: String

	public val absolutePath: String
	public val absoluteFile: KmFile

	public val canonicalPath: String
	public val canonicalFile: KmFile

	public fun exists(): Boolean
	public val isFile: Boolean
	public val isDirectory: Boolean
	public val isDevice: Boolean // not in Java.io.KmFile
	public val isSymbolicLink: Boolean // not in Java.io.KmFile
	public val isHidden: Boolean

	public fun canWrite():Boolean

	public fun canRead(): Boolean
	public fun lastModified(): Long
	public fun creationTime(): Long // not in Java.io.KmFile
	public fun length(): Long

	public fun renameTo(newFile: KmFile): Boolean

	// some extras not found in java.io.KmFile:
	public enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	public enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, TERMINATE, ABORT }

	public fun walkDir(callBack: (callBackFor: CallBackFor, file: KmFile, errorStr: String?) -> CallBackResult): CallBackResult

	public fun delete(retries: UInt = 0U): Boolean
}

public fun KmFile.deleteDir(filesOnly: Boolean, retries: UInt = 0U): Boolean { // not in Java.io.KmFile
	// recursively delete dir
	if (name.isEmpty() || !isDirectory) return false

	val walkRC = walkDir callBack@{ callBackFor, foundFile, _ ->
		when (callBackFor) {
			KmFile.CallBackFor.ENTERDIR -> KmFile.CallBackResult.ENTER

			KmFile.CallBackFor.LEAVEDIR,
			KmFile.CallBackFor.FILE     -> {
				filesOnly && foundFile.isDirectory && return@callBack KmFile.CallBackResult.OK
				foundFile.delete(retries)
				KmFile.CallBackResult.OK
			}
			else                        -> KmFile.CallBackResult.OK
		}
	}

	return true
}

/**
 * try to rename KmFile to newFile
 * @param removeDestFirst: remove fileDstPath if it exists
 * @return `true` on success, `false` on cannot rename
 */
public fun KmFile.tryRenameTo(newFile: KmFile, removeDestFirst: Boolean = false): Boolean {
	try {
		if (!newFile.exists() || newFile.delete(1U))	return this.renameTo(newFile)
	} catch (e: Exception) {
	}
	return false
}


/**
 * Reads this file line by line  calls [action] for each line.
 * charset is UTF-8.
 *
 * @param action function to process file lines.
 */
public expect fun KmFile.forEachLine(action: (line: String) -> Unit): Unit

/**
 * Gets the entire content of this file as a String using UTF-8
 *
 * This method is not recommended on huge files. It has an internal limitation of 2 GB file size.
 *
 * @return the entire content of this file as a String.
 */
public expect fun KmFile.readText(): String

/**
 * returns the time span since the last modification
 *
 * @return Duration since last modification.
 */
public fun KmFile.modificationAge():Duration = (System.currentTimeMillis() - this.lastModified()).milliseconds