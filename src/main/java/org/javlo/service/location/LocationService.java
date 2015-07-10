package org.javlo.service.location;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;

public class LocationService {
	
	public static Location getLocation(double latitude, double longitude, String lg) throws IOException {		
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
		format.setMinimumFractionDigits(6);
		return getLocation(""+format.format(latitude)+", "+format.format(longitude), lg);
	}
	
	public static Location getLocation(String coord, String lg) throws IOException {
		System.out.println("***** LocationService.getLocation : coord = "+coord); //TODO: remove debug trace
		final Geocoder geocoder = new Geocoder();
		GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(coord).setLanguage(lg).getGeocoderRequest();
		GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
		Location loc = new Location();
		for (GeocoderAddressComponent comp : geocoderResponse.getResults().get(0).getAddressComponents()) {
			String t = comp.getTypes().get(0);
			if (t.equals("street_number")) {
				loc.setNumber(comp.getLongName());
			} else if (t.equals("route")) {
				loc.setRoute(comp.getLongName());
			} else if (t.equals("locality")) {
				loc.setLocality(comp.getLongName());
			} else if (t.equals("country")) {
				loc.setCountry(comp.getLongName());
			}
		}
		return loc;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(getLocation("50.616910, 4.813719", "en"));
	}

}
