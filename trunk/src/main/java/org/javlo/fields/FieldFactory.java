package org.javlo.fields;

import java.util.Properties;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

public class FieldFactory {

	private static Field[] fields = new Field[] { new Field(), new FieldLargeText(), new FieldDate(), new FieldFile(), new FieldList(), new FieldMultiList(), new FieldImage(), new FieldExternalLink(), new FieldInternalLink(), new FieldEmail(), new FieldTextList(), new FieldBoolean(), new Heading("h1"), new Heading("h2"), new Heading("h3"), new Heading("h4"), new Heading("h5"), new Heading("h6"), new DateOfPublication(), new EndDateOfPublication() };

	public static Field getField(IContentVisualComponent component, StaticConfig staticConfig, GlobalContext globalContext, I18nAccess i18nAccess, Properties properties, String label, String name, String type, String id) {
		for (Field field : fields) {
			if (field.getType().equals(type)) {
				try {
					Field newField = field.newInstance();
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
					return newField;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
