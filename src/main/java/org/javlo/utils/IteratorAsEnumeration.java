package org.javlo.utils;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorAsEnumeration<E> implements Enumeration<E> {
	
	private final Iterator<E> iterator;
	
	public IteratorAsEnumeration(Iterator<E> iterator) {
		super();
		this.iterator = iterator;
	}

	@Override
	public boolean hasMoreElements() {		
		return iterator.hasNext();
	}

	@Override
	public E nextElement() {
		return iterator.next();
	}

}
