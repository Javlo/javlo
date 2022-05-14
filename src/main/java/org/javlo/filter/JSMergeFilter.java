package org.javlo.filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.lesscss.LessCompiler;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.FileContext;

public class JSMergeFilter implements Filter {

	private static Logger logger = Logger.getLogger(JSMergeFilter.class.getName());
	
	private static void mimifyFolder(GlobalContext globalContext, File jsFile) throws IOException {
		File jsDir = jsFile.getParentFile();
		if (jsDir.exists()) {
			Collection<File> jsFiles;
			if (jsFile.getName().contains("rec")) {
				jsFiles = ResourceHelper.getAllFiles(jsDir, new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return StringHelper.getFileExtension(pathname.getName()).equalsIgnoreCase("js");
					}
				});
			} else {
				File[] files = jsDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return StringHelper.getFileExtension(name).equalsIgnoreCase("js");
					}
				});
				jsFiles = new LinkedList<>();
				for (File file : files) {
					jsFiles.add(file);
				}
			}
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			for (File jsFilePart : jsFiles) {
				if (!jsFilePart.getName().startsWith("_")) {
					if (globalContext == null || !globalContext.getStaticConfig().isProd()) {
						out.println("");
						out.println("");
						out.println("/**** " + jsFilePart.getName() + " ****/");
						out.println("");
					}
					out.println('{'+ResourceHelper.loadStringFromFile(jsFilePart)+'}');
				}
			}
			out.flush();
			jsFile.createNewFile();
			String finalJS = new String(outStream.toByteArray());
			if (globalContext != null && globalContext.getStaticConfig().isProd()) {
				finalJS = ResourceHelper.mimifyJS(finalJS);
			}
			ResourceHelper.writeStringToFile(jsFile, finalJS, ContentContext.CHARACTER_ENCODING);
			out.close();
		} else {
			logger.severe("js folder not found : " + jsDir);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain next) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String path = httpRequest.getServletPath();
		String fileName = StringHelper.getFileNameFromPath(path);
		if (fileName.equals("_all.js") || fileName.equals("_all_rec.js")) {
			GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
			if (path.startsWith('/' + globalContext.getContextKey())) {
				path = path.replaceFirst('/' + globalContext.getContextKey(), "");
			}
			File jsFile = new File(ResourceHelper.getRealPath(httpRequest.getSession().getServletContext(), path));
			if (!jsFile.exists()) {
				synchronized (this) {
					if (!jsFile.exists()) {
						mimifyFolder(globalContext, jsFile);
					}
				}
			} else {
				logger.severe("not found : "+jsFile);
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
		File dir = new File("C:/opt/tomcat8/webapps/javlo/wktp/bootstrap-4.1.0/sexy/main_lib/_all_rec.js");
		mimifyFolder(null, dir);

	}

}
