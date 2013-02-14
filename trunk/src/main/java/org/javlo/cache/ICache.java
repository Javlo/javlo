package org.javlo.cache;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface use for Cache
 * 
 * @author Patrick Vandermaesen
 * 
 */
public interface ICache extends Serializable {

	/**
	 * get the name of the cache.
	 * 
	 * @return
	 */
	public String getName();

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

	/**
	 * return all keys contains in cache.
	 * 
	 * @return
	 */
	public Collection<String> getKeys();
}
