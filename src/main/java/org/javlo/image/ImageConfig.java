package org.javlo.image;

import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.rendering.Device;
import org.javlo.template.Template;

public class ImageConfig {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ImageConfig.class.getName());

	PropertiesConfiguration properties = new PropertiesConfiguration();

	private static final String FILE = "/WEB-INF/config/image-config.properties";
	private static final String FILE_BASE = "/WEB-INF/config/image-config-base.properties";
	private static final String KEY = ImageConfig.class.getName();

	private List<String> filters = new LinkedList<String>();

	private ImageConfig(GlobalContext globalContext, HttpSession session, Template template) {

		List<String> filtersCol = new LinkedList<String>();

		InputStream in = session.getServletContext().getResourceAsStream(FILE_BASE);
		if (in == null) {
			logger.warning("config file for image not found : " + FILE_BASE);
		} else {
			try {
				properties.load(new InputStreamReader(in));
			} catch (Exception e) {
				logger.warning("config file for thumbnails can not be loaded (msg: " + e.getMessage() + ")");
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		in = session.getServletContext().getResourceAsStream(FILE);
		if (in == null) {
			logger.warning("config file for thunbnails not found : " + FILE);
		} else {
			try {
				properties.load(new InputStreamReader(in));
			} catch (Exception e) {
				logger.warning("config file for thumbnails can not be loaded (msg: " + e.getMessage() + ")");
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		if (template != null) {
			PropertiesConfiguration templateProperties = template.getImageConfig();
			if (templateProperties != null) {
				// override with new properties
				Iterator<String> keys = templateProperties.getKeys();
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.endsWith(".layer")) {
						String layerURL = URLHelper.mergePath(template.getLocalWorkTemplateFolder(), template.getId(), globalContext.getContextKey());
						layerURL = URLHelper.mergePath(layerURL, templateProperties.getString(key));
						properties.setProperty(key, layerURL);
					} else {
						properties.setProperty(key, templateProperties.getProperty(key));
					}
				}
			} else {
				logger.warning("no image config file for template : " + template.getName());
			}
		} else {
			logger.warning("template not found.");
		}

		Iterator<String> propList = properties.getKeys();
		while (propList.hasNext()) {
			String key = propList.next();
			if (key.indexOf(".") > 0) {
				String filter = key.substring(0, key.indexOf("."));
				if (!filtersCol.contains(filter)) {
					filtersCol.add(filter);
				}
			}
		}

		filters = filtersCol;
		Collections.sort(filters);
	}

	public static ImageConfig getNewInstance(GlobalContext globalContext, HttpSession session, Template template) {
		String key;
		if (template == null) {
			key = KEY + '-' + Template.EDIT_TEMPLATE_CODE;
		} else {
			key = KEY + '-' + template.getId();
		}
		ImageConfig outCfg = new ImageConfig(globalContext, session, template);
		globalContext.setAttribute(key, outCfg);
		return outCfg;
	}

	public static ImageConfig getInstance(GlobalContext globalContext, HttpSession session, Template template) {
		ImageConfig outCfg;
		String key;
		if (template == null) {
			key = KEY + '-' + Template.EDIT_TEMPLATE_CODE; // template can be null only in edit mode
		} else {
			key = KEY + '-' + template.getId();
		}

		synchronized (FILE) {
			outCfg = (ImageConfig) globalContext.getAttribute(key);
			if (outCfg == null) {
				outCfg = new ImageConfig(globalContext, session, template);
				globalContext.setAttribute(key, outCfg);
			}
		}
		return outCfg;
	}

	private String getKey(Device device, String filter, String area, String param) {
		String key = filter + '.' + area + '.' + device.getCode() + '.' + param;
		if (properties.containsKey(key)) {
			return key;
		} else {
			key = filter + '.' + device.getCode() + '.' + param;
			if (properties.containsKey(key)) {
				return key;
			} else {
				key = filter + '.' + area + '.' + param;
				if (properties.containsKey(key)) {
					return key;
				} else {
					return filter + '.' + param;
				}
			}
		}
	}

	public static int alignToGrid(int size, int grid) {
		if (grid > 0) {
			return size - size % grid;
		} else {
			return size;
		}
	}

	public int getGridWidth(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "grid-width");

			int deviceWith = properties.getInt(key, -1);
			if (deviceWith != -1) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".grid-width", -1);
	}

	public int getGridHeight(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "grid-height");

			int deviceWith = properties.getInt(key, -1);
			if (deviceWith != -1) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".grid-height", -1);
	}

	public int getWidth(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "width");

			int deviceWith = properties.getInt(key, -1);
			if (deviceWith != -1) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".width", -1);
	}

	public int getMaxWidth(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "max-width");
		return properties.getInt(key, -1);
	}

	public int getHeight(Device device, String filter, String area) {
		if (device != null) {
			String key = getKey(device, filter, area, "height");
			int deviceHeigth = properties.getInt(key, -1);
			if (deviceHeigth != -1) {
				return deviceHeigth;
			}
		}

		return properties.getInt(filter + ".height", -1);
	}

	public int getMaxHeight(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "max-height");
		return properties.getInt(key, -1);
	}

	public int getMarginLeft(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-left");
		return properties.getInt(key, 0);
	}

	public int getMarginRigth(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-right");
		return properties.getInt(key, 0);
	}

	public int getMarginTop(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-top");
		return properties.getInt(key, 0);
	}

	public int getMarginBottom(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-bottom");
		return properties.getInt(key, 0);
	}

	public String getFileExtension(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "file-extension");
		String deviceValue = properties.getString(key, null);
		return deviceValue;
	}

	public String getLayer(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "layer");
		String deviceValue = properties.getString(key, null);
		return deviceValue;
	}

	public boolean isBackGroudColor(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "background-color");
		return properties.getString(key, null) != null;
	}

	public Color getBGColor(Device device, String filter, String area) {

		String key = getKey(device, filter, area, "background-color");

		String deviceValue = properties.getString(key, null);
		if (deviceValue != null) {
			try {
				return Color.decode(deviceValue);
			} catch (NumberFormatException e) {
				logger.warning("bad BG color found in image config file ( filter: " + filter + ", device: " + deviceValue + " area :" + area + " ) : " + deviceValue);
				return null;
			}
		} else {
			return null;
		}
	}

	public Color getAdjustColor(Device device, String filter, String area) {

		String key = getKey(device, filter, area, "adjust-color");

		String deviceValue = properties.getString(key, null);
		if (deviceValue != null) {
			try {
				return Color.decode(deviceValue);
			} catch (NumberFormatException e) {
				logger.warning("bad adjust color found in image config file (filter: " + filter + ", device: " + deviceValue + ", area: " + area + ") : " + deviceValue);
				return null;
			}
		} else {
			return null;
		}
	}

	public Color getReplaceAlpha(Device device, String filter, String area) {

		String key = getKey(device, filter, area, "replace-alpha");

		String deviceValue = properties.getString(key, null);
		if (deviceValue != null) {
			try {
				return Color.decode(deviceValue);
			} catch (NumberFormatException e) {
				logger.warning("bad realce alpha color found in image config file (filter: " + filter + ", device: " + deviceValue + ", area: " + area + ") : " + deviceValue);
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean isRoundCorner(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "round-corner");
		return properties.getBoolean(key, false);
	}

	public boolean isGrayscale(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "grayscale");
		return properties.getBoolean(key, false);
	}

	public boolean isCrystallize(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "crystallize");
		return properties.getBoolean(key, false);
	}

	public boolean isEdge(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "edge");
		return properties.getBoolean(key, false);
	}

	public boolean isCropResize(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "crop-resize");
		return properties.getBoolean(key, false);
	}

	public boolean isAddBorder(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "add-border");
		return properties.getBoolean(key, false);
	}

	public boolean isFocusZone(Device device, String filter, String area) {
		String key = getKey(device, filter, area, ".focus-zone");
		return properties.getBoolean(key, false);
	}

	public boolean isFraming(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "framing");
		return properties.getBoolean(key, false);
	}

	public boolean isEmboss(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "emboss");
		return properties.getBoolean(key, false);
	}

	public boolean isWeb2(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2");
		return properties.getBoolean(key, false);
	}

	public int getWeb2Height(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2.height");
		return properties.getInt(key, -1);
	}

	public int getWeb2Separation(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2.separation");
		return properties.getInt(key, -1);
	}

	public List<String> getFilters() {
		return filters;
	}

	public PropertiesConfiguration getProperties() {
		return properties;
	}

}
