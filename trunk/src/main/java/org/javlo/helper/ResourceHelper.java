/*
 * Created on 27-dec.-2003
 */
package org.javlo.helper;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.module.ticket.Comment;
import org.javlo.module.ticket.TicketBean;
import org.javlo.service.resource.Resource;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

public class ResourceHelper {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ResourceHelper.class.getName());

	public static Object SYNCHRO_RESOURCE = new Object();

	static final String CONFIG_DIR = "/WEB-INF/config";

	static final String STATIC_COMPONENT_DIR = "/static/components";

	public static class ImageFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File file, String fileName) {
			return StringHelper.isImage(fileName);
		}

	}

	public static class VideoFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File file, String fileName) {
			return StringHelper.isVideo(fileName);
		}

	}

	public synchronized static final void appendLineToFile(File file, String content, String encoding) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		List<String> lines = FileUtils.readLines(file, encoding);
		lines.add(content);
		FileUtils.writeLines(file, lines);
	}

	public static boolean checksumEquals(String checksum1, String checksum2) {
		return checksum1 == null ? checksum2 == null : checksum1.equals(checksum2);
	}

	public static void closeResource(Closeable... resources) {
		for (Closeable resource : resources) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return the standard checksum of the specified file. <br/>
	 * The following functions are complementary: {@link #getChecksumInputStream(InputStream)}, {@link #getChecksumResult(InputStream)}, {@link #formatChecksum(long)}
	 * 
	 * @param file
	 * @return the standard checksum of the specified file
	 * @throws IOException
	 */
	public static String computeChecksum(File file) throws IOException {
		long crc32 = FileUtils.checksumCRC32(file);
		return formatChecksum(crc32);
	}

	public static boolean deleteFileAndParentDir(File file) {
		boolean returnDelete = file.delete();
		File dir = file.getParentFile();
		if (dir.listFiles().length == 0) {
			dir.delete();
		}
		return returnDelete;
	}

	public static void downloadResource(String localDir, String baseURL, Collection<Resource> resources) throws IOException {
		for (Resource resource : resources) {
			URL url = new URL(URLHelper.mergePath(baseURL, resource.getUri()));
			File localFile = new File(URLHelper.mergePath(localDir, resource.getUri()));
			if (!localFile.exists()) {
				InputStream in = null;
				try {
					in = url.openStream();
					ResourceHelper.writeStreamToFile(in, localFile);
					logger.info("download resource : " + url + " in " + localFile);
				} finally {
					if (in != null) {
						in.close();
					}
				}
			} else {
				logger.warning("download url error : file allready exist in local : " + localFile);
			}

		}
	}

	public static String downloadResourceAsString(URL workURL) throws IOException {
		InputStream in = workURL.openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[8192];
			int len, b = in.read();
			while (b >= 0) {
				out.write(b);
				len = in.read(buffer);
				out.write(buffer, 0, len);

				b = in.read();
			}
			return new String(out.toByteArray());
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(out);
		}
	}

	/**
	 * extract a relative path from a full path.
	 * 
	 * @param application
	 *            the servlet context.
	 * @param fullPath
	 *            a full path
	 * @return retrun a relative path (sample: /var/data/static/test.png -> /static/test.png)
	 */
	public static String extractNotStaticDir(StaticConfig staticConfig, GlobalContext globalContext, String fullPath) {
		String realPath = globalContext.getDataFolder();

		realPath = ResourceHelper.getLinuxPath(realPath);
		fullPath = ResourceHelper.getLinuxPath(fullPath);

		if (fullPath.startsWith(realPath)) {
			return fullPath.substring(realPath.length());
		}
		return fullPath;
	}

	/**
	 * extract a relative path from a full path.
	 * 
	 * @param application
	 *            the servlet context.
	 * @param fullPath
	 *            a full path
	 * @return retrun a relative path (sample: /opt/tomcat/webapps/dc/WEB-INF/static/images -> /WEB-INF/static/images)
	 */
	public static String extractRelativeDir(ServletContext application, String fullPath) {
		String realPath = application.getRealPath("");
		if (fullPath.startsWith(realPath)) {
			return fullPath.substring(realPath.length());
		}
		return fullPath;
	}

	/**
	 * extract a relative path from a full path.
	 * 
	 * @param application
	 *            the servlet context.
	 * @param fullPath
	 *            a full path
	 * @return retrun a relative path (sample: /var/data/static/test.png -> /test.png)
	 */
	public static String extractResourceDir(StaticConfig staticConfig, GlobalContext globalContext, String fullPath) {
		String realPath = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());

		realPath = ResourceHelper.getLinuxPath(realPath);
		fullPath = ResourceHelper.getLinuxPath(fullPath);

		if (fullPath.startsWith(realPath)) {
			return fullPath.substring(realPath.length());
		}
		return fullPath;
	}

	public static void filteredFileCopy(File file1, File file2, Map<String, String> filter) throws IOException {
		if (!file2.exists()) {
			file2.getParentFile().mkdirs();
			file2.createNewFile();
		}
		String content = FileUtils.readFileToString(file1, ContentContext.CHARACTER_ENCODING);
		Collection<String> keys = filter.keySet();
		for (String key : keys) {
			if (filter.get(key) != null) {
				content = content.replace(key, filter.get(key));
				content = content.replace(key.toUpperCase(), filter.get(key));
			}
		}
		FileUtils.writeStringToFile(file2, content, ContentContext.CHARACTER_ENCODING);
	}

	public static void filteredFileCopyEscapeScriplet(File file1, File file2, Map<String, String> filter) throws IOException {
		if (!file2.exists()) {
			file2.getParentFile().mkdirs();
			file2.createNewFile();
		}
		String content = FileUtils.readFileToString(file1);

		String errorMsg = "<strong>Error : no scripled in template.</strong>";
		content = content.replace("<%", errorMsg);
		content = content.replace(errorMsg + '@', "<%@");

		Collection<String> keys = filter.keySet();
		for (String key : keys) {
			if (filter.get(key) != null) {
				content = content.replace(key, filter.get(key));
				content = content.replace(key.toUpperCase(), filter.get(key));
			}
		}
		FileUtils.writeStringToFile(file2, content, ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * Standart method to format the checksum into a {@link String}. <br/>
	 * This method is private because, only the following functions can call it: {@link #getChecksumInputStream(InputStream)}, {@link #getChecksumResult(InputStream)}, {@link #computeChecksum(File)} <br/>
	 * and because the implementation of the format can be changed in future.
	 * 
	 * @param crc32
	 * @return a standard formatted checksum
	 */
	private static String formatChecksum(long crc32) {
		String out = Long.toHexString(crc32).toUpperCase();
		while (out.length() < 8) {
			out = "0" + out;
		}
		return out;
	}

	/**
	 * get all directories under a directory (recursivly).
	 * 
	 * @param dir
	 * @return a list of directories (without files).
	 */
	public static Collection<File> getAllDirList(File dir) {
		Collection<File> res = new LinkedList<File>();
		File[] childs = dir.listFiles();
		if (childs != null) {
			for (File child : childs) {
				if (child.isDirectory()) {
					res.add(child);
					res.addAll(getAllDirList(child));
				}
			}
		}
		return res;
	}

	public static Collection<File> getAllFiles(File dir, FileFilter filter) {
		return getAllFiles(dir, filter, null);
	}

	public static Collection<File> getAllFiles(File dir, FileFilter filter, Comparator<File> comp) {
		if (dir == null || !dir.isDirectory() || !dir.exists()) {
			return Collections.EMPTY_LIST;
		}
		Set<File> outFiles;
		if (comp == null) {
			outFiles = new TreeSet<File>();
		} else {
			outFiles = new TreeSet<File>(comp);
		}
		File[] files = dir.listFiles(filter);
		if (files != null) {
			for (File file : files) {
				outFiles.add(file);
			}
		}
		files = dir.listFiles(new DirectoryFilter());
		if (files != null) {
			for (File file : files) {
				outFiles.addAll(getAllFiles(file, filter, comp));
			}
		}

		return outFiles;
	}

	/**
	 * get all files under a directory (recursivly).
	 * 
	 * @param dir
	 * @return a list of files (without directories).
	 */
	public static Collection<File> getAllFilesList(File dir) {
		Collection<File> res = new LinkedList<File>();
		File[] childs = dir.listFiles();
		if (childs != null) {
			for (File child : childs) {
				if (child.isDirectory()) {
					res.addAll(getAllFilesList(child));
				} else {
					res.add(child);
				}
			}
		}
		return res;
	}

	private static void getAllResource(Collection<File> files, File currentDir) {
		File[] children = currentDir.listFiles();
		for (File child : children) {
			if (child.isFile()) {
				files.add(child);
			} else if (child.isDirectory()) {
				getAllResource(files, child);
			}
		}
	}

	/*
	 * public static final int writeStreamToStream(InputStream in, OutputStream out) throws IOException { int read = in.read(); int size = 0; while (read >= 0) { size++; out.write(read); byte[] buffer = new byte[in.available()]; read = in.read(buffer); if (read >= 0) { out.write(buffer); size = size + buffer.length; read = in.read(); } } return size; }
	 */

	public static Collection<File> getAllResources(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());

		File staticDir = new File(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder()));

		Collection<File> outFiles = new LinkedList<File>();
		getAllResource(outFiles, staticDir);

		return outFiles;

	}

	/**
	 * Add a checksum computing layer to the given {@link InputStream}. <br/>
	 * Give the returned {@link InputStream} to {@link #getChecksumResult(InputStream)} to retrieve the checksum result. <br/>
	 * The following functions are complementary: {@link #getChecksumResult(InputStream)}, {@link #computeChecksum(File)}, {@link #formatChecksum(long)}
	 * 
	 * @param in
	 * @return an {@link InputStream} computing the checksum during the read, call {@link #getChecksumResult(InputStream)} to retrieve the checksum result.
	 */
	public static InputStream getChecksumInputStream(InputStream in) {
		return new CheckedInputStream(in, new CRC32());
	}

	/**
	 * Exctract the result from a {@link InputStream} returned by {@link #getChecksumInputStream(InputStream)}. <br/>
	 * The following functions are complementary: {@link #getChecksumInputStream(InputStream)}, {@link #computeChecksum(File)}, {@link #formatChecksum(long)}
	 * 
	 * @param chkIn
	 * @return the standard checksum of readed bytes from the given {@link InputStream} previously wrapped by {@link #getChecksumInputStream(InputStream)}
	 */
	public static String getChecksumResult(InputStream chkIn) {
		long crc32 = ((CheckedInputStream) chkIn).getChecksum().getValue();
		return formatChecksum(crc32);
	}

	public static final InputStream getConfigFile(ServletContext servletContext, String fileName) throws ResourceNotFoundException {
		String resourceName = CONFIG_DIR + "/" + fileName;
		InputStream in = servletContext.getResourceAsStream(resourceName);
		if (in == null) {
			throw new ResourceNotFoundException("resource not found : " + resourceName);
		}
		return in;
	}

	private static Collection<String> getDirList(File dir, String parentPath, int sorting) {
		Collection<String> res = new LinkedList<String>();
		File[] childs = dir.listFiles();

		if (childs != null) {

			for (File child : childs) {
				if (child.isDirectory()) {
					String path = parentPath + '/' + child.getName();
					res.add(path);
					res.addAll(getDirList(child, path, sorting));
				}
			}
		}
		return res;
	}

	/**
	 * return a recursive directory array path
	 * 
	 * @param directory
	 *            the base directory
	 * @param request
	 *            the current request
	 * @return a array of path
	 */
	public static String[] getDirList(String directory) {
		ArrayList<String> list = new ArrayList<String>();
		list.add("/");
		File dir = new File(directory);
		list.addAll(getDirList(dir, "", FileComparator.SIZE));
		String[] res = new String[list.size()];
		list.toArray(res);
		// TODO: test if sorting needed
		// Arrays.sort(res);
		return res;
	}

	public static String getFile(String fileName) {
		String[] elems = fileName.split("/");
		if (elems.length < 1) {
			return null;
		}
		return elems[elems.length - 1];
	}

	public static String getFileContent(File file) throws FileNotFoundException, IOException {
		String ext = FilenameUtils.getExtension(file.getName());
		String outContent = "";
		try {
			if (ext.toLowerCase().equals("doc")) {
				if (file != null) {
					WordExtractor we = new WordExtractor(new FileInputStream(file));
					outContent = we.getText();
				} else {
					logger.warning("file not found : " + file);
				}
			} else if (ext.toLowerCase().equals("pdf")) {
				PDDocument doc = PDDocument.load(file);
				PDFTextStripper text = new PDFTextStripper();
				outContent = text.getText(doc);
				doc.close();
			} else if (StringHelper.isHTML(file.getName())) {
				return loadStringFromFile(file);
			}
		} catch (Throwable t) {
			logger.warning("error when read : " + file + "+ [" + t.getMessage() + "]");
			t.printStackTrace();
		}
		return outContent;
	}

	public static final String getFileExtensionToManType(String ext) {
		ext = ext.trim().toLowerCase();
		if (ext.equals("gif")) {
			return "image/GIF";
		} else if (ext.equals("png")) {
			return "image/GIF";
		} else if (ext.equals("ico")) {
			return "image/x-icon";
		} else if ((ext.equals("jpg")) || (ext.equals("jpeg"))) {
			return "image/JPEG";
		} else if (ext.equals("mpg") || ext.equals("mpeg") || ext.equals("mpe")) {
			return "video/mpeg";
		} else if (ext.equals("mp4")) {
			return "video/mp4";
		} else if (ext.equals("avi") || ext.equals("wmv")) {
			return "video/msvideo";
		} else if (ext.equals("qt") || ext.equals("mov")) {
			return "video/quicktime";
		} else if (ext.equals("ogg") || ext.equals("ogv")) {
			return "video/ogg";
		} else if (ext.equals("webm")) {
			return "video/webm";
		} else if (ext.equals("qt") || ext.equals("mov")) {
			return "video/quicktime";
		} else if (ext.equals("pdf")) {
			return "application/pdf";
		} else if (ext.equals("css")) {
			return "text/css";
		} else if (ext.equals("csv")) {
			return "text/csv";
		} else if (ext.equals("html")) {
			return "text/html";
		} else if (ext.equals("html")) {
			return "text/html";
		} else if (ext.equals("swf")) {
			return "application/x-shockwave-flash";
		} else if (ext.equals("properties")) {
			return "text/text";
		}
		return "application/octet-stream";
	}

	public static File[] getFileList(String directory, FilenameFilter filter, HttpServletRequest request) {
		String basePath = request.getSession().getServletContext().getRealPath(directory);
		File dir = new File(basePath);
		File[] res;
		if (dir.exists()) {
			res = dir.listFiles(filter);
		} else {
			res = new File[0];
		}
		return res;
	}

	public static File[] getFileList(String directory, HttpServletRequest request) {
		File dir = new File(directory);
		File[] res;
		if (dir.exists()) {
			res = dir.listFiles();
		} else {
			res = new File[0];
		}
		return res;
	}

	public static final long getFileSize(String filePath) {
		File file = new File(filePath);
		return file.length();
	}

	/**
	 * convert a path to a correct path for current OS. sample: /static/images on windows -> \static\images and on unix no change.
	 * 
	 * @param path
	 *            a path to a file
	 * @return a correct file for current OS
	 */
	public static final String getLinuxPath(String path) {
		String outPath = path;
		outPath = outPath.replace('\\', '/');
		return outPath;
	}

	/**
	 * convert a path to a correct path for current OS. sample: /static/images on windows -> \static\images and on unix no change.
	 * 
	 * @param path
	 *            a path to a file
	 * @return a correct file for current OS
	 */
	public static final String getOSPath(String path) {
		String outPath = path;
		outPath = outPath.replace('/', File.separatorChar);
		outPath = outPath.replace('\\', File.separatorChar);
		return outPath;
	}

	public static String getPath(String fileName) {
		StringBuffer strBuf = new StringBuffer();

		String[] elems = fileName.split("[/\\\\]");

		int start = 0;
		if (elems.length > 0) {
			if (elems[0].indexOf(':') > 0) { /* drive for windows */
				start = 1;
				strBuf.append('/');
			}
		}

		for (int i = start; i < elems.length - 1; i++) {
			strBuf.append(elems[i]);
			if (i < elems.length - 1) {
				strBuf.append('/');
			}
		}
		return strBuf.toString();
	}

	public static Collection<File> getResourceWithoutMT(ContentContext ctx) throws Exception {
		Collection<File> outFiles = new LinkedList<File>();
		Collection<File> allFiles = getAllResources(ctx);
		for (File file : allFiles) {
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			if (!staticInfo.isPertinent(ctx)) {
				outFiles.add(file);
			}
		}
		return outFiles;
	}

	public static final InputStream getStaticComponentResource(ServletContext application, String componentType, String resource) throws FileNotFoundException {
		String fullName = STATIC_COMPONENT_DIR + "/" + componentType + "/" + resource;
		InputStream res = application.getResourceAsStream(fullName);
		if (res == null) {
			throw new FileNotFoundException("file not found : " + fullName);
		}

		return res;
	}

	public static String getUserDirName(String userName) {
		return StringHelper.stringWithoutSpecialChar(userName) + '-' + StringHelper.encryptPassword(userName).substring(0, 5);
	}

	public static String getUserStaticDir(GlobalContext globalContext, HttpSession session) {
		EditContext editContext = EditContext.getInstance(globalContext, session);
		String userStaticPath = editContext.getUserStaticDirectory();

		String userDir = "_not_logged";
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, session);
		if (userFactory.getCurrentUser(session) != null) {
			String userName = userFactory.getCurrentUser(session).getLogin();
			userDir = getUserDirName(userName);
		}
		userStaticPath = userStaticPath + '/' + userDir;
		return userStaticPath;
	}

	public static File getUserStaticRealDir(GlobalContext globalContext, HttpSession session) {
		EditContext editContext = EditContext.getInstance(globalContext, session);
		String userStaticPath = session.getServletContext().getRealPath(editContext.getUserStaticDirectory());

		String userDir = "_not_logged";
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, session);
		if (userFactory.getCurrentUser(session) != null) {
			String userName = userFactory.getCurrentUser(session).getLogin();
			userDir = getUserDirName(userName);
		}
		userStaticPath = userStaticPath + '/' + userDir;
		File dir = new File(userStaticPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	/**
	 * with iexplorer the name of the file is all the path this method extract the file name from a windows path
	 */
	public static String getWindowsFileName(String fileName) {
		String name = fileName;
		int lastIndex = name.lastIndexOf('\\');
		if ((lastIndex >= 0) && (lastIndex < name.length())) {
			name = name.substring(lastIndex + 1);
		}
		return name;
	}

	/**
	 * check if a file (or a folder) is under a folder. sample : /tmp/test/me.jpg with /tmp retrun true
	 * 
	 * @param file
	 *            a file, this file must be a real file or method return false.
	 * @param folder
	 *            a folder (if file -> return false), this file must be a real file or method return false.
	 * @return true if the file is under the folder.
	 * @throws IOException
	 */
	public static boolean isChildOf(File file, File folder) throws IOException {
		if (!file.exists() || !folder.exists() || file.getParentFile() == null) {
			return false;
		}
		if (!folder.isDirectory()) {
			return false;
		}
		return file.getParentFile().getCanonicalPath().startsWith(folder.getCanonicalPath());
	}

	public static boolean isPreviewFile(String file) {
		file = StringHelper.getFileNameFromPath(file);
		return file.startsWith("content_" + ContentContext.PREVIEW_MODE);
	}

	public static Properties loadProperties(File file) throws IOException {
		Properties properties = new Properties();
		InputStream in = new FileInputStream(file);
		properties.load(in);
		in.close();
		return properties;
	}

	public static final String loadStringFromStream(InputStream in, Charset encoding) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
		try {
			String line = reader.readLine();
			while (line != null) {
				out.println(line);
				line = reader.readLine();
			}
		} finally {
			ResourceHelper.closeResource(reader);
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public static final String loadStringFromFile(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		String content = loadStringFromStream(in, ContentContext.CHARSET_DEFAULT);
		return content;
	}

	public static void main2(String[] args) {

		try {
			URL url = new URL("http://twitter.com/statuses/user_timeline/104782747.rss");

			InputStream in = url.openStream();
			int read = in.read();
			while (read >= 0) {
				System.out.print((char) read);
				read = in.read();
			}

			/*
			 * final byte[] buffer = new byte[1024 * 4]; int size = 0; int byteReaded = in.read(buffer); while (byteReaded > 0) { size = size + byteReaded; System.out.write(buffer, 0, byteReaded); byteReaded = in.read(buffer); } System.out.println("*** byteReaded = "+byteReaded);
			 */
			in.close();

			// String content = NetHelper.readPage(url);

			// +System.out.println(content);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Move a file or a folder to the global trash, depending on staticConfig.
	 * 
	 * @param staticConfig
	 * @param fileOrFolder
	 * @return <code>true</code> if origin doesn't exist; or the result of {@link File#renameTo(File)}.
	 */
	public static boolean moveToGlobalTrash(StaticConfig staticConfig, String fileOrFolder) {
		File file = new File(fileOrFolder);
		if (file.exists()) {
			String trashFolder = staticConfig.getTrashFolder();
			File dest;
			int i = 1;
			String version = "";
			do {
				dest = new File(trashFolder + '/' + file.getName() + version);
				version = "." + i;
				i++;
			} while (dest.exists());
			dest.getParentFile().mkdirs();
			return file.renameTo(dest);
		}
		return true;
	}

	/**
	 * remove the data folder directory this method is used for obtain a relative file path from a ablute file path.
	 * 
	 * @param path
	 * @return
	 */
	public static String removeDataFolderDir(GlobalContext globalContext, String path) {
		int indexOfStaticPath = path.indexOf(globalContext.getFolder()) + globalContext.getFolder().length();
		if (indexOfStaticPath < globalContext.getFolder().length()) {
			return path;
		}
		return path.substring(indexOfStaticPath, path.length());
	}

	/**
	 * remove the path from a string this method is used for obtain a relative file path from a ablute file path.
	 * 
	 * @param path
	 * @return
	 */
	public static String removePath(String pathCuted, String path) {
		int indexOfStaticPath = path.indexOf(pathCuted);
		if (indexOfStaticPath < 0) {
			return path;
		}
		return path.substring(indexOfStaticPath + pathCuted.length(), path.length());
	}

	/**
	 * change all the reference to a resource when a resource path or name if changed
	 * 
	 * @param ctx
	 * @param oldName
	 * @param newName
	 * @throws Exception
	 */
	public static void renameResource(ContentContext ctx, File file, File newFile) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentContext lgCtx = new ContentContext(ctx);
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			lgCtx.setRequestContentLanguage(lg);
			List<IContentVisualComponent> comps = ComponentFactory.getAllComonentFromContext(lgCtx);
			for (IContentVisualComponent comp : comps) {
				if (comp instanceof IStaticContainer) {
					((IStaticContainer) comp).renameResource(ctx, file, newFile);
				}
			}
		}
		StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
		staticInfo.renameFile(ctx, newFile);

		// delete old ref in cache
		String fromDataFolder = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).delete(fromDataFolder);
	}

	/**
	 * Close streams, writers, readers, etc without any exception even if they are <code>null</code>.
	 * 
	 * @param closeables
	 *            the objects to close
	 */
	public static void safeClose(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	public static final void writePropertiesToFile(Properties properties, File file, String title) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		properties.store(out, title);
		out.close();
	}

	public static final void writePropertiesToFile(PropertiesConfiguration properties, File file) throws ConfigurationException, IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		properties.save(out, ContentContext.CHARACTER_ENCODING);
		out.close();
	}

	public static final int writeStreamToFile(InputStream in, File file) throws IOException {

		int countByte = 0;

		if (file.getParentFile() != null && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			countByte = writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(out);
		}

		return countByte;
	}

	public static final int writeFileToFile(File fileIn, File file) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(fileIn);
			return writeStreamToFile(in, file);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public static final File writeFileItemToFolder(FileItem fileItem, File folder, boolean overwrite, boolean rename) throws IOException {
		if (!folder.isDirectory()) {
			return null;
		}
		File file = new File(URLHelper.mergePath(folder.getAbsolutePath(), fileItem.getName()));
		if (!file.exists()) {
			file.createNewFile();
		} else {
			if (!overwrite && !rename) {
				throw new FileExistsException("File allready exist.");
			}
			if (rename) {
				file = ResourceHelper.getFreeFileName(file);
			}
		}
		InputStream in = null;
		try {
			in = fileItem.getInputStream();
			writeStreamToFile(in, file);
			return file;
		} catch (IOException e) {
			ResourceHelper.closeResource(in);
			file.delete();
			throw e;
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	/**
	 * write a InputStream in a OuputStream, without close.
	 * 
	 * @return the size of transfered data in byte.
	 */
	public static final int writeStreamToStream(InputStream in, OutputStream out) throws IOException {
		final byte[] buffer = new byte[1024 * 4];
		int size = 0;
		int byteReaded = in.read(buffer);
		while (byteReaded >= 0) {
			size = size + byteReaded;
			out.write(buffer, 0, byteReaded);
			byteReaded = in.read(buffer);
		}
		return size;
	}

	public static final void writeStringToFile(File file, String content) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		for (int i = 0; i < content.length(); i++) {
			out.write(content.charAt(i));
		}
		out.close();
	}

	public static final void writeBytesToFile(File file, byte[] content) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		for (byte element : content) {
			out.write(element);
		}
		out.close();
	}

	public static final void writeStringToFile(File file, String content, String encoding) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		byte[] contentByte = content.getBytes(encoding);
		out.write(contentByte);
		out.close();
	}

	public static final void writeStringToStream(String content, OutputStream out) throws IOException {
		for (int i = 0; i < content.length(); i++) {
			out.write(content.charAt(i));
		}
		out.close();
	}

	/**
	 * return a free file name. if file exist add a number as suffix.
	 * 
	 * @param file
	 * @return
	 */
	public static File getFreeFileName(File file) {
		if (!file.exists()) {
			return file;
		}
		File folder = file.getParentFile();
		String newName = file.getName();
		String ext = StringHelper.getFileExtension(file.getName());
		String fileName = StringHelper.getFileNameWithoutExtension(file.getName());
		for (int i = 1; i < 999999; i++) {
			newName = fileName + "_" + i + '.' + ext;
			File newFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), newName));
			if (!newFile.exists()) {
				return newFile;
			}
		}
		return null;
	}

	/**
	 * return true if file is insise template folder
	 * 
	 * @param globalContext
	 * @param file
	 * @return
	 */
	public static boolean isTemplateFile(GlobalContext globalContext, File file) {
		String filePath = file.getAbsolutePath().replace('\\', '/');
		String templatePath = globalContext.getStaticConfig().getTemplateFolder().replace('\\', '/');
		return filePath.startsWith(templatePath);
	}

	/**
	 * return the name of the template
	 * 
	 * @param globalContext
	 * @param file
	 * @return
	 */
	public static String extractTemplateName(GlobalContext globalContext, File file) {
		String filePath = file.getAbsolutePath().replace('\\', '/');
		String templatePath = globalContext.getStaticConfig().getTemplateFolder().replace('\\', '/');

		if (filePath.startsWith(templatePath)) {
			filePath = filePath.replaceFirst(templatePath, "");
			if (filePath.startsWith("/")) {
				filePath = filePath.substring(1);
			}
			return filePath.split("/")[0];
		}
		return null;
	}

	public static String readLine(RandomAccessFile file, Charset cs) throws IOException {
		ByteArrayOutputStream lineBytes = new ByteArrayOutputStream();
		int b = -1;
		boolean eol = false;
		while (!eol) {
			switch (b = file.read()) {
			case -1:
			case '\n':
				eol = true;
				break;
			case '\r':
				eol = true;
				long cur = file.getFilePointer();
				if ((file.read()) != '\n') {
					file.seek(cur);
				}
				break;
			default:
				lineBytes.write(b);
				break;
			}
		}

		byte[] bytes = lineBytes.toByteArray();
		if (b == -1) {
			return null;
		}
		return new String(bytes, cs);
	}

	public static String createModulePath(ContentContext ctx, String path) throws ModuleException, Exception {
		Module currentModule = ModulesContext.getInstance(ctx.getRequest().getSession(), GlobalContext.getInstance(ctx.getRequest())).getCurrentModule();
		String insideModulePath = URLHelper.mergePath("/modules", currentModule.getName(), path);
		return insideModulePath;
	}

	public static Serializable loadBeanFromXML(String xml) {
		Serializable obj;
		InputStream in;
		try {
			in = new ByteArrayInputStream(xml.getBytes(ContentContext.CHARACTER_ENCODING));
			XMLDecoder decoder = new XMLDecoder(in);
			obj = (Serializable) decoder.readObject();
			return obj;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String storeBeanFromXML(Serializable bean) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// XMLEncoder encoder = new XMLEncoder(out, ContentContext.CHARACTER_ENCODING, false, 0);
		XMLEncoder encoder = new XMLEncoder(out);
		encoder.writeObject(bean);
		encoder.flush();
		encoder.close();
		try {
			return new String(out.toByteArray(), ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] storeBeanToBin(Serializable bean) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream obj_out = new ObjectOutputStream(out);
		obj_out.writeObject(bean);
		return out.toByteArray();
	}

	public static void main(String[] args) {
		TicketBean bean = new TicketBean();
		bean.setAuthors("patrick");
		bean.addComments(new Comment("patrick", "coucou"));
		bean.addComments(new Comment("catherine", "coucou bis"));
		String xml = storeBeanFromXML(bean);
		System.out.println(xml);
		TicketBean bean2 = (TicketBean) loadBeanFromXML(xml);
		System.out.println("***** ResourceHelper.main : authors = " + bean2.getAuthors()); // TODO: remove debug trace
	}
}
