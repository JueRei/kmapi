/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import java.lang.Process
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public actual class KmProcess actual constructor(process: Any) {
	private var jvmProcess: java.lang.Process = process as java.lang.Process
	public actual val outputStream: OutputStream = jvmProcess.outputStream as OutputStream // connected do STDIN of process
	public actual val inputStream: InputStream = jvmProcess.inputStream as InputStream // connected do STDOUT of process
	public actual val errorStream: InputStream = jvmProcess.errorStream as InputStream // connected do STDERR of process

	public actual fun waitFor(): Int = jvmProcess.waitFor()

	public actual fun waitFor(timeout: Long, unit: DurationUnit): Boolean = jvmProcess.waitFor(timeout, unit)

	public actual fun destroy(): Unit = jvmProcess.destroy()

	public actual fun destroyForcibly(): KmProcess {
		destroy()
		return this
	}

	public actual val isAlive: Boolean
		get() = jvmProcess.isAlive()

	public actual fun exitValue(): Int = jvmProcess.exitValue()

}

