package org.javlo.macro;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.ztatic.StaticInfo;

/**
 * clean static info storage, remove meta data if resource not found and remove
 * reference to defaut value
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class StaticInfoAutoFill extends AbstractMacro {

	private static Logger logger = Logger.getLogger(StaticInfoAutoFill.class.getName());

	@Override
	public String getName() {
		return "static-info-filled";
	}

	@Override
	public String perform(ContentContext inCtx, Map<String, Object> params) throws Exception {
		File staticFolder = new File(inCtx.getGlobalContext().getStaticFolder());
		int count = 0;
		for (File child : ResourceHelper.getAllFilesList(staticFolder)) {
			StaticInfo si = StaticInfo.getInstance(inCtx, child);
			si.setAutoFill(inCtx, false);
			count++;
		}
		return count+" files found.";
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

}
