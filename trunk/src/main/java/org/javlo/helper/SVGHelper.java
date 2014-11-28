package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class SVGHelper {

	public static Logger logger = Logger.getLogger(SVGHelper.class.getName());

	public static BufferedImage getSVGImage(File svgFile) {
		return null;
	}

	private static class BufferedImageTranscoder {

		private BufferedImage img = null;

		
		public BufferedImage createImage(int w, int h) {
			return null;
		}

		
		public void writeImage(BufferedImage img, Object output) {
			this.img = img;
		}

		public BufferedImage getBufferedImage() {
			return img;
		}

//		@Override
//		protected void setImageSize(float docWidth, float docHeight) {
//			// TODO Auto-generated method stub
//			System.out.println(docWidth);
//			System.out.println(docHeight);
//			super.setImageSize(docWidth, docHeight);
//		}

	}

	public static void main(String[] args) {
		File folder = new File("F:\\tmp\\svg\\");
		File outFolder = new File(folder, "png");
		for (File in : folder.listFiles()) {
//		for (String inName : new String[] { "410.svg", "AJ_Digital_Camera.svg", "compuserver_msn_Ford_Focus.svg" }) {
//			File in = new File(folder, inName);
			if (!in.getName().endsWith(".svg")) {
				continue;
			}
			try {
				System.out.println(in);
				File out = new File(outFolder, in.getName() + ".png");
				BufferedImage img = getSVGImage(in);
				ImageIO.write(img, "png", out);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("end");
	}

}
