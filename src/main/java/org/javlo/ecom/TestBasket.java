package org.javlo.ecom;

public class TestBasket extends Basket {
	
	public TestBasket(){};
	
	@Override
	public String getFirstName() {
		return "first name";
	}
	
	@Override
	public String getLastName() {
		return "last name";
	}
	
	@Override
	public double getTotalExcludingVAT() {
		return 1;
	}
	
	@Override
	public double getTotalIncludingVAT() {	
		return 1.21;
	}
	
	@Override
	public String getCity() {
		return "city";
	}
	
	@Override
	public String getZip() {
		return "1234";
	}

	@Override
	public String getCountry() {	
		return "be";
	}
	
	@Override
	public String getContactEmail() {	
		return "test@javlo.org";
	}
	
	@Override
	public String getContactPhone() {
		return "0123456789";
	}
	
	@Override
	public String getCurrencyCode() {	
		return "EUR";
	}
}
