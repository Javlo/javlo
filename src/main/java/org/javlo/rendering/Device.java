package org.javlo.rendering;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;

public class Device implements Serializable {

	public static final String FORCE_DEVICE_PARAMETER_NAME = "force-device-code";

	private static Logger logger = Logger.getLogger(Device.class.getName());

	public static final Device getDevice(HttpServletRequest request) {
		Device currentDevice = (Device) request.getSession().getAttribute(Device.class.getCanonicalName());
		if (currentDevice == null) {
			StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
			String userAgent = request.getHeader("User-Agent");
			System.out.println("***** Device.getDevice : userAgent = "+userAgent); //TODO: remove debug trace
			currentDevice = new Device();
			currentDevice.devices = staticConfig.getDevices();
			currentDevice.setUserAgent(userAgent);
			request.getSession().setAttribute(Device.class.getCanonicalName(), currentDevice);
			logger.fine("Create new device : '" + currentDevice.getCode() + "' userAgent : " + userAgent);
		}
		String forcedCode = request.getParameter(FORCE_DEVICE_PARAMETER_NAME);
		if (forcedCode != null) {
			currentDevice.setForcedCode(forcedCode);
		}
		return currentDevice;
	}

	public static void main(String[] args) {
		String regDevice = ".*(iphone|htc).*?>ipad";
		Pattern pattern = Pattern.compile(regDevice);

		System.out.println("**** match ? = " + pattern.matcher("lkdsjf sjfoi iphone ldsjfqoij, kjoij").matches());
		System.out.println("**** match ? = " + pattern.matcher("lkdsjf sjfoi iphone ipad ldsjfqoij, kjoij").matches());
		System.out.println("**** match ? = " + pattern.matcher("lkdsjf sjfoi iphone htc ldsjfqoij, kjoij").matches());
		System.out.println("**** match ? = " + pattern.matcher("lkdsjf sjfoi ipad ldsjfqoij, kjoij").matches());
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
		Collection<Map.Entry<String, String>> entries = devices.entrySet();
		for (Map.Entry<String, String> entry : entries) {
			if (entry.getKey().endsWith(".in")) { // if "." also this not a device but config of device.
				Pattern pattern = Pattern.compile(entry.getValue());
				if (userAgent != null && pattern.matcher(userAgent).matches()) {
					String localCode = entry.getKey().split("\\.")[0];
					String exclude = devices.get(localCode + ".out");
					if (exclude != null) { // if "." also this not a device but config of device.
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

	@Override
	public String toString() {
		return getLabel();
	}

	public boolean isHuman() {
		return !NetHelper.isUserAgentRobot(getUserAgent());
	}

	public void unforceDefault() {
		forcedCode = null;
	}

}
