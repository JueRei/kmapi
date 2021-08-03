/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class SysCommonTest {

	@Test
	fun sysTest() {
		println("SysCommonTest.sysTest start currentTimeMillis=${System.currentTimeMillis()}")

		println("lineSeparator=${System.lineSeparator.encodeToByteArray().map { it }}")
		println("getProperty(\"line.separator\")=${System.getProperty("line.separator")?.encodeToByteArray()?.map { it }}")

		println("app.name=${System.getProperty("app.name")}")
		println("sun.java.command=${System.getProperty("sun.java.command")}")

		println("${System.getProperties().toString().replace("\r", "\\r").replace("\n", "\\n")}")
		var property = System.setProperty("http.agent", "Chrome")
		println("1. setProperty=$property")
		assertNull(property, "old value for property http.agent expected to be null")

		property = System.setProperty("http.agent", "Chrome")
		println("2. setProperty=$property")
		assertEquals(property, "Chrome","old value for property http.agent expected to be Chrome")

		property = System.setProperty("http.agent", "Firefox")
		println("3. setProperty=$property")
		assertEquals(property, "Chrome","old value for property http.agent expected to be Chrome")

		property = System.getProperty("http.agent")
		println("4. getProperty=$property")
		assertEquals(property, "Firefox","new value for property http.agent expected to be Firefox")

		println("SysCommonTest.sysTest end")
	}

}
