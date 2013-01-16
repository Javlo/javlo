package org.javlo.macro;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class CleanResourceNameMacro extends AbstractMacro {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(CleanResourceNameMacro.class.getName());

	public String getName() {
		return "clean-resource-name";
	}

	protected String getNewName(File file) {
		return StringHelper.createFileName(file.getName());
	}

	private void renameFile(ContentContext ctx, File file) throws Exception {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (File child : children) {
				renameFile(ctx, child);
			}
		} else {
			File newFile = new File(URLHelper.mergePath(file.getParentFile().getAbsolutePath(), getNewName(file)));
			if (!file.equals(newFile)) {
				ResourceHelper.renameResource(ctx, file, newFile);		
				file.renameTo(newFile);
			}
		}
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String staticFolder = URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder());
		renameFile(ctx, new File(staticFolder));
		return null;
	}
}
