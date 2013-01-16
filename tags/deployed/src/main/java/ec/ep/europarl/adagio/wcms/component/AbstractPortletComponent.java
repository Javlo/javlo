package ec.ep.europarl.adagio.wcms.component;

import java.util.List;

import javax.portlet.PortletMode;

import org.javlo.context.ContentContext;
import org.javlo.component.portlet.AbstractPortletWrapperComponent;

public abstract class AbstractPortletComponent extends AbstractPortletWrapperComponent {

	private static final PortletMode ADMIN = new PortletMode("admin");
	
	@Override
	public String getInitPortletValueEventName() {
		return "initXMLValue";
	}

	@Override
	public String getPortletValueChangedEventName() {
		return "xmlValueChanged";
	}

	@Override
	public String getDeletePortletEventName() {
		return "deleteInstance";
	}

	@Override
	public List<PortletMode> getPortletModes(int renderMode) {
		List<PortletMode> result = super.getPortletModes(renderMode);
		if (ContentContext.EDIT_MODE == renderMode) {
			result.add(ADMIN);
//		} else if (ContentContext.ADMIN_MODE == renderMode) {
//			result.add(0, ADMIN);
		}
		return result;
	}

	@Override
	public String getWindowIdPrefix(int renderMode, PortletMode portletMode) {
		if (ADMIN.equals(portletMode)) {
			return ""; // always show the "view" data in admin mode
		} else {
			return super.getWindowIdPrefix(renderMode, portletMode);
		}
	}
}
