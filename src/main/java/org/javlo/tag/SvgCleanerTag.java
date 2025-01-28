package org.javlo.tag;

import jakarta.servlet.jsp.tagext.TagSupport;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.JspException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgCleanerTag extends TagSupport {
    private String svgContent;

    public void setSvgContent(String svgContent) {
        this.svgContent = svgContent;
    }

    @Override
    public int doStartTag() throws JspException {
        if (svgContent == null || svgContent.isEmpty()) {
            return SKIP_BODY;
        }

        try {
            // Supprimer width et height uniquement dans la balise <svg>
            Pattern svgPattern = Pattern.compile("<svg([^>]*)(\\s*width=\"[^\"]*\"|\\s*height=\"[^\"]*\")([^>]*)>");
            Matcher svgMatcher = svgPattern.matcher(svgContent);
            String cleanedSvg = svgMatcher.replaceFirst("<svg$1$3>");

            // Supprimer fill partout
            cleanedSvg = cleanedSvg.replaceAll("\\s*fill=\"[^\"]*\"", "");

            // Écrire le résultat
            JspWriter out = pageContext.getOut();
            out.write(cleanedSvg);

        } catch (Exception e) {
            throw new JspException("Error in SvgCleanerTag", e);
        }
        return SKIP_BODY;
    }
}
