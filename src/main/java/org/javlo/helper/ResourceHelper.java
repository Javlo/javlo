/*
 * Created on 27-dec.-2003O
 */
package org.javlo.helper;

import ch.simschla.minify.adapter.Minifier;
import com.google.gson.JsonElement;
import fr.opensagres.poi.xwpf.converter.core.FileURIResolver;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.javlo.actions.DataAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.filter.DirectoryFilter;
import org.javlo.helper.Comparator.FileComparator;
import org.javlo.io.TransactionFile;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.resource.Resource;
import org.javlo.user.AdminUserSecurity;
import org.javlo.utils.ConfigurationProperties;
import org.javlo.xml.NodeXML;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;
import org.owasp.encoder.Encode;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class ResourceHelper {

	public static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ResourceHelper.class.getName());

	public static Object SYNCHRO_RESOURCE = new Object();

	static final String CONFIG_DIR = "/WEB-INF/config";

	/**
	 * file in private dir could not be downloaded
	 */
	public static final String PRIVATE_DIR = "_private";

	static final String STATIC_COMPONENT_DIR = "/static/components";

	public static final List<String> STATIC_FILE_EXTENSION = List.of(
			"txt",  // Text file
			"doc",  // Microsoft Word (ancien format)
			"docx", // Microsoft Word (format OpenXML)
			"xls",  // Microsoft Excel (ancien format)
			"xlsx", // Microsoft Excel (format OpenXML)
			"ppt",  // Microsoft PowerPoint (ancien format)
			"pptx", // Microsoft PowerPoint (format OpenXML)
			"pdf",  // Adobe Portable Document Format
			"jpg",  // Image JPEG
			"jpeg", // Image JPEG (autre extension)
			"png",  // Image PNG
			"gif",  // Image GIF
			"bmp",  // Image BMP
			"mp3",  // Audio MP3
			"mp4",  // Vidéo MP4
			"avi",  // Vidéo AVI
			"mkv",  // Vidéo MKV
			"zip",  // Archive ZIP
			"rar",  // Archive RAR
			"7z",   // Archive 7-Zip
			"tar",  // Archive TAR
			"gz",   // Archive GZipped
			"java", // Code source Java
			"odt",  // Document texte OpenDocument
			"ods",  // Feuille de calcul OpenDocument
			"odp",  // Présentation OpenDocument
			"odg",  // Dessin/graphique OpenDocument
			"odf",  // Formule mathématique OpenDocument
			"odb",  // Base de données OpenDocument
			"odm"   // Document maître texte OpenDocument
	);

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

	public static boolean checksumEquals(String checksum1, String checksum2) {
		return checksum1 == null ? checksum2 == null : checksum1.equals(checksum2);
	}

	public static void closeResource(Closeable... resources) {
		for (Closeable resource : resources) {
			if (resource != null) {
				try {
					resource.close();
				} catch (Throwable t) {
					logger.warning(t.getMessage());
				}
			}
		}
	}

	public static void closeResource(HttpURLConnection... connections) {
		for (HttpURLConnection conn : connections) {
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Throwable t) {
					logger.warning(t.getMessage());
				}
			}
		}
	}

	/**
	 * Return the standard checksum of the specified file. <br/>
	 * The following functions are complementary:
	 * {@link #getChecksumInputStream(InputStream)},
	 * {@link #getChecksumResult(InputStream)}, {@link #formatChecksum(long)}
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

	public static void downloadResource(ContentContext ctx, String localDir, String baseURL, NodeXML nodeXML) throws Exception {
		NodeXML child = nodeXML.getChild("resource");
		while (child != null) {
			Resource resource = new Resource();
			resource.setUri(child.getAttributeValue("uri"));
			URL url = new URL(Encode.forUri(URLHelper.mergePath(baseURL, resource.getUri())));
			File localFile = new File(URLHelper.mergePath(localDir, resource.getUri()));
			InputStream in = null;
			try {
				if (!localFile.exists()) {
					in = url.openStream();
					ResourceHelper.writeStreamToFile(in, localFile);
					logger.info("download resource : " + url + " in " + localFile);
				}
				/** load static info **/
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, localFile);
				ContentContext lgCtx = new ContentContext(ctx);
				if (child.getAttributeValue("id") != null) {
					for (String lg : ctx.getGlobalContext().getContentLanguages()) {
						lgCtx.setAllLanguage(lg);
						String jsonURL = url.toString() + "json?lg=" + lg;
						JsonElement jsonElement = NetHelper.readJson(new URL(jsonURL));
						Map<String, String> jsonMap = new HashMap<String, String>();
						for (Entry entry : jsonElement.getAsJsonObject().entrySet()) {
							jsonMap.put((String) entry.getKey(), "" + entry.getValue());
						}
						staticInfo.setTitle(lgCtx, StringHelper.removeQuote(jsonMap.get("title")));
						staticInfo.setDescription(lgCtx, StringHelper.removeQuote(jsonMap.get("description")));
						staticInfo.setLocation(lgCtx, StringHelper.removeQuote(jsonMap.get("location")));
						staticInfo.setCopyright(lgCtx, StringHelper.removeQuote(jsonMap.get("copyright")));
						staticInfo.setDate(lgCtx, StringHelper.parseSortableTime(StringHelper.removeQuote(jsonMap.get("sortableDate"))));
						staticInfo.setFocusZoneX(lgCtx, Integer.parseInt(StringHelper.removeQuote(jsonMap.get("focusZoneX"))));
						staticInfo.setFocusZoneY(lgCtx, Integer.parseInt(StringHelper.removeQuote(jsonMap.get("focusZoneY"))));
					}
				} else {
					staticInfo.fromXML(ctx, child);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				if (in != null) {
					in.close();
				}
			}

			child = child.getNext("resource");
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
	 * @return retrun a relative path (sample: /var/data/static/test.png ->
	 *         /static/test.png)
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
	 * @return retrun a relative path (sample: /var/data/static/test.png ->
	 *         /test.png)
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

	/**
	 * filter copy file
	 * @param source
	 * @param target
	 * @param filter list of key,value to be replaced
	 * @param soft if true target file is replaced only if source file is more recent
	 * @throws IOException
	 */
	public static void filteredFileCopy(File source, File target, Map<String, String> filter, boolean soft) throws IOException {

		if (soft && target.exists() && source.lastModified() <= target.lastModified()) {
			return;
		}

		if (soft) {
			logger.info("copy file in soft mode : "+target);
		}

		if (!target.exists()) {
			target.getParentFile().mkdirs();
			target.createNewFile();
		} else {
			return;
		}
		String content = FileUtils.readFileToString(source, ContentContext.CHARACTER_ENCODING);
		List<String> keys = new LinkedList<String>(filter.keySet());
		Collections.sort(keys, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s2.length() - s1.length();
			}
		});
		for (String key : keys) {
			if (!StringHelper.isEmpty(key) && filter.get(key) != null && filter.get(key).trim().length() > 0) {
				content = content.replace(key, filter.get(key));
				content = content.replace(key.toUpperCase(), filter.get(key));
			}
		}
		FileUtils.writeStringToFile(target, content, ContentContext.CHARACTER_ENCODING);
	}

	public static void copyDir(File dir1, File dir2, boolean overwrite, FileFilter filter) throws IOException {
		if (dir1.isFile()) {
			if (dir2.exists() && !overwrite) {
				return;
			}
			if (filter == null || filter.accept(dir1)) {
				writeFileToFile(dir1, dir2);
			}
		} else {
			for (File child : dir1.listFiles()) {
				copyDir(child, new File(dir2.getAbsolutePath() + '/' + child.getName()), overwrite, filter);
			}
		}
	}

	/**
	 * transactional copy a file other file
	 *
	 * @param source
	 *            the source file, must exist
	 * @param destination
	 *            the target file, could not exist
	 * @param overwrite
	 *            if true and file desctination exist, this method done nothing
	 * @return true if file is copied, false otherwise
	 * @throws IOException
	 *             error width IO, file destination is'nt modified if there are
	 *             error
	 */
	public static boolean copyFile(File source, File destination, boolean overwrite) throws IOException {
		return copyFile(source, destination, overwrite, false);
	}

	/**
	 *
	 * @param source
	 * @param destination
	 * @param overwrite
	 * @param soft if true copy only if modification date in more recent in source.
	 * @return
	 * @throws IOException
	 */
	public static boolean copyFile(File source, File destination, boolean overwrite, boolean soft) throws IOException {
		if (!overwrite && destination.exists()) {
			return false;
		} else {
			if (soft && destination.exists() && (source.lastModified() <= destination.lastModified())) {
				return false;
			}
			if (soft) {
				logger.info("copy file in soft mode : "+destination);
			}
			FileInputStream in = null;
			try {
				TransactionFile transactionFile = new TransactionFile(destination);
				in = new FileInputStream(source);
				writeStreamToStream(in, transactionFile.getOutputStream());
				closeResource(in);
				transactionFile.commit();
			} finally {
				closeResource(in);
			}
			return true;
		}
	}

	public static String extractResourcePathFromURL(ContentContext ctx, String url) {
		if (url == null) {
			return null;
		}
		ContentContext createUrlCtx = new ContentContext(ctx);
		if (StringHelper.isURL(url)) {
			createUrlCtx.setAbsoluteURL(true);
		}
		String baseResourceUrl = URLHelper.createResourceURL(createUrlCtx, "/");
		if (url.startsWith(baseResourceUrl)) {
			return url.substring(baseResourceUrl.length());
		}
		String baseMediaUrl = URLHelper.createMediaURL(createUrlCtx, "/");
		if (url.startsWith(baseMediaUrl)) {
			return url.substring(baseMediaUrl.length());
		}
		return url;
	}

	/**
	 *
	 * @param file1
	 * @param file2
	 * @param compress
	 * @param secure
	 * @param filter list of key,value to be replaced
	 * @param soft if true target file is replaced only if source file is more recent
	 * @throws IOException
	 */
	public static void filteredFileCopyEscapeScriplet(File file1, File file2, Map<String, String> filter, boolean compress, boolean secure, boolean soft) throws IOException {

		if (soft && file2.exists()) {
			if (file2.lastModified() <= file1.lastModified()) {
				return;
			}
		}

		if (!file2.exists()) {
			file2.getParentFile().mkdirs();
			file2.createNewFile();
		} else {
			return;
		}
		String content = FileUtils.readFileToString(file1);

		if (secure) {
			String errorMsg = "<strong>Error : no scripled in template in high secure mode.</strong>";
			content = content.replace("<%", errorMsg);
			content = content.replace(errorMsg + '@', "<%@");
		}

		List<String> keys = new LinkedList<String>(filter.keySet());
		// sort because big key must be replaced before little key.

		Collections.sort(keys, new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s2.length() - s1.length();
			}
		});
		for (String key : keys) {
			if (!StringHelper.isEmpty(key) && filter.get(key) != null && filter.get(key).trim().length() > 0) {
				content = content.replace(key, filter.get(key));
				content = content.replace(key.toUpperCase(), filter.get(key));
			}
		}
		/*if (compress && StringHelper.getFileExtension(file1.getName()).equalsIgnoreCase("jsp")) {
			content = XHTMLHelper.compress(content);
		}*/
		FileUtils.writeStringToFile(file2, content, ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * Standart method to format the checksum into a {@link String}. <br/>
	 * This method is private because, only the following functions can call it:
	 * {@link #getChecksumInputStream(InputStream)},
	 * {@link #getChecksumResult(InputStream)}, {@link #computeChecksum(File)} <br/>
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
	public static List<File> getAllDirList(File dir) {
		List<File> res = new LinkedList<File>();
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
		Collection<File> outFiles;
		if (comp == null) {
			outFiles = new LinkedList<File>();
		} else {
			outFiles = new TreeSet<File>(comp);
		}
		File[] files = dir.listFiles(filter);
		if (files != null) {
			for (File file : files) {
				outFiles.add(file);
			}
		}
		files = dir.listFiles((FilenameFilter) new DirectoryFilter());
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

	public static long getLatestModificationFileOnFolder(File folder, String... exts) {
		if (folder.isFile()) {
			return folder.lastModified();
		}
		List<String> extsList = Arrays.asList(exts);
		long latest = Long.MIN_VALUE;
		for (File file : getAllFilesList(folder)) {
			String ext = StringHelper.getFileExtension(file.getName()).toLowerCase();
			if (extsList.size() == 0 || extsList.contains(ext)) {
				if (file.lastModified() > latest) {
					latest = file.lastModified();
				}
			}
		}
		return latest;
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
	 * public static final int writeStreamToStream(InputStream in, OutputStream out)
	 * throws IOException { int read = in.read(); int size = 0; while (read >= 0) {
	 * size++; out.write(read); byte[] buffer = new byte[in.available()]; read =
	 * in.read(buffer); if (read >= 0) { out.write(buffer); size = size +
	 * buffer.length; read = in.read(); } } return size; }
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
	 * Give the returned {@link InputStream} to
	 * {@link #getChecksumResult(InputStream)} to retrieve the checksum result.
	 * <br/>
	 * The following functions are complementary:
	 * {@link #getChecksumResult(InputStream)}, {@link #computeChecksum(File)},
	 * {@link #formatChecksum(long)}
	 *
	 * @param in
	 * @return an {@link InputStream} computing the checksum during the read, call
	 *         {@link #getChecksumResult(InputStream)} to retrieve the checksum
	 *         result.
	 */
	public static InputStream getChecksumInputStream(InputStream in) {
		return new CheckedInputStream(in, new CRC32());
	}

	/**
	 * Exctract the result from a {@link InputStream} returned by
	 * {@link #getChecksumInputStream(InputStream)}. <br/>
	 * The following functions are complementary:
	 * {@link #getChecksumInputStream(InputStream)}, {@link #computeChecksum(File)},
	 * {@link #formatChecksum(long)}
	 *
	 * @param chkIn
	 * @return the standard checksum of readed bytes from the given
	 *         {@link InputStream} previously wrapped by
	 *         {@link #getChecksumInputStream(InputStream)}
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
		if (file == null) {
			return null;
		}
		String ext = FilenameUtils.getExtension(file.getName());
		String outContent = "";
		try {
			if (ext.toLowerCase().equals("doc")) {
				WordExtractor we = new WordExtractor(new FileInputStream(file));
				outContent = we.getText();
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

	public static final String getMineTypeToFileExtension(String mineType) {
		mineType = mineType.trim().toLowerCase();
		if (mineType.equals("image/gif")) {
			return "gif";
		} else if (mineType.equals("image/png")) {
			return "png";
		} else if (mineType.equals("image/webp")) {
			return "webp";
		} else if (mineType.equals("image/jpg") || mineType.equals("image/jpeg")) {
			return "jpg";
		} else if (mineType.equals("application/xml")) {
			return "xml";
		}
		return "bin";
	}

	public static final String getFileExtensionToMineType(String ext) {
		ext = ext.trim().toLowerCase();
		if (ext.equals("gif")) {
			return "image/GIF";
		} else if (ext.equals("png")) {
			return "image/PNG";
		} else if (ext.equals("webp")) {
			return "image/webp";
		} else if (ext.equals("xml")) {
			return "application/xml";
		} else if (ext.equals("ico")) {
			return "image/x-icon";
		} else if ((ext.equals("jpg")) || (ext.equals("jpeg"))) {
			return "image/JPEG";
		} else if (ext.equals("mpg") || ext.equals("mpeg") || ext.equals("mpe")) {
			return "video/mpeg";
		} else if (ext.equals("svg")) {
			return "image/svg+xml";
		} else if (ext.equals("mp3")) {
			return "audio/mpeg";
		} else if (ext.equals("mp4")) {
			return "video/mp4";
		} else if (ext.equals("m4a")) {
			return "audio/mp4";
		} else if (ext.equals("avi") || ext.equals("wmv")) {
			return "video/msvideo";
		} else if (ext.equals("qt") || ext.equals("mov")) {
			return "video/quicktime";
		} else if (ext.equals("ogg") || ext.equals("ogv")) {
			return "video/ogg";
		} else if (ext.equals("aif") || ext.equals("aiff") || ext.equals("aifc")) {
			return "audio/x-aiff";
		} else if (ext.equals("webm")) {
			return "video/webm";
		} else if (ext.equals("qt") || ext.equals("mov")) {
			return "video/quicktime";
		} else if (ext.equals("pdf")) {
			return "application/pdf";
		} else if (ext.equals("js")) {
			return "application/javascript";
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
		} else if (ext.equals("zip")) {
			return "application/zip";
		} else if (ext.equals("properties")) {
			return "text/text";
		} else if (ext.equals("xls")) {
			return "application/vnd.ms-excel";
		} else if (ext.equals("ppt")) {
			return "application/vnd.ms-powerpoint";
		} else if (ext.equals("xlsx")) {
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else if (ext.equals("doc")) {
			return "application/msword";
		} else if (ext.equals("xlsx")) {
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		} else if (ext.equals("odp")) {
			return "application/vnd.oasis.opendocument.presentation";
		} else if (ext.equals("odt")) {
			return "application/vnd.oasis.opendocument.text";
		} else if (ext.equals("ods")) {
			return "application/vnd.oasis.opendocument.spreadsheet";
		} else if (ext.equals("ics")) {
			return "text/calendar";
		} else if (ext.equals("json")) {
			return "application/json";
		} else if (ext.equals("epub")) {
			return "application/epub+zip";
		}
		return "application/octet-stream";
	}

	public static File[] getFileList(String directory) {
		File dir = new File(directory);
		File[] res;
		if (dir.exists()) {
			res = dir.listFiles();
		} else {
			res = new File[0];
		}
		return res;
	}

	public static File[] getFileList(String directory, HttpServletRequest request) {
		return getFileList(directory);
	}

	public static final long getFileSize(String filePath) {
		File file = new File(filePath);
		return file.length();
	}

	public static boolean isStaticFile(String filePath) {
		String fileExt = StringHelper.getFileExtension(filePath);
		fileExt = fileExt.toLowerCase();
		return STATIC_FILE_EXTENSION.contains(fileExt);
	}

	/**
	 * convert a path to a correct path for current OS. sample: /static/images on
	 * windows -> \static\images and on unix no change.
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
	 * convert a path to a correct path for current OS. sample: /static/images on
	 * windows -> \static\images and on unix no change.
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

	/**
	 * with iexplorer the name of the file is all the path this method extract the
	 * file name from a windows path
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
	 * check if a file (or a folder) is under a folder. sample : /tmp/test/me.jpg
	 * with /tmp retrun true
	 *
	 * @param file
	 *            a file, this file must be a real file or method return false.
	 * @param folder
	 *            a folder (if file -> return false), this file must be a real file
	 *            or method return false.
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

	/**
	 * check if this file is a document (list of extenion define in
	 * static-config.properties
	 *
	 * @param ctx
	 * @param filename
	 * @return
	 */
	public static boolean isDocument(ContentContext ctx, String filename) {
		if (filename == null) {
			return false;
		} else {
			String ext = StringHelper.getFileExtension(filename);
			if (StringHelper.isEmpty(ext)) {
				return false;
			} else {
				ext = ext.trim();
				return ctx.getGlobalContext().getStaticConfig().getDocumentExtension().contains(ext.toLowerCase());
			}
		}
	}

	/**
	 * check if this file is a document (list of extenion define in
	 * static-config.properties
	 *
	 * @param ctx
	 * @param filename
	 * @return
	 */
	public static boolean isSound(ContentContext ctx, String filename) {
		if (filename == null) {
			return false;
		} else {
			String ext = StringHelper.getFileExtension(filename);
			if (StringHelper.isEmpty(ext)) {
				return false;
			} else {
				ext = ext.trim();
				return ctx.getGlobalContext().getStaticConfig().getSoundExtension().contains(ext.toLowerCase());
			}
		}
	}

	public static boolean isResourceURL(ContentContext ctx, String url) {
		if (ctx == null) {
			return url.endsWith("doc");
		}
		String startURL = URLHelper.createResourceURL(ctx, "/");
		String fileURL = URLHelper.createFileURL(ctx, "/");
		String shortURL = URLHelper.createStaticURL(ctx, ElementaryURLHelper.IMG_SERVLET_PATH);
		return url.startsWith(shortURL) || url.startsWith(startURL) || url.startsWith(fileURL);
	}

	public static boolean isTransformURL(ContentContext ctx, String url) throws Exception {

		if (ctx == null) {
			return url.contains("/transform");
		}

		final String FAKE_FILTER = "___FAKE_FILTER___";
		String startURL = URLHelper.createTransformURL(ctx, "/", FAKE_FILTER);
		if (startURL.contains(FAKE_FILTER)) {
			startURL = startURL.substring(0, startURL.indexOf(FAKE_FILTER));
		}
		return url.startsWith(startURL);
	}

	public static Properties loadProperties(File file) throws IOException {
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream in = new FileInputStream(file);
			try {
				properties.load(in);
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		return properties;
	}

	public static void storeProperties(Properties prop, File file) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			prop.store(out, "");
		} finally {
			ResourceHelper.closeResource(out);
		}
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
		if (!file.exists()) {
			return "";
		}
		InputStream in = new FileInputStream(file);
		String content;
		try {
			content = loadStringFromStream(in, ContentContext.CHARSET_DEFAULT);
		} finally {
			closeResource(in);
		}
		return content;
	}

	public static final List<String> loadCollectionFromFile(File file) throws IOException {
		if (!file.exists()) {
			return Collections.EMPTY_LIST;
		}
		List<String> outLines = new LinkedList<String>();
		Reader in = new FileReader(file);

		BufferedReader reader = new BufferedReader(in);
		String line = reader.readLine();
		while (line != null) {
			outLines.add(line);
			line = reader.readLine();
		}
		closeResource(reader);

		return outLines;
	}

	public static final void storeCollectionToFile(File file, List<String> lines) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		Writer in = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(in);
		for (String line : lines) {
			writer.write(line);
			writer.newLine();
		}
		closeResource(writer);
	}

	public static final void appendStringToFile(File file, String line) throws IOException {
		Writer in = new FileWriter(file, true);
		BufferedWriter writer = new BufferedWriter(in);
		writer.write(line);
		writer.newLine();
		closeResource(writer);
	}

	/**
	 * Move a file or a folder to the global trash, depending on staticConfig.
	 *
	 * @param staticConfig
	 * @param fileOrFolder
	 * @return <code>true</code> if origin doesn't exist; or the result of
	 *         {@link File#renameTo(File)}.
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
				version = "" + i;
				i++;
			} while (dest.exists());
			dest.getParentFile().mkdirs();
			return file.renameTo(dest);
		}
		return true;
	}

	/**
	 * remove the data folder directory this method is used for obtain a relative
	 * file path from a ablute file path.
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
	 * remove the path from a string this method is used for obtain a relative file
	 * path from a absolute file path.
	 *
	 * @param path
	 * @return
	 */
	public static String removePath(String path, String pathCuted) {
		pathCuted = StringHelper.cleanPath(pathCuted);
		path = StringHelper.cleanPath(path);
		int indexOfStaticPath = path.indexOf(pathCuted);
		if (indexOfStaticPath < 0) {
			return path;
		}
		return path.substring(indexOfStaticPath + pathCuted.length(), path.length());
	}

	/**
	 * change all the reference to a resource when a resource path or name if
	 * changed
	 *
	 * @param ctx
	 * @param oldName
	 * @param newName
	 * @throws Exception
	 */
	public static void renameResource(ContentContext ctx, File file, File newFile) throws Exception {
		synchronized (ctx.getGlobalContext().getLockLoadContent()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			ContentContext lgCtx = new ContentContext(ctx);
			lgCtx.setRenderMode(ContentContext.EDIT_MODE);
			Collection<String> lgs = globalContext.getContentLanguages();
			for (String lg : lgs) {
				lgCtx.setRequestContentLanguage(lg);
				List<IContentVisualComponent> comps = ComponentFactory.getAllComponentsFromContext(lgCtx);
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
			FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).delete(ctx, fromDataFolder);

			PersistenceService.getInstance(globalContext).setAskStore(true);
		}
	}

	/**
	 * duplicate static info of a resource to a new file
	 *
	 * @param ctx
	 * @param oldName
	 * @param newName
	 * @throws Exception
	 */
	public static void copyResourceData(ContentContext ctx, File file, File newFile) throws Exception {
		synchronized (ctx.getGlobalContext().getLockLoadContent()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			staticInfo.duplicateFile(ctx, newFile);

			// delete old ref in cache
			String fromDataFolder = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
			FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).delete(ctx, fromDataFolder);

			PersistenceService.getInstance(globalContext).setAskStore(true);
		}
	}

	public static boolean deleteResource(ContentContext ctx, File file) throws Exception {
		if (file.isDirectory()) {
			boolean deletedOk = true;
			for (File child : file.listFiles()) {
				deleteResourceData(ctx, file);
				deletedOk = deletedOk && deleteResource(ctx, child);
			}
			FileUtils.deleteDirectory(file);
			return deletedOk;
		} else {
			FileCache.getInstance(ctx.getRequest().getSession().getServletContext()).deleteAllFile(ctx.getGlobalContext().getContextKey(), file.getName());
			deleteResourceData(ctx, file);
			return file.delete();
		}
	}

	public static void deleteResourceData(ContentContext ctx, File file) throws Exception {
		synchronized (ctx.getGlobalContext().getLockLoadContent()) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
			staticInfo.deleteFile(ctx);
			PersistenceService.getInstance(globalContext).setAskStore(true);
		}
	}

	/**
	 * Close streams, writers, readers, etc without any exception even if they are
	 * <code>null</code>.
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

	public static final void writePropertiesToFile(ConfigurationProperties properties, File file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		properties.save(out);
		out.close();
	}

	public static final int writeStreamToFile(InputStream in, File file) throws IOException {
		return writeStreamToFile(in, file, Long.MAX_VALUE);
	}

	public static final int writeStreamToFile(InputStream in, File file, long maxSize) throws IOException {
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
			countByte = writeStreamToStream(in, out, maxSize);
			if (countByte < 0) {
				ResourceHelper.closeResource(out);
				file.delete();
				return -1;
			}
		} finally {
			ResourceHelper.closeResource(out);
		}

		return countByte;
	}

	public static final String writeStreamToString(InputStream in, String encoding) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeStreamToStream(in, out);
		return new String(out.toByteArray(), Charset.forName(encoding));
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

	public static final int writeFileToStream(File fileIn, OutputStream out) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(fileIn);
			return writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public static final File writeFileItemToFolder(FileItem fileItem, File folder, boolean overwrite, boolean rename) throws IOException {
		if (!folder.isDirectory()) {
			return null;
		}
		File file = new File(URLHelper.mergePath(folder.getAbsolutePath(), StringHelper.createFileName(StringHelper.getFileNameFromPath(fileItem.getName()))));

		if (!file.exists()) {
			file.createNewFile();
		} else {
			if (!overwrite && !rename) {
				logger.warning("file allready exisit : " + file);
				throw new FileExistsException("File already exists.");
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
			logger.warning(e.getMessage());
			throw e;
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public static final int writeStreamToStream(InputStream in, OutputStream out) throws IOException {
		return writeStreamToStream(in, out, Long.MAX_VALUE);
	}

	/**
	 * write a InputStream in a OuputStream, without close.
	 *
	 * @return the size of transfered data in byte.
	 */
	/*public static final int writeStreamToStream(InputStream in, OutputStream out, long maxSize) throws IOException {
		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int size = 0;
		int byteReaded = in.read(buffer);
		while (byteReaded >= 0) {
			size = size + byteReaded;
			if (size > maxSize && maxSize > 0) {
				return -1;
			}
			out.write(buffer, 0, byteReaded);
			byteReaded = in.read(buffer);
		}
		return size;
	}*/

	public static final int writeStreamToStream(InputStream in, OutputStream out, long maxSize) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int totalSize = 0;
		int bytesRead;

		while ((bytesRead = in.read(buffer)) != -1) {
			totalSize += bytesRead;
			if (maxSize > 0 && totalSize >= maxSize) {
				return -1;
			}
			out.write(buffer, 0, bytesRead);
		}

		return totalSize;
	}


	/**
	 * Copy the given byte range of the given input to the given output.
	 *
	 * @param input
	 *            The input to copy the given range to the given output for.
	 * @param output
	 *            The output to copy the given range from the given input for.
	 * @param start
	 *            Start of the byte range.
	 * @param length
	 *            Length of the byte range.
	 * @throws IOException
	 *             If something fails at I/O level.
	 */
	public static void copyStream(InputStream input, OutputStream output, long start, long length) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;

		// Write partial range.
		if (start > 0) {
			input.skip(start);
		}
		long toRead = length;

		while ((read = input.read(buffer)) > 0) {
			if ((toRead -= read) > 0) {
				output.write(buffer, 0, read);
			} else {
				output.write(buffer, 0, (int) toRead + read);
				break;
			}
		}

	}

	public static final void writeStringToFile(File file, String content) throws IOException {
		if (!file.exists()) {
			File dir = file.getParentFile();
			if (dir != null && !dir.exists()) {
				dir.mkdirs();
			}
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
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			for (byte element : content) {
				out.write(element);
			}
			out.flush();
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	public static final void writeStringToFile(File file, String content, String encoding) throws IOException {
		if (!StringHelper.isEmpty(content)) {
			if (encoding == null) {
				encoding = ContentContext.CHARACTER_ENCODING;
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream out = new FileOutputStream(file);
			try {
				byte[] contentByte = content.getBytes(encoding);
				out.write(contentByte);
			} finally {
				closeResource(out);
			}
		}
	}

	public static final void writeStringToStream(String content, OutputStream out, String encoding) throws IOException {
		byte[] contentByte = content.getBytes(encoding);
		out.write(contentByte);
	}

	public static final void writeStringToStream(String content, OutputStream out) throws IOException {
		byte[] contentByte = content.getBytes();
		out.write(contentByte);
	}

	/**
	 * return a free file name. if file exist add a number as suffix.
	 *
	 * @param file
	 * @return
	 */
	public synchronized static File getFreeFileName(File file) {
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
		String insideModulePath = URLHelper.mergePath("/" + currentModule.getModuleFolder(), currentModule.getName(), path);
		return insideModulePath;
	}

	public static Serializable loadBeanFromXML(String xml) {
		return loadBeanFromXML(xml, ResourceHelper.class.getClassLoader());
	}

	public static Serializable loadBeanFromXML(File file) throws FileNotFoundException {
		InputStream in = new FileInputStream(file);
		try {
			return loadBeanFromXML(in);
		} finally {
			closeResource(in);
		}
	}

	public static Serializable loadBeanFromXML(InputStream in) {
		Serializable obj;
		XMLDecoder decoder = null;
		try {
			decoder = new XMLDecoder(in, ResourceHelper.class.getClassLoader());
			obj = (Serializable) decoder.readObject();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (decoder != null) {
				decoder.close();
			}
		}
		return null;
	}

	public static Serializable loadBeanFromXML(String xml, ClassLoader cl) {
		Serializable obj;
		InputStream in;
		XMLDecoder decoder = null;
		try {
			in = new ByteArrayInputStream(xml.getBytes(ContentContext.CHARACTER_ENCODING));
			decoder = new XMLDecoder(in, cl);
			obj = (Serializable) decoder.readObject();
			return obj;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			if (decoder != null) {
				decoder.close();
			}
		}
		return null;
	}

	public static String storeBeanFromXML(Serializable bean) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(out, ContentContext.CHARACTER_ENCODING, true, 0);
		// XMLEncoder encoder = new XMLEncoder(out);
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

	public static String storeBean(Serializable bean, OutputStream out) {
		XMLEncoder encoder = null;
		try {
			encoder = new XMLEncoder(out, ContentContext.CHARACTER_ENCODING, true, 0);
			encoder.writeObject(bean);
			encoder.flush();
		} finally {
			if (encoder != null) {
				encoder.close();
			}
		}
		return null;
	}

	public static String storeBean(Serializable bean, File file) throws FileNotFoundException {
		OutputStream out = new FileOutputStream(file);
		XMLEncoder encoder = null;
		try {
			encoder = new XMLEncoder(out, ContentContext.CHARACTER_ENCODING, true, 0);
			encoder.writeObject(bean);
			encoder.flush();
		} finally {
			closeResource(out);
			if (encoder != null) {
				encoder.close();
			}
		}
		return null;
	}

	public static byte[] storeBeanToBin(Serializable bean) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream obj_out = new ObjectOutputStream(out);
		obj_out.writeObject(bean);
		return out.toByteArray();
	}

	public static IIOMetadata getImageMetadata(File image) throws IOException {
		Iterator readers = ImageIO.getImageReadersBySuffix(StringHelper.getFileExtension(image.getName()));
		if (StringHelper.isImage(image.getName())) {
			if (!readers.hasNext()) {
				return null;
			} else {
				ImageReader imageReader = (ImageReader) readers.next();
				FileImageInputStream in = new FileImageInputStream(image);
				try {
					imageReader.setInput(in);
					return imageReader.getImageMetadata(0);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			return null;
		}
	}

	public static boolean writeImageMetadata(IIOMetadata imageMetadata, File target) throws IOException {
		if (imageMetadata == null) {
			return false;
		}
		Iterator writers = ImageIO.getImageWritersBySuffix(StringHelper.getFileExtension(target.getName()));
		if (!writers.hasNext()) {
			return false;
		} else {
			ImageWriter writer = (ImageWriter) writers.next();
			TransactionFile transFile = new TransactionFile(target);
			FileImageOutputStream out = null;
			try {
				out = new FileImageOutputStream(transFile.getTempFile());
				writer.setOutput(out);
				BufferedImage image = ImageIO.read(target);
				IIOImage ioimage = new IIOImage(image, null, imageMetadata);
				writer.write(null, ioimage, null);
				out.close();
				transFile.commit();
			} catch (Exception e) {
				e.printStackTrace();
				if (out != null) {
					out.close();
				}
				transFile.rollback();
			}
			return true;
		}
	}

	public static String excutePost(String targetURL, String urlParameters) {
		return excutePost(targetURL, urlParameters, "application/x-www-form-urlencoded", "en-US", null, null);
	}

	public static String excutePost(String targetURL, String urlParameters, String contentType, String lang, String user, String pwd) {
		URL url;
		HttpURLConnection connection = null;
		BufferedReader rd = null;
		PrintWriter writer = null;

		if (urlParameters == null) {
			urlParameters = "";
		}
		Map<String, String> params = URLHelper.getParams(urlParameters);
		StringBuffer encodedParam = new StringBuffer();
		String sep = "";
		for (Map.Entry<String, String> param : params.entrySet()) {
			encodedParam.append(sep);
			encodedParam.append(param.getKey());
			encodedParam.append("=");
			encodedParam.append(URLEncoder.encode(param.getValue()));
			sep = "&";
		}

		try {
			// Create connection
			url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Content-Language", lang);
			connection.setDoOutput(true);

			// user authentification
			if (user != null && pwd != null) {
				connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64((user + ':' + pwd).getBytes()));
			}

			// Send request
			writer = new PrintWriter(connection.getOutputStream());
			writer.write(encodedParam.toString());
			writer.flush();

			// Get Response
			InputStream is = connection.getInputStream();
			String line;
			rd = new BufferedReader(new InputStreamReader(is));
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			while ((line = rd.readLine()) != null) {
				out.println(line);
			}
			out.close();
			return new String(outStream.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			closeResource(rd, writer);
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * clear a folder list, remove '/' if found as first char and replace '\' by '/'
	 *
	 * @param folders
	 * @return
	 */
	public static List<String> cleanFolderList(Collection<String> folders) {
		List<String> outFolders = new LinkedList<String>();
		for (String folder : folders) {
			if (folder != null) {
				folder = folder.replace('\\', '/');
				while (folder.length() > 0 && folder.startsWith("/")) {
					folder = folder.substring(1);
				}
				outFolders.add(folder);
			} else {
				outFolders.add(null);
			}
		}
		return outFolders;
	}

	public static List<String> removePrefixFromPathList(Collection<? extends Object> pathList, String prefix) {
		List<String> outPathList = new LinkedList<String>();
		prefix = StringHelper.cleanPath(prefix);
		for (Object path : pathList) {
			outPathList.add(StringUtils.replaceOnce(StringHelper.cleanPath(path.toString()), prefix, ""));
		}
		return outPathList;
	}

	public static String changeExtention(String filename, String newext) {
		if (filename == null || newext == null) {
			return filename;
		} else {
			String currentExt = StringHelper.getFileExtension(filename);
			if (currentExt == null || currentExt.trim().length() == 0) {
				return filename + '.' + newext;
			} else {
				return filename.substring(0, filename.length() - (currentExt.length() + 1)) + '.' + newext;
			}
		}
	}

	public static void writeUrlToFile(URL url, File imageFile) throws IOException {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			URLConnection conn = url.openConnection();
			// skip https validation
			if (conn instanceof HttpsURLConnection) {
				try {
					NetHelper.nocheckCertificatHttps();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(!imageFile.getParentFile().exists()) {
				imageFile.getParentFile().mkdirs();
			}
				conn.setRequestProperty("User-Agent", NetHelper.JAVLO_USER_AGENT);
			in = conn.getInputStream();
			out = new FileOutputStream(imageFile);
			writeStreamToStream(in, out);
			out.flush();
		} finally {
			safeClose(in, out);
		}
	}

	public static String fileStructureToHtml(File file) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<ul class=\"file-structure\">");
		fileStructureToHtml(out, file);
		out.println("</ul>");
		out.close();
		return new String(outStream.toByteArray());
	}

	private static long fileStructureToHtml(PrintStream out, File file) {
		long size = 0;
		if (file.isFile()) {
			out.println("<li class=\"file\">" + file.getName() + "[" + StringHelper.renderSize(file.length()) + "]</li>");
			return file.length();
		} else {
			out.println("<li class=\"folder\">" + file.getName());
			out.println("<ul>");
			for (File child : file.listFiles()) {
				size = size + fileStructureToHtml(out, child);
			}
			out.println("</ul></li>");
		}
		return size;
	}

	public static boolean canModifFolder(ContentContext ctx, String folder) {
		boolean canModif = AdminUserSecurity.isCurrentUserCanUpload(ctx);
		if (!canModif) {
			try {
				String importFolder = URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getImportFolder(), DataAction.createImportFolder(ctx.getCurrentPage()));
				importFolder = URLHelper.cleanPath(importFolder, false);
				if (URLHelper.cleanPath(folder, false).contains(importFolder)) {
					canModif = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return canModif;
	}

	/**
	 * delete import without reference to a existing page.
	 *
	 * @param ctx
	 * @return nomber of deleted import folder.
	 * @throws Exception
	 */
	public static int cleanImportResources(ContentContext ctx) throws Exception {
		File importImageFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getImageFolder(), ctx.getGlobalContext().getStaticConfig().getImportFolder()));
		File importFileFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getFileFolder(), ctx.getGlobalContext().getStaticConfig().getImportFolder()));
		File importGallryFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getGalleryFolder(), ctx.getGlobalContext().getStaticConfig().getImportFolder()));
		int deleted = 0;
		deleted = cleanImportResources(ctx, importImageFolder);
		deleted += cleanImportResources(ctx, importFileFolder);
		deleted += cleanImportResources(ctx, importGallryFolder);
		return deleted;
	}

	public static boolean isImportPageExist(ContentContext ctx, File childImport) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		for (MenuElement navChild : root.getAllChildrenList()) {
			String folderName = DataAction.createImportFolder(navChild.getName());
			if (childImport.getName().equals(folderName)) {
				return true;
			}
		}
		return false;
	}

	public static int cleanImportResource(ContentContext ctx, File childImport) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		int deleted = 0;
		if (childImport.isDirectory() && !childImport.getName().equals(root.getName())) {
			boolean deleteFile = !isImportPageExist(ctx, childImport);
			if (deleteFile) {
				logger.info("delete folder (user:" + ctx.getCurrentEditUser() + " context:" + ctx.getGlobalContext().getContextKey() + ") : " + childImport);
				try {
					deleteResource(ctx, childImport);
					deleted++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (childImport.isFile()) {
			if (!isImportPageExist(ctx, childImport.getParentFile())) {
				deleteResource(ctx, childImport);
				deleted++;
				if (childImport.getParentFile().listFiles().length == 0) {
					deleteResource(ctx, childImport.getParentFile());
				}
			}
		}
		return deleted;
	}

	private static int cleanImportResources(ContentContext ctx, File importFolder) throws Exception {
		if (!importFolder.exists()) {
			logger.warning("folder not found : " + importFolder);
			return 0;
		} else {
			int deleted = 0;
			ContentService content = ContentService.getInstance(ctx.getRequest());
			MenuElement root = content.getNavigation(ctx);
			if (root.getChildList().length == 0) {
				throw new Exception("you need at least one page for clean import file.");
			}
			for (File child : importFolder.listFiles()) {
				deleted += cleanImportResource(ctx, child);
			}
			return deleted;
		}
	}

	public static List<IContentVisualComponent> getComponentsUseResource(ContentContext ctx, String uri) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<IContentVisualComponent> outList = new LinkedList<IContentVisualComponent>();
		ContentContext lgCtx = new ContentContext(ctx);
		for (String lg : ctx.getGlobalContext().getContentLanguages()) {
			lgCtx.setAllLanguage(lg);
			for (IContentVisualComponent comp : content.getAllContent(lgCtx)) {
				if (comp instanceof IStaticContainer) {
					IStaticContainer container = (IStaticContainer) comp;
					if (container.contains(lgCtx, uri)) {
						outList.add(comp);
					}
				}
			}
		}
		return outList;
	}

	public static boolean isComponentsUseResource(ContentContext ctx, String uri) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		for (IContentVisualComponent comp : content.getAllContent(ctx)) {
			if (comp instanceof IStaticContainer) {
				IStaticContainer container = (IStaticContainer) comp;
				if (container.contains(ctx, uri)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isAcceptedImage(ContentContext ctx, String fileName) {
		if (StringHelper.isEmpty(fileName)) {
			return true;
		} else {
			String extension = ',' + ctx.getGlobalContext().getStaticConfig().getImageFormat() + ',';
			return extension.contains(',' + StringHelper.getFileExtension(fileName).toLowerCase() + ',');
		}
	}

	public static boolean isAcceptedVideo(ContentContext ctx, String fileName) {
		if (StringHelper.isEmpty(fileName)) {
			return true;
		} else {
			String extension = ',' + ctx.getGlobalContext().getStaticConfig().getVideoFormat() + ',';
			return extension.contains(',' + StringHelper.getFileExtension(fileName).toLowerCase() + ',');
		}
	}

	public static boolean isAcceptedDocument(ContentContext ctx, String fileName) {
		if (StringHelper.isEmpty(fileName)) {
			return true;
		} else {
			return ctx.getGlobalContext().getStaticConfig().getDocumentExtension().contains(StringHelper.getFileExtension(fileName).toLowerCase());
		}
	}

	public static String getRealPath(ServletContext application, String path) {
		String rootPath = application.getRealPath("/");
		return URLHelper.mergePath(rootPath, path);
	}

	/**
	 * normalize all file name from a dir
	 *
	 * @param file
	 * @return true one file name has changed
	 * @throws IOException
	 */
	public static boolean cleanAllFileName(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException("" + file);
		}
		boolean done = false;
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				done = cleanAllFileName(child) || done;
			}
		}
		String normalizedName = StringHelper.createFileName(file.getName());
		if (!normalizedName.equals(file.getName())) {
			File newFile = new File(URLHelper.mergePath(file.getParentFile().getCanonicalPath(), normalizedName));
			newFile = getFreeFileName(newFile);
			file.renameTo(newFile);
			done = true;
		}
		return done;
	}

	public static String getRelativeStaticURL(ContentContext ctx, File file) {
		String fullURL = URLHelper.cleanPath(file.getPath(), false);
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = ctx.getGlobalContext();
		String fullStaticFolder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());
		if (ResourceHelper.isTemplateFile(globalContext, file)) {
			fullStaticFolder = staticConfig.getTemplateFolder();
		} else if (!file.getAbsolutePath().contains('/' + staticConfig.getStaticFolder() + '/')) {
			fullStaticFolder = globalContext.getDataFolder();
		}
		fullStaticFolder = URLHelper.cleanPath(fullStaticFolder, false);
		String relURL = "/";
		if (fullURL.length() > fullStaticFolder.length()) {
			relURL = StringUtils.replace(fullURL, fullStaticFolder, "");
		}
		return relURL;
	}

	public static int countLines(File file) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

	public static boolean deleteFolder(File folder) {
		boolean out = false;
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					out = f.delete();
				}
			}
		}
		out = folder.delete();
		return out;
	}

	public static String mimifyJS(String js) {
		Minifier minifier = Minifier.forFileName("script.js");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		minifier.minify(new ByteArrayInputStream(js.getBytes()), out, ContentContext.CHARSET_DEFAULT, null);
		return new String(out.toByteArray());
	}

	public static void docx2html(File docx, File html) throws IOException {
		InputStream in = new FileInputStream(docx);
		XWPFDocument document = new XWPFDocument(in);
		in.close();

		XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(new File(html.getParentFile().getAbsolutePath())));
		OutputStream out = new FileOutputStream(html);
		XHTMLConverter.getInstance().convert(document, out, options);
		out.close();

	}

	public static String sha512(File file) throws IOException, IllegalArgumentException {
		try (InputStream in = new FileInputStream(file)) {
			return sha(in, "SHA-512");
		}
	}

	public static String sha512(final InputStream in) throws IOException, IllegalArgumentException {
		return sha(in, "SHA-512");
	}

	public static String sha256(final InputStream in) throws IOException, IllegalArgumentException {
		return sha(in, "SHA-256");
	}

	private static String sha(final InputStream in, String algo) throws IOException, IllegalArgumentException {
		final int BUFFER_SIZE = 1024 * 1024;
		Objects.requireNonNull(in);
		try {
			final byte[] buf = new byte[BUFFER_SIZE];
			final MessageDigest messageDigest = MessageDigest.getInstance(algo);
			int bytesRead;
			while ((bytesRead = in.read(buf)) != -1) {
				messageDigest.update(buf, 0, bytesRead);
			}
			return new String(java.util.Base64.getEncoder().encode(messageDigest.digest()));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 hashing algorithm unknown in this VM.", e);
		}
	}

	public static void main(String[] args) throws IOException {





//		File docx = new File("c:/trans/test_javlo2.docx");
//		File html = new File("c:/trans/test_javlo2.html");
//		docx2html(docx, html);
//		System.out.println("done : " + html);

		// File file = new File("c:/trans/changelog.txt");
		// File target = new File("c:/trans/changelog.md");
		//
		// PrintStream out = new PrintStream(new FileOutputStream(target));
		// try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		// String line;
		// while ((line = br.readLine()) != null) {
		// if (!StringHelper.isEmpty(line)) {
		// if (line.startsWith("*")) {
		// if (line.contains("[")) {
		// line = line.replace("[", "- ");
		// line = line.replace("]", "");
		// line = "## [" + line.substring(2).replaceFirst(" ", "] ");
		// } else {
		// line = "## [" + line.substring(2).replaceFirst(" ", "] - ");
		// }
		// out.println("");
		// out.println(line);
		// out.println("### Added");
		// } else if (line.startsWith(" *")) {
		// line = line.replaceFirst(" \\*", "-");
		// out.println(line);
		// }
		// }
		// }
		// }
		// out.close();

		// System.out.println(">>>>>>>>> ResourceHelper.mimifyJS : " + mimifyJS("var
		// js='test'; \njs='test2';")); // TODO: remove debug trace

	}

}
