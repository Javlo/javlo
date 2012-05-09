package org.javlo.portlet.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.helper.ResourceHelper;

public class MultiReadRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

	protected static Logger logger = Logger.getLogger(MultiReadRequestWrapper.class.getName());

	public static final long MAX_UPLOAD_SIZE = 2l * 1024l * 1024l * 1024l; // 2 giga max size
	private static final String TEMP_FILE_PREFIX = "temp-file-8938404834";

	
	private static class TempFileFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			return name.startsWith(TEMP_FILE_PREFIX);
		}
	}

	
	public MultiReadRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	public static void clearTempDir(ServletContext application) {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File tempDirToClear;
		if (staticConfig.getTempDir() == null) {
			tempDirToClear = new File(System.getProperty("java.io.tmpdir"));
		} else {
			tempDirToClear = new File(staticConfig.getTempDir());
		}
		if (tempDirToClear.exists()) {
			File[] tempFiles = tempDirToClear.listFiles(new TempFileFilter());
			for (File file : tempFiles) {
				file.delete();
			}
		}
	}

	File tempFile = null;

	/**
	 * makes the ServletInputStream content available for several parsing by
	 * copying its content in a file buffer
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (tempFile == null) {
			final File tempDir;
			StaticConfig staticConfig = StaticConfig.getInstance(super.getSession().getServletContext());
			if (staticConfig.getTempDir() != null) {
				tempDir = new File(staticConfig.getTempDir());
			} else {
				tempDir = new File(System.getProperty("java.io.tmpdir"));
			}
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}
			logger.fine("temp dir defined : " + tempDir);

			FileOutputStream fos = null;
			try {
				InputStream is = super.getInputStream();

				tempFile = File.createTempFile(TEMP_FILE_PREFIX, null, tempDir);

				logger.log(Level.FINE, "create temp file : " + tempFile);

				fos = new FileOutputStream(tempFile);
				tempFile.deleteOnExit();

				ResourceHelper.writeStreamToStream(is, fos);
			} finally {
				ResourceHelper.closeResource(fos);
			}
		}
		InputStream is = new FileInputStream(tempFile);
		return new ServletInputStreamDelegate(is);
	}

	@Override
	protected void finalize() throws Throwable {
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
		super.finalize();
	}

	
	private class ServletInputStreamDelegate extends ServletInputStream {

		protected final Logger logger = Logger.getLogger(ServletInputStreamDelegate.class.getName());

		private final InputStream is;
		private int size = 0;

		public ServletInputStreamDelegate(InputStream is) throws FileNotFoundException {
			this.is = is;
		}

		@Override
		public int read() throws IOException {
			int i = is.read();
			size++;

			if (i == -1) {
				// end of stream, is doesn't need a close or reset
			}

			if (size > MAX_UPLOAD_SIZE) {
				logger.warning("MAX UPLOAD FILESIZE.");
				close();
				throw new IOException("upload file size to big max size : " + MAX_UPLOAD_SIZE);
			}
			return i;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int res = is.read(b, off, len);
			size = size + res;

			if (res < len) {
				// end of stream, is doesn't need a close or reset
			}

			if (size > MAX_UPLOAD_SIZE) {
				logger.warning("MAX UPLOAD FILESIZE.");
				close();
				throw new IOException("size to big max size : " + MAX_UPLOAD_SIZE);
			}
			return res;
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
	}
}
