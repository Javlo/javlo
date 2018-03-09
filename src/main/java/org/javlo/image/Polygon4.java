package org.javlo.image;

public class Polygon4 {
	
	private Polygon4 square = null;
	
	private int x1;
	private int y1;
	private int x2;
	private int y2;
	private int x3;
	private int y3;
	private int x4;
	private int y4;
	
	public Polygon4(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.x3 = x3;
		this.y3 = y3;
		this.x4 = x4;
		this.y4 = y4;
	}
	public int getX1() {
		return x1;
	}
	public void setX1(int x1) {
		this.x1 = x1;
	}
	public int getY1() {
		return y1;
	}
	public void setY1(int y1) {
		this.y1 = y1;
	}
	public int getX2() {
		return x2;
	}
	public void setX2(int x2) {
		this.x2 = x2;
	}
	public int getY2() {
		return y2;
	}
	public void setY2(int y2) {
		this.y2 = y2;
	}
	public int getX3() {
		return x3;
	}
	public void setX3(int x3) {
		this.x3 = x3;
	}
	public int getY3() {
		return y3;
	}
	public void setY3(int y3) {
		this.y3 = y3;
	}
	public int getX4() {
		return x4;
	}
	public void setX4(int x4) {
		this.x4 = x4;
	}
	public int getY4() {
		return y4;
	}
	public void setY4(int y4) {
		this.y4 = y4;
	}
	public Polygon4 getSquare() {
		if (square == null) {
			int sx1 = Math.min(x1,x4);
			int sy1 = Math.min(y1,y2);
			int sx2 = Math.max(x2,x3);
			int sy2 = Math.max(y3,y4);
			square = new Polygon4(sx1, sy1, sx2, sy1, sx2, sy2, sx1, sy2); 
		}
		return square;					
	}	
	
}
