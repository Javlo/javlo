package org.javlo.service.location;

public class Location {
	
	private String country = null;
	private String locality = null;
	private String postalCode = null;
	private String number = null;
	private String route = null;

	public Location() {
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
	
	@Override
	public String toString() {	
		return getCountry()+' '+getLocality()+' '+getRoute()+' '+getNumber();
	}

}
