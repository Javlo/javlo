package org.javlo.module.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageEngine;
import org.javlo.service.RequestService;

/**
 * 
 * @author Benoit Dumont de Chassart
 * 
 */
public abstract class ELFinder {

	private static Logger logger = Logger.getLogger(ELFinder.class.getName());

	private static final String PROTOCOL_VERSION = "2.0";
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public void process(Writer out, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Map<String, Object> apiResponse = new LinkedHashMap<String, Object>();
		try {
			RequestService rs = RequestService.getInstance(request);
			String command = rs.getParameter("cmd", null);
			if ("file".equals(command)) {
				ELFile file = getFile(request, "target");
				if (file != null) {
					ContentContext ctx = ContentContext.getContentContext(request, response);
					response.addHeader("Content-Descriptionn", "File Transfer");
					response.addHeader("Content-Type", ResourceHelper.getFileExtensionToMineType(StringHelper.getFileExtension(file.getFile().getName())));
					response.addHeader("Content-Disposition", "attachment; filename="+ file.getFile().getName()); 
					response.addHeader("Content-Transfer-Encoding", "binary");
					response.addHeader("Connection","Keep-Alive");
					response.addHeader("Expires","0");
					response.addHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
					response.addHeader("Pragma","public");
					response.addHeader("Content-Length",""+file.getFile().length());
					ResourceHelper.writeFileToStream(file.getFile(), response.getOutputStream());
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				}
			} else if ("open".equals(command)) {
				open(getBoolean(request, "init", false), getFile(request, "target"), getBoolean(request, "tree", false), apiResponse);
			} else if ("parents".equals(command)) {
				parents(getFile(request, "target"), apiResponse);
			} else if ("tree".equals(command)) {
				tree(getFile(request, "target"), apiResponse);
			} else if ("mkfile".equals(command)) {
				createFile(request.getParameter("target"), request.getParameter("name"), apiResponse);
			} else if ("mkdir".equals(command)) {
				createDir(request.getParameter("target"), request.getParameter("name"), apiResponse);
			} else if ("rm".equals(command)) {
				deleteFile(request, rs.getParameterValues("targets[]", null), apiResponse);
			} else if ("duplicate".equals(command)) {
				duplicateFile(rs.getParameterValues("targets[]", null), apiResponse);
			} else if ("put".equals(command)) {
				updateFile(rs.getParameter("content", null), rs.getParameter("target", null), apiResponse);
			} else if ("search".equals(command)) {
				searchFiles(rs.getParameter("q", null), apiResponse);
			} else if ("get".equals(command)) {
				getFile(rs.getParameter("target", null), apiResponse);
			} else if ("rename".equals(command)) {
				renameFile(request, response, rs.getParameter("target", null), rs.getParameter("name", null), apiResponse);
			} else if ("upload".equals(command)) {
				uploadFile(rs.getParameter("target", null), rs.getFileItemMap().get("upload[]"), rs.getParameter("name", null), apiResponse);
			} else if ("archive".equals(command)) {
				compressFiles(rs.getParameterValues("targets[]", null), rs.getParameter("type", null), apiResponse);
			} else if ("extract".equals(command)) {
				extractFile(rs.getParameter("target", null), apiResponse);
			} else if ("resize".equals(command)) {
				String mode = rs.getParameter("mode", null);
				String target = rs.getParameter("target", null);
				int height = Integer.parseInt(rs.getParameter("height", "-1"));
				int width = Integer.parseInt(rs.getParameter("width", "-1"));
				int degree = Integer.parseInt(rs.getParameter("degree", "0"));
				int x = Integer.parseInt(rs.getParameter("x", "-1"));
				int y = Integer.parseInt(rs.getParameter("y", "-1"));
				transformFile(target, mode, width, height, x, y, degree, apiResponse);
			} else if ("paste".equals(command)) {
				pasteFiles(rs.getParameter("src", null), rs.getParameter("dst", null), rs.getParameterValues("targets[]", null), StringHelper.isTrue(rs.getParameter("cut", "false")), apiResponse);
			}
			if (request.getSession().getAttribute("ELPath") != null) {
				apiResponse.clear();
				init("" + request.getSession().getAttribute("ELPath"), apiResponse);
				request.getSession().removeAttribute("ELPath");
			}
		} catch (ELFinderException elEx) {
			apiResponse.clear();
			apiResponse.put("error", elEx.getMessage());
		} catch (Throwable ex) {
			ex.printStackTrace();
			apiResponse.clear();
			apiResponse.put("error", "Exception: " + ex.toString());
		}

		JSONSerializer.writeJSONString(apiResponse, out);

	}

