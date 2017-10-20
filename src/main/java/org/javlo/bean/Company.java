package org.javlo.bean;

import java.util.Date;

public class Company {
	private Date date;
	private String name;
	private String addresse;
	
	public Company(Date date, String name, String addresse) {
		super();
		this.date = date;
		this.name = name;
		this.addresse = addresse;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddresse() {
		return addresse;
	}
	public void setAddresse(String addresse) {
		this.addresse = addresse;
	}
	
}
