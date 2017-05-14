package org.javlo.service.shared.pixabay;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.shared.AbstractSharedContentProvider;
import org.javlo.service.shared.SharedContent;
import org.javlo.utils.TimeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PixabaySharedContentProvider extends AbstractSharedContentProvider {
	
	private static final Map<String,JsonObject> cache = new TimeMap<String, JsonObject>(24*60*60);

	private static Logger logger = Logger.getLogger(PixabaySharedContentProvider.class.getName());

	public PixabaySharedContentProvider() {
		setName("pixabay.com");
		try {
			setURL(new URL("https://pixabay.com/"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void refresh(ContentContext ctx) {
		getContent(ctx);
	}

	@Override
	public Collection<SharedContent> searchContent(ContentContext ctx, String query) {
		StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();
		String basicURL = "https://pixabay.com/api/?key=" + staticConfig.getSharedPixaBayAPIKey() + "&q=" + query
				+ "&image_type=photo&pretty=true&per_page=100";		
		Collection<SharedContent> outSharedContent = new LinkedList<SharedContent>();
		try {
			JsonObject json = cache.get(query);
			if (json == null) {
				String jsonRAW = NetHelper.readPageGet(new URL(basicURL));
				json = (JsonObject) new JsonParser().parse(jsonRAW);
				cache.put(query, json);				
			}			
			JsonArray hits = (JsonArray) json.get("hits");
			for (int i = 0; i < hits.size(); i++) {
				JsonObject item = (JsonObject) hits.get(i);
				String id = StringHelper.neverNull(item.get("id"));
				PixabaySharedContent sharedContent = new PixabaySharedContent(id, null);
				String title = StringHelper.neverNull(item.get("tags"));
				title = title.replaceAll("\"", "");
				sharedContent.setTitle(title.trim());
				sharedContent.setImageUrl(item.get("webformatURL").getAsString());
				sharedContent.setRemoteImageUrl(item.get("webformatURL").getAsString());
				outSharedContent.add(sharedContent);
			}
			return outSharedContent;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getType() {
		return TYPE_IMAGE;
	}

	@Override
	public int getContentSize(ContentContext ctx) {
		return 0;
	}

	@Override
	public int getCategoriesSize(ContentContext ctx) {
		return 0;
	}

	public static void main(String[] args) throws MalformedURLException, Exception {
		String basicURL = "https://pixabay.com/api/?key=974407-6afc4f4961c68647f45c385ad&q=sexy&image_type=photo&pretty=true";
		String jsonRAW = NetHelper.readPageGet(new URL(basicURL));
		JsonObject json = (JsonObject) new JsonParser().parse(jsonRAW);
		JsonArray hits = (JsonArray) json.get("hits");
		for (int i = 0; i < hits.size(); i++) {
			System.out.println(((JsonObject) hits.get(i)).get("previewURL"));
		}
		System.out.println("#=" + json.size());
	}

}
