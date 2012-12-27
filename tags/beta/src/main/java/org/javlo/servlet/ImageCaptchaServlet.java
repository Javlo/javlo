package org.javlo.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;
import org.javlo.service.CaptchaService;

import com.github.cage.Cage;
import com.github.cage.IGenerator;

public class ImageCaptchaServlet extends HttpServlet {

	private static class JCage extends Cage {

		private IGenerator generator = new JGenerator();

		private JCage(int size) {
			JGenerator jGenerator = new JGenerator();
			jGenerator.size = size;
			generator = jGenerator;
		}

		private static class JGenerator implements IGenerator<String> {

			private int size = 4;

			@Override
			public String next() {
				return StringHelper.getRandomString(size, "0123456789abefhklrstwxyz");
			}

		}

		@Override
		public IGenerator<String> getTokenGenerator() {
			return generator;
		}

	}

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
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
		Cage cage = new JCage(staticConfig.getCaptchaSize());
		String token = cage.getTokenGenerator().next();
		CaptchaService.getInstance(request.getSession()).setCurrentCaptchaCode(token);
		cage.draw(token, responseOutputStream);

		responseOutputStream.flush();
		responseOutputStream.close();
	}
}
