package org.javlo.component.dynamic;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.Fetcher.FetchField;
import org.javlo.component.dynamic.Fetcher.HtmlFetcher;
import org.javlo.component.dynamic.Fetcher.JsonFetcher;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.FieldImage;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ContentService;

public class DynamicComponentFactory extends AbstractVisualComponent {


	protected Properties properties = new Properties();

	public static final String TYPE = "dynamic-component-factory";

	private static final String COMP_TYPE = "component.type";
	private static final String SOURCE_TYPE = "source.type";
	private static final String SOURCE_URL = "source.url";
	private static final String LIST_SELECTOR = "list.selector";
	private static final String LIST_ID = "list.id";
	private static final String FIELD_PREFIX = "field.";

	private static final String HTML_SOURCE_TYPE = "html";
	private static final String JSON_SOURCE_TYPE = "json";


	public DynamicComponentFactory() {
	}

	@Override
	public String getType() {
		return 	TYPE;
	}
	
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {	
		super.init(bean, ctx);
		properties.load(stringToStream(getValue()));
	}
	
	@Override
	public String getHexColor() {	
		return CONTAINER_COLOR;
	}
	
	protected String getDynamicComponentType() {
		return properties.getProperty(COMP_TYPE);
	}
	
	protected String getSourceType() {
		return properties.getProperty(SOURCE_TYPE);
	}

	protected String getSourceUrl() {
		return properties.getProperty(SOURCE_URL);
	}

	protected String getListSelector() {
		return properties.getProperty(LIST_SELECTOR);
	}

	protected String getIdField() {
		return properties.getProperty(LIST_ID);
	}

	public void update(ContentContext ctx) throws Exception {

		String dynamicComponentType = StringHelper.trimAndNullify(getDynamicComponentType());
		String idField = StringHelper.trimAndNullify(getIdField());
		if (dynamicComponentType == null || idField == null) {
			return;
		}
		Fetcher<?> fetcher = buildFetcher(ctx);
		if (fetcher == null) {
			return;
		}
		Map<String, DynamicComponent> existing = new HashMap<String, DynamicComponent>();
		for (IContentVisualComponent comp : getPage().getContent(ctx).getIterable(ctx)) {
			if (comp instanceof DynamicComponent) {
				DynamicComponent dyncomp = (DynamicComponent) comp;
				if (dyncomp.getDynamicId() != null && dyncomp.getDynamicId().startsWith(getId() + "::")) {
					existing.put(dyncomp.getDynamicId(), dyncomp);
				}
			}
		}
		GlobalContext globalContext = ctx.getGlobalContext();
		StaticConfig staticConfig = globalContext.getStaticConfig();
		String imageFolderSubPath = "dcomp-import-" + getPage().getName();
		File imageFolder = new File(URLHelper.mergePath(globalContext.getStaticFolder(), staticConfig.getImageFolderName(), imageFolderSubPath));
		imageFolder.mkdirs();
		ContentService content = ContentService.getInstance(globalContext);
		List<Map<String, String>> list = fetcher.fetch();
		String previousId = getId();
		for (Map<String, String> item : list) {
			String idValue = item.get(idField);
			String dynamicId = getId() + "::" + idValue;
			DynamicComponent comp = existing.remove(dynamicId);
			if (comp == null) {
				ComponentBean bean = new ComponentBean(StringHelper.getRandomId(), dynamicComponentType, "", getComponentBean().getLanguage(), false, ctx.getCurrentEditUser());
				bean.setArea(getArea());
				getPage().addContent(previousId, bean);
				comp = (DynamicComponent) content.getComponent(ctx, bean.getId());
			}
			previousId = comp.getId();
			List<Field> fields = comp.getFields(ctx);
			for (Field field : fields) {
				String value = item.get(field.getName());
				if (value != null) {
					if (field instanceof FieldImage) {
						try {
							String extension = StringHelper.getFileExtension(value);
							String imageFileName = idValue + "." + extension;
							File imageFile = new File(imageFolder, imageFileName);
							ResourceHelper.writeUrlToFile(new URL(value), imageFile);
							FieldImage img = (FieldImage) field;
							img.setCurrentFolder(imageFolderSubPath);
							img.setCurrentFile(imageFileName);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						field.setValue(value);
					}
				}
			}
			comp.setDynamicId(dynamicId);
		}
		for (DynamicComponent comp : existing.values()) {
			getPage().removeContent(ctx, comp.getId());
		}
	}

	private Fetcher<?> buildFetcher(ContentContext ctx) {
		String sourceType = StringHelper.trimAndNullify(getSourceType());
		String sourceUrl = StringHelper.trimAndNullify(getSourceUrl());
		String listSelector = StringHelper.trimAndNullify(getListSelector());
		if (sourceType == null || sourceUrl == null || listSelector == null) {
			return null;
		}
		List<FetchField> fields = getFetchFields(ctx);
		if (fields == null || fields.isEmpty()) {
			return null;
		}
		Fetcher<?> fetcher = null;
		if (HTML_SOURCE_TYPE.equals(sourceType)) {
			fetcher = new HtmlFetcher();
		} else if (JSON_SOURCE_TYPE.equals(sourceType)) {
			fetcher = new JsonFetcher();
		} else {
			//Wrong config
			return null;
		}
		fetcher.setSourceUrl(sourceUrl);
		fetcher.setListSelector(listSelector);
		fetcher.setFields(fields);
		return fetcher;
	}

	private List<FetchField> getFetchFields(ContentContext ctx) {
		Map<String, FetchField> out = new HashMap<String, FetchField>();
		int prefixLength = FIELD_PREFIX.length();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (key.startsWith(FIELD_PREFIX)) {
				key = StringHelper.trimAndNullify(key.substring(prefixLength));
				if (key != null) {
					String[] parts = key.split("\\.", 2);
					key = parts[0];
					FetchField field = out.get(key);
					if (field == null) {
						field = new FetchField();
						field.setName(key);
						out.put(key, field);
					}
					String property = null;
					if (parts.length > 1) {
						property = parts[1];
					}
					if (property == null || "selector".equals(property)) {
						field.setSelector(value);
					} else if ("name".equals(property)) {
						field.setName(value);
					} else if ("transform".equals(property)) {
						field.setTransform(value);
					}
				}
			}
		}
		return new LinkedList<FetchField>(out.values());
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {	
		super.performEdit(ctx);
		properties.clear();
		properties.load(stringToStream(getValue()));
		update(ctx);
	}

}
