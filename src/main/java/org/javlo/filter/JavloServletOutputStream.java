package org.javlo.filter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * The implementation of javax.servlet.ServletOutputStream.
 *
 * When a flush or close method is called on an implementation of this class,
 * any data buffered by the servlet engine is sent to the client and the
 * response is considered to be "committed". Note that calling close on an
 * object of this type doesn't necessarily close the underlying socket stream.
 */

// BUGBUG - override print methods for better performance!!!!
class JavloServletOurputStream extends ServletOutputStream {
	private static final int mtuSize = 1460;

	/** Actual output stream */
	final private BufferedOutputStream realOut;

	/** response object */
	final private HttpServletResponse response;

	/** false if the ServletOutputStream has been closed */
	private boolean open;

	/** Place to buffer the output data */
	private ByteArrayOutputStream buffer;

	/** Place to buffer the output data */
	private OutputStream out;

	/** true if the flush method should flush */
	private boolean flush = true;

	JavloServletOurputStream(OutputStream realOut, HttpServletResponse response) {
	if (realOut instanceof BufferedOutputStream)
		this.realOut = (BufferedOutputStream) realOut;
	else
		// use a common mtuSize as the buffer size (bug 179282)
		this.realOut = new BufferedOutputStream(realOut, mtuSize);
	this.response = response;

	// BUGBUG Make the default buffer size configurable.
	buffer = new ByteArrayOutputStream(8192); /* start with a 8k buffer */
	out = buffer; /* begin with buffer */	
	flush = true;
}

	/**
	 * This method is called by a wrapper to disable normal flush function until
	 * the close method is called.
	 */
	public synchronized void disableFlush() {
		flush = false; /* disable flush until we are closed */
	}
	
	@Override
	public void flush() throws IOException {
		if (flush) {
			super.flush();
			out.flush();
		}
	}

	public synchronized void close() throws IOException {
		flush = true;
		super.close(); 
	}

	public synchronized void write(byte[] bytes) throws IOException {
		if (open) {
			out.write(bytes, 0, bytes.length);
		} else {
			throw new IOException("closed"); //$NON-NLS-1$
		}
	}

	public synchronized void write(byte[] bytes, int off, int len) throws IOException {
		if (open) {
			out.write(bytes, off, len);
		} else {
			throw new IOException("closed"); //$NON-NLS-1$
		}
	}

	public synchronized void write(int b) throws IOException {
		if (open) {
			out.write(b);
		} else {
			throw new IOException("closed"); //$NON-NLS-1$
		}
	}
}
