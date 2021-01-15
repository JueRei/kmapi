package de.rdvsb.kmapi

public expect open class IOException : Throwable {
	public constructor()
	public constructor(message: String?)
	public constructor(message: String?, cause: Throwable?)
	public constructor(cause: Throwable?)
}
