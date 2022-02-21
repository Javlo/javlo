/*package org.javlo.component.thymeleaf;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogFilter implements Filter {

    private ServletContext context;
    public LogFilter() {
    }


    /* @Override
     public void init(FilterConfig fConfig) throws ServletException
     {
         System.out.println("LogFilter init!");
     }

 */

     /*
    public void init(FilterConfig fConfig) throws ServletException {
        this.context = fConfig.getServletContext();
        this.context.log("AuthenticationFilter initialized");
    }

    @Override
    public void destroy()
    {
        System.out.println("LogFilter destroy!");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        HttpServletResponse res = (HttpServletResponse) response;

        String uri = ((HttpServletRequest) request).getRequestURI();

        this.context.log("Requested Resource::"+uri);

        HttpSession session = req.getSession(false);

        if(session == null && !(uri.endsWith("html"))){
            this.context.log("Unauthorized access request");

            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        else{
            // pass the request along the filter chain
            chain.doFilter(request, response);
        }

       /* System.out.println("#INFO " + new Date() + " - ServletPath :" + servletPath //
                + ", URL =" + req.getRequestURL());

        chain.doFilter(request,response);


    }



}
*/