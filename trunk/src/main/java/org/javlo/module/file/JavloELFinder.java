package org.javlo.module.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.ztatic.FileCache;

/**
 * 
 * @author Benoit Dumont de Chassart
 * 
 */
public class JavloELFinder extends ELFinder {

	private Map<String, String> MIME_TYPES;

	private List<ELVolume> volumes;

	private Map<ELFile, String> fileToHash = new HashMap<ELFile, String>();
	private Map<String, ELFile> hashToFile = new HashMap<String, ELFile>();

	private ServletContext application;

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
		ContentContext ctx = ContentContext.getAdminContentContext(request, response);
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
		ContentContext ctx = ((JavloELFile)file).getContentContext();
		FileModuleContext fileModuleContext;
		try {
			fileModuleContext = (FileModuleContext)LangHelper.smartInstance(ctx.getRequest(), ctx.getResponse(), FileModuleContext.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}	
		fileModuleContext.setPath(file.getFile().getAbsolutePath().replace(file.getVolume().getRoot().getFile().getAbsolutePath(), file.getVolume().getRoot().getFile().getName()));
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
	protected void transformFile(String fileHash, String mode, int width, int height, int x, int y, Map<String, Object> apiResponse) throws Exception {
		super.transformFile(fileHash, mode, width, height, x, y, apiResponse);
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		if (file.getFile().exists()) {
			StaticConfig staticConfig = StaticConfig.getInstance(application);
			String fromDateFolderURL = URLHelper.mergePath(staticConfig.getStaticFolder(), file.getStaticInfo().getStaticURL());
			FileCache.getInstance(application).delete(fromDateFolderURL);
		}
	}

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
				JavloELFile newELFile = new JavloELFile(file.getVolume(), newFile, file.getParentFile());
				ResourceHelper.writeFileToFile(file.getFile(), newELFile.getFile());
				addedFiles.add(newELFile);
			}
		}
		apiResponse.put("added", printFiles(addedFiles));
	}

	@Override
	protected void renameFile(String fileHash, String name, Map<String, Object> apiResponse) throws Exception {
		JavloELFile file = (JavloELFile) hashToFile(fileHash);
		if (file.getFile().exists()) {
			File newFile = new File(URLHelper.mergePath(file.getFile().getParent(), name));
			I18nAccess i18nAccess = I18nAccess.getInstance(file.getContentContext().getRequest());
			if (newFile.exists()) {
				throw new ELFinderException(i18nAccess.getText("file.message.error.allready-exist"));
			} else {
				file.getFile().renameTo(newFile);
			}
		}
	}

	@Override
	protected void uploadFile(String folderHash, FileItem[] filesItem, String parameter, Map<String, Object> apiResponse) throws Exception {
		JavloELFile folder = (JavloELFile) hashToFile(folderHash);
		List<ELFile> addedFiles = new LinkedList<ELFile>();
		if (folder != null && folder.getFile().exists()) {			
			for (FileItem fileItem : filesItem) {
				File newFile = new File(URLHelper.mergePath(folder.getFile().getAbsolutePath(), fileItem.getName()));
				if (newFile.exists()) {
					newFile = ResourceHelper.getFreeFileName(newFile);
				}
				InputStream in = fileItem.getInputStream();
				ResourceHelper.writeStreamToFile(in, newFile);
				ResourceHelper.closeResource(in);
				addedFiles.add(new JavloELFile(folder.getVolume(), newFile, folder));
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
		Map<String, Object> outOptions = super.printOptions(file);		
		if (ResourceHelper.isTemplateFile(GlobalContext.getInstance(((JavloELFile)file).getContentContext().getRequest()), file.getFile())) {
			outOptions.remove("url");
			String templateName = ResourceHelper.extractTemplateName(GlobalContext.getInstance(((JavloELFile)file).getContentContext().getRequest()), file.getFile());
			outOptions.put("url", URLHelper.createTemplateResourceURL(((JavloELFile)file).getContentContext(), '/'+templateName+'/'));
		}
		return outOptions;
	}
}