package org.javlo.ztatic;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

public class InitInterest {

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
