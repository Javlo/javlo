package org.javlo.module.file;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Benoit Dumont de Chassart
 * 
 */
public abstract class ELFinder {

	private static final String PROTOCOL_VERSION = "2.0";
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public void process(Writer out, HttpServletRequest request, HttpServletResponse response) throws IOException {

		Map<String, Object> apiResponse = new LinkedHashMap<String, Object>();
		try {
			String command = request.getParameter("cmd");
			if ("open".equals(command)) {
				open(getBoolean(request, "init", false), getFile(request, "target"), getBoolean(request, "tree", false), apiResponse);
			} else if ("parents".equals(command)) {
				parents(getFile(request, "target"), apiResponse);
			} else if ("tree".equals(command)) {
				tree(getFile(request, "target"), apiResponse);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			apiResponse.clear();
			apiResponse.put("error", "Exception: " + ex.toString());
		}

		JSONSerializer.writeJSONString(apiResponse, out);

	}

	private ELFile getFile(HttpServletRequest request, String name) {
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

	public void open(boolean init, ELFile target, boolean tree, Map<String, Object> response) {
		System.out.println("open - target:" + target);
		if (target == null) {
			ELVolume volume = getVolumes().iterator().next();
			target = volume.getRoot();
		}
		response.put("cwd", printFile(target));
		response.put("options", printOptions(target));
		Set<ELFile> files = new LinkedHashSet<ELFile>();
		files.addAll(target.getChildren());
		if (tree) {
			files.addAll(getVolumeFiles());
			ELFile parent = target.getParentFile();
			while (parent != null) {
				files.addAll(parent.getChildren());
				parent = parent.getParentFile();
			}
		}
		response.put("files", printFiles(files));
		if (init) {
			extend(response, prop("api", PROTOCOL_VERSION), prop("uplMaxSize", "32M"));
		}
	}

	protected abstract List<ELVolume> getVolumes();

	public void parents(ELFile target, Map<String, Object> response) {
		// System.out.println("parents - target:" + target);
		// if (target.isRoot()) {
		// response.put("tree", listFiles(volumeFiles()));
		// } else {
		// List<FileObject> treeFiles = new ArrayList<FileObject>();
		// FileObject parent = target.getParentFile();
		// for (FileObject child : parent.getChildren()) {
		// if (child.isDirectory() && !child.equals(target)) {
		// treeFiles.add(child);
		// }
		// }
		// treeFiles.add(0, target);
		// while (parent != null) {
		// treeFiles.add(0, parent);
		// parent = parent.getParentFile();
		// }
		// response.put("tree", listFiles(treeFiles));
		// }
	}

	public void tree(ELFile target, Map<String, Object> response) {
		System.out.println("tree - target:" + target);
		List<ELFile> treeFiles = new ArrayList<ELFile>();
		treeFiles.add(target);
		treeFiles.addAll(filterDirectories(target.getChildren()));
		response.put("tree", printFiles(treeFiles));
	}

	private List<ELFile> getVolumeFiles() {
		List<ELFile> volumeFiles = new ArrayList<ELFile>();
		for (ELVolume volume : getVolumes()) {
			volumeFiles.add(volume.getRoot());
		}
		return volumeFiles;
	}

	protected Map<String, Object> printFile(ELFile file) {
		Map<String, Object> out = obj(
				// (String) name of file/dir. Required
				prop("name", file.getFile().getName()),
				// (String) hash of current file/dir path, first symbol must be letter, symbols before _underline_ - volume id, Required.
				prop("hash", fileToHash(file)),
				// (String) mime type. Required.
				prop("mime", getMimeType(file.getFile())),
				// (Number) file modification time in unix timestamp. Required.
				prop("ts", file.getFile().lastModified()),
				// (Number) file size in bytes
				prop("size", file.getFile().length()),
				// (Number) is readable
				prop("read", toInt(true)),
				// (Number) is writable
				prop("write", toInt(false)),
				// (Number) is file locked. If locked that object cannot be deleted and renamed
				prop("locked", toInt(false)));

		// (Number) Only for directories. Marks if directory has child directories inside it. 0 (or not set) - no, 1 - yes. Do not need to calculate amount.
		if (file.isDirectory()) {
			List<ELFile> children = file.getChildren();
			extend(out, prop("childs", toInt(children.size() > 0)));
			List<ELFile> childDirectories = filterDirectories(children);
			extend(out, prop("dirs", toInt(childDirectories.size() > 0)));
		}
		if (file.isRoot()) {
			// (String) Volume id. For root dir only.
			extend(out, prop("volumeid", file.getVolume().getId()));
		} else {
			// (String) hash of parent directory. Required except roots dirs.
			extend(out, prop("phash", fileToHash(file.getParentFile())));
		}
		return out;
	}

	protected Map<String, Object> printOptions(ELFile file) {
		return obj(prop("path", file.getRelativePath()),// (String) Current folder path
				prop("url", file.getURL()),// (String) Current folder URL
				prop("tmbURL", file.getThumbnailURL()),// (String) Thumbnails folder URL
				prop("separator", "/"),
				prop("disabled", array()), // (Array) List of commands not allowed (disabled) on this volume
				prop("copyOverwrite", 1),
				propObj("archivers", prop("create", array()), prop("extract", array())));
	}

	protected List<Object> printFiles(Collection<ELFile> files) {
		List<Object> out = new ArrayList<Object>();
		for (ELFile file : files) {
			out.add(printFile(file));
		}
		return out;
	}

	protected Object toInt(boolean b) {
		return b ? 1 : 0;
	}

	protected String getMimeType(File file) {
		String mime = null;
		if (file.isDirectory()) {
			mime = "directory";
		} else {
			mime = getFileMimeType(file.getName());
			if (mime == null) {
				mime = DEFAULT_MIME_TYPE;
			}
		}
		return mime;
	}

	protected abstract String getFileMimeType(String fileName);

	protected abstract ELFile hashToFile(String hash);

	protected abstract String fileToHash(ELFile file);

	protected static List<ELFile> filterDirectories(List<ELFile> children) {
		List<ELFile> out = new ArrayList<ELFile>();
		for (ELFile child : children) {
			if (child.isDirectory()) {
				out.add(child);
			}
		}
		return out;
	}

	protected <T> T[] array(T... values) {
		return values;
	}

	protected Map<String, Object> obj(Property... props) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		for (Property prop : props) {
			out.put(prop.name, prop.value);
		}
		return out;
	}

	protected void extend(Map<String, Object> base, Property... props) {
		for (Property prop : props) {
			base.put(prop.name, prop.value);
		}
	}

	protected Property prop(String name, Object value) {
		return new Property(name, value);
	}

	protected Property propObj(String name, Property... props) {
		return new Property(name, obj(props));
	}

	protected static class Property {
		private final String name;
		private final Object value;

		public Property(String name, Object value) {
			this.name = name;
			this.value = value;
		}
	}

}