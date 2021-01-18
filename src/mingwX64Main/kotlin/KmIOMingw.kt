package de.rdvsb.kmapi

/**
 * This abstract class is the superclass of all classes representing
 * an input stream of bytes.
 */
public actual abstract class InputStream : Closeable {
	@Throws(IOException::class)
	public actual abstract fun read(): Int

	@Throws(IOException::class)
	public actual fun read(b: ByteArray): Int {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public actual open fun read(b: ByteArray, off: Int, len: Int): Int {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public fun readAllBytes(): ByteArray {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public fun readNBytes(len: Int): ByteArray {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public actual open fun skip(n: Long): Long {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public actual open fun available(): Int {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public actual override fun close() {
	}

	public actual open fun mark(readlimit: Int) {
	}

	@Throws(IOException::class)
	public actual open fun reset() {
	}

	public actual open fun markSupported(): Boolean {
		TODO("Not yet implemented")
	}

	@Throws(IOException::class)
	public fun transferTo(out: OutputStream): Long {
		TODO("Not yet implemented")
	}

	public companion object {
		/**
		 * Returns a new `InputStream` that reads no bytes.
		 */
		public fun nullInputStream(): InputStream {
			TODO("Not yet implemented")
		}

	}

}

/**
 * This abstract class is the superclass of all classes representing
 * an output stream of bytes.
 *
 */
public actual abstract class OutputStream : Closeable, Flushable {
	@Throws(IOException::class)
	public actual abstract fun write(b: Int)

	@Throws(IOException::class)
	public actual open fun write(b: ByteArray) {
	}

	@Throws(IOException::class)
	public actual open fun write(b: ByteArray, off: Int, len: Int) {
	}

	@Throws(IOException::class)
	public actual override fun flush() {
	}

	@Throws(IOException::class)
	public actual override fun close() {
	}

	public companion object {
		/**
		 * Returns a new `OutputStream` which discards all bytes.
		 */
		public fun nullOutputStream(): OutputStream {
			TODO("Not yet implemented")
		}
	}

}