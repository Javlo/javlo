package org.javlo.ztatic;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

/**
 * this class is used for cache transformation of a file. The transformation is is identified from a key.
 * 
 * @author pvandermaesen
 * 
 */
public class FileCache {

	private static Logger logger = Logger.getLogger(FileCache.class.getName());

	private static final String KEY = FileCache.class.getName();
	private static final String BASE_DIR = "/WEB-INF/.files_cache";

	ServletContext application = null;

	private FileCache(ServletContext inApplication) {
		application = inApplication;
		application.setAttribute(KEY, this);

		File file = new File(application.getRealPath(BASE_DIR + '/'));
		if (!file.exists()) {
			logger.info("create dir : " + file);
			file.mkdirs();
		}
	}

	/**
	 * create a instance of FileCache
	 * 
	 * @param inApplication
	 *            the ServletContext
	 * @return a instance in application scope.
	 */
	public static final FileCache getInstance(ServletContext inApplication) {
		FileCache fc = (FileCache) inApplication.getAttribute(KEY);
		if (fc == null) {
			fc = new FileCache(inApplication);
		}
		return fc;
	}

	private File getFileName(String key, String fileName) {
		fileName = fileName.replace('\\', '/');
		String cacheFileName = BASE_DIR + '/' + key;
		if (fileName.startsWith("/")) {
			cacheFileName = cacheFileName + fileName;
		} else {
			cacheFileName = cacheFileName + '/' + fileName;
		}
		return new File(application.getRealPath(cacheFileName));
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @param in
	 *            the data of the file.
	 * @throws IOException
	 */
	public void saveFile(String key, String fileName, InputStream in) throws IOException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);

		try {
			int available = in.available();
			byte[] buffer = new byte[available];
			int read = in.read(buffer);
			while (read > 0) {
				out.write(buffer);
				available = in.available();
				buffer = new byte[available];
				read = in.read(buffer);
			}
		} finally {
			out.close();
		}
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @param in
	 *            the data of the file.
	 * @throws IOException
	 */
	public static void saveFile(String fileName, InputStream in) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			int available = in.available();
			byte[] buffer = new byte[available];
			int read = in.read(buffer);
			while (read > 0) {
				out.write(buffer);
				available = in.available();
				buffer = new byte[available];
				read = in.read(buffer);
			}
		} finally {
			out.close();
		}
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @return a outstream to the new file.
	 * @throws IOException
	 */
	public OutputStream saveFile(String key, String fileName) throws IOException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		return out;
	}

	/**
	 * get a file in the cache, return null if file is'nt in the cache.
	 * 
	 * @param key
	 *            the key of caching
	 * @param fileName
	 *            the file name
	 * @return a existing file or null.
	 */
	public File getFile(String key, String fileName) {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			return null;
		} else {
			return file;
		}
	}

	/**
	 * get a file in the cache, return null if file is'nt in the cache.
	 * 
	 * @param key
	 *            the key of caching
	 * @param fileName
	 *            the file name
	 * @return a inputStream from file.
	 * @throws FileNotFoundException
	 */
	public InputStream getFileInputStream(String key, String fileName, long latestModificationDate) throws FileNotFoundException {
		File file = getFileName(key, fileName);
		if (latestModificationDate > file.lastModified()) {
			return null;
		}
		if (!file.exists() || file.isDirectory()) {
			return null;
		} else {
			return new FileInputStream(file);
		}
	}

	/**
	 * get the last modified of a cached file.
	 * 
	 * @param key
	 *            the key of caching
	 * @param fileName
	 *            the file name
	 * @return a inputStream from file.
	 * @throws FileNotFoundException
	 */
	public long getLastModified(String key, String fileName) throws FileNotFoundException {
		File file = getFileName(key, fileName);
		return file.lastModified();
	}

	/**
	 * clear a file for all keys.
	 * 
	 * @param fileName
	 *            a file name.
	 */
	public void delete(String fileName) {
		File cacheDir = new File(application.getRealPath(BASE_DIR));
		// File[] keys = cacheDir.listFiles(new DirectoryFilter());

		Collection<File> keys = ResourceHelper.getAllDirList(cacheDir);

		for (File file : keys) {
			// File cacheFile = getFileName(file.getName(), fileName);

			File cacheFile = new File(URLHelper.mergePath(file.getAbsolutePath(), fileName));

			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		}
	}

	public void clear() {
		File cacheDir = new File(application.getRealPath(BASE_DIR));
		try {
			FileUtils.deleteDirectory(cacheDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File oldCacheDir = new File(application.getRealPath("_dc_cache"));
		try {
			FileUtils.deleteDirectory(oldCacheDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		cacheDir.mkdirs();
	}

	public void storeBean(String key, Serializable obj) throws IOException {
		String fileName = StringHelper.createFileName(key) + ".serial.xml";
		File file = new File(URLHelper.mergePath(application.getRealPath(BASE_DIR), fileName));
		if (file.exists() && obj == null) {
			file.delete();
			return;
		}
		file.createNewFile();
		XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file));
		try {
			encoder.writeObject(obj);
			encoder.flush();
		} finally {
			encoder.close();
		}
	}

	public Serializable loadBean(String key) throws ClassNotFoundException, IOException {
		String fileName = StringHelper.createFileName(key) + ".serial.xml";
		File file = new File(URLHelper.mergePath(application.getRealPath(BASE_DIR), fileName));
		if (!file.exists()) {			
			return null;
		}
		XMLDecoder decorder = new XMLDecoder(new FileInputStream(file));
		try {
			logger.fine("load bean : "+file);
			return (Serializable) decorder.readObject();
		} finally {
			decorder.close();
		}
	}

}
