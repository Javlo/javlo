package org.javlo.helper;

public class MathHelper {
	
	public static int max(int... numbers) {
		int out = Integer.MIN_VALUE;
		for (int n : numbers) {
			if (n>out) {
				out = n;
			}
		}
		return out;
	}
	
	public static int min(int... numbers) {
		int out = Integer.MAX_VALUE;
		for (int n : numbers) {
			if (n<out) {
				out = n;
			}
		}
		return out;
	}

}
