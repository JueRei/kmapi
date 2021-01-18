package de.rdvsb.kmapi

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.Synchronized

public interface Closeable {
	@Throws(IOException::class)
	public fun close(): Unit
}

// copied from kotlin.io (but simplified)
@ExperimentalContracts
public inline fun <T : Closeable?, R> T.use(block: (T) -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	try {
		return block(this)
	} catch (e: Throwable) {
		throw e
	} finally {
		try {
			this?.close()
		} catch (closeException: Throwable) {
			// ignored here
		}
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public interface Flushable {
	/**
	 * Flushes this stream
	 *
	 * @throws IOException on I/O errors
	 */
	@Throws(IOException::class)
	public fun flush()
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This abstract class is the superclass of all classes representing
 * an input stream of bytes.
 * @see java.io.InputStream
 *
 */
public expect abstract class InputStream {
	/**
	 * Reads the next byte of data from the input stream.
	 */
	@Throws(IOException::class)
	public abstract fun read(): Int

	/**
	 * Reads some number of bytes from the input stream and stores them into
	 * the buffer array `b`.
	 */
	@Throws(IOException::class)
	public actual fun read(b: ByteArray): Int

	/**
	 * Reads up to `len` bytes of data from the input stream into
	 * an array of bytes.
	 */
	@Throws(IOException::class)
	public open fun read(b: ByteArray, off: Int, len: Int): Int



	/**
	 * Skips over and discards `n` bytes of data from this input
	 * stream.
	 */
	@Throws(IOException::class)
	public open fun skip(n: Long): Long

	/**
	 * Returns an estimate of the number of bytes that can be read (or skipped
	 * over) from this input stream without blocking
	 */
	@Throws(IOException::class)
	public open fun available(): Int

	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream.
	 */
	@Throws(IOException::class)
	public fun close()

	/**
	 * Marks the current position in this input stream. A subsequent call to
	 * the `reset` method repositions this stream at the last marked
	 * position so that subsequent reads re-read the same bytes.
	 */
	@Synchronized
	public open fun mark(readlimit: Int)

	/**
	 * Repositions this stream to the position at the time the
	 * `mark` method was last called on this input stream.
	 */
	@Synchronized
	@Throws(IOException::class)
	public open fun reset()

	/**
	 * Tests if this input stream supports the `mark` and
	 * `reset` methods.
	 */
	public open fun markSupported(): Boolean

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * This abstract class is the superclass of all classes representing
 * an output stream of bytes.
 * @see java.io.OutputStream
 *
 */
public expect abstract class OutputStream {
	/**
	 * Writes the specified byte to this output stream.
	 */
	@Throws(IOException::class)
	public abstract fun write(b: Int)

	/**
	 * Writes `b.length` bytes from the specified byte array
	 * to this output stream.
	 */
	@Throws(IOException::class)
	public open fun write(b: ByteArray)

	/**
	 * Writes `len` bytes from the specified byte array
	 * starting at offset `off` to this output stream.
	 */
	@Throws(IOException::class)
	public open fun write(b: ByteArray, off: Int, len: Int)

	/**
	 * Flushes this output stream and forces any buffered output bytes
	 * to be written out.
	 */
	@Throws(IOException::class)
	public fun flush()

	/**
	 * Closes this output stream and releases any system resources
	 * associated with this stream.
	 */
	@Throws(IOException::class)
	public fun close()

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

