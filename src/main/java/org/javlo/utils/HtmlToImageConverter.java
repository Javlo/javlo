package org.javlo.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.xhtmlrenderer.simple.Graphics2DRenderer;

public class HtmlToImageConverter {
	
	 private static final int WIDTH = 1600;
	    private static final String IMAGE_FORMAT = "png";

	    public static void convertHtmlToImage(URL inUrl, String imageFilePath) {
	        try {
	            String url = inUrl.toExternalForm();
	            BufferedImage image = Graphics2DRenderer.renderToImageAutoSize(url, WIDTH, BufferedImage.TYPE_INT_ARGB);
	            ImageIO.write(image, IMAGE_FORMAT, new File(imageFilePath));
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static void main(String[] args) throws MalformedURLException {
	    	HtmlToImageConverter.convertHtmlToImage(new URL("https://www.javlo.org/"), "c:/trans/javlo2.png");
		}

}



