package org.javlo.macro.interactive;

import java.util.logging.Logger;

public class CreateArticleWidthTemplatesYear extends CreateArticleWidthTemplates {

    private static Logger logger = Logger.getLogger(CreateArticleWidthTemplatesYear.class.getName());

    @Override
    public String getName() {
        return "create-article-width-templates-year";
    }

    @Override
    public boolean isMonth() {
        return false;
    }
}