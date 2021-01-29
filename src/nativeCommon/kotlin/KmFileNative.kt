/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

// implement common functions for native implementation
public interface FileNativeCommon {
	public val path: String
	public val name: String
		get() {
			val ix = path.lastIndexOf(File.separatorChar)
			return path.substring(ix + 1)
		}

	public val isInvalid: Boolean get() = path.isEmpty() || path.contains('\u0000')

}


/**
 * Reads this file line by line  calls [action] for each line.
 * charset is UTF-8.
 *
 * @param action function to process file lines.
 */
public actual fun File.forEachLine(action: (line: String) -> Unit) {
	// Note: close is called at forEachLine
	TODO("forEachLine not implemented for native")
}

/**
 * Gets the entire content of this file as a String using UTF-8
 *
 * This method is not recommended on huge files. It has an internal limitation of 2 GB file size.
 *
 * @return the entire content of this file as a String.
 */
public actual fun File.readText(): String = TODO("readText not implemented for native")