package de.rdvsb.kmapi

import java.io.IOException
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import java.io.File as JavaIOFile
import java.nio.file.attribute.BasicFileAttributes

import java.nio.file.Files
import kotlin.io.path.isDirectory
import kotlin.io.path.name

//public actual typealias File = java.io.File

public actual class File actual constructor(pathName: String): JavaIOFile(pathName) {

	public actual companion object {
		public actual val separatorChar: Char
			get() = JavaIOFile.separatorChar
		public actual val separator: String
			get() = JavaIOFile.separator

		@OptIn(ExperimentalPathApi::class)
		public actual fun createTempDirectory(prefix: String): File {
			return File(java.nio.file.Files.createTempDirectory(prefix).name)
		}
	}

	public actual val name: String
		@JvmName("getNameX")  // solution for https://youtrack.jetbrains.com/issue/KT-6653
		get() = super.getName()
	public actual val path: String
		@JvmName("getPathX")
		get() = super.getPath()
	public actual val absolutePath: String
		@JvmName("getAbsolutePathX")
		get() = super.getAbsolutePath()
	public actual val absoluteFile: File
		@JvmName("getAbsoluteFileX")
		get() = File(getAbsolutePath())
	public actual val canonicalPath: String
		@JvmName("getCanonicalPathX")
		get() = super.getCanonicalPath()
	public actual val canonicalFile: File
		@JvmName("getCanonicalFileX")
		get() = File(getCanonicalPath())


	private var fileAttr: BasicFileAttributes? = null

	@OptIn(ExperimentalPathApi::class)
	private fun fillFileAttributes(isQuiet: Boolean = false): BasicFileAttributes? {
		if (name.isEmpty()) return null
		if (fileAttr == null) {
			try {
				val attrs = Files.readAttributes(Path(path), BasicFileAttributes::class.java)
				fileAttr = attrs
			} catch (exp: IOException) {
				return null;
			}
		}
		return fileAttr
	}

	public actual val isFile: Boolean
		@JvmName("isFileX")
		get() = super.isFile()
	public actual val isDirectory: Boolean
		@JvmName("isDirectoryX")
		get() = super.isDirectory()
	public actual val isHidden: Boolean
		@JvmName("isHiddenX")
		get() = super.isHidden()

	@OptIn(ExperimentalPathApi::class)
	public actual val isSymbolicLink: Boolean get() = fillFileAttributes()?.isSymbolicLink ?: false

	@OptIn(ExperimentalPathApi::class)
	public actual val isDevice: Boolean
		get() {
			if (isFile || isDirectory) return false;
			val attrs = fillFileAttributes() ?: return false
			if (attrs.isOther) return true

			// shouldn't really get here:
			//   Java has no std access to the Unix/Windows device attribute, so we simulate it by checking device names
			//   this is just a heuristic!
			val absPath = super.getAbsolutePath().toLowerCase()
			if (absPath.startsWith("/dev/") || absPath.startsWith("\\device\\") || absPath.startsWith("\\dosdevices\\")) return true
			val msdosName = absPath.removeSuffix(":").toUpperCase()
			if (msdosName == "NUL" || msdosName == "PRT" || msdosName == "AUX"
				|| msdosName == "AUX" || msdosName == "COM1" || msdosName == "COM2" || msdosName == "COM3"
				|| msdosName == "LPT1" || msdosName == "LPT2" || msdosName == "LPT3") {
				return true
			}
			return false
		}

	public actual fun creationTime(): Long = fillFileAttributes()?.creationTime()?.toMillis() ?: 0L

	public actual fun renameTo(newFile: File): Boolean = super.renameTo(newFile)

	public actual fun delete(retries: UInt): Boolean {
		if (super.delete()) return true
		for (i in 1U..retries) {
			if (super.delete()) {
				fileAttr = null
				return true
			}

			if (i == retries) {
				//perror("cannot delete \"${canonicalPath}\"")
			} else {
				Thread.sleep(i.toLong() * 1000L)
			}
		}

		return false
	}

	public actual enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	public actual enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, TERMINATE, ABORT }

	@OptIn(ExperimentalPathApi::class)
	public actual fun walkDir(callBack: (callBackFor: CallBackFor, file: File, errorStr: String?) -> CallBackResult): CallBackResult {
		if (isFile) return callBack(CallBackFor.FILE, this, null)
		if (!isDirectory) return CallBackResult.NOK

		var callBackResult = callBack(CallBackFor.ENTERDIR, this, null)
		if (callBackResult != CallBackResult.ENTER) return callBackResult


		val dirStream = try {
			java.nio.file.Files.newDirectoryStream(Path(absolutePath))
		} catch (ex: Exception) {
			System.err.println("newDirectoryStream \"${canonicalPath}\" => error=\"${ex}\"")
			return callBack(CallBackFor.LEAVEDIR, this, ex.toString())
		}

		NextFile@ for (dirEnt in dirStream) {
			var errStr: String? = null

			val fName = dirEnt.name
			if (fName == "." || fName == "..") continue@NextFile
			//println("Find*FileW: fName=$fName findFileDataBuf.nFileSizeHigh=${findFileDataBuf.nFileSizeHigh} findFileDataBuf.nFileSizeLow=${findFileDataBuf.nFileSizeLow}")

			val foundFile = File("$path$separatorChar$fName")

			if (dirEnt.isDirectory()) {
				callBackResult = foundFile.walkDir(callBack)
			} else{
				callBackResult = callBack(CallBackFor.FILE, foundFile, null)
			}

			when (callBackResult) {
				CallBackResult.TERMINATE, CallBackResult.ABORT -> {
					dirStream.close()
					return callBackResult
				}

				CallBackResult.LEAVE -> {
					dirStream.close()
					return callBack(CallBackFor.LEAVEDIR, this, null)
				}
			}
		}

		dirStream.close()
		return callBack(CallBackFor.LEAVEDIR, this, null)
	}


}

