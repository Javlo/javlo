package org.javlo.cache;

/**
 * Interface use for Cache
 * 
 * @author Patrick Vandermaesen
 * 
 */
public interface ICache {

	/**
	 * get a item from cache.
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key);

	/**
	 * add item to cache
	 * 
	 * @param key
	 *            the key of item
	 */
	public void put(String key, Object item);

	/**
	 * remove item and return true if removed.
	 * 
	 * @param key
	 * @return
	 */
	public boolean removeItem(String key);

	/**
	 * remove all items from cache.
	 * 
	 * @return true if at least one item was removed.
	 */
	public void removeAll();

	/**
	 * return the number of items contains in Cache.
	 * 
	 * @return
	 */
	public int getSize();

}