	private void init(String inFolder, Map<String, Object> apiResponse) {
		String[] folders = inFolder.split("/");
		ELFile currentFile = null;
		if (folders.length > 1) {
			/*
			 * String volName = folders[1]; ELFile currentFile = null; for
			 * (ELFile volume : getVolumeFiles()) { if
			 * (volume.getFile().getName().equals(volName)) { currentFile =
			 * volume; } }
			 */
			currentFile = getVolumeFiles().iterator().next();
			if (currentFile != null) {
				for (int i = 1; i < folders.length; i++) {
					for (ELFile file : currentFile.getChildren()) {
						if (file.getFile().getName().equals(folders[i])) {
							currentFile = file;
						}
					}
				}
			}
		}
		open(true, currentFile, true, apiResponse);
	}

	protected abstract void pasteFiles(String srcHashFolder, String dstHashFolder, String[] files, boolean cut, Map<String, Object> apiResponse) throws IOException;

	/*
	 * ELFile dstFolder = hashToFile(dstHashFolder); List<ELFile> addedFiles =
	 * new LinkedList<ELFile>(); List<ELFile> removeFiles = new
	 * LinkedList<ELFile>(); for (String file : files) { ELFile oldFile =
	 * hashToFile(file); File newFile = new
	 * File(URLHelper.mergePath(dstFolder.getFile().getAbsolutePath(),
	 * oldFile.getFile().getName())); if (!newFile.exists()) { if
	 * (oldFile.getFile().isFile()) { ELFile newELFile = createELFile(dstFolder,
	 * newFile); ResourceHelper.writeFileToFile(oldFile.getFile(), newFile);
	 * addedFiles.add(newELFile); if (cut) { oldFile.getFile().delete();
	 * removeFiles.add(oldFile); } } else {
	 * FileUtils.moveDirectory(oldFile.getFile(), newFile); ELFile newELFile =
	 * createELFile(dstFolder, newFile); addedFiles.add(newELFile); } } }
	 * apiResponse.put("added", printFiles(addedFiles));
	 * apiResponse.put("removed", printFiles(removeFiles)); }
	 */

	protected abstract ELFile createELFile(ELFile parent, File file);

	protected abstract void uploadFile(String folderHash, FileItem[] filesItem, String parameter, Map<String, Object> apiResponse) throws Exception;

	protected abstract void extractFile(String fileHash, Map<String, Object> apiResponse) throws Exception;

	protected abstract void compressFiles(String[] files, String type, Map<String, Object> apiResponse) throws Exception;

	protected abstract void renameFile(HttpServletRequest request, HttpServletResponse response, String fileHash, String name, Map<String, Object> apiResponse) throws ELFinderException, Exception;

	protected abstract void duplicateFile(String[] filesHash, Map<String, Object> apiResponse) throws IOException;

	protected void transformFile(String fileHash, String mode, int width, int height, int x, int y, int degree, Map<String, Object> apiResponse) throws Exception {
		ELFile file = hashToFile(fileHash);
		if (file.getFile().exists()) {
			IIOMetadata metadata = ResourceHelper.getImageMetadata(file.getFile());
			if ("resize".equals(mode)) {
				BufferedImage img = ImageEngine.loadImage(file.getFile());
				img = ImageEngine.resizeImage(img, width, height);
				ImageEngine.storeImage(img, file.getFile());
				ResourceHelper.writeImageMetadata(metadata, file.getFile());
				apiResponse.put("changed", printFiles(Arrays.asList(new ELFile[] { file })));
			} else if ("crop".equals(mode)) {
				BufferedImage img = ImageEngine.loadImage(file.getFile());
				img = ImageEngine.cropImage(img, width, height, x, y);
				ImageEngine.storeImage(img, file.getFile());
				ResourceHelper.writeImageMetadata(metadata, file.getFile());
				apiResponse.put("changed", printFiles(Arrays.asList(new ELFile[] { file })));
			} else if ("rotate".equals(mode)) {
				BufferedImage img = ImageEngine.loadImage(file.getFile());
				img = ImageEngine.rotate(img, degree, null);
				ImageEngine.storeImage(img, file.getFile());
				ResourceHelper.writeImageMetadata(metadata, file.getFile());
				apiResponse.put("changed", printFiles(Arrays.asList(new ELFile[] { file })));
			}
		}
	}

	private void getFile(String fileHash, Map<String, Object> apiResponse) throws FileNotFoundException, IOException {
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		if (file.getFile().exists()) {
			apiResponse.put("content", ResourceHelper.loadStringFromFile(file.getFile()));
		}
	}

	private static void searchFiles(ELFile file, String q, boolean inside, List<ELFile> result) throws FileNotFoundException, IOException {
		if (file.getFile().isFile()) {
			if (file.getFile().getName().contains(q)) {
				result.add(file);
			} else if (inside) {
				if (ResourceHelper.getFileContent(file.getFile()).contains(q)) {
					result.add(file);
				}
			}
		} else {
			List<ELFile> children = file.getChildren();
			for (ELFile elFile : children) {
				searchFiles(elFile, q, inside, result);
			}
		}
	}

