package org.javlo.service.location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletContext;

import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.javlo.config.StaticConfig;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.TimeMap;
import org.jcodec.common.logging.Logger;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ibm.icu.util.Calendar;

public class LocationService {

	/*
	 * init on AccessServlet src of data : https://lite.ip2location.com/
	 */
	public static File ipDbFile = null;

	private static IP2Location ip2Location;

	private static ArrayList<IpRange> ipData = null;

	private static final Map<String, IpGeoLocationBean> ipPositionCache = Collections.synchronizedMap(new TimeMap<>(60 * 60 * 24 * 30, 10000000));
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		downloadRemoteIP2File(new File("c:/trans/"), "https://www.ip2location.com/download?token=Wseyn0nNMiML1q254HQBvJx1VMhcWhsISdpVfQIsbj91xz2j5T6p4MyepuLxIHEw&file=DB1ACLIPV6");
	}
	
	private static File downloadRemoteIP2File(File folder, String url) throws MalformedURLException, IOException {
		File downloadFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), "ip2-downloaded-data.zip"));
		File finFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), "ip2-downloaded-data.bin"));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		if (url != null && (!finFile.exists() || finFile.lastModified() < cal.getTimeInMillis())) {
			downloadFile.getParentFile().mkdirs();
			ResourceHelper.writeUrlToFile(new URL(url), downloadFile);
			ZipFile zipFile = ZipHelper.openZipFile(downloadFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (StringHelper.getFileExtension(entry.getName()).equalsIgnoreCase("bin")) {
					InputStream in = zipFile.getInputStream(entry);
					ResourceHelper.writeStreamToFile(in, finFile);
					in.close();
					Logger.info("create file : "+finFile);
				}
			}
			zipFile.close();
			downloadFile.delete();
			Logger.info("file updated : " + finFile + " (url:" + url + ")");
		}
		return finFile;
	}

	public static synchronized void init(ServletContext application) throws MalformedURLException, IOException {
		if (ipDbFile == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(application);
			// check url on all mounts
			File downloadFile = downloadRemoteIP2File(new File(application.getRealPath("/WEB-INF/data/")), staticConfig.getIP2LocationURL());
			if (downloadFile.exists()) {
				ipDbFile = downloadFile;
			} else {
				ipDbFile = new File(application.getRealPath("/WEB-INF/data/ip2-local-data.bin"));
			}
			
		}
	}

	public static Location getLocation(double longitude, double latitude, String lg) throws IOException {
		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
		format.setMinimumFractionDigits(6);
		return getLocation("" + format.format(longitude) + ", " + format.format(latitude), lg);
	}

	public static long ipToLong(String ipStr) throws UnknownHostException {
		String[] ipAddressInArray = ipStr.split("\\.");
		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {
			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);
		}
		return result;
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

	private static IpGeoLocationBean getIpPositionOnline(String ip) throws JsonSyntaxException, MalformedURLException, Exception {
		IpGeoLocationBean outPos = ipPositionCache.get(ip);
		if (outPos == null) {
			Thread.sleep(500);
			outPos = new Gson().fromJson(NetHelper.readPageGet(new URL("https://api.ipgeolocationapi.com/geolocate/" + ip)), IpGeoLocationBean.class);
			ipPositionCache.put(ip, outPos);
		}
		return outPos;
	}

	public static IpPosition getIpPosition(String ip) throws JsonSyntaxException, MalformedURLException, Exception {
		
		if (ip == null || ip.equalsIgnoreCase("localhost") || ip.equals("127.0.0.1")) {
			return null;
		}
		
		if (ipDbFile == null || !ipDbFile.exists()) {
			//Logger.warn("ipDbFile not found.");
			return null;
		}
		if (ip2Location == null) {
			ip2Location = new IP2Location(ipDbFile.getAbsolutePath());
		}
		IP2LocationResult res = ip2Location.query(ip);
		IpPosition outPos = null;
		if (res == null || StringHelper.isEmpty(res.country_code)) {
//			IpGeoLocationBean geo = getIpPositionOnline(ip);
//			if (geo != null) {
//				
//				outPos = new IpPosition(geo.alpha2, geo.name, null, null);
//			}
		} else {
			outPos = new IpPosition(res.country_code, res.country_name, res.region, res.city);
		}
		return outPos;
	}

	// public static IpPosition getIpPosition(String ip) throws JsonSyntaxException,
	// MalformedURLException, Exception {
	// if (ipData == null) {
	// synchronized (LocationService.class) {
	// if (ipData == null) {
	// ipData = new ArrayList<IpRange>();
	// CSVFactory csv = new CSVFactory(ipDbFile);
	// for (String[] line : csv.getArray()) {
	// ipData.add(new IpRange(Long.parseLong(line[0]), Long.parseLong(line[1]),
	// line[2], line[3],line[4], line[5]));
	// }
	// }
	// }
	// }
	// long ipVal = ipToLong(ip);
	// System.out.println(ipVal);
	//
	// int index = -1; // will return -1 if element not found.
	// int start = 0;
	// int end = ipData.size() - 1;
	//
	// while (start <= end) {
	// int mid = (start + end) / 2;
	// if (ipData.get(mid).getStart() > ipVal && ipData.get(mid).getEnd() < ipVal) {
	// index = mid; // Update Index, which is the upper index here.
	// start = mid + 1; // This is the main step.
	// } else if (ipData.get(mid).getStart() > ipVal) {
	// end = mid - 1;
	// } else {
	// start = mid + 1;
	// }
	// }
	//
	// IpPosition outPos = null;
	// if (index>-1) {
	// outPos = new IpPosition(ipData.get(index));
	// }
	// return outPos;
	// }

}
