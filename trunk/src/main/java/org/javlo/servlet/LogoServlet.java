package org.javlo.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoServlet extends HttpServlet {

	private static final String getSVG(String color) {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		out.println("<!-- Generator: Adobe Illustrator 18.0.0, SVG Export Plug-In . SVG Version: 6.00 Build 0)  -->");
		out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
		out.println("<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"");
		out.println("viewBox=\"246.2 375.4 119.7 41.3\" enable-background=\"new 246.2 375.4 119.7 41.3\" xml:space=\"preserve\">");
		out.println("<g>");
		out.println("<path fill=\""+color+"\" d=\"M265.5,402.5c-0.3,2.2-0.8,4.2-1.5,5.9s-1.7,3.2-2.8,4.5c-1.1,1.2-2.5,2.2-4.1,2.8c-1.6,0.7-3.3,1-5.4,1");
		out.println("c-1,0-1.9-0.1-2.8-0.2s-1.8-0.3-2.7-0.5l0.1-0.5c0.4-2.7,2.7-4.7,5.4-4.6l0,0c0.8,0,1.6-0.1,2.3-0.4s1.4-0.8,1.9-1.4");
		out.println("c0.5-0.7,1-1.5,1.4-2.5c0.4-1,0.7-2.3,0.8-3.8l2.5-20.3c0.5-4,3.9-7.1,7.9-7.1h0.1L265.5,402.5z\"/>");
		out.println("<path fill=\""+color+"\" d=\"M287.2,416.1L287.2,416.1l-0.1-5.8c-1.3,1.9-2.6,3.5-4.2,4.6c-1.6,1.1-3.3,1.7-5.1,1.7c-1.1,0-2-0.2-3-0.7");
		out.println("c-0.9-0.4-1.7-1.1-2.4-1.9s-1.3-1.9-1.6-3.1c-0.4-1.3-0.6-2.7-0.6-4.4s0.2-3.3,0.7-4.9c0.4-1.6,1-3.1,1.8-4.5");
		out.println("c0.8-1.4,1.7-2.7,2.8-3.9c1.1-1.2,2.3-2.2,3.7-3c1.4-0.8,2.9-1.5,4.4-2c1.6-0.5,3.2-0.7,5-0.7c1.3,0,2.5,0.1,3.8,0.3c0,0,0,0,0.1,0");
		out.println("c2.1,0.3,3.5,2.3,3.3,4.4l-2.3,18C293,413.5,290.4,416,287.2,416.1z M280.4,411.1c0.8,0,1.5-0.2,2.2-0.8c0.7-0.5,1.4-1.2,2-2.2");
		out.println("c0.7-0.9,1.3-2,1.8-3.2c0.5-1.3,1-2.6,1.4-3.9l1.1-8.5c-0.5-0.1-1-0.1-1.4-0.1c-1.5,0-2.9,0.4-4.2,1c-1.3,0.7-2.5,1.6-3.4,2.8");
		out.println("c-1,1.2-1.7,2.6-2.2,4.1c-0.5,1.6-0.8,3.2-0.8,5c0,2,0.3,3.4,1,4.4C278.5,410.6,279.3,411.1,280.4,411.1z\"/>");
		out.println("<path fill=\""+color+"\" d=\"M299.5,388.1h1c0.1,0,0.6,0,1.2,0.1c2.9,0.2,5.3,2.3,5.8,5c0,0.1,2.3,11.7,2.6,13.6c0,0.1,0,0.3,0.1,0.4");
		out.println("c0.1,0.8,0.1,1.7,0.2,2.4c0.2-0.9,0.5-1.7,0.8-2.6s0.7-1.8,1.1-2.7c0,0,5.4-12.2,5.5-12.2c1.2-2.3,3.5-3.9,6.2-4");
		out.println("c0.6-0.1,1-0.1,1.1-0.1h1.3L315,411.5c-1.6,3.1-4.7,5.1-8.2,5.1h-0.1L299.5,388.1z\"/>");
		out.println("<path fill=\""+color+"\" d=\"M326.9,416.6l5-41.2h0.1c3.5,0,6.2,3,5.8,6.6l-3.3,28.1C333.9,413.8,330.7,416.6,326.9,416.6L326.9,416.6");
		out.println("L326.9,416.6z\"/>");
		out.println("<path fill=\""+color+"\" d=\"M350.9,416.6c-1.6,0-3-0.2-4.3-0.8c-1.3-0.5-2.5-1.3-3.4-2.3c-1-1-1.7-2.3-2.3-3.7c-0.5-1.5-0.8-3.2-0.8-5");
		out.println("c0-2.5,0.4-4.7,1.2-6.8s1.9-3.9,3.2-5.5c1.4-1.6,2.9-2.7,4.7-3.6s3.7-1.3,5.7-1.3c1.6,0,3,0.2,4.3,0.8c1.3,0.5,2.5,1.3,3.5,2.3");
		out.println("c1,1,1.7,2.3,2.3,3.7c0.5,1.5,0.8,3.2,0.8,5c0,2.4-0.4,4.7-1.2,6.8s-1.9,3.9-3.2,5.5c-1.4,1.6-2.9,2.7-4.7,3.6");
		out.println("C354.9,416.2,353,416.6,350.9,416.6z M351.5,411.4c1.1,0,2.2-0.3,3.1-1c0.9-0.7,1.7-1.5,2.3-2.6c0.7-1.1,1.1-2.3,1.5-3.8");
		out.println("c0.4-1.4,0.5-3,0.5-4.5c0-2.3-0.4-4-1.2-5.1s-2-1.6-3.5-1.6c-1.1,0-2.2,0.3-3.1,1c-0.9,0.7-1.7,1.5-2.3,2.6");
		out.println("c-0.7,1.1-1.1,2.4-1.5,3.8s-0.5,3-0.5,4.6c0,2.3,0.4,3.9,1.1,5C348.8,410.9,350,411.4,351.5,411.4z\"/>");
		out.println("</g>");
		out.println("</svg>");

		out.close();
		return new String(outStream.toByteArray());

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String color = request.getParameter("color");
		if (color == null) {
			color = "cccccc";
		}
		PrintStream out;
		try {
			response.setContentType("image/svg+xml;charset=UTF-8");
			response.setHeader("Cache-Control", "max-age=3600,must-revalidate");
			out = new PrintStream(response.getOutputStream());
			out.print(getSVG('#' + color));
			out.flush();
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
