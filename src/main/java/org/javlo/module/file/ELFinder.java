package org.javlo.module.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Benoit Dumont de Chassart
 *
 */
public class ELFinder { 
 
	private static final String PROTOCOL_VERSION = "2.0";
	private static final String HASH_ENCODING = "UTF-8";
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	private static final String VOLUME_SEPARATOR = "_";

	private Map<String, String> MIME_TYPES;

	private Map<String, Volume> volumes;
 
	public ELFinder(String rootPath, String resourcePath) {
		Volume volume = new Volume();
		volume.id = "AB";
		volume.root = new File(rootPath);
		this.volumes = new HashMap<String, Volume>();
		this.volumes.put(volume.id, volume);
		loadMimeTypes(resourcePath);
		System.out.println(rootPath);
	}

	public void process(Writer out, HttpServletRequest request, HttpServletResponse response) throws IOException {

		Map<String, Object> apiResponse = new LinkedHashMap<String, Object>();

		String command = request.getParameter("cmd");
		if ("open".equals(command)) {
			open(getBoolean(request, "init", false), getFile(request, "target"), getBoolean(request, "tree", false), apiResponse);
		} else if ("parents".equals(command)) {
			parents(getFile(request, "target"), apiResponse);
		} else if ("tree".equals(command)) {
			tree(getFile(request, "target"), apiResponse);
		}

		JSONSerializer.writeJSONString(apiResponse, out);

		//out.write("{\"error\" : \"Invalid backend configuration\"}");
	}

	private FileObject getFile(HttpServletRequest request, String name) {
		String hash = getString(request, name, null);
		if (hash != null && !hash.isEmpty()) {
			return hashToFile(hash);
		}
		return null;
	}

	private String getString(HttpServletRequest request, String name, String defaultValue) {
		return request.getParameter(name);
	}

	private Boolean getBoolean(HttpServletRequest request, String name, Boolean defaultValue) {
		String str = getString(request, name, null);
		if (str != null) {
			if ("1".equals(str)) {
				return true;
			} else {
				return false;
			}
		}
		return defaultValue;
	}

	private void loadMimeTypes(String resourcePath) {
		try {
			Map<String, String> mimes = new HashMap<String, String>();
			InputStream in = new FileInputStream(resourcePath + "/mime-types.properties");
			Properties p = new Properties();
			p.load(in);
			for (Entry<Object, Object> entry : p.entrySet()) {
				String mime = (String) entry.getKey();
				String[] extensions = ((String) entry.getValue()).split("\\s");
				for (String extension : extensions) {
					mimes.put(extension.toLowerCase(), mime);
				}
			}
			MIME_TYPES = mimes;
			in.close();
		} catch (IOException ex) {
			throw new RuntimeException("Forwarded exception.", ex);
		}
	}

	public void open(boolean init, FileObject target, boolean tree, Map<String, Object> response) {
		System.out.println("open - target:" + target);
		if (target == null) {
			Volume volume = volumes.values().iterator().next();
			target = new FileObject(volume, volume.root);
		}
		response.put("cwd", printFile(target));
		response.put("options", printOptions(target));
		Set<FileObject> files = new LinkedHashSet<FileObject>();
		files.addAll(target.getChildren());
		if (tree) {
			files.addAll(volumeFiles());
			FileObject parent = target.getParentFile();
			while (parent != null) {
				files.addAll(parent.getChildren());
				parent = parent.getParentFile();
			}
		}
		System.out.println("ListFiles:");
		response.put("files", printFiles(files));
		if (init) {
			extend(response,
					prop("api", PROTOCOL_VERSION),
					prop("uplMaxSize", "32M"));
		}
	}

	public void parents(FileObject target, Map<String, Object> response) {
//		System.out.println("parents - target:" + target);
//		if (target.isRoot()) {
//			response.put("tree", listFiles(volumeFiles()));
//		} else {
//			List<FileObject> treeFiles = new ArrayList<FileObject>();
//			FileObject parent = target.getParentFile();
//			for (FileObject child : parent.getChildren()) {
//				if (child.isDirectory() && !child.equals(target)) {
//					treeFiles.add(child);
//				}
//			}
//			treeFiles.add(0, target);
//			while (parent != null) {
//				treeFiles.add(0, parent);
//				parent = parent.getParentFile();
//			}
//			response.put("tree", listFiles(treeFiles));
//		}
	}

