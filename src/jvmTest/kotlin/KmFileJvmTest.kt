package de.rdvsb.kmapi

import kotlin.test.Test
import kotlin.test.assertTrue
//import java.io.File

internal class FileJvmTest {
	private val tstFile = File("/tmp/x.x")
	private val f = java.io.File("/tmp/x1.x")

	@Test
	fun fileName() {
		println("FileJvmTest.fileName start")

		println("  fileName=\"${tstFile.name}\" f=\"${f.name}\", tst=${tstFile.absoluteFile.path}")
		assertTrue(tstFile.name.startsWith("x.x"))

		println("FileJvmTest.fileName end")

	}


}