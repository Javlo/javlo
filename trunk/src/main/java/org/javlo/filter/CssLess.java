package org.javlo.filter;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

public class CssLess implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getServletPath();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		File cssFile = new File(httpRequest.getSession().getServletContext().getRealPath(path));
		if (!cssFile.exists() || !globalContext.getStaticConfig().isLessCache()) {
			File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
			if (lessFile.exists()) {
				LessCompiler lessCompiler = new LessCompiler();
				try {
					lessCompiler.compile(lessFile, cssFile);
				} catch (LessException e) {
					e.printStackTrace();
				}
			}
		}
		next.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
