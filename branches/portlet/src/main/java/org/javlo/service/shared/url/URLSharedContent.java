package org.javlo.service.shared.url;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.shared.SharedContent;

public class URLSharedContent extends SharedContent {
	
	private String remoteImageUrl;

	public URLSharedContent(String id, Collection<ComponentBean> content) throws Exception {		
		super(id, content);
	}

	public String getRemoteImageUrl() {
		return remoteImageUrl;
	}

	public void setRemoteImageUrl(String remoteImageUrl) {
		this.remoteImageUrl = remoteImageUrl;
	}
	
	@Override
	public void loadContent(ContentContext ctx) {
		super.loadContent(ctx);	
		if (content == null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			InputStream in = null;
			try {			
				URL imageURL = new URL(getRemoteImageUrl());
				String baseStaticFolder = URLHelper.mergePath(SHARED_CONTENT_FOLDER, imageURL.getHost() );		
				
				String imageFolder = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder(), baseStaticFolder);
				
				File imageFile = new File (URLHelper.mergePath(imageFolder, getId()+".jpg"));
				
				in = imageURL.openStream();
				ResourceHelper.writeStreamToFile(in, imageFile);				
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("dir=" + baseStaticFolder);
				out.println("file-name=" + imageFile.getName());
				out.println(GlobalImage.IMAGE_FILTER + "=full");
				if (getTitle() != null) {
					out.println("label=" + getTitle());
				}
				out.close();
				String value = new String(outStream.toByteArray());
				ComponentBean bean = new ComponentBean(GlobalImage.TYPE, value, ctx.getRequestContentLanguage());
				bean.setArea(ctx.getArea());
				content = new LinkedList<ComponentBean>();
				content.add(bean);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ResourceHelper.closeResource(in);
			}			
		}
	}
}
