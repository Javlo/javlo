package org.javlo.bean;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.beans.BeansUtils;

public class Company {
	private String number;
	private String name;
	private String address;
	
	public Company() {		
	}	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException {
		Company c1 = new Company();
		Company c2 = new Company();
		Log log = LogFactory.getLog(BeansUtils.class);
		Logger logger = LoggerFactory.getLogger(BeansUtils.class);
		
		System.out.println(">>>>>>>>> Company.main : log.isDebugEnabled() = "+logger.isDebugEnabled()); //TODO: remove debug trace
		System.out.println(">>>>>>>>> Company.main : log = "+log); //TODO: remove debug trace
		
		//LoggerHelper.changeLogLevel(LoggerHelper.LEVEL_INFO);
		
		System.out.println(">>>>>>>>> Company.main : log.isDebugEnabled() = "+logger.isDebugEnabled()); //TODO: remove debug trace
		System.out.println(">>>>>>>>> Company.main : log = "+log); //TODO: remove debug trace
		
		//BeanUtils.copyProperties(c1, c2);
	}
	
}
