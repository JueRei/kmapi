/*
 * Copyright 2021 Jürgen Reichmann, Jettingen, Germany
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package de.rdvsb.kmapi

import kotlinx.cinterop.*
import platform.linux.ENOTFOUND
import platform.posix.*
import kotlin.math.max

private val setLocale = setlocale(LC_ALL, "en_US.UTF-8"); // allow thousands separators

// share a common thread local C char buffer
private const val cstrBufLen = 2048
@ThreadLocal // one C variable for each thread
private val cstrBuf = nativeHeap.allocArray<ByteVar>(cstrBufLen)



public fun posixErrorMessage(err: Int): String {

	strerror_r(err, cstrBuf, cstrBufLen.toULong())
	return cstrBuf.toKString()
}


public actual class File actual constructor(pathName: String) : FileNativeCommon {
	private var pathName = pathName
	actual override val path: String
		get() = pathName

	actual override fun toString(): String = pathName

	@kotlin.native.concurrent.ThreadLocal
	public actual companion object {
		public actual val separatorChar: Char
			get() = '/'
		public actual val separator: String
			get() = "/"
		public actual fun createTempDirectory(prefix: String): File {
			val dirName = "/tmp/$prefix${random()}"
			mkdir(dirName, 7U shl 6) // 0700 only owner:rwx
			return File(dirName)
		}

		private val wcharBufSize = max(128 * 256, PATH_MAX)
		// one buffer for each thread
		private val wcharBuf = nativeHeap.allocArray<wchar_tVar>(wcharBufSize)
		// one buffer for each thread
		private val cstrBuf = nativeHeap.allocArray<ByteVar>(wcharBufSize)
		// one buffer for each thread
		private val fileStatBuf1 = nativeHeap.alloc<stat>()
	}

	private var absName: String? = null
	private var fileMode: UInt = 0U
	private var mTimeMS = 0L
	private var cTimeMS = 0L
	private var fileLength = 0L

	private constructor(pathName: String, absName: String?, canonicalName: String?) : this(pathName) {
		this.absName = absName
		//this.canonicalName = canonicalName
	}

	private fun fillFileAttributes(isQuiet: Boolean = false): Boolean {
		if (pathName.isEmpty()) return false

		if (fileMode == 0U) {
			val fName = canonicalPath
			if (stat(fName, fileStatBuf1.ptr) != 0) {
				if (!isQuiet) perror("cannot stat dev1 $fName")
				return false
			}
			fileMode = fileStatBuf1.st_mode
			mTimeMS = fileStatBuf1.st_mtim.tv_sec * 1_000 + fileStatBuf1.st_mtim.tv_nsec / 1_000_000
			cTimeMS = fileStatBuf1.st_ctim.tv_sec * 1_000 + fileStatBuf1.st_ctim.tv_nsec / 1_000_000
			fileLength = fileStatBuf1.st_size
		}

		return fileMode != 0U
	}

	public actual val absolutePath: String
		get() {
			if (absName == null) {
				var rp = realpath(pathName, cstrBuf) // cstrBuf must be at last the size of PATH_MAX
				rp.rawValue.let { absName = cstrBuf.toKString() }
			}
			return absName ?: ""
		}
	public actual val absoluteFile: File
		get() = File(absolutePath)
	public actual val canonicalPath: String

		get() =  absolutePath
	public actual val canonicalFile: File
		get() = absoluteFile


	public actual fun exists(): Boolean = fillFileAttributes(true)
	public actual val isDirectory: Boolean get() = fillFileAttributes(false) && (fileMode and S_IFDIR.toUInt() != 0U)
	public actual val isDevice: Boolean get() = fillFileAttributes(true) && (fileMode and (S_IFBLK.toUInt() or S_IFCHR.toUInt()) != 0U)
	public actual val isSymbolicLink: Boolean get() = fillFileAttributes(true) && (fileMode and S_IFLNK.toUInt() != 0U) // not in Java File class
	public actual val isHidden: Boolean get() = name.startsWith('.')
	public actual val isFile: Boolean get() = !isDirectory && !isDevice && fileMode != 0U
	public actual fun canWrite(): Boolean = fillFileAttributes(true) && (fileMode and S_IWUSR.toUInt() != 0U)
	public actual fun canRead(): Boolean = fillFileAttributes(true) && (fileMode and S_IRUSR.toUInt() != 0U)

	public actual fun lastModified(): Long = if (fillFileAttributes(true)) mTimeMS else 0L
	public actual fun creationTime(): Long = if (fillFileAttributes(true)) cTimeMS else 0L // not in Java File class
	public actual fun length(): Long = if (fillFileAttributes(true)) fileLength else 0L

	public actual fun renameTo(newFile: File): Boolean {
		if (pathName.isEmpty() || newFile.pathName.isEmpty() || canonicalPath == newFile.canonicalPath) return false

		val rc = rename(canonicalPath, newFile.canonicalPath)
		if (rc != 0) {
			perror("cannot rename \"$canonicalPath\" to \"${newFile.canonicalPath}\"")
			return false
		}

		pathName = newFile.path
		absName = newFile.absolutePath
		return true

	}

	public actual fun delete(retries: UInt): Boolean {
		if (isInvalid) return false

		for (i in 0U..retries) {
			var rc =
				if (isDirectory) {
					rmdir(canonicalPath)
				} else {
					unlink(canonicalPath)
				}

			if (rc == 0 || rc == ENOTFOUND) {
				fileMode = 0U
				return true
			}

			if (i == retries) {
				perror("cannot delete \"$canonicalPath\"")
			} else {
				sleep(i)
			}
		}

		return false
	}

	public actual enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	public actual enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, TERMINATE, ABORT }

	public actual fun walkDir(callBack: (callBackFor: CallBackFor, file: File, errorStr: String?) -> CallBackResult): CallBackResult {
		if (isFile) return callBack(CallBackFor.FILE, this, null)
		if (!isDirectory) return CallBackResult.NOK

		var callBackResult = callBack(CallBackFor.ENTERDIR, this, null)
		if (callBackResult != CallBackResult.ENTER) return callBackResult

		val dirH = opendir(canonicalPath)
		if (dirH == null) {
			val errStr = "$errno:${posixErrorMessage(errno)}"
			System.err.println("opendir \"${canonicalPath}\" => error=\"$errStr\"")
			return callBack(CallBackFor.LEAVEDIR, this, errStr)
		}

		NextFile@ while (true) {
			set_posix_errno(0)
			val dirEnt = readdir(dirH)
			var errStr: String? = null
			if (dirEnt == null) {
				if (errno != 0) {
					errStr = "$errno:${posixErrorMessage(errno)}"
					System.err.println("readdir \"${canonicalPath}\" => error=\"$errStr\"")
				}
				closedir(dirH)
				return callBack(CallBackFor.LEAVEDIR, this, errStr)
			}

			val fName = dirEnt.pointed.d_name.toKString()
			if (fName == "." || fName == "..") continue@NextFile
			//println("Find*FileW: fName=$fName findFileDataBuf.nFileSizeHigh=${findFileDataBuf.nFileSizeHigh} findFileDataBuf.nFileSizeLow=${findFileDataBuf.nFileSizeLow}")

			val foundFile = File("$path$separatorChar$fName", "$absolutePath$separatorChar$fName", "$canonicalPath$separatorChar$fName")

			var isDir: Boolean
			if (dirEnt.pointed.d_type.toInt() == DT_UNKNOWN) {
				isDir = isDirectory
			} else {
				isDir = dirEnt.pointed.d_type.toInt() == DT_DIR
			}

			if (isDir) {
				callBackResult = foundFile.walkDir(callBack)
			} else{
				callBackResult = callBack(CallBackFor.FILE, foundFile, null)
			}

			when (callBackResult) {
				CallBackResult.TERMINATE, CallBackResult.ABORT -> {
					break@NextFile
				}

				CallBackResult.LEAVE -> {
					closedir(dirH)
					return callBack(CallBackFor.LEAVEDIR, this, null)
				}
			}
		}

		closedir(dirH)
		return callBackResult
	}

}