	public void tree(FileObject target, Map<String, Object> response) {
		System.out.println("tree - target:" + target);
		List<FileObject> treeFiles = new ArrayList<FileObject>();
		treeFiles.add(target);
		treeFiles.addAll(filterDirectories(target.getChildren()));
		response.put("tree", printFiles(treeFiles));
	}

	private List<FileObject> volumeFiles() {
		List<FileObject> volumeFiles = new ArrayList<FileObject>();
		for (Volume volume : volumes.values()) {
			volumeFiles.add(new FileObject(volume, volume.root));
		}
		return volumeFiles;
	}

	private Map<String, Object> printFile(FileObject file) {
		System.out.println("listFile: " + file.getRelativePath());
		Map<String, Object> out = obj(
				// (String) name of file/dir. Required
				prop("name", file.file.getName()),
				// (String) hash of current file/dir path, first symbol must be letter, symbols before _underline_ - volume id, Required.
				prop("hash", fileToHash(file)),
				// (String) mime type. Required.
				prop("mime", getMimeType(file.file)),
				// (Number) file modification time in unix timestamp. Required.
				prop("ts", file.file.lastModified()),
				// (Number) file size in bytes
				prop("size", file.file.length()),
				// (Number) is readable
				prop("read", toInt(true)),
				// (Number) is writable
				prop("write", toInt(false)),
				// (Number) is file locked. If locked that object cannot be deleted and renamed
				prop("locked", toInt(false)));

//		if (isSymbLink(file)) {
//			extend(out,
//					// (String) For symlinks only. Symlink target path.
//					prop("alias", "files/images"),
//					// (String) For symlinks only. Symlink target hash.
//					prop("thash", "l1_c2NhbnMy"));
//		}

//		if (isImage(file)) {
//			extend(out,
//					// (String) Only for images. Thumbnail file name, if file do not have thumbnail yet, but it can be generated than it must have value "1"
//					prop("tmb", "bac0d45b625f8d4633435ffbd52ca495.png"),
//					// (String) For images - file dimensions. Optionally.
//					prop("dim", "640x480"));
//		}

		// (Number) Only for directories. Marks if directory has child directories inside it. 0 (or not set) - no, 1 - yes. Do not need to calculate amount.
		if (file.isDirectory()) {
			List<FileObject> children = file.getChildren();
			extend(out, prop("childs", toInt(children.size() > 0)));
			List<FileObject> childDirectories = filterDirectories(children);
			extend(out, prop("dirs", toInt(childDirectories.size() > 0)));
		}
		if (file.isRoot()) {
			// (String) Volume id. For root dir only.
			extend(out, prop("volumeid", file.volume.id));
		} else {
			// (String) hash of parent directory. Required except roots dirs.
			extend(out, prop("phash", fileToHash(file.getParentFile())));
		}
		return out;
	}

	private Map<String, Object> printOptions(FileObject fil) {
		return obj(
				prop("path", fil.getRelativePath()),// (String) Current folder path
				prop("url", "http://localhost/elfinder/files/folder42/"),// (String) Current folder URL
				prop("tmbURL", "http://localhost/elfinder/files/folder42/.tmb/"),// (String) Thumbnails folder URL
				prop("separator", "/"), // (String) Разделитель пути для текущего тома
				prop("disabled", array()), // (Array) List of commands not allowed (disabled) on this volume
				prop("copyOverwrite", 1), // (Number) Разрешена или нет перезапись файлов с одинаковыми именами на текущем томе
				propObj("archivers", // (Object) Настройки архиваторов
						prop("create", array() // (Array)  Список mime типов архивов, которые могут быть созданы
						),
						prop("extract", array() // (Array)  Список mime типов архивов, которые могут быть распакованы
						)
				));
	}

	protected List<Object> printFiles(Collection<FileObject> files) {
		List<Object> out = new ArrayList<Object>();
		for (FileObject file : files) {
			out.add(printFile(file));
		}
		return out;
	}

	private Object toInt(boolean b) {
		return b ? 1 : 0;
	}

