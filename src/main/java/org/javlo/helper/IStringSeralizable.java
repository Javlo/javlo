package org.javlo.helper;

/**
 * class with this interface can store content to string and load data from string.
 * @author user
 *
 */
public interface IStringSeralizable {
	
	/**
	 * load from String
	 * @param data 
	 * @return false if unvalid data
	 */
	public boolean loadFromString(String data);
	
	public String storeToString();

}
