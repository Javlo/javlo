package org.javlo.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.ServletHelper;

public class JSPFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext((HttpServletRequest) request, (HttpServletResponse) response);
			ServletHelper.prepareModule(ctx);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
		next.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
