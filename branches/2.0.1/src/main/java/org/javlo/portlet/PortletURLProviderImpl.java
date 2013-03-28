package org.javlo.portlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.ResourceURL;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.pluto.container.PortletURLProvider;
import org.apache.pluto.container.PortletWindow;
import org.javlo.helper.URLHelper;

public class PortletURLProviderImpl implements PortletURLProvider {

	public static final String PORTLET_ID_PARAM_NAME = "javlo-portlet-id";
	public static final String PORTLET_RESOURCE_PARAM_NAME = "javlo-portlet-resource";

	private final PortletURLProvider.TYPE type;
	private final String url;
	private final PortletWindow portletWindow;
	private final Map<String, String[]> params = new HashMap<String, String[]>();
	

	public PortletURLProviderImpl(PortletURLProvider.TYPE type, PortletWindow portletWindow, String url, PortalContext portalContext) {
		this.type = type;
		this.portletWindow = portletWindow;
		
		String fullId = portletWindow.getId().getStringId();
		String[] portletIds = { fullId.substring(fullId.lastIndexOf('/') + 1) };
		this.params.put(PORTLET_ID_PARAM_NAME, portletIds);

		if (PortletURLProvider.TYPE.ACTION == type/* && !this.params.containsKey(portalContext.getActionParamName())*/) {
			this.params.put(((PortalContextImpl) portalContext).getActionParamName(), portletIds);
		}

		if (url != null) {
			int index = url.indexOf('?');
			if (index > -1) {
				this.url = url.substring(0, index);
				StringTokenizer st = new StringTokenizer(url.substring(index + 1), "&");
				while (st.hasMoreTokens()) {
					String paramValue = st.nextToken();
					int split = paramValue.indexOf('=');
					if (split > 0) {
						String paramName = paramValue.substring(0, split);
						String[] values;
						if (this.params.get(paramName) != null) {
							values = new String[this.params.get(paramName).length + 1];
							System.arraycopy(this.params.get(paramName), 0, values, 0, this.params.get(paramName).length);
							values[values.length - 1] = paramValue.substring(split + 1);
						} else {
							values = new String[1];
							values[0] = paramValue.substring(split + 1);
						}
						this.params.put(paramName, values);
					}
				}
			} else {
				this.url = url;
			}
		} else {
			this.url = "";
		}
	}
	
	 
	@Override
	public TYPE getType() {
		return type;
	}

	@Override
	public void setPortletMode(PortletMode mode) {
		if (mode != null && (portletWindow.getPortletMode() == null || !mode.equals(portletWindow.getPortletMode()))) {
			String[] values = { mode.toString() };
			this.params.put("portletMode", values);
		}
	}

	@Override
	public PortletMode getPortletMode() {
		if (params.get("portletMode") != null) {
			return new PortletMode(params.get("portletMode")[0]);
		}
		return null;
	}
	 
	@Override
	public void setWindowState(WindowState state) {
		if (state != null && (portletWindow.getWindowState() == null || !state.equals(portletWindow.getWindowState()))) {
			String[] values = { state.toString() };
			this.params.put("windowState", values);
		}
	}
	
	@Override
	public WindowState getWindowState() {
		if (params.get("windowState") != null) {
			return new WindowState(params.get("windowState")[0]);
		}
		return null;
	}

    public void setSecure() throws PortletSecurityException {
        throw new PortletSecurityException("No Supported");
    }

	 
    public boolean isSecure() {
        return false;
    }

	@Override
	public Map<String, String[]> getPublicRenderParameters() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, String[]> getRenderParameters() {
		return params;
	}

	@Override
	public void setCacheability(String cacheLevel) {
		// not supported
	}

	@Override
	public String getCacheability() {
		return ResourceURL.PAGE;
	}

	@Override
	public void setResourceID(String resourceID) {
		if (resourceID != null) {
			String[] values = { resourceID };
			this.params.put(PORTLET_RESOURCE_PARAM_NAME, values);
		}
	}
	 
	@Override
	public String getResourceID() {
		if (params.get(PORTLET_RESOURCE_PARAM_NAME) != null) {
			return params.get(PORTLET_RESOURCE_PARAM_NAME)[0];
		}
		return null;
	}

	@Override
	public void setSecure(boolean secure) throws PortletSecurityException {
		// not supported
	}

	@Override
	public String toURL() {
		String result = url;

		if (PortletURLProvider.TYPE.RESOURCE == type) {
			result = URLHelper.mergePath(result, getResourceID());
		}

		if (this.params != null && this.params.size() > 0) {
			result = result + '?';
			for (String paramName : this.params.keySet()) {
				for (String value : this.params.get(paramName)) {
					result = result + paramName + "=" + value + '&';
				}
			}
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	@Override
	public void write(Writer out, boolean escapeXML) throws IOException {
        String result = toURL();
        if (escapeXML) {
//            result = result.replaceAll("&", "&amp;");
//            result = result.replaceAll("<", "&lt;");
//            result = result.replaceAll(">", "&gt;");
//            result = result.replaceAll("\'", "&#039;");
//            result = result.replaceAll("\"", "&#034;");
        	result = StringEscapeUtils.escapeXml(result);
        }
        out.write(result);
	}

	@Override
	public Map<String, List<String>> getProperties() {
		return Collections.emptyMap();
	}

//	@Override 
//	public String toString() {
//		return toURL();
//	}
}
