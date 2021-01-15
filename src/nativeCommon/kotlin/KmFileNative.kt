package de.rdvsb.kmapi

// implement common functions for native implementation
public interface FileNativeCommon {
	public val path: String
	public val name: String
		get() {
			val ix = path.lastIndexOf(File.separatorChar)
			return path.substring(ix + 1)
		}

	public val isInvalid: Boolean get() = path.isEmpty() || path.contains('\u0000')

}


