package de.rdvsb.kmapi

public expect class File(pathName: String) {

	public companion object {
		public val separatorChar: Char
		public val separator: String
		public fun createTempDirectory(prefix: String): File
	}

	override fun toString(): String

	public val name: String
	public val path: String

	public val absolutePath: String
	public val absoluteFile: File

	public val canonicalPath: String
	public val canonicalFile: File

	public fun exists(): Boolean
	public val isFile: Boolean
	public val isDirectory: Boolean
	public val isDevice: Boolean // not in Java.io.File
	public val isSymbolicLink: Boolean // not in Java.io.File
	public val isHidden: Boolean

	public fun canWrite():Boolean

	public fun canRead(): Boolean
	public fun lastModified(): Long
	public fun creationTime(): Long // not in Java.io.File
	public fun length(): Long

	public fun renameTo(newFile: File): Boolean

	// some extras not found in java.io.File:
	public enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	public enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, TERMINATE, ABORT }

	public fun walkDir(callBack: (callBackFor: CallBackFor, file: File, errorStr: String?) -> CallBackResult): CallBackResult

	public fun delete(retries: UInt = 0U): Boolean
}

public fun File.deleteDir(filesOnly: Boolean, retries: UInt = 0U): Boolean { // not in Java.io.File
	// recursively delete dir
	if (name.isEmpty() || !isDirectory) return false

	val walkRC = walkDir callBack@{ callBackFor, foundFile, _ ->
		when (callBackFor) {
			File.CallBackFor.ENTERDIR -> return@callBack File.CallBackResult.ENTER

			File.CallBackFor.LEAVEDIR,
			File.CallBackFor.FILE     -> {
				filesOnly && foundFile.isDirectory && return@callBack File.CallBackResult.OK
				foundFile.delete(retries)
				File.CallBackResult.OK
			}
		}
	}

	return true
}


//private fun x(): Unit {
//	val f = File("x")
//	f.canWrite()
//	f.canWrite()
//	f.name
//	f.path
//	f.absoluteFile
//	f.parent
//	f.isDirectory
//	f.exists()
//	f.lastModified()
//	f.length()
//	f.renameTo(f)
//
//}

