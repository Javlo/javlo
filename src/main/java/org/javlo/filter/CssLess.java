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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.lesscss.LessCompiler;

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
		boolean compileLess = !cssFile.exists();
		File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
		if (!compileLess) {
			if (!StaticConfig.getInstance(((HttpServletRequest) request).getSession().getServletContext()).isProd()) {
				if (lessFile.lastModified() > cssFile.lastModified()) {
					compileLess = true;
				}
			}
		}
		if (compileLess) {
			if (!globalContext.getContextKey().equals(globalContext.getMainContextKey())) {
				lessFile = new File(StringUtils.replaceOnce(lessFile.getAbsolutePath(), File.separator+globalContext.getMainContextKey()+File.separator, File.separator+globalContext.getContextKey()+File.separator));
				cssFile.getParentFile().mkdirs();
			}
			if (lessFile.exists()) {
				if (compile (lessFile, cssFile)) {
					System.out.println("***** CssLess.doFilter : cssFile exist : "+cssFile.exists()); //TODO: remove debug trace
					((HttpServletResponse)response).setStatus(HttpServletResponse.SC_ACCEPTED);
				} else {
					System.out.println("***** CssLess.doFilter : ERROR COMPILE LESSs"); //TODO: remove debug trace
				}
			}
		}
		next.doFilter(request, response);
	}
	
	private static boolean compile(File lessFile, File cssFile) {		
		LessCompiler lessCompiler = new LessCompiler();
		try {
			lessCompiler.setEncoding(ContentContext.CHARACTER_ENCODING);					
			lessCompiler.compile(lessFile, cssFile);
			return true;
		} catch (Exception e) {
			logger.severe("error on less file '"+lessFile+"' : "+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}
	
	public static void main(String[] args) {
		// Instantiate the LESS compiler
		LessCompiler lessCompiler = new LessCompiler();
		System.out.println("***** CssLess.main : less js = "+lessCompiler.getLesscJs()); //TODO: remove debug tracelessCompiler.getLesscJs();
		try {			
			File bootStrap = new File("C:/Users/pvandermaesen/Dropbox/work/data/javlo/template/bootstrap-3.2.0/css/bootstrap.less");			
			compile(bootStrap, new File("c:/trans/bootstrap.css"));			
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}

}

