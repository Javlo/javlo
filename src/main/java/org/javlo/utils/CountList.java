package org.javlo.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class CountList<E> implements Collection<E> {
	
	private LinkedHashMap<E,Integer> list = new LinkedHashMap<E,Integer>();

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return list.containsKey(o);
	}

	@Override
	public Iterator<E> iterator() {
		return list.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		return list.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.keySet().toArray(a);
	}

	@Override
	public boolean add(E e) {
		if (list.containsKey(e)) {
			list.put(e, list.get(e)+1);
		} else {
			list.put(e,1);
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		int size = list.size();
		list.remove(o);
		return list.size() < list.size();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return list.keySet().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean out=false;
		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			add(e);
			out=true;
		}
		return out;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return list.keySet().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return list.keySet().retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
	}

	
}
