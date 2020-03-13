package org.javlo.search;

import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.search.SearchResult.SearchElement;

public class ElasticSearchEngine implements ISearchEngine {

	@Override
	public List<SearchElement> search(ContentContext ctx, String groupId, String searchStr, String sort, List<String> componentList) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateData(ContentContext ctx) throws Exception {
		// TODO Auto-generated method stub
		
	}

//	private static Logger logger = Logger.getLogger(ElasticSearchEngine.class.getName());
//
//	// The config parameters for the connection
//	private static final String HOST = "elastic.javlo.org";
//	private static final int PORT = 9200;
//	private static final String SCHEME = "http";
//
//	private static RestHighLevelClient restHighLevelClient = null;
//
//	private static final String INDEX_PREFIX = IVersion.NAME + '_';
//	private static final String TYPE = "person";
//
//	@Override
//	public List<SearchElement> search(ContentContext ctx, String groupId, String searchStr, String sort,
//			List<String> componentList) throws Exception {
//		return null;
//	}
//
//	public static RestHighLevelClient client() {
//		RestClientBuilder builder = RestClient.builder(new HttpHost(HOST, PORT, SCHEME));
//		RestHighLevelClient client = new RestHighLevelClient(builder);
//		return client;
//	}
//
//	private static final String getIndexName(ContentContext ctx) {
//		return INDEX_PREFIX + ctx.getGlobalContext().getContextKey() + '_' + ctx.getRequestContentLanguage();
//	}
//
//	@Override
//	public void updateData(ContentContext ctx) throws Exception {
//		String index = getIndexName(ctx).toLowerCase();
//		
//		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> index = "+index);
//		
//		logger.info("update index : "+index);
//		
//		MenuElement root = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE));
//		CreateIndexRequest request = new CreateIndexRequest(index);
//		request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 2));
//		Map<String, Object> message = new HashMap<>();
//		message.put("type", "text");
//		Map<String, Object> properties = root.getContentAsMap(ctx);		
//		Map<String, Object> mapping = new HashMap<>();
//		mapping.put("properties", properties);
//		request.mapping(mapping);
//		restHighLevelClient = client();
//		CreateIndexResponse indexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
//		
//		logger.info("response id : "+indexResponse.index());
//	}
//
//	public static void main(String[] args) throws IOException {
////		CreateIndexRequest request = new CreateIndexRequest("users");
////		request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 2));
////		Map<String, Object> message = new HashMap<>();
////		message.put("type", "text");
////		Map<String, Object> properties = new HashMap<>();
////		properties.put("userId", message);
////		properties.put("name", message);
////		Map<String, Object> mapping = new HashMap<>();
////		mapping.put("properties", properties);
////		request.mapping(mapping);
////		restHighLevelClient = client();
////		CreateIndexResponse indexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
////		System.out.println("response id: " + indexResponse.index());
//		
//		String index = "myindex";
//		
//		{
//			CreateIndexRequest request = new CreateIndexRequest(index);
//			request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 2));
//			Map<String, Object> json = new HashMap<String, Object>();
//			json.put("user","kimchy");
//			json.put("postDate",new Date());
//			json.put("mess-age","trying out Elasticsearch");
//			IndexRequest indexRequest = new IndexRequest("posts").id("1").source(json);			
//			System.out.println("response id: " + indexRequest.index());
//		}
//		
////		{
////			RestHighLevelClient client = client();		
////			UpdateRequest updateRequest = new UpdateRequest();
////			updateRequest.index(index);
////			updateRequest.type("_doc");
////			updateRequest.id("1");			
////			Map<String, Object> json = new HashMap<String, Object>();
////			json.put("user","kimchy2");
////			json.put("postDate",new Date());
////			json.put("message","trying out Elasticsearch");			
////			updateRequest.doc(json);
////			RequestOptions options = RequestOptions.DEFAULT;
////			client.update(updateRequest, options );
////		}
// 
//	}

}
