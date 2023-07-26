/*
 * Copyright 2023 Jürgen Reichmann
, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package de.rdvsb.kmapi

public expect open class IOException : Throwable {
	public constructor()
	public constructor(message: String?)
	public constructor(message: String?, cause: Throwable?)
	public constructor(cause: Throwable?)
}
