package org.javlo.filter;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.FileContext;
import org.apache.commons.lang3.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.lesscss.LessCompiler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URI;
import java.util.logging.Logger;

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
		// File cssFile = new
		// File(httpRequest.getSession().getServletContext().getRealPath(path));
		File cssFile = new File(ResourceHelper.getRealPath(httpRequest.getSession().getServletContext(), path));
		boolean compileFile = !cssFile.exists();
		if (!compileFile) {
			if (!globalContext.isProd()) {

				ScssCssCleaner.cleanOldCssFiles(cssFile.getParentFile().getAbsolutePath());

				//File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
				//File sassFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".scss");
				
//				if (lessFile.exists() || sassFile.exists()) {
//
//					long latest = ResourceHelper.getLatestModificationFileOnFolder(cssFile.getParentFile(), "scss", "less");
//
//					if (latest > cssFile.lastModified()) {
//						compileFile = true;
//						cssFile.delete();
//					}
//
//				}
//				if (latest > cssFile.lastModified()) {
//					compileFile = true;
//					cssFile.delete();
//				}
			}
		}
		if (compileFile) {
			synchronized (globalContext) {
				if (!cssFile.exists()) {
					File lessFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".less");
					File sassFile = new File(cssFile.getAbsolutePath().substring(0, cssFile.getAbsolutePath().length() - 4) + ".scss");
					long startTime = System.currentTimeMillis();
					if (!globalContext.getContextKey().equals(globalContext.getSourceContextKey())) {
						lessFile = new File(StringUtils.replaceOnce(lessFile.getAbsolutePath(), File.separator + globalContext.getSourceContextKey() + File.separator, File.separator + globalContext.getContextKey() + File.separator));
						cssFile.getParentFile().mkdirs();
					}
					File tempCssFile = new File(cssFile.getAbsolutePath() + ".__TEMP.css");
					boolean sass = false;
					if (sassFile.exists()) {
						sass = true;
						StaticConfig staticConfig = StaticConfig.getInstance(httpRequest.getSession().getServletContext());
						compileSass(globalContext.isProd(), sassFile, tempCssFile);
					} else if (lessFile.exists()) {
						compile(lessFile, tempCssFile, globalContext.isProd());
					}
					if (!tempCssFile.renameTo(cssFile)) {
						logger.severe("error : rename file:" + tempCssFile + " to " + cssFile);
					} else {
						logger.info("compile "+(sass?"sass":"less")+" ("+(System.currentTimeMillis()-startTime)/1000+" sec.) : "+cssFile);
					}
				}
			}
		}
		next.doFilter(request, response);
	}

	private static boolean compileSass(boolean prod, File in, File out) throws IOException {
		URI inputFile = in.toURI();
		if (!out.exists()) {
			out.createNewFile();
		}

		Compiler compiler = new Compiler();
		Options options = new Options();
		if (!prod) {
			options.setSourceComments(true);
			options.setSourceMapContents(true);
			options.setSourceMapEmbed(true);
		}
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
			// lessCompiler.setCompress(compress);
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
		// compileSass(sassFile, cssFile);
		cssFile = new File("c:/trans/test_less.css");
		System.out.println("sassFile file exist : " + sassFile.exists());
		compileSass(false, sassFile, cssFile);
	}

}
