package org.javlo.fields;

import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

public class FieldFactory {
	
	private static Logger logger = Logger.getLogger(FieldFactory.class.getName());

	private static Field[] fields = new Field[] { new Field(), new FieldSmallText(), new FieldLargeText(), new FieldWysiwyg(), new FieldWysiwygWithTitle(), new FieldDescription(), new FieldDate(), new FieldAge(),new FieldFile(), new FieldList(), new OpenList(), new OpenMultiList(), new FieldMultiList(), new FieldImage(), new FieldSound(), new FieldExternalLink(), new FieldInternalLink(), new FieldEmail(), new FieldTextList(), new FieldBoolean(), new FieldNumber(), new Heading("h1"), new Heading("h2"), new Heading("h3"), new Heading("h4"), new Heading("h5"), new Heading("h6"), new DateOfPublication(), new EndDateOfPublication(), new StaticContent(), new FieldFont() };

	public static Field getField(IContentVisualComponent component, StaticConfig staticConfig, GlobalContext globalContext, I18nAccess i18nAccess, Properties properties, String label, String name, String type, String id) {
		return getField(component, staticConfig, globalContext, i18nAccess, properties, label, null, name, type, id);
	}
	
	public static Field getField(IContentVisualComponent component, StaticConfig staticConfig, GlobalContext globalContext, I18nAccess i18nAccess, Properties properties, String label, String placeholder, String name, String type, String id) {
		for (Field field : fields) {
			if (field.getType().equals(type)) {				
				try {
					Field newField = field.newInstance(component);
					newField.setId(id);
					newField.setName(name);
					if (properties != null) {
						newField.setProperties(properties);
					} else {
						newField.setProperties(new Properties());
					}
					newField.setStaticConfig(staticConfig);
					newField.setGlobalContext(globalContext);
					newField.setI18nAccess(i18nAccess);
					newField.setLabel(label);
					newField.setPlaceholder(placeholder);
					
					return newField;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		logger.warning("field not found : "+type);
		return null;
	}

}
