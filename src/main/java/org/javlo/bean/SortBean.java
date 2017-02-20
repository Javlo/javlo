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
		if (order != o.order) {
			return order-o.order;
		} else if (o.obj instanceof Comparable) {
			return ((Comparable)obj).compareTo(o.getObj());
		} else {
			return 0;
		}
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
