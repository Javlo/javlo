package org.javlo.module.dropbox;

import java.io.File;

import org.apache.log4j.Logger;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.dropbox.DropboxAction.DropboxConfig;
import org.javlo.service.syncro.DropboxService;
import org.javlo.service.syncro.DropboxService.DropboxServiceException;

public class DropboxThread extends Thread {
	
	private static Logger logger = Logger.getLogger(DropboxThread.class.getName());
	
	private DropboxService service;
	private File localRoot;
	private String dropboxFolder;

	public DropboxThread (ContentContext ctx, DropboxConfig config) {
		setName("dropbox thread");
		service = DropboxService.getInstance(ctx, config.getToken());				
		localRoot = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), config.getLocalFolder()));
		localRoot.mkdirs();
		this.dropboxFolder = config.getDropboxFolder();
	}
	
	@Override
	public void run() {
		logger.info("start dropbox thread. localRoot="+localRoot);
		super.run();
		try {
			service.synchronize(localRoot, dropboxFolder);
		} catch (DropboxServiceException e) {
			e.printStackTrace();
		}
		service = null;
		localRoot = null;
		dropboxFolder = null;
		logger.info("end dropbox thread. localRoot="+localRoot);
	}
	
	public String getHumanDownloadSize() {
		if (service == null) {
			return "0";
		} else {
			long size = service.getTotalDownloadSize();
			return StringHelper.renderSize(size);
		}
	}

}
