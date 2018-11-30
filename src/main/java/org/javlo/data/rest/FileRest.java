package org.javlo.data.rest;

import java.io.File;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.ztatic.StaticInfo;

public class FileRest implements IRestFactory {
	
	public FileRest() {
	}

	@Override
	public String getName() {
		return "file";
	}

	@Override
	public IRestItem search(ContentContext ctx, String path, String query) throws Exception {
		
		System.out.println(">>>>>>>>> FileRest.search : path = "+path); //TODO: remove debug trace
		System.out.println(">>>>>>>>> FileRest.search : query = "+query); //TODO: remove debug trace
		
		File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), path));
		
		System.out.println(">>>>>>>>> FileRest.search : file = "+file); //TODO: remove debug trace
		
		if (!file.exists()) {
			return null;
		} else {
			return StaticInfo.getInstance(ctx, file);
		}
	}	

}