package org.javlo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.utils.TimeMap;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.owasp.encoder.Encode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

public class GeoService {

	private static Long latestCall = System.currentTimeMillis();
	private static final long WAIT_BETWEENN_CALL = 2 * 1000; // wait 2 sec between 2 call

	public static class Coord {
		private double lon = -1;
		private double lat = -1;

		public static Coord fromString(String coord) {
			if (coord != null) {
				coord = coord.replace("[", "").replace("]", "").trim();
				String[] coordStr = coord.split(",");
				if (coordStr.length == 2) {
					if (StringHelper.isFloat(coordStr[0]) && StringHelper.isFloat(coordStr[1])) {
						return new Coord(Double.parseDouble(coordStr[0]), Double.parseDouble(coordStr[1]));
					}
				}
			}
			return null;
		}

		public Coord(double latitude, double longitude) {
			super();
			this.lat = latitude;
			this.lon = longitude;
		}

		public double getLon() {
			return lon;
		}

		public void setLon(double longitude) {
			this.lon = longitude;
		}

		public double getLat() {
			return lat;
		}

		public void setLat(double latitude) {
			this.lat = latitude;
		}

		public double[] getCoord() {
			return new double[] { lat, lon };
		}

		@Override
		public String toString() {
			return "[" + lat + "," + lon + "]";
		}
	}

	private static final String KEY = "geoService";
	private File cacheFile = new File("/tmp/geoService_cache.properties");
	private Properties cache = null;

	public static GeoService getInstance(GlobalContext globalContext) {
		if (globalContext == null) { // DEBUG
			return new GeoService();
		}
		GeoService geoService = (GeoService) globalContext.getAttribute(KEY);
		if (geoService == null) {
			geoService = new GeoService();
			geoService.cacheFile = new File(
					URLHelper.mergePath(globalContext.getDataFolder(), "geoService_cache.properties"));
			globalContext.setAttribute(KEY, geoService);
		}
		return geoService;
	}

	private Properties getCache() throws IOException {
		if (cache == null) {
			cache = ResourceHelper.loadProperties(cacheFile);
		}
		return cache;
	}

	public String getCacheItem(String key) throws IOException {
		return getCache().getProperty(key);
	}

	public synchronized String setCacheItem(String key, String value) throws IOException {
		if (value == null) {
			getCache().remove(key);
		} else {
			getCache().setProperty(key, value);
		}
		ResourceHelper.storeProperties(getCache(), cacheFile);
		return value;
	}
	
	public void setCoordInExcel(File excelFile, String addressCol, String latCol, String lonCol) throws Exception {
		InputStream in = new FileInputStream(excelFile);
		OutputStream out = null;
		XSSFWorkbook workbook = null;
		try {
			try {
				workbook = new XSSFWorkbook(in);
				XSSFSheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				int w = 0;
				int h = 0;
				while (rowIterator.hasNext()) {
					h++;
					Row row = rowIterator.next();
					if (row.getLastCellNum() > w) {
						w = row.getLastCellNum();
					}
				}

				int addressPos = -1;
				int latPos = -1;
				int lonPos = -1;
				for (int x = 0; x < w; x++) {
					if (sheet.getRow(0).getCell(x) != null) {
						String title = sheet.getRow(0).getCell(x).getStringCellValue().trim();
						System.out.print(title+" -  ");
						if (sheet.getRow(1) != null && title != null) {
							if (title.equals(addressCol)) {
								addressPos = x;
							}
							if (title.equals(latCol)) {
								latPos = x;
							}
							if (title.equals(lonCol)) {
								lonPos = x;
							}
						}
					}
				}

				if (addressPos == -1) {
					throw new Exception("address not found.");
				}
				if (latPos == -1) {
					throw new Exception("latPos not found.");
				}
				if (lonPos == -1) {
					throw new Exception("lonPos not found.");
				}

				for (int y = 0; y < h; y++) {
					if (sheet.getRow(y) != null && sheet.getRow(y).getCell(addressPos) != null) {
						String val = ArrayHelper.readExcelCell(null, sheet.getRow(y).getCell(latPos));
						if (StringHelper.isEmpty(val)) {
							String add = sheet.getRow(y).getCell(addressPos).getStringCellValue();
							System.out.println(add);
							Coord c = getCoord(add);
							System.out.println(c);
							if (c == null) {
								sheet.getRow(y).getCell(latPos).setCellValue("Err");
								sheet.getRow(y).getCell(lonPos).setCellValue("Err");
							} else {
								if (sheet.getRow(y).getCell(latPos) == null) {
									sheet.getRow(y).createCell(latPos);
								}
								if (sheet.getRow(y).getCell(lonPos) == null) {
									sheet.getRow(y).createCell(lonPos);
								}
								sheet.getRow(y).getCell(latPos).setCellValue(c.getLat());
								sheet.getRow(y).getCell(lonPos).setCellValue(c.getLon());
							}
						}
					}
				}
			} finally {
				ResourceHelper.closeResource(in);
			}
			out = new FileOutputStream(excelFile);
			workbook.write(out);
		} finally {
			ResourceHelper.closeResource(workbook, out);
		}
	}

