package org.javlo.data.source;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.GlobalContext;

public class TestDataSource implements IDataSource {

	public TestDataSource(GlobalContext globalContext) {
	}

	@Override
	public Collection<Object> getList() {
		return getMap().values();
	}

	@Override
	public Map<String, Object> getMap() {
		Map<String, Object> outMap = new HashMap<String, Object>();
		outMap.put("test1", "test@javlo.org");
		outMap.put("test2", "test2@javlo.org");
		outMap.put("test3", "test3@javlo.org");
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
