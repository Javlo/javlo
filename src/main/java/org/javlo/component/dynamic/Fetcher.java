package org.javlo.component.dynamic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.helper.NetHelper;
import org.javlo.utils.JSONMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public abstract class Fetcher<I> {

	public static void main(String[] args) throws MalformedURLException, Exception {
		{
			Fetcher<?> f = new JsonFetcher();
			f.setSourceUrl("http://www.europarl.europa.eu/meps/fr/json/getDistricts.html?&bodyType=ALL&bodyValue=&country=&countryCircons=&mepId=&politicalGroup=");
			f.setListSelector("result");
			f.setFields(Arrays.asList(
					new FetchField("persId", "persId"),
					new FetchField("name", "fullName"),
					new FetchField("infoPageUrl", "detailUrl", "http://www.europarl.europa.eu__VALUE__"),
					new FetchField("logo", "photoUrl", "http://www.europarl.europa.eu__VALUE__")
					));

			List<Map<String, String>> result = f.fetch();
			for (Map<String, String> map : result) {
				System.out.println(map);
			}
		}

		{
			Fetcher<?> f = new HtmlFetcher();
			f.setSourceUrl("http://www.jeka.be/fr/quicklinks/html/contact-equipe");
			f.setListSelector(".member");
			f.setFields(Arrays.asList(
					new FetchField("name", "h3"),
					new FetchField("image", "img@abs:src"),
					new FetchField("image2", "img@src", "http://www.jeka.be__VALUE__")
					));
			List<Map<String, String>> result = f.fetch();
			for (Map<String, String> map : result) {
				System.out.println(map);
			}
		}
	}

	public static class JsonFetcher extends Fetcher<JsonElement> {

		@Override
		protected Iterator<JsonElement> parseItems(String content) {
			JSONMap map = JSONMap.parseMap(content);
			List<JsonElement> list = map.getValue(listSelector, new TypeToken<List<JsonElement>>() {
			}.getType());
			return list.iterator();
		}

		@Override
		protected String getValue(JsonElement item, FetchField field) {
			return item.getAsJsonObject().get(field.getSelector()).getAsString();
		}

	}

	public static class HtmlFetcher extends Fetcher<Element> {

		@Override
		protected Iterator<Element> parseItems(String content) {
			Document doc = Jsoup.parse(content);
			doc.setBaseUri(sourceUrl);
			return doc.select(listSelector).iterator();
		}

		@Override
		protected String getValue(Element item, FetchField field) {
			String[] parts = field.getSelector().split("@", 2);
			String css = parts[0];
			String attr = null;
			if (parts.length > 1) {
				attr = parts[1];
			}
			Elements fieldElement = item.select(css);
			String value;
			if (attr != null) {
				value = fieldElement.attr(attr);
			} else {
				value = fieldElement.text();
			}
			return value;
		}

	}

	protected String sourceUrl;
	protected String listSelector;
	protected List<FetchField> fields;

	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getListSelector() {
		return listSelector;
	}
	public void setListSelector(String listSelector) {
		this.listSelector = listSelector;
	}

	public List<FetchField> getFields() {
		return fields;
	}
	public void setFields(List<FetchField> fields) {
		this.fields = fields;
	}

	public List<Map<String, String>> fetch() throws Exception {
		String content = NetHelper.readPageGet(new URL(sourceUrl));
		Iterator<I> iterator = parseItems(content);
		List<Map<String, String>> out = new LinkedList<Map<String, String>>();
		while (iterator.hasNext()) {
			I item = iterator.next();
			Map<String, String> parsed = parseItem(item);
			out.add(parsed);
		}
		return out;
	}

	protected abstract Iterator<I> parseItems(String content);

	protected Map<String, String> parseItem(I item) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		for (FetchField field : fields) {
			String value = getValue(item, field);
			value = transformValue(value, field);
			out.put(field.getName(), value);
		}
		return out;
	}

	private String transformValue(String value, FetchField field) {
		if (value != null && field.getTransform() != null) {
			value = field.getTransform().replace("__VALUE__", value);
		}
		return value;
	}
	protected abstract String getValue(I item, FetchField field);

	public static class FetchField {

		private String name;
		private String selector;
		private String transform;

		public FetchField() {
		}

		public FetchField(String name, String selector) {
			this.name = name;
			this.selector = selector;
		}
		public FetchField(String name, String selector, String transform) {
			this.name = name;
			this.selector = selector;
			this.transform = transform;
		}

		public String getName() {
			return name;
		}
		public void setName(String fieldName) {
			this.name = fieldName;
		}

		public String getSelector() {
			return selector;
		}
		public void setSelector(String fieldSelector) {
			this.selector = fieldSelector;
		}

		public String getTransform() {
			return transform;
		}
		public void setTransform(String transform) {
			this.transform = transform;
		}

	}

}
