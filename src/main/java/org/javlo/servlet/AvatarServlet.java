package org.javlo.servlet;

import org.javlo.helper.ResourceHelper;
import org.owasp.encoder.Encode;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class AvatarServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("image/svg+xml");
        String text = request.getParameter("text");
        if (text == null) {
            text = "";
        }
        /*
         * the text lands inside a text element of the svg, which is served same
         * origin and can carry script : it has to be xml escaped.
         */
        text = Encode.forXmlContent(text);
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"#eee\" viewBox=\"0 0 160 160\">" +
                "<circle cx=\"80\" cy=\"80\" r=\"70\"/>" +
                "<text x=\"50%\" y=\"85\" dominant-baseline=\"middle\" text-anchor=\"middle\" fill=\"#999\" style=\"font-family: Arial; font-size: 50px\">"+text+"</text>" +
                "</svg>";
        ResourceHelper.writeStringToStream(svg, response.getOutputStream());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
