package org.javlo.macro;

import org.javlo.context.ContentContext;
import org.javlo.template.Template;

import java.util.Map;
import java.util.logging.Logger;

public class UpdateTemplate extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(UpdateTemplate.class.getName());

	@Override
	public String getName() {
		return "update-template";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		Template template = ctx.getCurrentTemplate();
		logger.info("template : "+template.getName());
		template.importTemplateInWebapp(ctx.getGlobalContext().getStaticConfig(), ctx, false, true);
		template.reload();		
//		Collection<File> allFiles = ResourceHelper.getAllFilesList(new File(template.getWorkTemplateRealPath(ctx.getGlobalContext())));
//		long olderModifDate = 0;
//		for (File file : allFiles) {
//			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("scss")) {
//				if (file.lastModified() > olderModifDate) {
//					olderModifDate = file.lastModified();
//				}
//			}
//		}
//		for (File file : allFiles) {
//			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("scss")) {
//				File cssFile = new File(StringHelper.getFileNameWithoutExtension(file.getAbsolutePath())+".css");
//				if (cssFile.exists() && cssFile.lastModified() < olderModifDate) {
//					logger.info("delete : "+cssFile);
//					cssFile.delete();
//				}
//			}
//		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
