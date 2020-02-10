package org.javlo.service.location;

public class IpRange {
	
	private long start;
	private long end;
	private String countryCode;
	private String countryName;
	private String region;
	private String city;
	
	public IpRange(long start, long end, String countryCode, String countryName, String region, String city) {
		this.start = start;
		this.end = end;
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.region = region;
		this.city = city;
	}
	public long getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public long getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}

}
