package org.javlo.filter;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class MinifyJsFilter implements Filter {

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

        if (realPath == null) {
            chain.doFilter(request, response);
            return;
        }

        File requestedFile = new File(realPath);
        if (!requestedFile.exists()) {
            boolean isMinified = requestURI.contains(".min.");

            // Déterminer le fichier source (sans ".min")
            String sourceFilePath = isMinified ? realPath.replace(".min", "") : realPath;
            File sourceFile = new File(sourceFilePath);

            if (sourceFile.exists()) {
                // Lire le fichier source
                String sourceCode = new String(Files.readAllBytes(sourceFile.toPath()), StandardCharsets.UTF_8);
                String processedCode = isMinified ? minifyJavaScript(sourceCode) : sourceCode;

                if (processedCode != null) {
                    // Sauvegarde le fichier généré
                    Files.write(requestedFile.toPath(), processedCode.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        // Continue le traitement normalement
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    /**
     * Minifie du JavaScript en utilisant Google Closure Compiler
     */
    private String minifyJavaScript(String jsCode) {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        SourceFile input = SourceFile.fromCode("input.js", jsCode);
        SourceFile extern = SourceFile.fromCode("externs.js", "");

        Result result = compiler.compile(extern, input, options);
        return compiler.toSource();
    }
}

