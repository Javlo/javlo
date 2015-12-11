/*
 * Created on 13-mars-2004
 */
package org.javlo.helper;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author pvandermaesen help for transform java standard objet
 */
public class JavaHelper {

	public static class MapEntriesSortOnValue implements Comparator<Map.Entry<? extends Comparable, ? extends Comparable>> {

		@Override
		public int compare(Entry<? extends Comparable, ? extends Comparable> o1, Entry<? extends Comparable, ? extends Comparable> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}

	}

	public static class MapEntriesSortOnKey implements Comparator<Map.Entry<Comparable, Comparable>> {

		@Override
		public int compare(Entry<Comparable, Comparable> o1, Entry<Comparable, Comparable> o2) {
			return o1.getKey().compareTo(o2.getKey());
		}

	}

	public static final byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	
	public static final byte[] longToByteArray(long value) {
		return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
	}

	public static final int byteArrayToInt(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
	}

	public static final <K, V> Map<K, V> createMap(K[] keys, V[] values) {
		DebugHelper.checkAssert(keys.length != values.length, "values (length=" + values.length + ") and keys (length=" + keys.length + ") must be have the same length.");
		Map<K, V> res = new HashMap<K, V>();
		for (int i = 0; i < values.length; i++) {
			res.put(keys[i], values[i]);
		}
		return res;
	}

}
