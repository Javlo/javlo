package org.javlo.helper;

public class MapGeoHelper {

	private static double deg2rad(double x) {
		return Math.PI * x / 180;
	}

	public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
		double earth_radius = 6378137; // Terre = sphère de 6378km de rayon
		double rlo1 = deg2rad(lng1); // CONVERSION
		double rla1 = deg2rad(lat1);
		double rlo2 = deg2rad(lng2);
		double rla2 = deg2rad(lat2);
		double dlo = (rlo2 - rlo1) / 2;
		double dla = (rla2 - rla1) / 2;
		double a = (Math.sin(dla) * Math.sin(dla)) + Math.cos(rla1) * Math.cos(rla2) * (Math.sin(dlo) * Math.sin(dlo));
		double d = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return (earth_radius * d);
	}
	
	public static void main(String[] args) {
		double[] coordImm = new double[] {50.617241, 4.814011};
		double[] coordPE = new double[] {50.83903, 4.37342};
		System.out.println(">>>>>>>>> MapGeoHelper.main : getDistance M  = "+getDistance(coordImm[0], coordImm[1], coordPE[0], coordPE[1])); //TODO: remove debug trace
		System.out.println(">>>>>>>>> MapGeoHelper.main : getDistance KM = "+Math.round(getDistance(coordImm[0], coordImm[1], coordPE[0], coordPE[1])/1000)); //TODO: remove debug trace
		double[] coordMrs = new double[] {43.2946,5.3746};
		System.out.println(">>>>>>>>> MapGeoHelper.main : getDistance M  = "+getDistance(coordImm[0], coordImm[1], coordMrs[0], coordMrs[1])); //TODO: remove debug trace
		System.out.println(">>>>>>>>> MapGeoHelper.main : getDistance KM = "+Math.round(getDistance(coordImm[0], coordMrs[1], coordMrs[0], coordPE[1])/1000)); //TODO: remove debug trace
		
	}

}
