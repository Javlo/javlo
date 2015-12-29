package org.javlo.image;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.rendering.Device;
import org.javlo.template.Template;

public class ImageConfig {
	
	public static class ImageParameters {
		private int page = 1;
		private boolean lowDef = false;
		public ImageParameters(HttpServletRequest request) {
			if (request.getParameter("page") != null && StringHelper.isDigit(request.getParameter("page"))) {
				page = Integer.parseInt(request.getParameter("page"));
			}
			lowDef = StringHelper.isTrue(request.getParameter("lowdef"));
		}
		public int getPage() {
			return page;
		}
		public void setPage(int page) {
			this.page = page;
		}
		public boolean isLowDef() {
			return lowDef;
		}
		public void setLowDef(boolean lowDef) {
			this.lowDef = lowDef;
		}
		public String getKey() {
			return ""+page+'-'+lowDef;
		}
		
	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ImageConfig.class.getName());

	PropertiesConfiguration properties = new PropertiesConfiguration();

	private static final String FILE = "/WEB-INF/config/image-config.properties";
	private static final String FILE_BASE = "/WEB-INF/config/image-config-base.properties";
	private static final String KEY = ImageConfig.class.getName();
	private static final String ALL = "all";

	private List<String> filters = new LinkedList<String>();
	
	private ImageConfig(File file) {
		try {
			properties.load(file);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

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
		String deviceCode = Device.DEFAULT;
		if (device != null) {
			deviceCode = device.getCode();
		}
		String key = filter + '.' + area + '.' + deviceCode + '.' + param;
		if (properties.containsKey(key)) {
			return key;
		} else {
			key = filter + '.' + deviceCode + '.' + param;
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

			int deviceWith = properties.getInt(key, device!=null?getWidth(null, ALL, null):-2);
			if (deviceWith != -2) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".width", -1);
	}
	
	public boolean isHighQuality(Device device, String filter, String area) {
		if (device != null) {
			String key = getKey(device, filter, area, "hq");
			String quality = properties.getString(key, properties.getString(getKey(null, ALL, null, "hq"), null));
			if (quality != null) {
				return StringHelper.isTrue(quality);
			}
		}
		return properties.getBoolean(filter + ".hq", false);
	}
	
	public int getFolderWidth(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "folder.width");

			int deviceWith = properties.getInt(key, -2);
			if (deviceWith != -2) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".folder.width", -1);
	}
	
	public int getFolderHeight(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "folder.height");

			int deviceWith = properties.getInt(key, -2);
			if (deviceWith != -2) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".folder.height", -1);
	}
	
	public int getFolderThumbWidth(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "folder.thumb.width");

			int deviceWith = properties.getInt(key, -2);
			if (deviceWith != -2) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".folder.thumb.width", 100);
	}
	
	public int getFolderThumbHeight(Device device, String filter, String area) {
		if (device != null) {

			String key = getKey(device, filter, area, "folder.thumb.height");

			int deviceWith = properties.getInt(key, -2);
			if (deviceWith != -2) {
				return deviceWith;
			}
		}
		return properties.getInt(filter + ".folder.thumb.height", 100);
	}
	
	public boolean isFolderThumbShuffle(Device device, String filter, String area) {
		if (device != null) {
			String key = getKey(device, filter, area, "folder.thumb.shuffle");
			return properties.getBoolean(key, properties.getBoolean(filter + "folder.thumb.shuffle", true));
		}
		return properties.getBoolean(filter + "folder.thumb.shuffle", true);
	}

	public int getMaxWidth(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "max-width");
		return properties.getInt(key, -1);
	}

	public int getHeight(Device device, String filter, String area) {
		if (device != null) {
			String key = getKey(device, filter, area, "height");
			int deviceHeigth = properties.getInt(key, device!=null?getHeight(null, ALL, null):-2);
			if (deviceHeigth != -2) {
				return deviceHeigth;
			}
		}
		return properties.getInt(filter + ".height", -1);
	}
	
	public double getZoom(Device device, String filter, String area) {
		if (device != null) {
			String key = getKey(device, filter, area, "zoom");
			Double deviceZoom = properties.getDouble(key, device!=null?getZoom(null, ALL, null):null);
			if (deviceZoom != null) {
				return deviceZoom;
			}
		}
		return properties.getDouble(filter + ".zoom", 1);
	}


	public int getMaxHeight(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "max-height");
		return properties.getInt(key, device!=null?getMaxHeight(null, ALL, null):-1);
	}

	public int getMarginLeft(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-left");
		return properties.getInt(key, device!=null?getMarginLeft(null, ALL, null):0);
	}

	public int getMarginRigth(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-right");
		return properties.getInt(key, device!=null?getMarginRigth(null, ALL, null):0);
	}

	public int getMarginTop(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-top");
		return properties.getInt(key, device!=null?getMarginTop(null, ALL, null):0);
	}

	public int getMarginBottom(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "margin-bottom");
		return properties.getInt(key, device!=null?getMarginBottom(null, ALL, null):0);
	}

	public String getFileExtension(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "file-extension");
		String deviceValue = properties.getString(key, device!=null?getFileExtension(null, ALL, null):null);
		return deviceValue;
	}

	public String getLayer(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "layer");
		String deviceValue = properties.getString(key, device!=null?getLayer(null, ALL, null):null);
		return deviceValue;
	}

	public boolean isBackGroudColor(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "background-color");
		String bg = properties.getString(key,properties.getString(getKey(null, ALL,null, "background-color"),null));
		return  bg != null && !bg.equals("transparent");	
	}
	
	public Color getBGColor(Device device, String filter, String area) {

		String key = getKey(device, filter, area, "background-color");

		String deviceValue = properties.getString(key, properties.getString(getKey(null, ALL,null, "background-color"),null));
		if (deviceValue != null && !deviceValue.equals("-1") && !deviceValue.equals("transparent")) {
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

		String deviceValue = properties.getString(key, properties.getString(getKey(null, ALL,null, "adjust-color"),null));
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

		String deviceValue = properties.getString(key, properties.getString(getKey(null, ALL,null, "replace-alpha"),null));
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
	
	public Color getAlpha(Device device, String filter, String area) {

		String key = getKey(device, filter, area, "alpha");

		String deviceValue = properties.getString(key, properties.getString(getKey(null, ALL,null, "alpha"),null));
		if (deviceValue != null) {
			try {
				return Color.decode(deviceValue);
			} catch (NumberFormatException e) {
				logger.warning("bad alpha color found in image config file (filter: " + filter + ", device: " + deviceValue + ", area: " + area + ") : " + deviceValue);
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean isRoundCorner(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "round-corner");
		return properties.getBoolean(key, device!=null?isRoundCorner(null, ALL, null):false);
	}
	
	public boolean isHorizontalFlip(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "horizontal-flip");
		return properties.getBoolean(key, device!=null?isHorizontalFlip(null, ALL, null):false);
	}
	
	public boolean isVerticalFlip(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "vertical-flip");
		return properties.getBoolean(key, device!=null?isVerticalFlip(null, ALL, null):false);
	}

	public boolean isGrayscale(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "grayscale");
		return properties.getBoolean(key, device!=null?isGrayscale(null, ALL, null):false);
	}
	
	public boolean isGrayscaleAveraging(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "grayscale-averaging");
		return properties.getBoolean(key, device!=null?isGrayscaleAveraging(null, ALL, null):false);
	}
	
	public boolean isGrayscaleLuminosity(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "grayscale-luminosity");
		return properties.getBoolean(key, device!=null?isGrayscaleLuminosity(null, ALL, null):false);
	}	
	
	public boolean isGrayscaleDesaturation(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "grayscale-luminosity");
		return properties.getBoolean(key, device!=null?isGrayscaleDesaturation(null, ALL, null):false);
	}

	public boolean isCrystallize(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "crystallize");
		return properties.getBoolean(key, device!=null?isCrystallize(null,ALL,null):false);
	}
	
	public boolean isGlow(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "glow");
		return properties.getBoolean(key, device!=null?isGlow(null,ALL,null):false);
	}
	
	public int getSepiaIntensity(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "sepia");
		return properties.getInt(key, device!=null?getSepiaIntensity(null,ALL,null):0);
	}
	
	public float getConstrast(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "contrast");
		return properties.getFloat(key, device!=null?getConstrast(null,ALL,null):1);
	}
	
	public float getBrightness (Device device, String filter, String area) {
		String key = getKey(device, filter, area, "brightness");												   
		return properties.getFloat(key, device!=null?getBrightness(null,ALL,null):1);
	}
	
	public int getDashed(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "dashed");
		return properties.getInt(key, device!=null?getDashed(null,ALL,null):1);
	}

	public boolean isEdge(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "edge");
		return properties.getBoolean(key, device!=null?isEdge(null,ALL,null):false);
	}
	
	public boolean isIndexed(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "indexed");
		return properties.getBoolean(key, device!=null?isIndexed(null,ALL,null):false);
	}

	public boolean isCropResize(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "crop-resize");
		return properties.getBoolean(key, device!=null?isCropResize(null,ALL,null):false);
	}

	public boolean isAddBorder(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "add-border");
		return properties.getBoolean(key, device!=null?isAddBorder(null,ALL,null):false);
	}

	public boolean isFocusZone(Device device, String filter, String area) {
		String key = getKey(device, filter, area, ".focus-zone");
		return properties.getBoolean(key, device!=null?isFocusZone(null,ALL,null):false);
	}

	public boolean isFraming(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "framing");
		return properties.getBoolean(key, device!=null?isFraming(null,ALL,null):false);
	}

	public boolean isEmboss(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "emboss");
		return properties.getBoolean(key, device!=null?isEmboss(null,ALL,null):false);
	}
	
	public int getResizeDashed(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "resize-dashed");
		return properties.getInteger(key, device!=null?getResizeDashed(null,ALL,null):1);
	}

	public boolean isWeb2(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2");
		return properties.getBoolean(key, device!=null?isWeb2(null,ALL,null):false);
	}

	public int getWeb2Height(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2.height");
		return properties.getInt(key, device!=null?getWeb2Height(null,ALL,null):-1);
	}

	public int getWeb2Separation(Device device, String filter, String area) {
		String key = getKey(device, filter, area, "web2.separation");
		return properties.getInt(key, device!=null?getWeb2Separation(null,ALL,null):-1);
	}

	public List<String> getFilters() {
		return filters;
	}

	public PropertiesConfiguration getProperties() {
		return properties;
	}
	
	public static void main(String[] args) {
		ImageConfig imageConfig = new ImageConfig(new File("c:/trans/image.properties"));
		System.out.println("***** ImageConfig.main : grayscale ? "+imageConfig.isGrayscaleLuminosity(null, "preview", ALL)); //TODO: remove debug trace
	}
	

}
