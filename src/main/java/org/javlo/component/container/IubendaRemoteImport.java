package org.javlo.component.container;

import java.net.URL;
import java.util.Calendar;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.JSONMap;

public class IubendaRemoteImport extends AbstractVisualComponent {

	private String cache = null;

	public IubendaRemoteImport() {
	}

	@Override
	public String getType() {
		return "iubenda";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (StringHelper.isURL(getValue())) {
			Calendar cal = Calendar.getInstance();
			cal.roll(Calendar.DAY_OF_YEAR, false);
			String content = NetHelper.readPageGet(new URL(getValue()));
			JSONMap map = JSONMap.parseMap(content);
			cache = "" + map.get("content");
			return cache;
		} else {
			return "set correct URL to iubenda";
		}
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

}
