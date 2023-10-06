package org.javlo.service.shared.freepik;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.shared.AbstractSharedContentProvider;
import org.javlo.service.shared.SharedContent;
import org.javlo.utils.TimeMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class FreepikSharedContentProvider extends AbstractSharedContentProvider {
	
	private static final Map<String,JsonObject> cache = new TimeMap<String, JsonObject>(24*60*60);

	private static Logger logger = Logger.getLogger(FreepikSharedContentProvider.class.getName());

	public FreepikSharedContentProvider() {
		setName("freepik.com");
		try {
			setURL(new URL("https://freepik.com/"));
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
		String lg = ctx.getGlobalContext().getEditLanguage(ctx.getSession());
		String basicURL = "https://api.freepik.com/v1/resources?locale="+lg+"&page=1&limit=100&order=latest&term="+query;
		Collection<SharedContent> outSharedContent = new LinkedList<SharedContent>();
		try {
			JsonObject json = cache.get(query);
			if (json == null) {
				Map<String,String> header = new HashMap<>();
				header.put("Accept-Language", lg);
				header.put("Accept", "application/json");
				header.put("Content-Type", "application/json");
				header.put("X-Freepik-API-Key", staticConfig.getSharedFreepikAPIKey());
				String jsonRAW = NetHelper.readPageGet(new URL(basicURL), header);
				json = (JsonObject) new JsonParser().parse(jsonRAW);
				cache.put(query, json);				
			}			
			JsonArray hits = (JsonArray) json.get("data");
			for (int i = 0; i < hits.size(); i++) {
				JsonObject item = (JsonObject) hits.get(i);
				String id = StringHelper.neverNull(item.get("id"));
				RemoteImageSharedContent sharedContent = new RemoteImageSharedContent(id, null, "freepik");
				String title = StringHelper.neverNull(item.get("name"));
				title = title.replaceAll("\"", "");
				sharedContent.setTitle(title.trim());
				JsonObject el = (JsonObject)item.get("preview");
				String imageUrl = String.valueOf(el.get("url"));
				sharedContent.setPhotoPageLink(item.get("url").getAsString());
				sharedContent.setImageUrl(imageUrl);
				sharedContent.setRemoteImageUrl(imageUrl);
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
		String basicURL = "https://api.freepik.com/v1/resources?locale=en&page=1&limit=100&order=latest&term=woman";
w²
		String apiKey = "";

		Map<String,String> header = new HashMap<>();
		header.put("Accept-Language", "en");
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("X-Freepik-API-Key", apiKey);

		String jsonRAW = NetHelper.readPageGet(new URL(basicURL));
		JsonObject json = (JsonObject) new JsonParser().parse(jsonRAW);
		JsonArray hits = (JsonArray) json.get("hits");
		for (int i = 0; i < hits.size(); i++) {
			System.out.println(((JsonObject) hits.get(i)).get("previewURL"));
		}
		System.out.println("#=" + json.size());
	}
	
	@Override
	public boolean isLarge() {	
		return true;
	}

}
