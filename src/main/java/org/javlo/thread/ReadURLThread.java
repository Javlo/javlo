package org.javlo.thread;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.resource.Resource;
import org.javlo.service.resource.VisualResource;

public class ReadURLThread extends AbstractThread {

	private static final String URL = "url";
	private static final String READ_IMAGE = "readImage";
	private static final String EXECUTE_TIME = "executeTime";

	private String log = "";

	public void setData(URL url, boolean readImage, long timeInMs) {
		setField(URL, url.toString());
		setField(READ_IMAGE, "" + readImage);
		long executeTime = System.currentTimeMillis() + timeInMs;
		setField(EXECUTE_TIME, "" + executeTime);
	}

	@Override
	public boolean needRunning() {
		long executeTime = Long.parseLong(getField(EXECUTE_TIME));
		return System.currentTimeMillis() > executeTime;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(getField(URL));
			log = "read : " + url;
			String content = NetHelper.readPage(url);
			if (StringHelper.isTrue(getField(READ_IMAGE))) {
				List<VisualResource> images = NetHelper.extractImage(url, content, false);
				for (Resource image : images) {
					URL imageUrl = new URL(image.getUri());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					InputStream in = imageUrl.openStream();
					try {
						ResourceHelper.writeStreamToStream(in, out);
					} finally {
						ResourceHelper.closeResource(in);
						ResourceHelper.closeResource(out);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String logInfo() {
		return log;
	}

}
