package org.javlo.macro;

import java.io.File;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.PersistenceService;

public class DeleteTrackerCache extends AbstractMacro {

	@Override
	public String getName() {
		return "delete-tracker-cache";
	}
	
	private void deleteProperties(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				deleteProperties(file);
			} else {
				if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("properties")) {
					file.delete();
				}
			}
		}
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		File dir = new File(PersistenceService.getInstance(ctx.getGlobalContext()).getTrackingDirectory());
		if (dir.exists()) {
			deleteProperties(dir);
		}
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
