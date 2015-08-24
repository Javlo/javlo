package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.javlo.context.ContentContext;

public class Jsp2String extends HttpServletResponseWrapper {

	class JCacheOutputStream extends ServletOutputStream implements Serializable {

		private static final long serialVersionUID = 1L;

		public ByteArrayOutputStream getBuffer() {
			try {
				cache.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return cache;
		}

		public void write(int b) throws IOException {
			cache.write(b);
		}

		public void write(byte b[]) throws IOException {
			cache.write(b);
		}

		public void write(byte buf[], int offset, int len) throws IOException {
			cache.write(buf, offset, len);
		}
	}
	
	private ByteArrayOutputStream cache = new ByteArrayOutputStream();
	private PrintWriter printWriterCache;
	private JCacheOutputStream fakeServletOutputStream = new JCacheOutputStream();

	public Jsp2String(HttpServletResponse response) {
		super(response);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (printWriterCache == null) {
			printWriterCache = new PrintWriter(new OutputStreamWriter(cache,ContentContext.CHARACTER_ENCODING));
		}
		return printWriterCache;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return fakeServletOutputStream;
	}

	@Override
	public String toString() {
		if (printWriterCache != null) {
			printWriterCache.flush();
		}
		return new String(cache.toByteArray(), ContentContext.CHARSET_DEFAULT);
	}

}
