package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class SVGHelper {

	public static Logger logger = Logger.getLogger(SVGHelper.class.getName());

	public static BufferedImage getSVGImage(File svgFile) {
		FileInputStream in = null;
		BufferedImage out = null;
		try {
			in = new FileInputStream(svgFile);
			BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

			imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 10000F);
			imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 10000F);
			imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_MAX_WIDTH, 3000F);

			TranscoderInput input = new TranscoderInput(in);
			imageTranscoder.transcode(input, null);

			out = imageTranscoder.getBufferedImage();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when converting SVG to buffered image: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(in);
		}
		return out;
	}

	private static class BufferedImageTranscoder extends ImageTranscoder {

		private BufferedImage img = null;

		@Override
		public BufferedImage createImage(int w, int h) {
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			return bi;
		}

		@Override
		public void writeImage(BufferedImage img, TranscoderOutput output) {
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
