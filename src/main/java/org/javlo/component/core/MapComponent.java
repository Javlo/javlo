package org.javlo.component.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.javlo.helper.StringHelper;

public abstract class MapComponent extends AbstractVisualComponent {
	
	@Override
	public void setValue(String inContent) {
		if (!inContent.equals(getComponentBean().getValue())) {
			getComponentBean().setValue(inContent);
			setModify();
		}
	}

	private Map<String, String> getMapData() {
		return StringHelper.stringToMap(getValue());
	}

	protected void setField(String field, String value) {
		Map<String, String> map = getMapData();
		map.put(field, value);		
		String val = StringHelper.mapToString(map);
		setValue(val);		
	}
	
	protected void setField(String field, Collection<String> values) {
		Map<String, String> map = getMapData();
		map.put(field, StringHelper.collectionToString(values, "##"));		
		String val = StringHelper.mapToString(map);
		setValue(val);		
	}

	protected String getField(String field, String defaultValue) {		
		return StringHelper.neverEmpty(getMapData().get(field), defaultValue);
	}
	
	protected List<String> getFieldList(String field) {
		Map<String,String> map = getMapData();
		if (map.get(field) == null) {
			return Collections.emptyList();
		} else {
			return StringHelper.stringToCollection(map.get(field), "##");
		}
		
	}

}
