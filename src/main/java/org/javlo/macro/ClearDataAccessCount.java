package org.javlo.macro;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;

public class ClearDataAccessCount extends AbstractMacro {
	
	protected static Logger logger = Logger.getLogger(ClearDataAccessCount.class.getName());

	@Override
	public String getName() {
		return "clear-data-access";
	}
	
	public static void clearDataAccess(ContentContext ctx) {
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
		logger.info(keyForDelete.size()+ " data removed.");
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		clearDataAccess(ctx);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
