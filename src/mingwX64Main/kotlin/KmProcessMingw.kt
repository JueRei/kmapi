/*
 * Copyright 2023 Jürgen Reichmann
, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public actual class KmProcess actual constructor(process: Any) {
	private var mingwProcess: Any = process

	public actual  val outputStream: OutputStream = OutputStream.nullOutputStream() // connected do STDIN of process
	public actual  val inputStream: InputStream  = InputStream.nullInputStream() // connected do STDOUT of process
	public actual  val errorStream: InputStream =  InputStream.nullInputStream() // connected do STDERR of process

	public actual  fun waitFor(): Int = -1
	public actual fun waitFor(timeout: Long, unit: DurationUnit): Boolean {
		TODO("Not yet implemented")
	}

	public actual  fun exitValue(): Int = -1
	public actual  fun destroy(){
		TODO("Not yet implemented")
	}
	public actual fun destroyForcibly(): KmProcess {
		destroy()
		return this
	}

	public actual val isAlive: Boolean
		get() = TODO("Not yet implemented")

}

