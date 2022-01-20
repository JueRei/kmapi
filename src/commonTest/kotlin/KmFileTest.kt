/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlin.test.Test
import kotlin.test.assertTrue


internal class FileCommonTest {
	private val tstFile = KmFile("..${KmFile.separatorChar}x.x")

	@Test
	fun fileName() {
		println("FileCommonTest.fileName start")

		println("  KmFile: name=${tstFile.name} path=${tstFile.path} absolutePath=${tstFile.absolutePath}  canonicalPath=${tstFile.canonicalPath}")
		assertTrue(tstFile.name.startsWith("x.x"))
		assertTrue(tstFile.path.startsWith("..${KmFile.separatorChar}"))

		println("FileCommonTest.fileName end")
	}

}
