package org.javlo.ztatic;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.javlo.context.ContentContext;
import org.javlo.service.ContentService;
import org.javlo.ztatic.InitInterest.PointThread.PointAction;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

public class InitInterest {
	
	private static Logger logger = Logger.getLogger(InitInterest.class.getName());
	
	private static PointThread pointThread = null;
		
	private static ConcurrentLinkedQueue<PointAction> todo = new ConcurrentLinkedQueue<PointAction>();
	
	public static class PointThread extends Thread {
		
		public PointThread() {
			setName("PointThread");
		}
		
		boolean run = true;
		
		public static class PointAction {
			File file;
			ContentContext ctx;
			ContentService content;
			String keyX;
			String keyY;	
		}		
		
		@Override
		public void run() {
			super.run();
			while (todo.size() > 0) {
				PointAction action = todo.remove();
				if (action.file.exists()) {
					BufferedImage img;
					try {
						logger.info("search point of interest : "+action.file+" #todo:"+todo.size());
						img = ImageIO.read(action.file);					
						Point point = getPointOfInterest(img);
						if (point != null) {
						int focusX = (point.getX() * 1000) / img.getWidth();
						int focusY = (point.getY() * 1000) / img.getHeight();
						action.content.setAttribute(action.ctx, action.keyX, ""+focusX);
						action.content.setAttribute(action.ctx, action.keyY, ""+focusY);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					logger.warning("file not found : "+action.file);
				}
			}
			run = false;
		}		
	}

	public static class Point {
		private int x;
		private int y;

		public Point(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

	}
	
//	PUBLIC STATIC POINT GETPOINTOFINTEREST(BUFFEREDIMAGE BUFIMAGE) {
//		RETURN NULL;
//	}
	
	public static void setPointOfInterestWidthThread(ContentContext ctx, File file, String keyx, String keyy) {	
		PointAction action = new PointAction();
		action.ctx = ctx;
		action.content = ContentService.getInstance(ctx.getGlobalContext());
		action.file = file;
		action.keyX = keyx;
		action.keyY = keyy;
		todo.add(action);
		if (pointThread == null || !pointThread.run) {
			pointThread = new PointThread();
			pointThread.start();
		}
	}

	public static Point getPointOfInterest(BufferedImage bufImage) {		
		if (bufImage.getType() != BufferedImage.TYPE_INT_ARGB) {
			BufferedImage tmp = new BufferedImage(bufImage.getWidth(), bufImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			tmp.getGraphics().drawImage(bufImage, 0, 0, null);
			bufImage = tmp;
		}
		int[] srcPixels = ((DataBufferInt) bufImage.getRaster().getDataBuffer()).getData();
		MBFImage img = new MBFImage(srcPixels, bufImage.getWidth(), bufImage.getHeight());
		
		// A simple Haar-Cascade face detector
		HaarCascadeDetector det1 = new HaarCascadeDetector();
		//DetectedFace face1 = det1.detectFaces(img.flatten()).get(0);
		float maxConfidence = 1;
		Point point = null;
		for (DetectedFace face : det1.detectFaces(img.flatten())) {
			if (maxConfidence < face.getConfidence()) {
				maxConfidence = face.getConfidence();				
				point = new Point((int)Math.round(face.getShape().minX()+(face.getShape().getWidth()/2)), (int)Math.round(face.getShape().minY()+(face.getShape().getHeight()/2)));
			}			
		}		
		return point;
	}
}
