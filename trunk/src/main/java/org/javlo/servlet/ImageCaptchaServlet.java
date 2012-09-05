package org.javlo.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.service.CaptchaService;

import com.github.cage.Cage;
import com.github.cage.YCage;

public class ImageCaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// flush it in the response
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		ServletOutputStream responseOutputStream = response.getOutputStream();
		Cage cage = new YCage();
		String token = cage.getTokenGenerator().next();
		CaptchaService.getInstance(request.getSession()).setCurrentCaptchaCode(token);
		cage.draw(token, responseOutputStream);

		responseOutputStream.flush();
		responseOutputStream.close();
	}
}
