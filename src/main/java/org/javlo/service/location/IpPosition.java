package org.javlo.service.location;

public class IpPosition {
	
	private String countryCode;
	private String countryName;
	private String region;
	private String city;
	
	public IpPosition(IpRange ipRange) {
		this.countryCode = ipRange.getCountryCode();
		this.countryName = ipRange.getCountryName();
		this.region = ipRange.getRegion();
		this.city = ipRange.getCity();
	}
	
	
	
	public IpPosition(String countryCode, String countryName, String region, String city) {
		super();
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.region = region;
		this.city = city;
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
