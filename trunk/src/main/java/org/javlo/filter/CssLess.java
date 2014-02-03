package org.javlo.filter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;
import org.javlo.helper.XHTMLHelper;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

public class CssLess implements Filter {
	
	private static Logger logger = Logger.getLogger(CssLess.class.getName());

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getServletPath();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		File cssFile = new File(httpRequest.getSession().getServletContext().getRealPath(path));
		if (!cssFile.exists()) {		
			File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");			
			if (lessFile.exists()) {				
				XHTMLHelper.expandCSSImports(lessFile);
				LessCompiler lessCompiler = new LessCompiler();
				try {
					lessCompiler.compile(lessFile, cssFile);
				} catch (LessException e) {
					logger.severe("error on less file '"+lessFile+"' : "+e.getMessage());
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
