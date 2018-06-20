package org.javlo.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.lesscss.LessCompiler;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.FileContext;
//
//import com.vaadin.sass.internal.ScssContext;
//import com.vaadin.sass.internal.ScssStylesheet;
//import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
//import com.vaadin.sass.internal.handler.SCSSErrorHandler;

public class CssCompilationFilter implements Filter {

	private static Logger logger = Logger.getLogger(CssCompilationFilter.class.getName());

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getServletPath();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		//File cssFile = new File(httpRequest.getSession().getServletContext().getRealPath(path));
		File cssFile = new File(ResourceHelper.getRealPath(httpRequest.getSession().getServletContext(), path));
		boolean compileFile = !cssFile.exists();
		File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
		File sassFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".scss");
		if (!compileFile) {
			if (!StaticConfig.getInstance(((HttpServletRequest) request).getSession().getServletContext()).isProd()) {
				if (lessFile.lastModified() > cssFile.lastModified()) {
					compileFile = true;
				}
				if (sassFile.lastModified() > cssFile.lastModified()) {
					compileFile = true;
				}
			}
		}
		if (compileFile) {
			if (!globalContext.getContextKey().equals(globalContext.getMainContextKey())) {
				lessFile = new File(StringUtils.replaceOnce(lessFile.getAbsolutePath(), File.separator + globalContext.getMainContextKey() + File.separator, File.separator + globalContext.getContextKey() + File.separator));
				cssFile.getParentFile().mkdirs();
			}
			if (lessFile.exists()) {
				if (compile(lessFile, cssFile, globalContext.getStaticConfig().isProd())) {
					try {
						Thread.sleep(5 * 1000); // check why on linux we need
												// the sleep.
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (sassFile.exists()) {
				StaticConfig staticConfig = StaticConfig.getInstance(httpRequest.getSession().getServletContext());
				if (compileSass(staticConfig.isProd(), sassFile, cssFile)) {
					try {
						Thread.sleep(5 * 1000); // check why on linux we need
												// the sleep.
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		next.doFilter(request, response);
	}

//	private static boolean compileSass(boolean prod, File in, File out) {
//		ScssContext.UrlMode urlMode = ScssContext.UrlMode.MIXED;
//
//		boolean minify = true;
//		boolean ignoreWarnings = true;
//		try {
//
//			if (!in.canRead()) {
//				System.err.println(in.getCanonicalPath() + " could not be read!");
//				System.exit(-1);
//			}
//			String input = in.getCanonicalPath();
//
//			SCSSErrorHandler errorHandler = new SCSSErrorHandler();
//			errorHandler.setWarningsAreErrors(!ignoreWarnings);
//
//			// Parse stylesheet
//			ScssStylesheet scss = ScssStylesheet.get(input, null, new SCSSDocumentHandlerImpl(), errorHandler);
//			if (scss == null) {
//				System.err.println("The scss file " + input + " could not be found.");
//				return false;
//			}
//
//			// Compile scss -> css
//			scss.compile(urlMode);
//
//			// Write result
//			Writer writer = null;
//			try {
//				writer = createOutputWriter(out.getAbsolutePath());
//				scss.write(writer, minify);
//				writer.close();
//			} finally {
//				ResourceHelper.safeClose(writer);
//			}
//
//			if (errorHandler.isErrorsDetected()) {
//				logger.warning("error on sass transform.");
//				return false;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
	
	private static boolean compileSass(boolean prod, File in, File out) throws IOException {
		URI inputFile = in.toURI();	    	    
	    if (!out.exists()) {
	    	out.createNewFile();
	    }

	    Compiler compiler = new Compiler();
	    Options options = new Options();
//	    URI mapFile;
//		try {
//			mapFile = new URI(URLHelper.mergePath(out.getParentFile().getAbsolutePath(), out.getName()+".map"));
//		} catch (URISyntaxException e1) {
//			throw new IOException(e1);
//		}
//		System.out.println(">>>>>>>>> CssCompilationFilter.compileSass : mapFile = "+mapFile); //TODO: remove debug trace
	    if (!prod) {
	    	options.setSourceComments(true);
	    	options.setSourceMapContents(true);
	    	options.setSourceMapEmbed(true);
	    }
//	    options.setSourceMapFile(mapFile);
	    try {
	      FileContext context = new FileContext(inputFile, null, options);
	      Output output = compiler.compile(context);	      
	      ResourceHelper.writeStringToFile(out, output.getCss());
	      
	    } catch (CompilationException e) {
	      throw new IOException(e);
	    }
	    
	    return true;
	}

	private static Writer createOutputWriter(String filename) throws IOException {
		if (filename == null) {
			return new OutputStreamWriter(System.out, ContentContext.CHARACTER_ENCODING);
		} else {
			File file = new File(filename);
			return new FileWriter(file);
		}
	}

	private static boolean compile(File lessFile, File cssFile, boolean compress) {
		LessCompiler lessCompiler = new LessCompiler();
		FileOutputStream out = null;
		try {
			lessCompiler.setEncoding(ContentContext.CHARACTER_ENCODING);
			//lessCompiler.setCompress(compress);
			String cssContent = lessCompiler.compile(lessFile);
			out = new FileOutputStream(cssFile);
			ResourceHelper.writeStringToStream(cssContent, out, ContentContext.CHARACTER_ENCODING);
			out.flush();
			out.getFD().sync();
			return true;
		} catch (Exception e) {
			logger.severe("error on less file '" + lessFile + "' : " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	public static void main(String[] args) throws IOException {
		File sassFile = new File("C:/opt/tomcat8/webapps/javlo/wktp/bootstrap-4.0.0/sexy/scss/bootstrap.scss");
		File lessFile = new File("c:/trans/test.less");
		File cssFile = new File("c:/trans/test_sass.css");
		//compileSass(sassFile, cssFile);
		cssFile = new File("c:/trans/test_less.css");
		System.out.println("sassFile file exist : "+sassFile.exists());
		compileSass(false, sassFile, cssFile);
	}

}
