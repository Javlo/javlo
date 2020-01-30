package org.javlo.service.location;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Locale;

import org.javlo.helper.NetHelper;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class LocationService {
	
	public static Location getLocation(double longitude, double latitude, String lg) throws IOException {		
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
		format.setMinimumFractionDigits(6);
		return getLocation(""+format.format(longitude)+", "+format.format(latitude), lg);
	}
	
	public static Location getLocation(String coord, String lg) throws IOException {
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
	
	public static void main(String[] args) throws Exception {
		//System.out.println(getLocation("50.616910, 4.813719", "en"));
		IpPosition pos = getIpPosition("212.68.230.251");
		System.out.println(pos.getAlpha2());
	}
	
	public static IpPosition getIpPosition(String ip) throws JsonSyntaxException, MalformedURLException, Exception {
		return new Gson().fromJson(NetHelper.readPageGet(new URL("https://api.ipgeolocationapi.com/geolocate/"+ip)), IpPosition.class);
	}

}
