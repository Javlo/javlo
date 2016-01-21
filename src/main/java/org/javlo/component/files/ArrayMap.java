package org.javlo.component.files;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.AbstractMapEntry;
import org.javlo.helper.StringHelper;
import org.javlo.utils.Cell;

public class ArrayMap extends AbstractMap<String, Cell> {
	
	private Cell[][] data;
	private int titleRaw = 0;
	private int dataRaw = 0;

	public ArrayMap(Cell[][] inData) {
		this.data = inData;
		for (int row = 0; row < Math.min(inData.length, 10); row++) {
			if (!StringHelper.isEmpty(data[row][0]) && !StringHelper.isEmpty(data[row][1])) {
				titleRaw = row;								
				row = 99;
			}
		}
		for (int row = titleRaw+1; row < Math.min(inData.length, 10); row++) {
			if (!StringHelper.isEmpty(data[row][0]) && !StringHelper.isEmpty(data[row][1])) {
				dataRaw = row;								
				row = 99;
			}
		}
		System.out.println("titleRaw = "+dataRaw);
	}

	@Override
	public Set<java.util.Map.Entry<String, Cell>> entrySet() {
		Set<java.util.Map.Entry<String, Cell>> outEntries = new HashSet<Map.Entry<String,Cell>>();
		String key = data[titleRaw][0].toString();
		Cell value = data[dataRaw][0];	
		int i=1;
		while ((!StringHelper.isEmpty(key) || !StringHelper.isEmpty(value)) && (i<data[titleRaw].length)) {
			Map.Entry<String, Cell> entry = new AbstractMapEntry(key,value) {};
			outEntries.add(entry);
			entry = new AbstractMapEntry(""+i,value) {};
			outEntries.add(entry);
			entry = new AbstractMapEntry(StringHelper.getColName(i),value) {};
			outEntries.add(entry);			
			key = data[titleRaw][i].toString();
			value = data[dataRaw][i];	
			i++;
		}
		return outEntries;
	}

}
