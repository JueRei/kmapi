/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package de.rdvsb.kmapi

import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

//@OptIn(ExperimentalTime::class)
public expect class KmProcess(process: Any) {
	public val outputStream: OutputStream // connected do STDIN of process
	public val inputStream: InputStream // connected do STDOUT of process
	public val errorStream: InputStream // connected do STDERR of process

	public fun waitFor(): Int
	public fun waitFor(timeout: Long, unit: DurationUnit): Boolean

	public fun exitValue(): Int
	public fun destroy(): Unit
	public fun destroyForcibly(): KmProcess
	public val isAlive: Boolean
}


