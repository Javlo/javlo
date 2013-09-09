package org.javlo.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;

public class TestResponse implements HttpServletResponse {

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub		
	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCharacterEncoding() { 
		return ContentContext.CHARACTER_ENCODING;
	}

	@Override
	public String getContentType() { 
		return null;
	}

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return url;
	}

	@Override
	public String encodeRedirectUrl(String url) {
		return url;
	}

	@Override
	public String encodeURL(String url) {
		return url;
	}

	@Override
	public String encodeUrl(String url) {
		return url;
	}

	@Override
	public void sendError(int arg0) throws IOException {
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
	}

	@Override
	public void setHeader(String arg0, String arg1) {
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
	}

	@Override
	public void setStatus(int arg0) {
	}

	@Override
	public void setStatus(int arg0, String arg1) {
	}

}
