package org.javlo.module.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

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
	
	public JavloELFinder(String rootPath, ServletContext application) {
		super();
		loadMimeTypes(application);
		ELVolume volume = new ELVolume("AB");
		volume.setRoot(new RootJavloELFile(null, volume, new File(rootPath)));
		fileToHash(volume.getRoot());
		this.volumes = new ArrayList<ELVolume>();
		this.volumes.add(volume);		
	}

	public JavloELFinder(String rootPath, ContentContext ctx) {
		super();
		loadMimeTypes(ctx.getRequest().getSession().getServletContext());
		ELVolume volume = new ELVolume("AB");
		volume.setRoot(new RootJavloELFile(ctx, volume, new File(rootPath)));
		fileToHash(volume.getRoot());
		this.volumes = new ArrayList<ELVolume>();
		this.volumes.add(volume);
	}
	
	@Override
	public void process(Writer out, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<ELVolume> volumes = getVolumes();
		ContentContext ctx = ContentContext.getAdminContentContext(request, response);
		for (ELVolume elVolume : volumes) {
			RootJavloELFile root = (RootJavloELFile)elVolume.getRoot();
			root.setContentContext(ctx);
		}
		super.process(out,request,response);
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

}