package org.javlo.thread;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public abstract class AbstractThread {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(AbstractThread.class.getName());

	protected boolean needRunning = true;

	static class ThreadFileFilter implements FileFilter {

		public boolean accept(File pathname) {
			return StringHelper.getFileExtension(pathname.getAbsolutePath()).equals(THREAD_FILE_EXTENSION);
		}

	}

	public static final ThreadFileFilter threadFileFilter = new AbstractThread.ThreadFileFilter();

	private static final String THREAD_VERSION = "1.0";
	private static final String THREAD_FILE_EXTENSION = "thread";
	private static final String THREAD_INFO_PREFIX = "__thread";
	private static final String THREAD_CLASS_NAME = THREAD_INFO_PREFIX + ".classname";
	private static final long THREAD_TIMEOUT = 60 * 60 * 1000;

	private Properties properties = null;
	private File file = null;

	public static AbstractThread createInstance(String threadFolder, Class<? extends AbstractThread> clazz) throws InstantiationException, IllegalAccessException {
		String name = "t_" + StringHelper.getRandomId();
		AbstractThread thread = clazz.newInstance();
		File threadFolderFile = new File(threadFolder);
		if (!threadFolderFile.exists()) {
			threadFolderFile.mkdirs();
		}
		thread.file = new File(URLHelper.mergePath(threadFolder, name + "." + THREAD_FILE_EXTENSION));
		thread.properties = new Properties();
		thread.setField(THREAD_CLASS_NAME, clazz.getName());
		return thread;
	}

	public static AbstractThread getInstance(File file) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return null;
		}
		Properties properties = new Properties();
		InputStream in = new FileInputStream(file);
		try {
			properties.load(in);
		} finally {
			ResourceHelper.closeResource(in);
		}

		String className = properties.getProperty(THREAD_CLASS_NAME);
		if (className != null) {
			AbstractThread thread = (AbstractThread) Class.forName(className).newInstance();
			thread.properties = properties;
			thread.file = file;
			return thread;
		} else {
			logger.warning("Thread error could not instanciate : " + file);
			file.delete();
			return null;
		}
	}

	public String getField(String key) {
		return properties.getProperty(key);
	}

	public void setField(String key, String value) {
		properties.setProperty(key, value);
	}

	public void store() throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		OutputStream out = new FileOutputStream(file);
		properties.store(out, "thread - v " + THREAD_VERSION);
		out.close();
	}

	public boolean needRunning() {
		return needRunning;
	}

	/**
	 * destroy the thread, this method can be override for not delete the file after run the process (recurent process)
	 */
	public void destroy() {
		System.out.println("***** AbstractThread.destroy : "+file); //TODO: remove debug trace
		if (!file.delete()) {
			needRunning = false;
			logger.warning("could not delete file : " + file + ".  Thread running set to false.");
		}
	}

	/**
	 * info for display in log.
	 * 
	 * @return
	 */
	public String logInfo() {
		return "";
	}

	/**
	 * get the timeout, after this time thread is killed by thread manager.
	 * 
	 * @return timeout in ms
	 */
	public long getTimeout() {
		return THREAD_TIMEOUT;
	}

	public abstract void run();

}
