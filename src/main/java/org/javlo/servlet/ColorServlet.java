package org.javlo.servlet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.image.ColorImageEngine;

/**
 * @author pvandermaesen
 * 
 * 
 */
public class ColorServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final String COLOR_SERVLET_FOLDER = "/WEB-INF/images/corner/";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ColorServlet.class.getName());

	protected void sendError(HttpServletResponse response, String msg) throws IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
	}

	/**
	 * get the text and the picture and build a button
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			OutputStream out = null;
			String fileName = (new File(request.getPathInfo()).getName());
			if (!fileName.endsWith(".png")) {
				sendError(response, "bad extension user png sample : v_120_34e234_ffffff.png");
				return;
			}

			fileName = fileName.replace(".png", "");
			String[] data = fileName.split("_");

			if (data[0].toLowerCase().equals("corner")) {
				if (data.length != 4) {
					sendError(response, "bad format sample : corner_top_300_ffffff.png (corner on top width 300px bgcolor of internal zone is white.");
					return;
				}
				boolean top = data[1].equalsIgnoreCase("top");
				int size = Integer.parseInt(data[2]);
				Color bgColor = Color.decode('#' + data[3]);

				File leftImage = new File(getServletContext().getRealPath(COLOR_SERVLET_FOLDER + "left_bottom_middle.png"));
				File rightImage = new File(getServletContext().getRealPath(COLOR_SERVLET_FOLDER + "right_bottom_middle.png"));
				if (top) {
					leftImage = new File(getServletContext().getRealPath(COLOR_SERVLET_FOLDER + "left_top_middle.png"));
					rightImage = new File(getServletContext().getRealPath(COLOR_SERVLET_FOLDER + "right_top_middle.png"));
				}
				BufferedImage result = ColorImageEngine.getCornerLine(size, leftImage, rightImage, bgColor);
				response.setContentType("image/PNG");
				out = response.getOutputStream();
				ImageIO.write(result, "png", out);
			} else {

				if (data.length != 4) {
					sendError(response, "bad format sample : v_120_34e234_ffffff.png or v_120_34e234_t128.png ( from 34e234 to transparance (alpha 128) ).");
					return;
				}

				if (!data[0].toLowerCase().startsWith("v") && !data[0].toLowerCase().startsWith("h")) {
					sendError(response, "bad format start with v (vertical) or h (horizontal) sample : v_120_34e234_ffffff.png or v_120_34e234_t128.png ( from 34e234 to transparance (alpha 128) ).");
					return;
				}

				boolean vertical = data[0].toLowerCase().startsWith("v");
				int margin = 0;
				try {
					margin = Integer.parseInt(data[0].substring(1));
				} catch (NumberFormatException e1) {
					margin = 0;
				}
				int size;
				try {
					size = Integer.parseInt(data[1]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sendError(response, "bad size format sample : v_120_34e234_ffffff.png");
					return;
				}
				Color startColor = null;
				Color endColor = null;
				int alphaMax = 255;
				try {
					if (!data[2].contains("t")) {
						try {
							startColor = Color.decode('#' + data[2]);
						} catch (NumberFormatException e) {
							logger.warning(e.getMessage());
						}
					} else {
						String alphaStr = data[2].replace("t", "");
						try {
							alphaMax = Integer.parseInt(alphaStr);
						} catch (NumberFormatException e) {
							alphaMax = 255;
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sendError(response, "first color is bad : " + data[2] + " (sample : v_120_34e234_ffffff.png)");
					return;
				}
				try {
					if (!data[3].contains("t")) {
						endColor = Color.decode('#' + data[3]);
					} else {
						String alphaStr = data[3].replace("t", "");
						try {
							alphaMax = Integer.parseInt(alphaStr);
						} catch (NumberFormatException e) {
							alphaMax = 255;
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					sendError(response, "second color is bad : " + data[3] + " (sample : v_120_34e234_ffffff.png)");
					return;
				}

				response.setContentType("image/PNG");
				out = response.getOutputStream();
				System.out.println("***** ColorServlet.processRequest : vertical = "+vertical); //TODO: remove debug trace
				System.out.println("***** ColorServlet.processRequest : margin = "+margin); //TODO: remove debug trace
				System.out.println("***** ColorServlet.processRequest : size = "+size); //TODO: remove debug trace
				ImageIO.write(ColorImageEngine.getDegrade(vertical, margin, size, startColor, endColor, alphaMax), "png", out);
			}
		} catch (IOException e) {
			throw new ServletException(e);
		}

	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

}
