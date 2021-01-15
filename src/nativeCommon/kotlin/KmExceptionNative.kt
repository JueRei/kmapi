package de.rdvsb.kmapi

public actual open class IOException : Throwable {
	public actual constructor() : super()

	public actual constructor(message: String?) : super(message)

	public actual constructor(message: String?, cause: Throwable?) : super(message, cause)

	public actual constructor(cause: Throwable?) : super(cause)
}