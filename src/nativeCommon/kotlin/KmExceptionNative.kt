/*
 * Copyright 2023 Jürgen Reichmann
, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

public actual open class IOException : Throwable {
	public actual constructor() : super()

	public actual constructor(message: String?) : super(message)

	public actual constructor(message: String?, cause: Throwable?) : super(message, cause)

	public actual constructor(cause: Throwable?) : super(cause)
}