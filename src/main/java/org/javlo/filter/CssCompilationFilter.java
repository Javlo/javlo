package org.javlo.filter;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.context.FileContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CssCompilationFilter implements Filter {

	private static final Logger logger = Logger.getLogger(CssCompilationFilter.class.getName());

	private final LessCompiler lessCompiler = new LessCompiler();


	@Override
	public void init(FilterConfig filterConfig) {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String path = httpRequest.getServletPath();
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		String realPath = ResourceHelper.getRealPath(httpRequest.getServletContext(), path);

		File requestedFile = new File(realPath);
		logger.info("🔍 Requête pour : " + request);

		// Vérifier si le fichier existe déjà
		if (!requestedFile.exists()) {
			boolean isMinified = requestURI.contains(".min.");
			String baseFilePath = isMinified ? realPath.replace(".min", "") : realPath;

			// Recherche des fichiers source : .scss ou .less
			File scssFile = new File(baseFilePath.replace(".css", ".scss"));
			File lessFile = new File(baseFilePath.replace(".css", ".less"));
			File sourceFile = null;

			if (scssFile.exists()) {
				sourceFile = scssFile;
			} else if (lessFile.exists()) {
				sourceFile = lessFile;
			}

			if (sourceFile != null) {
				// Lire et compiler le fichier source

				String processedCode = null;
				if (sourceFile.getName().endsWith(".scss")) {
					processedCode = compileScssToCss(globalContext.isProd(), sourceFile);
				} else if (sourceFile.getName().endsWith(".less")) {
					processedCode = compileLessToCss(sourceFile);
				}

				if (processedCode != null) {
					if (isMinified) {
						processedCode = minifyCSS(processedCode);
					}
					// Enregistrer le fichier CSS compilé
					Files.write(Paths.get(requestedFile.getAbsolutePath()), processedCode.getBytes(StandardCharsets.UTF_8));
					logger.info("✅ Fichier CSS généré : " + requestedFile.getAbsolutePath());
				}
			} else {
				logger.warning("⚠️ Aucun fichier source trouvé pour : " + requestURI);
			}
		}
		// Continue le traitement normalement
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("🛑 CssCompilationFilter détruit.");
	}

	/**
	 * Compile du SCSS en CSS avec JSass
	 */
	/*private String compileScssToCss(String scssCode) {
		Compiler sassCompiler = new Compiler();
		Options options = new Options();
		try {
			Output output = sassCompiler.compileString(scssCode, options);
			return output.getCss();
		} catch (CompilationException e) {
			logger.log(Level.SEVERE, "❌ Erreur de compilation SCSS", e);
			return "/* Erreur SCSS : " + e.getMessage() + " /";
		}
	}*/

	private static String compileScssToCss(boolean prod, File in) throws IOException {
		URI inputFile = in.toURI();

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
			return output.getCss();

		} catch (CompilationException e) {
			throw new IOException(e);
		}
	}

	/*private static String compileScssToCss(boolean prod, File scssFile) throws IOException {
		Compiler compiler = new Compiler();
		Options options = new Options();

		File scssDirectory = scssFile.getParentFile();

		if (scssDirectory != null) {
			options.getIncludePaths().add(new File(scssDirectory.getAbsolutePath()));
		}

		if (!prod) {
			options.setSourceComments(true);
			options.setSourceMapContents(true);
			options.setSourceMapEmbed(true);
		}

		try {
			String scssCode = new String(Files.readAllBytes(scssFile.toPath()), StandardCharsets.UTF_8);
			Output output = compiler.compileString(scssCode, options);
			return output.getCss();

		} catch (CompilationException e) {
			throw new IOException("Erreur SCSS : " + e.getMessage(), e);
		}
	}*/

	/*private static String compileScssToCss(boolean prod, File scssFile) throws IOException {
		Compiler compiler = new Compiler();
		Options options = new Options();

		File scssDirectory = scssFile.getParentFile();

		if (scssDirectory != null) {
			options.getIncludePaths().add(new File(scssDirectory.getAbsolutePath()));
		}

		if (!prod) {
			options.setSourceComments(true);
			options.setSourceMapContents(true);
			options.setSourceMapEmbed(true);
		}

		try {
			String scssCode = new String(Files.readAllBytes(scssFile.toPath()), StandardCharsets.UTF_8);
			Output output = compiler.compileString(scssCode, options);
			return output.getCss();

		} catch (CompilationException e) {
			throw new IOException("Erreur SCSS : " + e.getMessage(), e);
		}
	}*/

	public String compileLessToCss(File lessFile) {
		try {
			logger.info("🔄 Compilation du fichier LESS : " + lessFile.getAbsolutePath());
			return lessCompiler.compile(lessFile);
		} catch (LessException | IOException e) {
			logger.log(Level.SEVERE, "❌ Erreur de compilation LESS", e);
			return "/* Erreur LESS : " + e.getMessage() + " */";
		}
	}

	/**
	 * Minifie du CSS en supprimant les espaces inutiles
	 */
	private String minifyCSS(String cssCode) {
		return cssCode.replaceAll("\\s+", " ") // Supprime les espaces inutiles
				.replaceAll("\\s*([:;,{}])\\s*", "$1") // Réduit espaces autour des caractères CSS
				.trim();
	}

}
