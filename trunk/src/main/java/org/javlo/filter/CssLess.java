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

import org.apache.commons.lang.StringUtils;
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
		if (!cssFile.exists()) {		
			File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
			
			if (!globalContext.getContextKey().equals(globalContext.getMainContextKey())) {
				lessFile = new File(StringUtils.replaceOnce(lessFile.getAbsolutePath(), File.separator+globalContext.getMainContextKey()+File.separator, File.separator+globalContext.getContextKey()+File.separator));
				cssFile.getParentFile().mkdirs();
			}
			if (lessFile.exists()) {
				compile (lessFile, cssFile);
			}
		}
		next.doFilter(request, response);
	}
	
	private static void compile(File lessFile, File cssFile) {		
		LessCompiler lessCompiler = new LessCompiler();
		try {
			lessCompiler.setEncoding(ContentContext.CHARACTER_ENCODING);					
			lessCompiler.compile(lessFile, cssFile);
		} catch (Exception e) {
			logger.severe("error on less file '"+lessFile+"' : "+e.getMessage());
			e.printStackTrace();
		}
	}

	/*@Override
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
				//XHTMLHelper.expandCSSImports(lessFile);
				InputStream in = null;
				OutputStream out = null;
				try {					
					Less less = Less.compiler();
					in = new FileInputStream(lessFile);
					out = new FileOutputStream(cssFile);
					less.transform(null, in, out);
					//lessCompiler.compile(null, cssFile);
					
				} catch (Exception e) {
					logger.severe("error on less file '"+lessFile+"' : "+e.getMessage());
					e.printStackTrace();
				} finally {
					ResourceHelper.closeResource(in);
					ResourceHelper.closeResource(out);
				}
			}
		}
		next.doFilter(request, response);
	}*/
	
/*	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
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
				// Instantiates a new LessEngine
				LessEngine engine = new LessEngine();
				// Creates a new file containing the compiled content
				try {
					engine.compile(lessFile,cssFile);
				} catch (LessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		next.doFilter(request, response);
	} */

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

