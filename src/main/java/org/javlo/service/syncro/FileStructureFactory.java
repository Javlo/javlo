package org.javlo.service.syncro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class FileStructureFactory {

	private static final String DELETED_FILES_FILENAME = ".structure_deleted_files.properties";
	private static final String KNOWN_FILES_FILENAME = ".structure_known_files.properties";
	private static final Logger logger = Logger.getLogger(FileStructureFactory.class.getName());
	private static final String CHARACTER_ENCODING = "UTF-8";

	private static Map<File, WeakReference<FileStructureFactory>> instances = new HashMap<File, WeakReference<FileStructureFactory>>();

	public static FileStructureFactory getInstance(File baseFolder) {
		WeakReference<FileStructureFactory> ref = instances.get(baseFolder);
		FileStructureFactory instance = null;
		if (ref != null) {
			instance = ref.get();
		}
		if (instance == null) {
			instance = new FileStructureFactory(baseFolder);
			ref = new WeakReference<FileStructureFactory>(instance);
			instances.put(baseFolder, ref);
			LangHelper.clearWeekReferenceMap(instances);
		}
		return instance;
	}

	private File baseFolder;
	private Properties deletedDates;
	protected FileStructureFactory(File baseFolder) {
		this.baseFolder = baseFolder;
		deletedDates = new Properties();

		File file = getDeletedFilesFile();
		if (file.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				deletedDates.load(in);
			} catch (IOException ex) {
			} finally {
				ResourceHelper.safeClose(in);
			}

		}
	}

	private void save() throws IOException {
		synchronized (deletedDates) {
			File deletedFilesFile = getDeletedFilesFile();
			if (deletedDates.size() == 0) {
				if (deletedFilesFile.exists()) {
					deletedFilesFile.delete();
				}
			} else {
				deletedFilesFile.getParentFile().mkdirs();
				FileOutputStream out = new FileOutputStream(deletedFilesFile);
				deletedDates.store(out, null);
				out.close();
			}
		}
	}

	private String getKey(File file) {
		return URLHelper.getRelativePath(baseFolder.getAbsolutePath(), file.getAbsolutePath());
	}

	private File getDeletedFilesFile() {
		return new File(URLHelper.mergePath(baseFolder.getAbsolutePath(), DELETED_FILES_FILENAME));
	}

	private File getKnownFilesFile() {
		return new File(URLHelper.mergePath(baseFolder.getAbsolutePath(), KNOWN_FILES_FILENAME));
	}

	public final void markAsDeleted(File file) throws IOException {
		synchronized (deletedDates) {
			deletedDates.put(getKey(file), StringHelper.renderFileTime(new Date()));
			save();
		}
	}

	public final boolean isMarkedAsDeleted(File file) throws IOException {
		return deletedDates.getProperty(getKey(file)) != null;
	}

	@Deprecated
	public final String fileTreeToProperties() throws IOException {
		return asString(fileTreeToList(false, false));
	}

	public Map<String, FileInfo> fileTreeToMap(boolean useKnownFiles, boolean includeDeleted) throws IOException {
		return asMapByPath(fileTreeToList(useKnownFiles, includeDeleted));
	}
	public List<FileInfo> fileTreeToList(boolean useKnownFiles, boolean includeDeleted) throws IOException {
		synchronized (deletedDates) {
			File knownFilesFile = getKnownFilesFile();
			Map<String, FileInfo> previousKnownFiles = null;
			if (useKnownFiles) {
				//Load previous known files
				if (knownFilesFile.exists()) {
					FileInputStream in = null;
					try {
						in = new FileInputStream(knownFilesFile);
						previousKnownFiles = readFromStream(in);
					} finally {
						ResourceHelper.safeClose(in);
					}
				}
			}
			if (previousKnownFiles == null) {
				previousKnownFiles = new HashMap<String, FileInfo>();
			}

			//Generate structure (and remove recreated files from deleted)
			List<FileInfo> out = new LinkedList<FileInfo>();
			File[] children = baseFolder.listFiles();
			if (children != null) {
				for (File child : children) {
					traverse("", child, out, previousKnownFiles);
				}
			}

			if (useKnownFiles) {
				//Save new known files
				OutputStream outStream = null;
				try {
					outStream = new FileOutputStream(knownFilesFile);
					outStream = new BufferedOutputStream(outStream);
					writeToStream(out, outStream);
				} finally {
					ResourceHelper.safeClose(outStream);
				}
			}

			if (includeDeleted && useKnownFiles) {
				//Detect new deleted files
				Date deletedDate = new Date();
				Map<String, FileInfo> newKnownFiles = asMapByPath(out);
				for (Entry<String, FileInfo> entry : previousKnownFiles.entrySet()) {
					if (!newKnownFiles.containsKey(entry.getKey())) {
						FileInfo fi = entry.getValue();
						if (!deletedDates.containsKey(fi.getPath())) {
							deletedDates.setProperty(fi.getPath(), StringHelper.renderFileTime(deletedDate));
						}
					}
				}

				//List deleted files in structure
				for (Object o : deletedDates.keySet()) {
					String fileRef = (String) o;
					try {
						Date date = StringHelper.parseFileTime(deletedDates.getProperty(fileRef));
						out.add(FileInfo.getDeletedFileInfo(fileRef, date.getTime()));
					} catch (ParseException ex) {
						logger.warning("Error parsing date in '" + DELETED_FILES_FILENAME + "': " + ex.getMessage());
					}
				}
			}

			save();
			return out;
		}
	}

	private final void traverse(String parentRelativePath, File file, List<FileInfo> out, Map<String, FileInfo> previousKnownFiles) throws IOException {
		if (DELETED_FILES_FILENAME.equals(file.getName()) || KNOWN_FILES_FILENAME.equals(file.getName())) {
			return;
		}
		String relativePath = parentRelativePath + '/' + file.getName();
		deletedDates.remove(relativePath);
		FileInfo previousInfo = previousKnownFiles.get(relativePath);
		String checksum = null;
		if (previousInfo != null) {
			if (previousInfo.getChecksum() != null && previousInfo.getModificationDate() == file.lastModified()) {
				checksum = previousInfo.getChecksum();
			}
		}
		out.add(FileInfo.getFileInfo(relativePath, file, checksum));
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {				
				traverse(relativePath, child, out, previousKnownFiles);
			}
		}
	}

	public static Map<String, FileInfo> asMapByPath(List<FileInfo> list) {
		Map<String, FileInfo> out = new HashMap<String, FileInfo>();
		for (FileInfo info : list) {
			out.put(info.getPath(), info);
		}
		return out;
	}

	public static Map<String, FileInfo> cloneMap(Map<String, FileInfo> map) {
		Map<String, FileInfo> out = new HashMap<String, FileInfo>();
		for (Entry<String, FileInfo> entry : map.entrySet()) {
			FileInfo fi = entry.getValue();
			out.put(entry.getKey(), fi.clone());
		}
		return out;
	}

	@Deprecated
	private static String asString(Collection<FileInfo> list) {
		StringWriter writer = null;
		try {
			writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			for (FileInfo fileInfo : list) {
				out.println(StringHelper.getPropertieskey(fileInfo.getPath()) + '=' + fileInfo.toRaw());
			}
			return writer.toString();
		} finally {
			ResourceHelper.safeClose(writer);
		}
	}

	public static Map<String, FileInfo> readFromStream(InputStream in) throws IOException {
		InputStreamReader reader = null;
		try {
			in = new BufferedInputStream(in);
			reader = new InputStreamReader(in, CHARACTER_ENCODING);
			Properties props = new Properties();
			props.load(reader);
			Map<String, FileInfo> out = new HashMap<String, FileInfo>();
			for (Entry<Object, Object> entry : props.entrySet()) {
				FileInfo fi = new FileInfo((String) entry.getValue());
				out.put(fi.getPath(), fi);
			}
			return out;
		} finally {
			ResourceHelper.safeClose(reader);
		}
	}

	public static void writeToStream(Collection<FileInfo> list, OutputStream outStream) throws IOException {
		outStream.write(asString(list).getBytes(CHARACTER_ENCODING));
	}

	public static void main(String[] args) {
		FileStructureFactory fsf = getInstance(new File("C:/Apache/Tomcat 6.0/webapps/dc/WEB-INF/data-ctx/ctx-121395557182868827380"));
		try {
			System.out.println(fsf.fileTreeToProperties());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
