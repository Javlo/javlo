package org.javlo.servlet.servletWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.helper.ResourceHelper;

public class BinaryServletWrapper implements IServletWrapper {
	
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(BinaryServletWrapper.class.getName());

	private byte[] data = new byte[0];
	private String contentType = null;

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		logger.info("export "+getData().length+" bytes.");
		
		InputStream in = new ByteArrayInputStream(getData());
		if (getContentType() != null) {
			response.setContentType(getContentType());
		}
		try {
			ResourceHelper.writeStreamToStream(in, response.getOutputStream());			
		} catch (IOException e) {
			throw new ServletException(e);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