	private void searchFiles(String q, Map<String, Object> apiResponse) throws FileNotFoundException, IOException {
		q = q.trim();
		boolean inside = !q.startsWith("name:");
		if (!inside) {
			q = q.replaceFirst("name:", "");
			q = q.trim();
		}
		List<ELFile> searchResult = new LinkedList<ELFile>();
		List<ELVolume> volumes = getVolumes();
		for (ELVolume elVolume : volumes) {
			searchFiles(elVolume.getRoot(), q, inside, searchResult);
		}

		apiResponse.put("files", printFiles(searchResult));
	}

	private void updateFile(String content, String fileHash, Map<String, Object> apiResponse) throws IOException {
		ELFile file = hashToFile(fileHash);
		if (file.getFile().exists()) {
			if (content != null) {
				ResourceHelper.writeStringToFile(file.getFile(), content, ContentContext.CHARACTER_ENCODING);
				apiResponse.put("changed", printFiles(Arrays.asList(new ELFile[] { file })));
			}

		}
	}

	protected abstract void createDir(String folderId, String fileName, Map<String, Object> response);

	protected abstract void deleteFile(HttpServletRequest request, String[] filesHash, Map<String, Object> apiResponse) throws IOException;

	private void createFile(String folderId, String fileName, Map<String, Object> response) throws IOException {
		JavloELFile folder = (JavloELFile) hashToFile(folderId);
		File newFile = new File(URLHelper.mergePath(folder.getFile().getAbsolutePath(), fileName));
		if (!newFile.exists()) {
			newFile.createNewFile();
			JavloELFile newElFile = new JavloELFile(folder.getVolume(), newFile, folder);
			response.put("added", printFiles(Arrays.asList(new ELFile[] { newElFile })));
		}
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
		if (target == null) {
			ELVolume volume = getVolumes().iterator().next();
			target = volume.getRoot();
		}
		response.put("cwd", printFile(target));
		response.put("options", printOptions(target));
		List<ELFile> files = new LinkedList<ELFile>();
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
			extend(response, prop("api", PROTOCOL_VERSION), prop("uplMaxSize", "512M"));
		}
		changeFolder(target);
	}

	protected abstract void changeFolder(ELFile file);

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
				// (String) hash of current file/dir path, first symbol must be
				// letter, symbols before _underline_ - volume id, Required.
				prop("hash", fileToHash(file)),
				// (String) mime type. Required.
				prop("mime", getMimeType(file.getFile())),
				// (Number) file modification time in unix timestamp. Required.
				prop("ts", Math.round(file.getFile().lastModified() / 1000)),
				// (Number) file size in bytes
				prop("size", file.getFile().length()),
				// (Number) is readable
				prop("read", toInt(true)),
				// (Number) is writable
				prop("write", toInt(true)),
				// (Number) is file locked. If locked that object cannot be
				// deleted and renamed
				prop("locked", toInt(false)), prop("tmb", file.getThumbnailURL()));

		// (Number) Only for directories. Marks if directory has child
		// directories inside it. 0 (or not set) - no, 1 - yes. Do not need to
		// calculate amount.
		if (file.isDirectory()) {
			List<ELFile> children = file.getChildren();
			extend(out, prop("childs", toInt(children.size() > 0)));
			List<ELFile> childDirectories = filterDirectories(children);
			extend(out, prop("dirs", toInt(childDirectories.size() > 0)));
		} else {
			
			if (StringHelper.isImage(file.getFile().getName())) {
				try {
					/*
					 * BufferedImage img = ImageIO.read(file.getFile());
					 * extend(out, prop("dim", "" + img.getWidth() + 'x' +
					 * img.getHeight()));
					 */
				} catch (Throwable e) {
					logger.warning(e.getMessage());
				}
			}
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
		String url = null;
		String path = null;
		if (file.getParentFile() != null) {
			path = file.getParentFile().getPath();
			url = file.getVolume().getRoot().getURL();
		}
		return obj(prop("path", path), // (String) Current folder path
				prop("url", url), // (String) Current folder URL
				prop("tmbURL", file.getThumbnailURL()), // (String) Thumbnails
														// folder URL
				prop("separator", "/"), prop("disabled", array()), // (Array)
																	// List of
																	// commands
																	// not
																	// allowed
																	// (disabled)
																	// on this
																	// volume
				prop("copyOverwrite", 1), propObj("archivers", prop("create", array()), prop("extract", array())));
	}

	protected List<Object> printFiles(List<ELFile> files) {
		List<Object> out = new ArrayList<Object>();
		Collections.sort(files, ELFile.FILE_NAME_COMPARATOR);
		for (ELFile file : files) {
			out.add(printFile(file));
		}
		return out;
	}

	protected List<Object> printFilesHash(Collection<ELFile> files) {
		List<Object> out = new ArrayList<Object>();
		for (ELFile file : files) {
			out.add(fileToHash(file));
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