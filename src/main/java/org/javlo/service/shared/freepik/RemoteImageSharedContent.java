package org.javlo.service.shared.freepik;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.shared.SharedContent;
import org.javlo.ztatic.StaticInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

public class RemoteImageSharedContent extends SharedContent {
	
	private String remoteImageUrl;
	
	private String folder;

	public RemoteImageSharedContent(String id, Collection<ComponentBean> content, String folder) throws Exception {		
		super(id, content);
		this.folder = folder;
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
			String baseStaticFolder = URLHelper.mergePath(SHARED_CONTENT_FOLDER, folder);			
			String imageFolder = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder(), baseStaticFolder);			
			File imageFile = new File (URLHelper.mergePath(imageFolder, getId()+".jpg"));
			InputStream in = null;
			try {
				if (!imageFile.exists()) {
					URL imageURL = new URL(getRemoteImageUrl());				
					in = imageURL.openStream();
					ResourceHelper.writeStreamToFile(in, imageFile);
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, imageFile);
					staticInfo.setTitle(ctx, getTitle());
				}
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("dir=" + baseStaticFolder);
				out.println("file-name=" + imageFile.getName());
				out.println(GlobalImage.IMAGE_FILTER + "="+ctx.getCurrentTemplate().getDefaultImageFilter());
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
