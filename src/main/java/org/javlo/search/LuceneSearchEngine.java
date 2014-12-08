package org.javlo.search;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.search.SearchResult.SearchElement;
import org.javlo.service.ContentService;

/**
 * Work in progress.
 * 
 * @author bdumont
 */
public class LuceneSearchEngine implements ISearchEngine {

	private static final String ID_FIELD = "_id";
	private static final String TYPE_FIELD = "_type";
	private static final String LEVEL_FIELD_PREFIX = "level";
	private static final String PAGE_TYPE = "page";

	private static final Pattern ESCAPER = Pattern.compile("([ +\\-!\\(\\)\\{\\}\\[\\\\\\]^\\\"~\\*\\?\\:]|&&|\\|\\|)");
	private static final String ESCAPER_REPLACE = "\\\\$0";

	private Directory index;
	private StandardAnalyzer analyzer;

	@Override
	public List<SearchElement> search(ContentContext ctx, String groupId, String searchStr, String sort, List<String> componentList) throws Exception {
		init(ctx);

		String queryPattern = "level3:{QUERY}^3 level2:{QUERY}^2 level1:{QUERY}^1";//TODO move this to static config

		String escapedStr = escapeLucene(searchStr.trim());

		String queryStr = queryPattern.replace("{QUERY}", escapedStr);
		System.out.println(queryStr);

		Query q = new QueryParser("level3", analyzer).parse(queryStr);

		int hitsPerPage = 5000;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		ContentContext ctxWithContent = ctx;

		List<SearchElement> result = new LinkedList<SearchElement>();

		for (ScoreDoc scoreDoc : hits) {
			Document doc = searcher.doc(scoreDoc.doc);
			String pageId = doc.get(ID_FIELD);
			MenuElement page = nav.searchChildFromId(pageId);
			addResult(result, ctx, page, searchStr, page.getName(), page.getFullLabel(ctxWithContent), URLHelper.createURL(ctxWithContent, page.getPath()), page.getDescription(ctxWithContent), 1);
		}
		if (result.size() == 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("search.title.no-result") + ' ' + searchStr, GenericMessage.ALERT));
		}

		return result;
	}

	private static String escapeLucene(String searchStr) {
		return ESCAPER.matcher(searchStr).replaceAll(ESCAPER_REPLACE);
	}

	private void addResult(List<SearchElement> result, ContentContext ctx, MenuElement page, String searchElement, String name, String title, String url, String description, int priority) {
		SearchElement rst = new SearchElement();
		rst.setName(name);
		rst.setUrl(url);
		rst.setTitle(title);
		rst.setSearchRequest(searchElement);
		rst.setDescription(description);
		rst.setPriority(priority);
		rst.setPath(page.getPath());
		//rst.setComponent(componentsRendered);
		try {
			rst.setLocation(page.getLocation(ctx));
			rst.setCategory(page.getCategory(ctx));
			rst.setTags(page.getTags(ctx));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			if (page.getContentDateNeverNull(ctx) != null) {
				Date date = page.getContentDateNeverNull(ctx);
				rst.setDate(date);
				rst.setDateString(StringHelper.renderSortableDate(date));
				rst.setShortDate(StringHelper.renderShortDate(ctx, date));
				rst.setFullDate(StringHelper.renderFullDate(ctx, date));
				rst.setMediumDate(StringHelper.renderMediumDate(ctx, date));
			} else {
				//logger.warning("date not found on : " + page.getPath());
			}
		} catch (Exception e) {
			//logger.warning(e.getMessage());
		}
		result.add(rst);
	}

	private void init(ContentContext ctx) throws Exception {
		if (index != null) {
			return;
		}

		analyzer = new StandardAnalyzer();
		index = new RAMDirectory();

		//TODO index by language
		//TODO move index to site scope (not session scope)

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement nav = content.getNavigation(ctx);

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
		IndexWriter w = null;
		try {
			w = new IndexWriter(index, config);
			indexPage(nav, ctx, w);
		} finally {
			ResourceHelper.safeClose(w);
		}
	}

	private void indexPage(MenuElement page, ContentContext ctx, IndexWriter w) throws Exception {

		if (!page.notInSearch(ctx)) {
			ContentContext ctxWithContent = ctx.getContextWithContent(page);
			if (ctxWithContent != null) {
				Document doc = new Document();
				doc.add(new StoredField(ID_FIELD, page.getId()));
				doc.add(new StoredField(TYPE_FIELD, PAGE_TYPE));

				//Index page groups? page.getGroupID(ctx)

				ContentElementList elemList = page.getLocalContentCopy(ctxWithContent);
				for (IContentVisualComponent cpt : elemList.getIterable(ctxWithContent)) {
					if (cpt.getSearchLevel() > 0) {
						doc.add(new TextField(LEVEL_FIELD_PREFIX + cpt.getSearchLevel(), cpt.getTextForSearch(), Field.Store.NO));
						//Automatic boost? (currently done via StaticConfig)
						//field.setBoost(1F + ((float) cpt.getSearchLevel()) / 10F);
					}
				}

				w.addDocument(doc);
			}
		}
		Collection<MenuElement> children = page.getChildMenuElements();
		for (MenuElement element : children) {
			indexPage(element, ctx, w);
		}
	}

}
