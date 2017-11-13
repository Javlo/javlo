package org.javlo.macro;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.ztatic.ResourceFactory;
import org.javlo.ztatic.StaticInfo;

public class MergeGalleryAndGalleries extends AbstractMacro {

	@Override
	public String getName() {
		return "merge-gallery-galleries";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		File galleriesFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "static", "galleries"));
		File galleryFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "static", "gallery"));
		ResourceFactory resourceFactory = ResourceFactory.getInstance(ctx);
		
		int sameFile = 0;
		if (!galleriesFolder.exists() || !galleryFolder.exists()) {
			return "one galleries not found. (galleries?"+galleriesFolder.exists()+", gallery?"+galleryFolder.exists();
		} else {
			for (File  file : ResourceHelper.getAllFilesList(galleriesFolder))  {
				File galleryFile = new File(StringUtils.replace(file.getAbsolutePath(), galleriesFolder.getAbsolutePath(), galleryFolder.getAbsolutePath()));
				if (galleryFile.exists()) {
					sameFile++;
					for (String lg : ctx.getGlobalContext().getContentLanguages()) {
						ContentContext lgCtx = new ContentContext(ctx);
						lgCtx.setContentLanguage(lg);								
						StaticInfo info = StaticInfo.getInstance(lgCtx, file);						
						StaticInfo galInfo = StaticInfo.getInstance(lgCtx, galleryFile);
						
						if (StringHelper.isEmpty(info.getManualTitle(lgCtx))) {							
							info.setTitle(lgCtx, galInfo.getManualTitle(lgCtx));
						}
						if (StringHelper.isEmpty(info.getAuthors(lgCtx))) {
							info.setAuthors(lgCtx, galInfo.getAuthors(lgCtx));
						}
						if (StringHelper.isEmpty(info.getCopyright(lgCtx))) {
							info.setCopyright(lgCtx, galInfo.getCopyright(lgCtx));
						}
						if (StringHelper.isEmpty(info.getManualDescription(lgCtx))) {
							info.setDescription(lgCtx, galInfo.getManualDescription(lgCtx));
						}
						if (StringHelper.isEmpty(info.getManualLocation(lgCtx))) {
							info.setLocation(lgCtx, galInfo.getManualLocation(lgCtx));
						}
						
						resourceFactory.update(ctx, info);
					}
				}				
			}
		}
		
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		messageRepository.setGlobalMessage(new GenericMessage("static info found : "+sameFile, GenericMessage.INFO));
		
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}
	
	@Override
	public boolean isAdmin() {
		return true;
	}

};

