package de.rdvsb.kmapi

import kotlin.test.Test
import kotlin.test.assertTrue


internal class FileNativeTest {
	private val tstFile = File("/tmp/x.x")

	@Test
	fun fileName() {
		println("FileNativeTest.fileName start")

		println("  fileName ${tstFile.name}")
		assertTrue(tstFile.name.startsWith("x.x"))

		println("FileNativeTest.fileName end")
	}
}

