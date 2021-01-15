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
	 * Flushes this stream by writing any buffered output to the underlying
	 * stream.
	 *
	 * @throws IOException If an I/O error occurs
	 */
	@Throws(IOException::class)
	public fun flush()
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * This abstract class is the superclass of all classes representing
 * an input stream of bytes.
 *
 *
 *  Applications that need to define a subclass of `InputStream`
 * must always provide a method that returns the next byte of input.
 *
 * @author  Arthur van Hoff
 * @see BufferedInputStream
 *
 * @see ByteArrayInputStream
 *
 * @see DataInputStream
 *
 * @see FilterInputStream
 *
 * @see InputStream.read
 * @see OutputStream
 *
 * @see PushbackInputStream
 *
 * @since   1.0
 */
public expect abstract class InputStream {
	/**
	 * Reads the next byte of data from the input stream. The value byte is
	 * returned as an `int` in the range `0` to
	 * `255`. If no byte is available because the end of the stream
	 * has been reached, the value `-1` is returned. This method
	 * blocks until input data is available, the end of the stream is detected,
	 * or an exception is thrown.
	 *
	 *
	 *  A subclass must provide an implementation of this method.
	 *
	 * @return     the next byte of data, or `-1` if the end of the
	 * stream is reached.
	 * @exception  IOException  if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public abstract fun read(): Int

	/**
	 * Reads some number of bytes from the input stream and stores them into
	 * the buffer array `b`. The number of bytes actually read is
	 * returned as an integer.  This method blocks until input data is
	 * available, end of file is detected, or an exception is thrown.
	 *
	 *
	 *  If the length of `b` is zero, then no bytes are read and
	 * `0` is returned; otherwise, there is an attempt to read at
	 * least one byte. If no byte is available because the stream is at the
	 * end of the file, the value `-1` is returned; otherwise, at
	 * least one byte is read and stored into `b`.
	 *
	 *
	 *  The first byte read is stored into element `b[0]`, the
	 * next one into `b[1]`, and so on. The number of bytes read is,
	 * at most, equal to the length of `b`. Let *k* be the
	 * number of bytes actually read; these bytes will be stored in elements
	 * `b[0]` through `b[`*k*`-1]`,
	 * leaving elements `b[`*k*`]` through
	 * `b[b.length-1]` unaffected.
	 *
	 *
	 *  The `read(b)` method for class `InputStream`
	 * has the same effect as: <pre>` read(b, 0, b.length) `</pre>
	 *
	 * @param      b   the buffer into which the data is read.
	 * @return     the total number of bytes read into the buffer, or
	 * `-1` if there is no more data because the end of
	 * the stream has been reached.
	 * @exception  IOException  If the first byte cannot be read for any reason
	 * other than the end of the file, if the input stream has been closed, or
	 * if some other I/O error occurs.
	 * @exception  NullPointerException  if `b` is `null`.
	 * @see InputStream.read
	 */
	@Throws(IOException::class)
	public actual fun read(b: ByteArray): Int

	/**
	 * Reads up to `len` bytes of data from the input stream into
	 * an array of bytes.  An attempt is made to read as many as
	 * `len` bytes, but a smaller number may be read.
	 * The number of bytes actually read is returned as an integer.
	 *
	 *
	 *  This method blocks until input data is available, end of file is
	 * detected, or an exception is thrown.
	 *
	 *
	 *  If `len` is zero, then no bytes are read and
	 * `0` is returned; otherwise, there is an attempt to read at
	 * least one byte. If no byte is available because the stream is at end of
	 * file, the value `-1` is returned; otherwise, at least one
	 * byte is read and stored into `b`.
	 *
	 *
	 *  The first byte read is stored into element `b[off]`, the
	 * next one into `b[off+1]`, and so on. The number of bytes read
	 * is, at most, equal to `len`. Let *k* be the number of
	 * bytes actually read; these bytes will be stored in elements
	 * `b[off]` through `b[off+`*k*`-1]`,
	 * leaving elements `b[off+`*k*`]` through
	 * `b[off+len-1]` unaffected.
	 *
	 *
	 *  In every case, elements `b[0]` through
	 * `b[off]` and elements `b[off+len]` through
	 * `b[b.length-1]` are unaffected.
	 *
	 *
	 *  The `read(b,` `off,` `len)` method
	 * for class `InputStream` simply calls the method
	 * `read()` repeatedly. If the first such call results in an
	 * `IOException`, that exception is returned from the call to
	 * the `read(b,` `off,` `len)` method.  If
	 * any subsequent call to `read()` results in a
	 * `IOException`, the exception is caught and treated as if it
	 * were end of file; the bytes read up to that point are stored into
	 * `b` and the number of bytes read before the exception
	 * occurred is returned. The default implementation of this method blocks
	 * until the requested amount of input data `len` has been read,
	 * end of file is detected, or an exception is thrown. Subclasses are
	 * encouraged to provide a more efficient implementation of this method.
	 *
	 * @param      b     the buffer into which the data is read.
	 * @param      off   the start offset in array `b`
	 * at which the data is written.
	 * @param      len   the maximum number of bytes to read.
	 * @return     the total number of bytes read into the buffer, or
	 * `-1` if there is no more data because the end of
	 * the stream has been reached.
	 * @exception  IOException If the first byte cannot be read for any reason
	 * other than end of file, or if the input stream has been closed, or if
	 * some other I/O error occurs.
	 * @exception  NullPointerException If `b` is `null`.
	 * @exception  IndexOutOfBoundsException If `off` is negative,
	 * `len` is negative, or `len` is greater than
	 * `b.length - off`
	 * @see InputStream.read
	 */
	@Throws(IOException::class)
	public open fun read(b: ByteArray, off: Int, len: Int): Int



	/**
	 * Skips over and discards `n` bytes of data from this input
	 * stream. The `skip` method may, for a variety of reasons, end
	 * up skipping over some smaller number of bytes, possibly `0`.
	 * This may result from any of a number of conditions; reaching end of file
	 * before `n` bytes have been skipped is only one possibility.
	 * The actual number of bytes skipped is returned. If `n` is
	 * negative, the `skip` method for class `InputStream` always
	 * returns 0, and no bytes are skipped. Subclasses may handle the negative
	 * value differently.
	 *
	 *
	 *  The `skip` method implementation of this class creates a
	 * byte array and then repeatedly reads into it until `n` bytes
	 * have been read or the end of the stream has been reached. Subclasses are
	 * encouraged to provide a more efficient implementation of this method.
	 * For instance, the implementation may depend on the ability to seek.
	 *
	 * @param      n   the number of bytes to be skipped.
	 * @return     the actual number of bytes skipped.
	 * @throws     IOException  if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public open fun skip(n: Long): Long

	/**
	 * Returns an estimate of the number of bytes that can be read (or skipped
	 * over) from this input stream without blocking, which may be 0, or 0 when
	 * end of stream is detected.  The read might be on the same thread or
	 * another thread.  A single read or skip of this many bytes will not block,
	 * but may read or skip fewer bytes.
	 *
	 *
	 *  Note that while some implementations of `InputStream` will
	 * return the total number of bytes in the stream, many will not.  It is
	 * never correct to use the return value of this method to allocate
	 * a buffer intended to hold all data in this stream.
	 *
	 *
	 *  A subclass's implementation of this method may choose to throw an
	 * [IOException] if this input stream has been closed by invoking the
	 * [.close] method.
	 *
	 *
	 *  The `available` method of `InputStream` always returns
	 * `0`.
	 *
	 *
	 *  This method should be overridden by subclasses.
	 *
	 * @return     an estimate of the number of bytes that can be read (or
	 * skipped over) from this input stream without blocking or
	 * `0` when it reaches the end of the input stream.
	 * @exception  IOException if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public open fun available(): Int

	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream.
	 *
	 *
	 *  The `close` method of `InputStream` does
	 * nothing.
	 *
	 * @exception  IOException  if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public fun close()

	/**
	 * Marks the current position in this input stream. A subsequent call to
	 * the `reset` method repositions this stream at the last marked
	 * position so that subsequent reads re-read the same bytes.
	 *
	 *
	 *  The `readlimit` arguments tells this input stream to
	 * allow that many bytes to be read before the mark position gets
	 * invalidated.
	 *
	 *
	 *  The general contract of `mark` is that, if the method
	 * `markSupported` returns `true`, the stream somehow
	 * remembers all the bytes read after the call to `mark` and
	 * stands ready to supply those same bytes again if and whenever the method
	 * `reset` is called.  However, the stream is not required to
	 * remember any data at all if more than `readlimit` bytes are
	 * read from the stream before `reset` is called.
	 *
	 *
	 *  Marking a closed stream should not have any effect on the stream.
	 *
	 *
	 *  The `mark` method of `InputStream` does
	 * nothing.
	 *
	 * @param   readlimit   the maximum limit of bytes that can be read before
	 * the mark position becomes invalid.
	 * @see InputStream.reset
	 */
	@Synchronized
	public open fun mark(readlimit: Int)

	/**
	 * Repositions this stream to the position at the time the
	 * `mark` method was last called on this input stream.
	 *
	 *
	 *  The general contract of `reset` is:
	 *
	 *
	 *  *  If the method `markSupported` returns
	 * `true`, then:
	 *
	 *  *  If the method `mark` has not been called since
	 * the stream was created, or the number of bytes read from the stream
	 * since `mark` was last called is larger than the argument
	 * to `mark` at that last call, then an
	 * `IOException` might be thrown.
	 *
	 *  *  If such an `IOException` is not thrown, then the
	 * stream is reset to a state such that all the bytes read since the
	 * most recent call to `mark` (or since the start of the
	 * file, if `mark` has not been called) will be resupplied
	 * to subsequent callers of the `read` method, followed by
	 * any bytes that otherwise would have been the next input data as of
	 * the time of the call to `reset`.
	 *
	 *  *  If the method `markSupported` returns
	 * `false`, then:
	 *
	 *  *  The call to `reset` may throw an
	 * `IOException`.
	 *
	 *  *  If an `IOException` is not thrown, then the stream
	 * is reset to a fixed state that depends on the particular type of the
	 * input stream and how it was created. The bytes that will be supplied
	 * to subsequent callers of the `read` method depend on the
	 * particular type of the input stream.
	 *
	 *
	 * The method `reset` for class `InputStream`
	 * does nothing except throw an `IOException`.
	 *
	 * @exception  IOException  if this stream has not been marked or if the
	 * mark has been invalidated.
	 * @see InputStream.mark
	 * @see IOException
	 */
	@Synchronized
	@Throws(IOException::class)
	public open fun reset()

	/**
	 * Tests if this input stream supports the `mark` and
	 * `reset` methods. Whether or not `mark` and
	 * `reset` are supported is an invariant property of a
	 * particular input stream instance. The `markSupported` method
	 * of `InputStream` returns `false`.
	 *
	 * @return  `true` if this stream instance supports the mark
	 * and reset methods; `false` otherwise.
	 * @see InputStream.mark
	 * @see InputStream.reset
	 */
	public open fun markSupported(): Boolean

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public expect abstract class OutputStream {
	/**
	 * Writes the specified byte to this output stream. The general
	 * contract for `write` is that one byte is written
	 * to the output stream. The byte to be written is the eight
	 * low-order bits of the argument `b`. The 24
	 * high-order bits of `b` are ignored.
	 *
	 *
	 * Subclasses of `OutputStream` must provide an
	 * implementation for this method.
	 *
	 * @param      b   the `byte`.
	 * @exception  IOException  if an I/O error occurs. In particular,
	 * an `IOException` may be thrown if the
	 * output stream has been closed.
	 */
	@Throws(IOException::class)
	public abstract fun write(b: Int)

	/**
	 * Writes `b.length` bytes from the specified byte array
	 * to this output stream. The general contract for `write(b)`
	 * is that it should have exactly the same effect as the call
	 * `write(b, 0, b.length)`.
	 *
	 * @param      b   the data.
	 * @exception  IOException  if an I/O error occurs.
	 * @see OutputStream.write
	 */
	@Throws(IOException::class)
	public open fun write(b: ByteArray)

	/**
	 * Writes `len` bytes from the specified byte array
	 * starting at offset `off` to this output stream.
	 * The general contract for `write(b, off, len)` is that
	 * some of the bytes in the array `b` are written to the
	 * output stream in order; element `b[off]` is the first
	 * byte written and `b[off+len-1]` is the last byte written
	 * by this operation.
	 *
	 *
	 * The `write` method of `OutputStream` calls
	 * the write method of one argument on each of the bytes to be
	 * written out. Subclasses are encouraged to override this method and
	 * provide a more efficient implementation.
	 *
	 *
	 * If `b` is `null`, a
	 * `NullPointerException` is thrown.
	 *
	 *
	 * If `off` is negative, or `len` is negative, or
	 * `off+len` is greater than the length of the array
	 * `b`, then an `IndexOutOfBoundsException` is thrown.
	 *
	 * @param      b     the data.
	 * @param      off   the start offset in the data.
	 * @param      len   the number of bytes to write.
	 * @exception  IOException  if an I/O error occurs. In particular,
	 * an `IOException` is thrown if the output
	 * stream is closed.
	 */
	@Throws(IOException::class)
	public open fun write(b: ByteArray, off: Int, len: Int)

	/**
	 * Flushes this output stream and forces any buffered output bytes
	 * to be written out. The general contract of `flush` is
	 * that calling it is an indication that, if any bytes previously
	 * written have been buffered by the implementation of the output
	 * stream, such bytes should immediately be written to their
	 * intended destination.
	 *
	 *
	 * If the intended destination of this stream is an abstraction provided by
	 * the underlying operating system, for example a file, then flushing the
	 * stream guarantees only that bytes previously written to the stream are
	 * passed to the operating system for writing; it does not guarantee that
	 * they are actually written to a physical device such as a disk drive.
	 *
	 *
	 * The `flush` method of `OutputStream` does nothing.
	 *
	 * @exception  IOException  if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public fun flush()

	/**
	 * Closes this output stream and releases any system resources
	 * associated with this stream. The general contract of `close`
	 * is that it closes the output stream. A closed stream cannot perform
	 * output operations and cannot be reopened.
	 *
	 *
	 * The `close` method of `OutputStream` does nothing.
	 *
	 * @exception  IOException  if an I/O error occurs.
	 */
	@Throws(IOException::class)
	public fun close()

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

