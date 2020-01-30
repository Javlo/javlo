package org.javlo.service.location;

import java.util.List;

public class IpPosition {
	
	private String continent;
	private String alpha2;
	private String name;
	private int countryCode;
	private String addressFromat;
	private List<String> languages;
	private Geo deo;
	private String currency;
	
	public String getContinent() {
		return continent;
	}
	public void setContinent(String continent) {
		this.continent = continent;
	}
	public String getAlpha2() {
		return alpha2;
	}
	public void setAlpha2(String alpha2) {
		this.alpha2 = alpha2;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(int countryCode) {
		this.countryCode = countryCode;
	}
	public String getAddressFromat() {
		return addressFromat;
	}
	public void setAddressFromat(String addressFromat) {
		this.addressFromat = addressFromat;
	}
	public List<String> getLanguages() {
		return languages;
	}
	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	public Geo getDeo() {
		return deo;
	}
	public void setDeo(Geo deo) {
		this.deo = deo;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
