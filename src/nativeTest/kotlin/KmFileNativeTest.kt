/*
 * Copyright 2021-2023 Jürgen Reichmann

, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlin.test.Test
import kotlin.test.assertTrue


internal class FileNativeTest {
	private val tstFile = KmFile("/tmp/x.x")

	@Test
	fun fileName() {
		println("FileNativeTest.fileName start")

		println("  fileName ${tstFile.name}")
		assertTrue(tstFile.name.startsWith("x.x"))

		println("FileNativeTest.fileName end")
	}
}