	private String getMimeType(File file) {
		String mime = null;
		if (file.isDirectory()) {
			mime = "directory";
		} else {
			String fileName = file.getName();
			int pos = fileName.lastIndexOf('.');
			while (pos >= 0) {
				String extension = fileName.substring(pos + 1);
				String fileMime = MIME_TYPES.get(extension.toLowerCase());
				//System.out.println(extension + " = " + fileMime);
				if (fileMime != null) {
					mime = fileMime;
				}
				pos = fileName.lastIndexOf('.', pos - 1);
			}
			if (mime == null) {
				mime = DEFAULT_MIME_TYPE;
			}
		}
		return mime;
	}

	public FileObject hashToFile(String hash) {
		int pos = hash.indexOf(VOLUME_SEPARATOR);
		String volumeId = hash.substring(0, pos);

		Volume volume = volumes.get(volumeId);
		if (volume == null) {
			throw new RuntimeException("Volume not found.");
		}
		String subPathEnc = hash.substring(pos + VOLUME_SEPARATOR.length());
		String path = decode(subPathEnc);
		return new FileObject(volume, path);
	}

	private static String fileToHash(FileObject file) {
		String subPath = file.file.getPath().substring(file.volume.root.getPath().length());
		return file.volume.id + VOLUME_SEPARATOR + encode(subPath);
	}

	private static String encode(String str) {
		try {
			return URLEncoder.encode(str, HASH_ENCODING)
					.replace("_", "%5F")
					.replace("+", "%20")
					.replace("-", "%2D")
					.replace('%', '-');
			//return new BASE64Encoder().encode(str.getBytes(HASH_ENCODING));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Forwarded exception.", ex);
		}
	}

	private String decode(String str) {
		try {
			return URLDecoder.decode(str.replace('-', '%'), HASH_ENCODING);
			//return new String(new BASE64Decoder().decodeBuffer(str), HASH_ENCODING);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Forwarded exception.", ex);
		} catch (IOException ex) {
			throw new RuntimeException("Forwarded exception.", ex);
		}
	}

	private static List<FileObject> filterDirectories(List<FileObject> children) {
		List<FileObject> out = new ArrayList<FileObject>();
		for (FileObject child : children) {
			if (child.isDirectory()) {
				out.add(child);
			}
		}
		return out;
	}

	private <T> T[] array(T... values) {
		return values;
	}

	private Map<String, Object> obj(Property... props) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		for (Property prop : props) {
			out.put(prop.name, prop.value);
		}
		return out;
	}

	private void extend(Map<String, Object> base, Property... props) {
		for (Property prop : props) {
			base.put(prop.name, prop.value);
		}
	}

	private Property prop(String name, Object value) {
		return new Property(name, value);
	}
	private Property propObj(String name, Property... props) {
		return new Property(name, obj(props));
	}

	public static class Property {
		private final String name;
		private final Object value;

		public Property(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

	public static class Volume {
		private File root;
		private String id;
	}

	public static class FileObject {
		private Volume volume;
		private File file;
		public FileObject(Volume volume, String path) {
			this.volume = volume;
			this.file = new File(volume.root, path);
		}

		public FileObject(Volume volume, File file) {
			this.volume = volume;
			this.file = file;
		}

		public List<FileObject> getChildren() {
			List<FileObject> children = new ArrayList<FileObject>();
			File[] array = file.listFiles();
			if (array != null) {
				for (File child : array) {
					children.add(new FileObject(volume, child));
				}
			}
			return children;
		}

		public FileObject getParentFile() {
			if (isRoot()) {
				return null;
			} else {
				return new FileObject(volume, file.getParentFile());
			}
		}

		public boolean isRoot() {
			return file.equals(volume.root);
		}

		public boolean isDirectory() {
			return file.isDirectory();
		}

		public String getRelativePath() {
			String path = "";
			FileObject parent = getParentFile();
			if (parent != null) {
				path += parent.getRelativePath() + "/";
			}
			path += file.getName();
			return path;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (!(obj instanceof FileObject)) {
				return false;
			} else {
				return obj.hashCode() == this.hashCode();
			}
		}

		@Override
		public int hashCode() {
			return fileToHash(this).hashCode();
		}

		@Override
		public String toString() {
			return file.toString();
		}
	}

}
