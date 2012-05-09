package org.javlo.data.source;

import java.util.Collection;
import java.util.Map;

public interface IDataSource {

	public Collection<Object> getList();

	public Map<String, Object> getMap();

	/**
	 * return true if the connection if ok and if you can use this instance for access data.
	 * 
	 * @return
	 */
	public boolean isValid();

}
