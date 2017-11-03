package org.javlo.helper;

/**
 * class with this interface can store content to string and load data from string.
 * @author user
 *
 */
public interface IStringSeralizable {
	
	public void loadFromString(String data);
	
	public String storeToString();

}
