package org.javlo.fields;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class FieldFactory {
	
	private static Logger logger = Logger.getLogger(FieldFactory.class.getName());

	private static Field[] fields = new Field[] { new Field(), new FieldSmallText(), new FieldLargeText(), new FieldWysiwyg(), new FieldWysiwygWithTitle(), new FieldDescription(), new FieldDate(), new FieldTime(), new FieldAge(),new FieldFile(), new FieldList(), new OpenList(), new OpenMultiList(), new FieldMultiList(), new FieldImage(), new FieldSound(), new FieldExternalLink(), new FieldInternalLink(), new FieldEmail(), new FieldTextList(), new FieldBoolean(), new FieldHelp(), new FieldNumber(), new Heading("h1"), new Heading("h2"), new Heading("h3"), new Heading("h4"), new Heading("h5"), new Heading("h6"), new DateOfPublication(), new EndDateOfPublication(), new StaticContent(), new FieldFont(), new UserList(), new FieldColor(), new FieldXhtml() };

	private static Map<String, Field> fieldsMap = new HashMap<>();

	static {
		for (Field field : fields) {
			fieldsMap.put(field.getType(), field);
		}
	}

	public static Field getField(ContentContext ctx, IContentVisualComponent component, StaticConfig staticConfig, GlobalContext globalContext, I18nAccess i18nAccess, Properties properties, String label, String name, String group, String type, String id) {
		return getField(ctx, component, staticConfig, globalContext, i18nAccess, properties, label, null, name, group, type, id);
	}
	
	private static Field getField(ContentContext ctx, IContentVisualComponent component, StaticConfig staticConfig, GlobalContext globalContext, I18nAccess i18nAccess, Properties properties, String label, String placeholder, String name, String group, String type, String id) {
		Field field = fieldsMap.get(type);
		if (field != null) {
			try {
				Field newField = field.newInstance(component);
				newField.setId(id);
				newField.setName(name);
				if (properties != null) {
					newField.setProperties(ctx, properties);
				} else {
					newField.setProperties(ctx , new Properties());
				}
				newField.setStaticConfig(staticConfig);
				newField.setGlobalContext(globalContext);
				newField.setI18nAccess(i18nAccess);
				newField.setLabel(label);
				newField.setPlaceholder(placeholder);
				newField.setGroup(group);
				return newField;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.warning("field not found : "+type);
		return null;
	}

}
