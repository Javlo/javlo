package org.javlo.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

public class CleanHTML implements javax.servlet.Filter {

	private static class ResponseWrapper extends HttpServletResponseWrapper {

		private PrintWriter printWriter = null;
		private StringWriter writer = null;
		private ByteArrayOutputStream stream = null;
		private ServletOutputStream servletOutputStream = null;

		public ResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if (printWriter != null) {
				throw new IOException("writer already retrieved.");
			} else {
				if (servletOutputStream == null) {
					stream = new ByteArrayOutputStream();
					servletOutputStream = new ServletOutputStream() {

						@Override
						public void write(byte[] b, int off, int len) throws IOException {
							stream.write(b, off, len);
						}

						@Override
						public void write(int b) throws IOException {
							stream.write(b);
						}
					};
				}
				return servletOutputStream;
			}
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			if (servletOutputStream != null) {
				throw new IOException("servletOutputStream already retrieved.");
			} else {
				if (printWriter == null) {
					writer = new StringWriter();
					printWriter = new PrintWriter(writer, true);
				}
				return printWriter;
			}
		}

	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {		
		if (StringHelper.isTrue(request.getParameter("clean-html"))) {
			ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
			next.doFilter(request, responseWrapper);
			Document doc;
			if (responseWrapper.stream != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(responseWrapper.stream.toByteArray());
				ContentContext ctx;
				try {
					ctx = ContentContext.getContentContext((HttpServletRequest)request, (HttpServletResponse) response);
					doc = Jsoup.parse(in, ContentContext.CHARACTER_ENCODING, URLHelper.createStaticURL(ctx, "/"));					
					in.close();					
				} catch (Exception e) {					
					throw new ServletException(e);
				}
			} else {
				doc = Jsoup.parse(responseWrapper.writer.toString());
			}		
			EscapeMode.xhtml.getMap().put('\u00A0', "#160");
			doc.outputSettings().escapeMode(EscapeMode.xhtml);
			response.getWriter().print(doc.outerHtml());
		} else {
			next.doFilter(request, response);
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}	

}
