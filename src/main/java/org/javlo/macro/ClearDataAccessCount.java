package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;

public class ClearDataAccessCount extends AbstractMacro {

	@Override
	public String getName() {
		return "clear-data-access";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());		
		List<Object> keyForDelete = new LinkedList<Object>();
		for (Object key : globalContext.getDataKeys()) {
			if (key.toString().startsWith("staticinfo-") && key.toString().contains("-access")) {
				keyForDelete.add(key);
			}
			if (key.toString().startsWith("clk__")) {
				keyForDelete.add(key);
			}
		}
		globalContext.removeData(keyForDelete);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
