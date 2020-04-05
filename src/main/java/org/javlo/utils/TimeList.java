package org.javlo.utils;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TimeList<E> extends AbstractList<E> {
	
	private int stayTime = Integer.MAX_VALUE;
	private int countSet = 0;
	
	private class TimeItem {
		private long creationTime;
		private E item;
		public TimeItem(long creationTime, E item) {
			super();
			this.creationTime = creationTime;
			this.item = item;
		}		
	}
	
	private List<TimeItem> internalList = new LinkedList<TimeItem>();
	private Set<E> containerList = new HashSet<E>();
	
	/**
	 * set the default time of the attribute live in second
	 */
	public TimeList(int stayTimeSecond) {
		this.stayTime = stayTimeSecond*1000;
	}
	
	private void clearList() {
		Iterator<TimeItem> ite = internalList.iterator();
		long limitTime = System.currentTimeMillis()-stayTime;
		while (ite.hasNext()) {
			TimeItem timeItem = ite.next();
			if (timeItem.creationTime < limitTime) {
				containerList.remove(timeItem.item);				
				ite.remove();
			} else {
				break;
			}
		}
	}
	
	@Override
	public boolean remove(Object o) {
		Iterator<TimeItem> ite = internalList.iterator();
		while (ite.hasNext()) {
			TimeItem timeItem = ite.next();
			if (timeItem.item != null && timeItem.item.equals(o)) {
				ite.remove();
				containerList.remove(o);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Object o) {
		return containerList.contains(o);
	}

	@Override
	public E get(int index) {
		clearList();
		if (internalList.size()<index) {
			return null;
		}
		TimeItem timeItem = internalList.get(index);
		if (timeItem != null) {
			return timeItem.item;
		} else {
			return null;
		}
	}

	@Override
	public int size() {
		clearList();
		return internalList.size();
	}
	
	@Override
	public boolean add(E item) {
		countSet++;
		if (countSet>1000) {
			countSet = 0;
			clearList();
		}
		containerList.add(item);
		return internalList.add(new TimeItem(System.currentTimeMillis(), item));
	}
	
}
