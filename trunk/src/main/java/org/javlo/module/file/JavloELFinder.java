package org.javlo.module.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.PersistenceService;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.ztatic.FileCache;
import org.javlo.ztatic.StaticInfo;

/**
 * 
 * @author Benoit Dumont de Chassart
 * 
 */
public class JavloELFinder extends ELFinder {

	private Map<String, String> MIME_TYPES;

	private final List<ELVolume> volumes;

	private final Map<ELFile, String> fileToHash = new HashMap<ELFile, String>();
	private final Map<String, ELFile> hashToFile = new HashMap<String, ELFile>();

	private final ServletContext application;

	public JavloELFinder(String rootPath, ServletContext application) {
		super();
		loadMimeTypes(application);
		ELVolume volume = new ELVolume("AB");
		volume.setRoot(new RootJavloELFile(null, volume, new File(rootPath)));
		fileToHash(volume.getRoot());
		this.volumes = new ArrayList<ELVolume>();
		this.volumes.add(volume);
		this.application = application;
	}

	public JavloELFinder(String rootPath, ContentContext ctx) {
		super();
		loadMimeTypes(ctx.getRequest().getSession().getServletContext());
		ELVolume volume = new ELVolume("AB");
		volume.setRoot(new RootJavloELFile(ctx, volume, new File(rootPath)));
		fileToHash(volume.getRoot());
		this.volumes = new ArrayList<ELVolume>();
		this.volumes.add(volume);
		this.application = ctx.getRequest().getSession().getServletContext();
	}

	@Override
	public void process(Writer out, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<ELVolume> volumes = getVolumes();
		ContentContext ctx = ContentContext.getFreeContentContext(request, response);
		for (ELVolume elVolume : volumes) {
			RootJavloELFile root = (RootJavloELFile) elVolume.getRoot();
			root.setContentContext(ctx);
		}
		super.process(out, request, response);
	}

	@Override
	protected ELFile hashToFile(String hash) {
		return hashToFile.get(hash);
	}

	@Override
	protected String fileToHash(ELFile file) {
		String hash = fileToHash.get(file);
		if (hash == null) {
			hash = 'F' + StringHelper.getRandomId();
			hashToFile.put(hash, file);
			fileToHash.put(file, hash);
		}
		return hash;
	}

