package org.javlo.component.config;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.template.Template;

public class ComponentConfig {
	
	public static final ComponentConfig EMPTY_INSTANCE = new ComponentConfig();

	private String templateBuildId = null;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ComponentConfig.class.getName());

	public static ComponentConfig getInstance() {
		ComponentConfig outCfg = new ComponentConfig();
		return outCfg;
	}

	public static ComponentConfig getInstance(ContentContext ctx, String type) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Template currentTemplate;
		ComponentConfig outCfg = null;
		try {						
			currentTemplate = ctx.getCurrentTemplate();	
			String templateId;
			if (currentTemplate != null) {
				templateId = currentTemplate.getId();
			} else {
				return ComponentConfig.EMPTY_INSTANCE;
			}
			synchronized (globalContext.getLockImportTemplate()) {
				String key = KEY + '-' + templateId + '-' + type;				
				outCfg = (ComponentConfig) globalContext.getAttribute(key);
				if (outCfg == null) {
					outCfg = new ComponentConfig(ctx, currentTemplate, type);
					globalContext.setAttribute(key, outCfg);
				} else {					
					if (!currentTemplate.getBuildId().equals(outCfg.templateBuildId)) {
						outCfg = new ComponentConfig(ctx, currentTemplate, type);
						globalContext.setAttribute(key, outCfg);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outCfg;
	}

	PropertiesConfiguration properties = null;

	private static final String KEY = "_comp_config";

	private ComponentConfig() {
	}

	public String getRAWConfig(ContentContext ctx, Template currentTemplate, String type) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			if (properties != null) {
				properties.save(out);
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return new String(out.toByteArray());
	}

	private ComponentConfig(ContentContext ctx, Template currentTemplate, String type) {
		try {
			if (currentTemplate != null) {
				templateBuildId = currentTemplate.getBuildId();
				Properties templateProp = currentTemplate.getConfigComponentFile(GlobalContext.getInstance(ctx.getRequest()), type);
				logger.info("create component template config : " + currentTemplate.getName());
				if (templateProp != null) {
					Enumeration<Object> keys = templateProp.keys();
					if (properties == null) {
						properties = new PropertiesConfiguration();
					}
					while (keys.hasMoreElements()) {
						String key = "" + keys.nextElement();
						if (key.startsWith("renderer.")) {
							/*ContentContext notAbstCtx = new ContentContext(ctx);
							notAbstCtx.setAbsoluteURL(false);*/
							// String renderer = URLHelper.createStaticTemplateURLWithoutContext(notAbstCtx, currentTemplate, "" + templateProp.get(key));
							if (properties.containsKey(key)) {
								properties.clearProperty(key);
							}
							properties.addProperty("" + key, templateProp.get(key));
						} else {
							if (properties.containsKey(key)) {
								properties.clearProperty(key);
							}
							properties.addProperty("" + key, templateProp.get(key));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("no config file for : " + type + " defined in template : " + currentTemplate.getId());
		}
	}

	public String getProperty(String key, String defaultValue) {
		if (properties == null) {
			return defaultValue;
		}
		String value = properties.getString(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public Map<String, String> getRenderes() {
		if (properties == null) {
			return Collections.EMPTY_MAP;
		}
		Map<String, String> outRenderers = new Hashtable<String, String>();
		Iterator keys = properties.getKeys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.startsWith("renderer.") && key.split(".").length < 3) {
				String value = (String) properties.getProperty(key);
				key = key.replaceFirst("renderer.", "");
				outRenderers.put(key, value);
			}
		}
		return outRenderers;
	}

	public String getDefaultRenderer() {
		if (properties == null) {
			return null;
		}
		String defaultRenderer = properties.getString("default-renderer");
		if (defaultRenderer != null) {
			if (getRenderes().get(defaultRenderer) == null) {
				logger.warning("default renderer not found in renderer list : " + defaultRenderer);
				defaultRenderer = null;
			}
		}
		return defaultRenderer;
	}

	public String[] getStyleLabelList() {
		if (properties == null) {
			return StringHelper.EMPTY_STRING_ARRAY;
		}
		String styleListLabel = properties.getString("style-list-label", null);
		if (styleListLabel == null) {
			return StringHelper.EMPTY_STRING_ARRAY;
		}
		return StringHelper.stringToArray(styleListLabel, ";");
	}

	public String[] getStyleList() {
		if (properties == null) {
			return StringHelper.EMPTY_STRING_ARRAY;
		}
		String styleList = properties.getString("style-list", null);
		if (styleList == null) {
			return StringHelper.EMPTY_STRING_ARRAY;
		}
		return StringHelper.stringToArray(styleList, ";");
	}

	public String getStyleTitle() {
		if (properties == null) {
			return null;
		}
		return properties.getString("style-title");
	}
	
	public int getComplexity(int defaultComplexity) {
		if (properties == null || properties.getString("complexity") == null) {
			return defaultComplexity;
		} else {			
			String complexity = properties.getString("complexity");
			if (StringHelper.isDigit(complexity)) {
				return Integer.parseInt(complexity);	
			} else {
				return defaultComplexity;
			}			
		}
	}

	public boolean isClickable() {
		if (properties == null) {
			return true;
		}
		return StringHelper.isTrue(properties.getString("comp.click", "true"));
	}

	public boolean isConfigFound() {
		return properties != null;
	}

	public boolean needJS() {
		if (properties == null) {
			return true;
		}
		return StringHelper.isTrue(properties.getString("comp.js", "true"));
	}
	
	public boolean isChooseBackgoundColor() {
		if (properties == null) {
			return false;
		}
		return StringHelper.isTrue(properties.getString("comp.color.background", null));
	}
	
	public boolean isChooseTextColor() {
		if (properties == null) {
			return false;
		}
		return StringHelper.isTrue(properties.getString("comp.color.text", null));
	}
	
	public boolean isPreviewEditable() {
		if (properties == null || properties.getString("edit.preview", null) == null) {
			return true;
		}
		return StringHelper.isTrue(properties.getString("edit.preview", null));

	}
	
	public boolean isDataFeedBack() {
		return StringHelper.isTrue(properties.getString("comp.data.feedBack", null));
	}

}
