package org.javlo.bean;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SortBean<T> implements Comparable<SortBean<T>> {
	
	private T obj;
	private int order = 0;

	public SortBean(T obj, int order) {
		this.obj = obj;
		this.order = order;
	}

	@Override
	public int compareTo(SortBean<T> o) {
		return order-o.order;
	}
	
	public static <T> List<T> transformList(Collection<SortBean<T>> inList) {
		List<T> outList = new LinkedList<T>();
		for (SortBean<T> sortBean : inList) {
			outList.add(sortBean.obj);
		}
		return outList;
	}
	
	public T getObj() {
		return obj;
	}
	

}