	@Override
	protected void changeFolder(ELFile file) {
		ContentContext ctx = ((JavloELFile) file).getContentContext();
		FileModuleContext fileModuleContext;
		try {
			fileModuleContext = (FileModuleContext) LangHelper.smartInstance(ctx.getRequest(), ctx.getResponse(), FileModuleContext.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		fileModuleContext.setPath(file.getFile().getAbsolutePath().replace(file.getVolume().getRoot().getFile().getAbsolutePath(), ""));
	}

	@Override
	protected List<ELVolume> getVolumes() {
		return volumes;
	}

	private void loadMimeTypes(ServletContext servletContext) {
		try {
			Map<String, String> mimes = new HashMap<String, String>();
			InputStream in = servletContext.getResourceAsStream("/modules/file/mime-types.properties");
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

	@Override
	protected String getFileMimeType(String fileName) {
		String mime = null;
		int pos = fileName.lastIndexOf('.');
		while (pos >= 0) {
			String extension = fileName.substring(pos + 1);
			String fileMime = MIME_TYPES.get(extension.toLowerCase());
			if (fileMime != null) {
				mime = fileMime;
			}
			pos = fileName.lastIndexOf('.', pos - 1);
		}
		return mime;
	}

	@Override
	protected void deleteFile(HttpServletRequest request, String[] filesHash, Map<String, Object> apiResponse) throws IOException {
		Collection<ELFile> deletedFiles = new LinkedList<ELFile>();
		for (String fileHash : filesHash) {
			JavloELFile file = (JavloELFile) hashToFile(fileHash);
			if (file.getFile().exists()) {
				GlobalContext globalContext = GlobalContext.getInstance(request);
				if (file.isDirectory()) {
					FileUtils.deleteDirectory(file.getFile());
					FileCache.getInstance(request.getSession().getServletContext()).clear(globalContext.getContextKey());
				} else {
					file.getFile().delete();
					FileCache.getInstance(request.getSession().getServletContext()).deleteAllFile(globalContext.getContextKey(), file.getFile().getName());
				}
				deletedFiles.add(file);
			}
		}
		apiResponse.put("removed", printFilesHash(deletedFiles));
	}

	@Override
	protected void transformFile(String fileHash, String mode, int width, int height, int x, int y, int degree, Map<String, Object> apiResponse) throws Exception {
		super.transformFile(fileHash, mode, width, height, x, y, degree, apiResponse);
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		if (file.getFile().exists()) {
			StaticConfig staticConfig = StaticConfig.getInstance(application);
			String fromDateFolderURL = URLHelper.mergePath(staticConfig.getStaticFolder(), file.getStaticInfo().getStaticURL());
			FileCache.getInstance(application).delete(fromDateFolderURL);
		}
	}

	@Override
	protected void createDir(String folderId, String fileName, Map<String, Object> response) {
		JavloELFile folder = (JavloELFile) hashToFile(folderId);
		File newFile = new File(URLHelper.mergePath(folder.getFile().getAbsolutePath(), fileName));
		if (!newFile.exists()) {
			newFile.mkdirs();
			JavloELFile newElFile = new JavloELFile(folder.getVolume(), newFile, folder);
			response.put("added", printFiles(Arrays.asList(new ELFile[] { newElFile })));
		}
	}

	@Override
	protected void duplicateFile(String[] filesHash, Map<String, Object> apiResponse) throws IOException {
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		for (String fileHash : filesHash) {
			JavloELFile file = (JavloELFile) hashToFile(fileHash);

			if (file.getFile().exists()) {
				File newFile = ResourceHelper.getFreeFileName(file.getFile());
				if (file.getFile().isDirectory()) {
					FileUtils.copyDirectory(file.getFile(), newFile);

					// check folder ???

					JavloELFile newELFile = new JavloELFile(file.getVolume(), newFile, file.getParentFile());
					addedFiles.add(newELFile);
				} else {
					JavloELFile newELFile = new JavloELFile(file.getVolume(), newFile, file.getParentFile());
					ResourceHelper.writeFileToFile(file.getFile(), newELFile.getFile());
					addedFiles.add(newELFile);
				}
			}
		}
		apiResponse.put("added", printFiles(addedFiles));
	}

	@Override
	protected void renameFile(HttpServletRequest request, HttpServletResponse response, String fileHash, String name, Map<String, Object> apiResponse) throws Exception {
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		if (file.getFile().exists()) {
			File newFile = new File(URLHelper.mergePath(file.getFile().getParent(), name));
			I18nAccess i18nAccess = I18nAccess.getInstance(file.getContentContext().getRequest());
			if (newFile.exists()) {
				throw new ELFinderException(i18nAccess.getText("file.message.error.allready-exist"));
			} else {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file.getFile());
				ResourceHelper.renameResource(ctx,file.getFile(),newFile);
				file.getFile().renameTo(newFile);
				staticInfo.renameFile(ctx, newFile);
				GlobalContext globalContext = GlobalContext.getInstance(request);
				PersistenceService.getInstance(globalContext).store(ctx);
				if (file.getFile().isDirectory()) {
					FileCache.getInstance(request.getSession().getServletContext()).clear(globalContext.getContextKey());
				} else {
					FileCache.getInstance(request.getSession().getServletContext()).deleteAllFile(globalContext.getContextKey(), file.getFile().getName());
				}
				apiResponse.put("removed", printFilesHash(Arrays.asList(new ELFile[] { file })));
				apiResponse.put("added", printFiles(Arrays.asList(new ELFile[] { new JavloELFile(file.getVolume(), newFile, file.getParentFile()) })));
				// apiResponse.put("added", printFile(new JavloELFile(file.getVolume(), newFile, file.getParentFile())));
				// apiResponse.put("removed", printFile(file));
			}
		}

	}

	@Override
	protected void extractFile(String fileHash, Map<String, Object> apiResponse) throws Exception {
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		if (file != null && file.getFile().exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(file.getFile());
				ZipInputStream zipIn = new ZipInputStream(in);
				ZipEntry entry = zipIn.getNextEntry();
				while (entry != null) {
					File zipFile = ZipManagement.saveFile(application, file.getParentFile().getFile().getAbsolutePath(), entry.getName(), zipIn);
					entry = zipIn.getNextEntry();
					if (zipFile.getParentFile().getAbsolutePath().equals(file.getParentFile().getFile().getAbsolutePath())) { // list only file inside current folder
						addedFiles.add(new JavloELFile(file.getParentFile().getVolume(), zipFile, file.getParentFile()));
					}
				}
				zipIn.close();
			} finally {
				ResourceHelper.closeResource(in);
			}

		}
		apiResponse.put("added", printFiles(addedFiles));
	}

	private static void compressFilesRecu(ZipOutputStream out, String root, File file) throws IOException {
		String zipName = StringUtils.removeStart(file.getAbsolutePath(), root);
		if (file.isFile()) {
			ZipEntry entry = new ZipEntry(zipName);
			out.putNextEntry(entry);
			InputStream in = new FileInputStream(file);
			try {
				ResourceHelper.writeStreamToStream(in, out);
			} finally {
				ResourceHelper.closeResource(in);
			}
		} else {
			File[] children = file.listFiles();
			for (File child : children) {
				compressFilesRecu(out, root, child);
			}
		}
	}

	@Override
	protected void compressFiles(String[] files, String type, Map<String, Object> apiResponse) throws Exception {
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		if (files.length == 0) {
			return;
		}

		ZipOutputStream out = null;
		try {
			ELFile firstFile = hashToFile(files[0]);
			File zipFileName = new File(StringHelper.getFileNameWithoutExtension(firstFile.getFile().getAbsolutePath()) + ".zip");
			zipFileName = ResourceHelper.getFreeFileName(zipFileName);
			addedFiles.add(new JavloELFile(firstFile.getVolume(), zipFileName, firstFile.getParentFile()));
			OutputStream outStream = new FileOutputStream(zipFileName);

			out = new ZipOutputStream(new BufferedOutputStream(outStream));

			for (String fileHash : files) {
				ELFile elfile = hashToFile(fileHash);
				compressFilesRecu(out, firstFile.getParentFile().getFile().getAbsolutePath(), elfile.getFile());
			}
		} finally {
			ResourceHelper.closeResource(out);
		}

		apiResponse.put("added", printFiles(addedFiles));
	}

	@Override
	protected void uploadFile(String folderHash, FileItem[] filesItem, String parameter, Map<String, Object> apiResponse) throws Exception {
		JavloELFile folder = (JavloELFile) hashToFile(folderHash);
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		if (folder != null && folder.getFile().exists()) {
			for (FileItem fileItem : filesItem) {
				String newFileName = StringHelper.createFileName(fileItem.getName());
				File newFile = new File(URLHelper.mergePath(folder.getFile().getAbsolutePath(), newFileName));
				InputStream in = fileItem.getInputStream();
				try {
					// if (!StringHelper.getFileExtension(newFile.getName()).toLowerCase().equals("zip")) {
					if (newFile.exists()) {
						newFile = ResourceHelper.getFreeFileName(newFile);
					}
					ResourceHelper.writeStreamToFile(in, newFile);
					ResourceHelper.closeResource(in);
					addedFiles.add(new JavloELFile(folder.getVolume(), newFile, folder));
					/*
					 * } else { ZipInputStream zipIn = new ZipInputStream(in); ZipEntry entry = zipIn.getNextEntry(); while (entry != null) { File file = ZipManagement.saveFile(application, folder.getFile().getAbsolutePath(), entry.getName(), zipIn); entry = zipIn.getNextEntry(); if (file.getParentFile().getAbsolutePath().equals(folder.getFile().getAbsolutePath())) { // list only file inside current folder addedFiles.add(new JavloELFile(folder.getVolume(), file, folder)); } } zipIn.close(); }
					 */
				} finally {
					ResourceHelper.closeResource(in);
				}
			}
		}
		apiResponse.put("added", printFiles(addedFiles));
	}

	@Override
	protected ELFile createELFile(ELFile parent, File file) {
		return new JavloELFile(parent.getVolume(), file, parent);
	}

	@Override
	protected Map<String, Object> printOptions(ELFile file) {
		GlobalContext globalContext = GlobalContext.getSessionInstance(((JavloELFile) file).getContentContext().getRequest().getSession());
		Map<String, Object> outOptions = super.printOptions(file);
		if (ResourceHelper.isTemplateFile(globalContext, file.getFile())) {
			outOptions.remove("url");
			String templateName = ResourceHelper.extractTemplateName(globalContext, file.getFile());
			if (!file.getVolume().getRoot().getFile().getName().equals(templateName)) { // for all templates browsing
				outOptions.put("url", URLHelper.createTemplateResourceURL(((JavloELFile) file).getContentContext(), "/"));
			} else {
				outOptions.put("url", URLHelper.createTemplateResourceURL(((JavloELFile) file).getContentContext(), '/' + templateName + '/'));
			}
		}

		Map<String, String[]> archivers = new HashMap<String, String[]>();
		archivers.put("create", new String[] { "application/zip" });
		archivers.put("extract", new String[] { "application/zip" });
		outOptions.put("archivers", archivers);

		return outOptions;
	}

	@Override
	protected void pasteFiles(String srcHashFolder, String dstHashFolder, String[] files, boolean cut, Map<String, Object> apiResponse) throws IOException {
		JavloELFile dstFolder = (JavloELFile) hashToFile(dstHashFolder);
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		List<ELFile> removeFiles = new LinkedList<ELFile>();
		for (String file : files) {
			ELFile oldFile = hashToFile(file);
			File newFile = new File(URLHelper.mergePath(dstFolder.getFile().getAbsolutePath(), oldFile.getFile().getName()));
			if (!newFile.exists()) {
				if (oldFile.getFile().isFile()) {
					ELFile newELFile = createELFile(dstFolder, newFile);
					ResourceHelper.writeFileToFile(oldFile.getFile(), newFile);
					addedFiles.add(newELFile);
					if (cut) {
						oldFile.getFile().delete();
						removeFiles.add(oldFile);
						try {
							ResourceHelper.renameResource(dstFolder.getContentContext().getContextWithOtherRenderMode(ContentContext.EDIT_MODE), oldFile.getFile(), newFile);
						} catch (Exception e) {
							e.printStackTrace();
							throw new IOException(e);
						}
					}
				} else {
					if (cut) {
						FileUtils.moveDirectory(oldFile.getFile(), newFile);
						if (newFile.exists()) {
							Collection<File> children = ResourceHelper.getAllFilesList(newFile);
							for (File child : children) {
								File oldChildren = new File(child.getAbsolutePath().replace(newFile.getAbsolutePath(), oldFile.getFile().getAbsolutePath()));
								if (child.isFile()) {
									try {
										ResourceHelper.renameResource(dstFolder.getContentContext().getContextWithOtherRenderMode(ContentContext.EDIT_MODE), oldChildren, child);
									} catch (Exception e) {
										e.printStackTrace();
										throw new IOException(e);
									}
								}
								removeFiles.add(new JavloELFile(oldFile.getVolume(), oldChildren, oldFile));
							}
						}
					} else {
						FileUtils.copyDirectory(oldFile.getFile(), newFile);
					}
					ELFile newELFile = createELFile(dstFolder, newFile);
					addedFiles.add(newELFile);
				}
			}
		}
		apiResponse.put("added", printFiles(addedFiles));
		apiResponse.put("removed", printFilesHash(removeFiles));
	}
}