	public Coord getCoord(String address) throws Exception {
		String coord = getCache().getProperty(address);
		if (StringHelper.isEmpty(address)) {
			return null;
		}
		if (coord != null) {
			Coord c = Coord.fromString(coord);
			if (c != null) {
				return c;
			} else { // bad format in cache
				setCacheItem(coord, null);
			}
		}
		synchronized (latestCall) {
			if (System.currentTimeMillis() < latestCall + WAIT_BETWEENN_CALL) {
				Thread.sleep(WAIT_BETWEENN_CALL);
			}
			coord = getCache().getProperty(address);
			if (coord != null) {
				Coord c = Coord.fromString(coord);
				if (c != null) {
					return c;
				}
			}
			URL url = new URL("https://nominatim.openstreetmap.org/search.php?format=xml&q=" + Encode.forUri(address));
			try {
				String xml = NetHelper.readPageGet(url);
				NodeXML n = XMLFactory.getFirstNodeFromXML(xml).getChild("place");
				if (n == null) {
					return null;
				}
				Coord c = Coord.fromString(n.getAttributeValue("lat") + "," + n.getAttributeValue("lon"));
				setCacheItem(address, c.toString());
				latestCall = System.currentTimeMillis();
				return c;
			} catch (Exception e) {
				return null;
			}		
		}
	}

	private static TimeMap<String, IpApiBean> ipApiCache = new TimeMap<>(60*60*24*30, 10000);

	public static class IpApiBean {
		private String ip;
		private String country_name;
		private String country;
		private String region;
		private String city;
		private String postal;
		private Double latitude;
		private Double longitude;

		public String getIp() { return ip; }
		public void setIp(String ip) { this.ip = ip; }

		public String getCountry_name() { return country_name; }
		public void setCountry_name(String country_name) { this.country_name = country_name; }

		public String getCountry() { return country; }
		public void setCountry(String country) { this.country = country; }

		public String getRegion() { return region; }
		public void setRegion(String region) { this.region = region; }

		public String getCity() { return city; }
		public void setCity(String city) { this.city = city; }

		public String getPostal() { return postal; }
		public void setPostal(String postal) { this.postal = postal; }

		public Double getLatitude() { return latitude; }
		public void setLatitude(Double latitude) { this.latitude = latitude; }

		public Double getLongitude() { return longitude; }
		public void setLongitude(Double longitude) { this.longitude = longitude; }
	}

	public static  IpApiBean getIpApiObject(String ip) throws Exception {
		IpApiBean out = ipApiCache.get(ip);
		if (out != null) {
			return out;
		}
		String urlStr = "https://ipapi.co/" + ip + "/json/";
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(conn.getInputStream())
		);

		String line;
		StringBuilder response = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();

		ObjectMapper mapper = new ObjectMapper();
		out = mapper.readValue(response.toString(), IpApiBean.class);
		ipApiCache.put(ip, out);
		return out;
	}

	public static void main(String[] args) throws Exception {
		GeoService geoService = GeoService.getInstance(null);
		//System.out.println(geoService.getCoord("Rue de la cayenne 49 1360 Perwez Belgique"));
		File excel = new File("C:\\work\\html\\leaflet\\data\\croix_rouge.xlsx");
		geoService.setCoordInExcel(excel, "Adresse complète", "lat", "lon");
		
		
	}

}
