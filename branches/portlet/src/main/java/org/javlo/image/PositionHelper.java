package org.javlo.image;

public class PositionHelper {
	
	static int distance (int x1, int y1, int x2, int y2) {
		int x = x1-x2;
		int y = y1-y2;
		double d = Math.sqrt(x*x+y*y);
		return (int)Math.round(d);
	}
	
	static int distanceX (int x1, int y1, int x2, int y2) {
		return Math.abs(x1-x2);
	}
	
	static int distanceY (int x1, int y1, int x2, int y2) {
		return Math.abs(y1-y2);
	}
	
	/**
	 * calcul if a point is in a circle.
	 * @param x coord. x of the point
	 * @param y coord. y of the point
	 * @param centerX center x of the circle
	 * @param centerY center y of the circle
	 * @param r radius of the circle
	 * @return true if point is in the circle.
	 */
	public static boolean inCicle (int x, int y, int centerX, int centerY, int r) {
		return distance (x,y,centerX,centerY) < r;
	}
	
	/**
	 * determine the zone of a point.
	 * 
	 * 1 2
	 *  c
	 * 3 4
	 * 	  
	 * @param x position x of the point.
	 * @param y position y of the point.
	 * @param centerX position x of the center.
	 * @param centerY position y of the center.
	 * @return the zone of the point.
	 */
	public static int zone (int x, int y, int centerX, int centerY ) {
		if (x<centerX) {
			if (y<centerY) {
				return 1;
			} else {
				return 3;
			}
		} else {
			if (y<centerY) {
				return 2;
			} else {
				return 4;
			}			
		}
	}
	
	public static boolean isOutRoundBorder (int x, int y, int width, int height, int radius) {
		// first circle
		if ((x < radius)&&(y < radius)) {
			if (distance(x, y, radius, radius) > radius) {
				return true;
			}
		} else if ((x<radius)&&(y>height-radius)) {
			if (distance(x, y, radius, height-radius) > radius) {
				return true;
			}			
		} else if ((x>width-radius)&&(y<radius)) {
			if (distance(x, y, width-radius, radius) > radius) {
				return true;
			}			
		} else if ((x>width-radius)&&(y>height-radius)) {
			if (distance(x, y, width-radius, height-radius) > radius) {
				return true;
			}			
		}
		
		return false;
	}

}
