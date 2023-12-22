package org.javlo.utilThymeleaf;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class ThymeleafConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        TemplateEngine engine = templateEngine(sce.getServletContext());
        TemplateEngineUtil.storeTemplateEngine(sce.getServletContext(), engine);
        System.out.println("ThymeleafConfig");
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    private TemplateEngine templateEngine(ServletContext servletContext) {
        System.out.println("******* templateEngine *******");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver(servletContext));
        System.out.println("ThymeleafConfig template engine");
        return engine;

    }

    private ITemplateResolver templateResolver(final ServletContext servletContext) {
       /* ServletContextTemplateResolver resolver = new ServletContextTemplateResolver(servletContext);

        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;*/
        return null;
    }



}


