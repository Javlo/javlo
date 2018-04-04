package org.javlo.service.shared.stockvault;

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
import org.javlo.service.shared.pixabay.RemoteImageSharedContent;
import org.javlo.utils.TimeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StockvaultSharedContentProvider extends AbstractSharedContentProvider {

	private static final Map<String,JsonArray> cache = new TimeMap<String, JsonArray>(24*60*60);

	private static Logger logger = Logger.getLogger(StockvaultSharedContentProvider.class.getName());

	public StockvaultSharedContentProvider() {
		setName("stockvault.net");
		try {
			setURL(new URL("https://www.stockvault.net/"));
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
		String basicURL = "https://www.stockvault.net/api/json/?apikey=" + staticConfig.getSharedStockvaultAPIKey() + "&query=" + query+ "&type=search";
		Collection<SharedContent> outSharedContent = new LinkedList<SharedContent>();
		try {
			JsonArray json = cache.get(query);
			if (json == null) {
				String jsonRAW = NetHelper.readPageGet(new URL(basicURL));
				json = (JsonArray) new JsonParser().parse(jsonRAW);
				cache.put(query, json);				
			}			
			JsonArray hits = json;
			for (int i = 0; i < hits.size(); i++) {
				JsonObject item = (JsonObject) hits.get(i);
				String url = item.get("photoPageLink").getAsString();				
				String id = StringHelper.stringToFileName(url.replace(getURL().toString(), ""));
				RemoteImageSharedContent sharedContent = new RemoteImageSharedContent(id, null, "stockvault");
				String title = StringHelper.neverNull(item.get("title"));
				title = title.replaceAll("\"", "");
				sharedContent.setTitle(title.trim());				
				sharedContent.setImageUrl(item.get("thumbnailLink").getAsString());			
				sharedContent.setRemoteImageUrl(item.get("sampleLink").getAsString());				
				sharedContent.setPhotoPageLink(url);
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
	
	@Override
	public boolean isLarge() {	
		return true;
	}

}
