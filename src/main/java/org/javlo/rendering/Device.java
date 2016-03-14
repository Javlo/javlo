package org.javlo.rendering;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;

public class Device implements Serializable {

	public static final String FORCE_DEVICE_PARAMETER_NAME = "force-device-code";

	public static final String DEFAULT = "default";

	private static Logger logger = Logger.getLogger(Device.class.getName());

	public static final Device getDevice(ContentContext ctx) {
		HttpServletRequest request = ctx.getRequest();
		Device currentDevice = (Device) request.getSession().getAttribute(Device.class.getCanonicalName());
		if (currentDevice == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
			String userAgent = request.getHeader("User-Agent");
			currentDevice = new Device();
			currentDevice.devices = staticConfig.getDevices();
			currentDevice.setUserAgent(userAgent);
			request.getSession().setAttribute(Device.class.getCanonicalName(), currentDevice);
			logger.fine("Create new device : '" + currentDevice.getCode() + "' userAgent : " + userAgent);
		}

		String forcedCode = request.getParameter(FORCE_DEVICE_PARAMETER_NAME);
		if (forcedCode != null) {
			if (forcedCode.equals(DEFAULT)) {
				currentDevice.setForcedCode(null);
			} else {
				currentDevice.setForcedCode(forcedCode);
			}
		}
		return currentDevice;
	}

	public static final Device getFakeDevice(String userAgent) {
		Device device = new Device();
		device.setUserAgent(userAgent);
		device.setLabel("fakeDevice");
		return device;
	}

	private String userAgent = null;

	public static final String DEFAULT_DEVICE = "pc";

	public static final String SWITCH_DEFAULT_DEVICE_PARAM_NAME = "switch-default-device";

	String code = DEFAULT_DEVICE;

	String forcedCode = null;

	private Map<String, String> devices = null;

	private String label = "pc";

	private Device() {
	}

	/**
	 * force device to be default device
	 */
	public void forceDefault() {
		forcedCode = DEFAULT_DEVICE;
	}

	public void setForcedCode(String forcedCode) {
		this.forcedCode = forcedCode;
	}

	public String getCode() {
		if (forcedCode != null) {
			return forcedCode;
		}
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getRealCode() {
		return code;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public boolean isDefault() {
		String pointerDevice = devices.get(getCode() + '.' + "default");
		if (pointerDevice == null) {
			return DEFAULT_DEVICE.equals(getCode());
		}
		return StringHelper.isTrue(pointerDevice);
	}

	public boolean isPointerDevice() {
		String pointerDevice = devices.get(getCode() + '.' + "pointer");

		if (pointerDevice == null) {
			return true;
		}
		return StringHelper.isTrue(pointerDevice);
	}

	public boolean isMobileDevice() {
		String pointerDevice = devices.get(getCode() + '.' + "mobile");

		if (pointerDevice == null) {
			return false;
		}
		return StringHelper.isTrue(pointerDevice);
	}

	protected void setLabel(String label) {
		this.label = label;
	}

	protected void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		if (devices != null) {
			Collection<Map.Entry<String, String>> entries = devices.entrySet();
			for (Map.Entry<String, String> entry : entries) {
				if (entry.getKey().endsWith(".in")) {
					Pattern pattern = Pattern.compile(entry.getValue());
					if (userAgent != null && pattern.matcher(userAgent).matches()) {
						String localCode = entry.getKey().split("\\.")[0];
						String exclude = devices.get(localCode + ".out");
						if (exclude != null) {
							pattern = Pattern.compile(exclude);
							if (!pattern.matcher(userAgent).matches()) {
								code = localCode;
							}
						} else {
							code = localCode;
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return getCode();
	}

	public boolean isHuman() {
		return !NetHelper.isUserAgentRobot(getUserAgent());
	}

	public boolean isForced() {
		return forcedCode != null;
	}

	public void unforceDefault() {
		forcedCode = null;
	}

	public static void main(String[] args) {
		String entry = "pdf?hash=1412688870812559429731099310123";
		String localCode = entry.split("\\.|\\?")[0];
		System.out.println("localCode = " + localCode);
	}

}
