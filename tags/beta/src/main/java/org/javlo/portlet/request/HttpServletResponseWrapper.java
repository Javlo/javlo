package org.javlo.portlet.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

public class HttpServletResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {

	private final Writer writer;
	private final PrintWriter out;
	
	public HttpServletResponseWrapper(HttpServletResponse response, Writer writer) {
		super(response);
		
		this.writer = writer;
		out = new PrintWriter(this.writer);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return out;
	}
	
	public String getContent() {
		return writer.toString();
	}
}
