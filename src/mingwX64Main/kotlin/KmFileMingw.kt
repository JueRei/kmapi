package de.rdvsb.kmapi

private fun posixErrorMessage(err: Int): String {

	strerror_s(cstrBuf, cstrBufLen.toULong(), err)
	return cstrBuf.toKString()
}

private fun winSysErrMessage(winErrorCode: UInt): String {
	memScoped {
		System.err.println("winSysErrMessage winErrorCode=\"$winErrorCode\"")
		val msgBufSize = 256
		val msgBuf = allocArray<WCHARVar>(msgBufSize)

		val rc = FormatMessageW((FORMAT_MESSAGE_FROM_SYSTEM + FORMAT_MESSAGE_IGNORE_INSERTS).toUInt(),
		                        null, winErrorCode, 0U, msgBuf, msgBufSize.toUInt(), null).toInt()

		rc == 0 && return "WinError $winErrorCode"

		return msgBuf.toKString().trimEnd() // ('\n', '\r')
	}
}

private const val WIN_TO_UNIX_EPOCH_MS = 11644473600 * 1000L // A Windows file time is a 64-bit value that represents the number relative to 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).


public actual class File actual constructor(pathName: String) : FileNativeCommon {
	private var pathName = pathName
	actual override val path: String
		get() = pathName

	actual override fun toString(): String = pathName

	// one buffer for each thread
	@kotlin.native.concurrent.ThreadLocal
	public actual companion object {
		public actual val separatorChar: Char
			get() = '/'
		public actual val separator: String
			get() = "/"
		public actual fun createTempDirectory(prefix: String): File {
			val dirName = "${System.getenv("Tmp") ?: System.getenv("Temp") ?: "c:{$separatorChar}Temp"}$separatorChar$prefix${random()}"
			TODO("create dir")
			return File(dirName)
		}
		private val wcharBufSize = max(128 * 256, PATH_MAX)
		private val wcharBuf = nativeHeap.allocArray<wchar_tVar>(wcharBufSize)
		private val cstrBuf = nativeHeap.allocArray<ByteVar>(wcharBufSize)
		private val fileAttrBuf1 = nativeHeap.alloc<WIN32_FILE_ATTRIBUTE_DATA>()
		private val findFileDataBuf = nativeHeap.alloc<WIN32_FIND_DATAW>()
	}
	private var absName: String? = null
	private var canonicalName: String? = null
	private var dwFileAttributes: Int = 0
	private var mTimeMS = 0L
	private var cTimeMS = 0L
	private var fileLength = 0L

	private constructor(name: String, absName: String?, canonicalName: String?) : this(name) {
		this.absName = absName
		this.canonicalName = canonicalName
	}

	private fun fillFileAttributes(isQuiet: Boolean = false): Boolean {
		if (name.length == 0) return false

		if (dwFileAttributes == 0) {
			fileAttrBuf1.nFileSizeLow = 800u
			fileAttrBuf1.nFileSizeHigh = 0u
			fileAttrBuf1.dwFileAttributes = 0u
			val fName = canonicalPath
			val rc = GetFileAttributesExW(fName, GET_FILEEX_INFO_LEVELS.GetFileExInfoStandard, fileAttrBuf1.ptr)
			if (rc == TRUE) {
				//ifDebugThen { println("GetFileAttributesExW: fName=$name($fName) fileAttrBuf1.nFileSizeHigh=${fileAttrBuf1.nFileSizeHigh} fileAttrBuf1.nFileSizeLow=${fileAttrBuf1.nFileSizeLow}") }
				dwFileAttributes = fileAttrBuf1.dwFileAttributes.toInt()
				// A Windows file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
				mTimeMS = (fileAttrBuf1.ftLastWriteTime.dwHighDateTime.toULong().shl(32) + fileAttrBuf1.ftLastWriteTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
				cTimeMS = (fileAttrBuf1.ftCreationTime.dwHighDateTime.toULong().shl(32) + fileAttrBuf1.ftCreationTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
				fileLength = (fileAttrBuf1.nFileSizeHigh.toULong().shl(32) + fileAttrBuf1.nFileSizeLow.toULong()).toLong()
			} else {
				if (!isQuiet) System.err.println("GetFileAttributesExW \"$fName\" failed (${winSysErrMessage(GetLastError())})")
			}
		}

		return dwFileAttributes != 0
	}

	public actual val absolutePath: String
		get() {
			if (absName == null) {
				var rc = GetFullPathNameW(name, File.wcharBufSize.toUInt(), File.wcharBuf, null)
				if (rc > 0U && rc <= File.wcharBufSize.toUInt()) absName = File.wcharBuf.toKString()
			}
			return absName ?: ""
		}

	public actual val absoluteFile: File
		get() = File(absolutePath, absName, canonicalName)

	public actual val canonicalPath: String
		get() {
		if (name.length > 0 && canonicalName == null) {
			val absName = absolutePath
			canonicalName = when { // "\\\\?\\$absDirName" // allow very long paths
				absName.length == 0           -> absName
				absName.startsWith("""\\?""") -> absName
				absName.startsWith("""\\""")  -> "\\\\?\\UNC${absName.substring(1)}"
				else                          -> "\\\\?\\$absName"
			}
		}
		return canonicalName ?: ""
	}

	public actual val canonicalFile: File
		get() = File(absolutePath, absName, canonicalName)

	public actual val exists: Boolean get() = fillFileAttributes(true)
	public actual val isDirectory: Boolean get() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY != 0)
	public actual val isDevice: Boolean get() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_DEVICE != 0)
	public actual val isSymbolicLink: Boolean get() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_REPARSE_POINT != 0) // not in Java File class
	public actual val isHidden: Boolean get() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_HIDDEN != 0)
	public actual val isFile: Boolean get() = !isDirectory && !isDevice && dwFileAttributes != 0
	public actual val canWrite: Boolean get() = fillFileAttributes() && isFile && (dwFileAttributes and FILE_ATTRIBUTE_READONLY == 0)
	public actual val canRead: Boolean get() = fillFileAttributes() && isFile // TODO: check if read/write on Windows devices is possible with regular read write API

	public actual val lastModified: Long get() = if (fillFileAttributes()) mTimeMS else 0L
	public actual val creationTime: Long get() = if (fillFileAttributes()) cTimeMS else 0L // not in Java File class
	public actual val length: Long get() = if (fillFileAttributes()) fileLength else 0L

	public fun renameTo(newFile: File): Boolean {
		if (name.isEmpty() || newFile.name.isEmpty() || canonicalPath == newFile.canonicalPath) return false

		val rc = MoveFileExW(canonicalPath, newFile.canonicalPath, MOVEFILE_COPY_ALLOWED)
		if (rc != TRUE) {
			val winErr = GetLastError()
			val errStr = "$winErr:${winSysErrMessage(winErr)}"
			System.err.println("rename \"$canonicalPath\" to \"${newFile.canonicalPath}\" => error=\"$errStr\"")

			return false
		}

		pathName = newFile.path
		absName = newFile.absolutePath
		canonicalName = newFile.canonicalPath

		return true
	}

	public actual fun delete(retries: UInt): Boolean {
		if (name.isEmpty()) return false

		for (i in 0U..retries) {
			var rc =
				if (isDirectory) {
					RemoveDirectoryW(canonicalPath)
				} else {
					DeleteFileW(canonicalPath)
				}

			if (rc == TRUE) {
				dwFileAttributes = 0
				return true
			}

			val winErr = GetLastError()
			if (winErr == ERROR_FILE_NOT_FOUND.toUInt()) return false

			if (i == retries) {
				val errStr = "$winErr:${winSysErrMessage(winErr)}"
				System.err.println("delete \"$canonicalPath\" => error=\"$errStr\"")
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

		var hdir: HANDLE? = INVALID_HANDLE_VALUE
		var isFirst = true

		NextFile@ while (true) {
			if (isFirst) {
				isFirst = false
				hdir = FindFirstFileExW(
					lpFileName = "$canonicalPath$separatorChar*", // .wcstr.ptr,
					fSearchOp = _FINDEX_SEARCH_OPS.FindExSearchNameMatch,
					fInfoLevelId = _FINDEX_INFO_LEVELS.FindExInfoBasic,
					lpFindFileData = findFileDataBuf.ptr, lpSearchFilter = null,
					dwAdditionalFlags = 0 // do not use FIND_FIRST_EX_LARGE_FETCH it results in wrong values for nFileSizeLow and nFileSizeHigh (a few 100 bytes difference!)
					// do not use FIND_FIRST_EX_ON_DISK_ENTRIES_ONLY => illegal param on Win7
				)
				if (hdir == INVALID_HANDLE_VALUE) {
					val winErr = GetLastError()
					val errStr = "$winErr:${winSysErrMessage(winErr)}"
					System.err.println("FindFirstFileExW \"$canonicalPath\" => error=\"$errStr\"")
					return callBack(CallBackFor.LEAVEDIR, this, errStr)
				}
			} else {
				val rc = FindNextFileW(hdir, findFileDataBuf.ptr)

				if (rc == FALSE) {
					val winErr = GetLastError()
					var errStr: String? = null
					if (winErr.toInt() != ERROR_NO_MORE_FILES) {
						errStr = "$winErr:${winSysErrMessage(winErr)}"
						System.err.println("FindNextFileW \"$canonicalPath\" => error=\"$errStr\"")
					}
					FindClose(hdir)
					return callBack(CallBackFor.LEAVEDIR, this, errStr)
				}
			}

			val fName = findFileDataBuf.cFileName.toKString()
			if (fName == "." || fName == "..") continue@NextFile
			//println("Find*FileW: fName=$fName findFileDataBuf.nFileSizeHigh=${findFileDataBuf.nFileSizeHigh} findFileDataBuf.nFileSizeLow=${findFileDataBuf.nFileSizeLow}")

			val foundFile = File("$path$separatorChar$fName", "$absolutePath$separatorChar$fName", "$canonicalPath$separatorChar$fName")
			foundFile.dwFileAttributes = findFileDataBuf.dwFileAttributes.toInt()
//
//			// A Windows file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
			foundFile.mTimeMS = (findFileDataBuf.ftLastWriteTime.dwHighDateTime.toULong().shl(32) + findFileDataBuf.ftLastWriteTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
			foundFile.cTimeMS = (findFileDataBuf.ftCreationTime.dwHighDateTime.toULong().shl(32) + findFileDataBuf.ftCreationTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
			foundFile.fileLength = (findFileDataBuf.nFileSizeHigh.toULong().shl(32) + findFileDataBuf.nFileSizeLow.toULong()).toLong()

			when {
				foundFile.isDirectory -> {
					callBackResult = foundFile.walkDir(callBack)
				}

				foundFile.isFile      -> {
					callBackResult = callBack(CallBackFor.FILE, foundFile, null)
				}
			}

			when (callBackResult) {
				CallBackResult.TERMINATE, CallBackResult.ABORT -> {
					break@NextFile
				}

				CallBackResult.LEAVE -> {
					FindClose(hdir)
					return callBack(CallBackFor.LEAVEDIR, this, null)
				}
			}
		}

		FindClose(hdir)
		return callBackResult
	}
}



/*
 * Native specials
 */

/*
private class FileX(fileName: String) {
	private var name: String = fileName
	private var absName: String? = null
	private var canonicalName: String? = null
	private var dwFileAttributes: Int = 0
	private var mTimeMS = 0L
	private var cTimeMS = 0L
	private var fileLength = 0L
	private val WIN_TO_UNIX_EPOCH_MS = 11644473600 * 1000L // A Windows file time is a 64-bit value that represents the number relative to 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).

	private constructor(name: String, absName: String?, canonicalName: String?) : this(name) {
		this.absName = absName
		this.canonicalName = canonicalName
	}

	@ThreadLocal
	companion object {
		val separatorChar: Char = (System.getProperty("file.separator") ?: "/")[0]

		private val wcharBufSize = 128 * 256
		// one buffer for each thread
		private val wcharBuf = nativeHeap.allocArray<WCHARVar>(wcharBufSize)
		// one buffer for each thread
		private val fileAttrBuf1 = nativeHeap.alloc<WIN32_FILE_ATTRIBUTE_DATA>()
		// one buffer for each thread
		private val findFileDataBuf = nativeHeap.alloc<WIN32_FIND_DATAW>()
	}

	override fun toString() = "File@${getPath()}"

	fun getPath() = name
	fun getName() = getPath().split(separatorChar).last()

	fun getAbsolutePath(): String {
		if (absName == null) {
			var rc = GetFullPathNameW(name, wcharBufSize.toUInt(), wcharBuf, null)
			if (rc > 0U && rc <= wcharBufSize.toUInt()) absName = wcharBuf.toKString()
		}
		return absName ?: ""
	}

	fun getAbsoluteFile(): File = File(getAbsolutePath(), absName, canonicalName)

	fun getCanonicalPath(): String {
		if (name.length > 0 && canonicalName == null) {
			val absName = getAbsolutePath()
			canonicalName = when { // "\\\\?\\$absDirName" // allow very long paths
				absName.length == 0           -> absName
				absName.startsWith("""\\?""") -> absName
				absName.startsWith("""\\""")  -> "\\\\?\\UNC${absName.substring(1)}"
				else                          -> "\\\\?\\$absName"
			}
		}
		return canonicalName ?: ""
	}

	fun getCanonicalFile(): File = File(getCanonicalPath(), absName, canonicalName)

	private fun fillFileAttributes(isQuiet: Boolean = false): Boolean {
		if (name.length == 0) return false

		if (dwFileAttributes == 0) {
			fileAttrBuf1.nFileSizeLow = 800u
			fileAttrBuf1.nFileSizeHigh = 0u
			fileAttrBuf1.dwFileAttributes = 0u
			val fName = getCanonicalPath()
			val rc = GetFileAttributesExW(fName, GET_FILEEX_INFO_LEVELS.GetFileExInfoStandard, fileAttrBuf1.ptr)
			if (rc == TRUE) {
				//ifDebugThen { println("GetFileAttributesExW: fName=$name($fName) fileAttrBuf1.nFileSizeHigh=${fileAttrBuf1.nFileSizeHigh} fileAttrBuf1.nFileSizeLow=${fileAttrBuf1.nFileSizeLow}") }
				dwFileAttributes = fileAttrBuf1.dwFileAttributes.toInt()
				// A Windows file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
				mTimeMS = (fileAttrBuf1.ftLastWriteTime.dwHighDateTime.toULong().shl(32) + fileAttrBuf1.ftLastWriteTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
				cTimeMS = (fileAttrBuf1.ftCreationTime.dwHighDateTime.toULong().shl(32) + fileAttrBuf1.ftCreationTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
				fileLength = (fileAttrBuf1.nFileSizeHigh.toULong().shl(32) + fileAttrBuf1.nFileSizeLow.toULong()).toLong()
			} else {
				if (!isQuiet) System.err.println("GetFileAttributesExW \"$fName\" failed (${winSysErrMessage(GetLastError())})")
			}
		}

		return dwFileAttributes != 0
	}

	fun exists() = fillFileAttributes(true)
	fun isDirectory() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY != 0)
	fun isDevice() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_DEVICE != 0)
	fun isSymbolicLink() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_REPARSE_POINT != 0) // not in Java File class
	fun isHidden() = fillFileAttributes(true) && (dwFileAttributes and FILE_ATTRIBUTE_HIDDEN != 0)
	fun isFile() = !isDirectory() && !isDevice() && dwFileAttributes != 0
	fun canWrite() = fillFileAttributes() && isFile() && (dwFileAttributes and FILE_ATTRIBUTE_READONLY == 0)
	fun canRead() = fillFileAttributes() && isFile()

	fun lastModified() = if (fillFileAttributes()) mTimeMS else 0L
	fun creationTime() = if (fillFileAttributes()) cTimeMS else 0L // not in Java File class
	fun length() = if (fillFileAttributes()) fileLength else 0L

	fun renameTo(newFile: File): Boolean {
		if (name.length == 0 || newFile.name.length == 0 || getCanonicalPath() == newFile.getCanonicalPath()) return false

		val rc = MoveFileExW(getCanonicalPath(), newFile.getCanonicalPath(), MOVEFILE_COPY_ALLOWED)
		if (rc == TRUE) {
			name = newFile.name
			absName = newFile.getAbsolutePath()
			canonicalName = newFile.getCanonicalPath()

			return true
		}

		val winErr = GetLastError()
		val errStr = "$winErr:${winSysErrMessage(winErr)}"
		System.err.println("rename \"$canonicalPath\" to \"${newFile.getCanonicalPath()}\" => error=\"$errStr\"")

		return false
	}

	fun delete(): Boolean {
		if (name.length == 0) return false

		var rc =
			if (isDirectory()) {
				RemoveDirectoryW(getCanonicalPath())
			} else {
				DeleteFileW(getCanonicalPath())
			}

		if (rc == TRUE) return true

		val winErr = GetLastError()
		if (winErr != ERROR_FILE_NOT_FOUND.toUInt()) {
			val errStr = "$winErr:${winSysErrMessage(winErr)}"
			System.err.println("delete \"$canonicalPath\" => error=\"$errStr\"")
		}

		return false
	}

	fun deleteWithRetries(retries: Int = 10): Boolean {
		if (name.length == 0) return false

		for (i in 1..retries) {
			var rc =
				if (isDirectory()) {
					RemoveDirectoryW(getCanonicalPath())
				} else {
					DeleteFileW(getCanonicalPath())
				}

			if (rc == TRUE) return true

			val winErr = GetLastError()
			if (winErr == ERROR_FILE_NOT_FOUND.toUInt()) return false

			if (i == retries) {
				val errStr = "$winErr:${winSysErrMessage(winErr)}"
				System.err.println("delete \"$canonicalPath\" => error=\"$errStr\"")
			} else {
				sleep(i.toUInt())
			}
		}

		return false
	}

	fun deleteDir(filesOnly: Boolean = false, retries: Int = 0): Boolean { // not in Java::File
		// recursively delete dir
		if (name.length == 0 || !isDirectory()) return false

		val walkRC = walkDir callBack@{ callBackFor, foundFile, _ ->
			when (callBackFor) {
				CallBackFor.ENTERDIR -> return@callBack File.CallBackResult.ENTER

				CallBackFor.LEAVEDIR,
				CallBackFor.FILE     -> {
					filesOnly && foundFile.isDirectory && return@callBack File.CallBackResult.OK
					if (retries > 0) {
						foundFile.deleteWithRetries(retries)
					} else {
						foundFile.delete()
					}
					File.CallBackResult.OK
				}
			}
		}

		return true
	}

	enum class CallBackFor { ENTERDIR, FILE, LEAVEDIR }
	enum class CallBackResult { OK, NOK, ENTER, SKIP, LEAVE, ABORT }

	fun walkDir(callBack: (callBackFor: CallBackFor, file: File, errorStr: String?) -> CallBackResult): CallBackResult {
		if (isFile()) return callBack(CallBackFor.FILE, this, null)
		if (!isDirectory()) return CallBackResult.NOK

		var callBackResult = callBack(CallBackFor.ENTERDIR, this, null)
		if (callBackResult != CallBackResult.ENTER) return callBackResult

		var hdir: HANDLE? = INVALID_HANDLE_VALUE
		var isFirst = true

		NextFile@ while (true) {
			if (isFirst) {
				isFirst = false
				hdir = FindFirstFileExW(
					lpFileName = "$canonicalPath$separatorChar*", // .wcstr.ptr,
					fSearchOp = _FINDEX_SEARCH_OPS.FindExSearchNameMatch,
					fInfoLevelId = _FINDEX_INFO_LEVELS.FindExInfoBasic,
					lpFindFileData = findFileDataBuf.ptr, lpSearchFilter = null,
					dwAdditionalFlags = 0 // do not use FIND_FIRST_EX_LARGE_FETCH it results in wrong values for nFileSizeLow and nFileSizeHigh (a few 100 bytes difference!)
					// do not use FIND_FIRST_EX_ON_DISK_ENTRIES_ONLY => illegal param on Win7
				)
				if (hdir == INVALID_HANDLE_VALUE) {
					val winErr = GetLastError()
					val errStr = "$winErr:${winSysErrMessage(winErr)}"
					System.err.println("FindFirstFileExW \"$canonicalPath\" => error=\"$errStr\"")
					return callBack(CallBackFor.LEAVEDIR, this, errStr)
				}
			} else {
				val rc = FindNextFileW(hdir, findFileDataBuf.ptr)

				if (rc == FALSE) {
					val winErr = GetLastError()
					var errStr: String? = null
					if (winErr.toInt() != ERROR_NO_MORE_FILES) {
						errStr = "$winErr:${winSysErrMessage(winErr)}"
						System.err.println("FindNextFileW \"$canonicalPath\" => error=\"$errStr\"")
					}
					FindClose(hdir)
					return callBack(CallBackFor.LEAVEDIR, this, errStr)
				}
			}

			val fName = findFileDataBuf.cFileName.toKString()
			if (fName == "." || fName == "..") continue@NextFile
			//println("Find*FileW: fName=$fName findFileDataBuf.nFileSizeHigh=${findFileDataBuf.nFileSizeHigh} findFileDataBuf.nFileSizeLow=${findFileDataBuf.nFileSizeLow}")

			val foundFile = File("${getPath()}$separatorChar$fName", "${getAbsolutePath()}$separatorChar$fName", "$canonicalPath$separatorChar$fName")
			foundFile.dwFileAttributes = findFileDataBuf.dwFileAttributes.toInt()
//
//			// A Windows file time is a 64-bit value that represents the number of 100-nanosecond intervals that have elapsed since 12:00 A.M. January 1, 1601 Coordinated Universal Time (UTC).
			foundFile.mTimeMS = (findFileDataBuf.ftLastWriteTime.dwHighDateTime.toULong().shl(32) + findFileDataBuf.ftLastWriteTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
			foundFile.cTimeMS = (findFileDataBuf.ftCreationTime.dwHighDateTime.toULong().shl(32) + findFileDataBuf.ftCreationTime.dwLowDateTime.toULong()).toLong() / 10_000L - WIN_TO_UNIX_EPOCH_MS
			foundFile.fileLength = (findFileDataBuf.nFileSizeHigh.toULong().shl(32) + findFileDataBuf.nFileSizeLow.toULong()).toLong()

			when {
				foundFile.isDirectory() -> {
					callBackResult = foundFile.walkDir(callBack)
				}

				foundFile.isFile()      -> {
					callBackResult = callBack(CallBackFor.FILE, foundFile, null)
				}
			}

			when (callBackResult) {
				CallBackResult.ABORT -> {
					break@NextFile
				}

				CallBackResult.LEAVE -> {
					FindClose(hdir)
					return callBack(CallBackFor.LEAVEDIR, this, null)
				}
			}
		}

		FindClose(hdir)
		return callBackResult
	}
}
*/
