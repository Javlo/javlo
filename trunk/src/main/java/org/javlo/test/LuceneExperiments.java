package org.javlo.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneExperiments {

	public static void main(String[] args) throws Exception {
		LuceneExperiments m = new LuceneExperiments();
		m.start("short définition");
		m.start("short^2 définition");
	}

	private void start(String searchStrIn) throws Exception {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		RAMDirectory index = new RAMDirectory();

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);//TODO try finally to close?
		IndexWriter w = new IndexWriter(index, config);

		addDoc(w, "id1", "namenamename",
				arr("admin", "public", "contrib"),
				"tiltetitle",
				"description description",
				arr(
						arro("content a", 1F),
						arro("ta mère en short", 1F),
						arro("va voir ailleurs si j'y suis", 1F),
						arro("est-ce que je mets un short aujourd'hui", 1F)
				));

		addDoc(w, "id2", "my-name-is",
				arr("public"),
				"This is my title",
				"My description is the description of the year",
				arr(
						arro("content b", 1F),
						arro("J'aime mettre des shorts", 1F),
						arro("Des shorts en veux-tu en voilà.", 1F)
				));

		addDoc(w, "id3", "short",
				arr("public", "admin"),
				"La page du short",
				"Description complète du short",
				arr(
						arro("content c", 1F),
						arro("Les shorts de par leurs définition sont court.", 1F),
						arro("Il existe des shorts mi-long", 1F),
						arro("Un short, c'est la vie!!", 1F)
				));
		addDoc(w, "id4", "short",
				arr("public", "admin"),
				"La page du short",
				"Description complète du short",
				arr(
						arro("content c", 1.1F),
						arro("Les shorts de par leurs définition sont court.", 1.1F),
						arro("Il existe des shorts mi-long", 1.1F),
						arro("Un short, c'est la vie!!", 1.1F)
				));

		w.close();

		//String searchStr = "short~ bonjour AND (short* OR content:sh*ort*)";
		//String searchStr = "short bonjour aujourd'hui l'appel";
		//String searchStr = "short^2";
		String searchStr = searchStrIn;

		Query q = parseQuery(searchStr);
		//Query q = new MultiFieldQueryParser(arr("title", "description", "content"), analyzer).parse(searchStr);
		//Query q = new QueryParser("content", analyzer).parse(searchStr);
		//Query q = new AnalyzingQueryParser("title", analyzer).parse(searchStr;

		System.out.println("=====Q:" + searchStr + "============================");
		printQuery(q);

		int hitsPerPage = 10;
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		TopDocs td = collector.topDocs();

		System.out.println("=================================");
		System.out.println("Found: " + td.totalHits);
		System.out.println("Docs: " + td.scoreDocs.length);

		String[] fields = arr("_id", "name", "group", "title", "description", "content");

		int i = 0;
		for (ScoreDoc scoreDoc : td.scoreDocs) {
			System.out.println("-----------");
			System.out.println("Result " + i + ": " + scoreDoc.score);
			Document doc = searcher.doc(scoreDoc.doc);
			for (String field : fields) {
				System.out.println("Field " + field + ": " + new LinkedList<String>(Arrays.asList(doc.getValues(field))));
			}
			i++;
		}

		System.out.println();
		System.out.println();
		System.out.println();
	}
	private Query parseQuery(String searchStr) {

		return null;
	}

	private void printQuery(Query q) {
		printQuery(q, "");
	}
	private void printQuery(Query q, String indent) {
		if (q instanceof BooleanQuery) {
			BooleanQuery qq = (BooleanQuery) q;
			System.out.println(indent + "Boolean" + " (Boost: " + qq.getBoost() + ")");
			for (BooleanClause cl : qq) {
				System.out.println(indent + "  " + /*"isProhibited=" + cl.isProhibited() + " isRequired=" + cl.isRequired() +*/" occur=" + cl.getOccur().name());
				printQuery(cl.getQuery(), indent + "\t");
			}
		} else if (q instanceof FuzzyQuery) {
			FuzzyQuery qq = (FuzzyQuery) q;
			System.out.println(indent + "Fuzzy: " + qq.getTerm() + " (Boost: " + qq.getBoost() + ")");
		} else if (q instanceof PrefixQuery) {
			PrefixQuery qq = (PrefixQuery) q;
			System.out.println(indent + "Prefix: " + qq.getPrefix() + " (Boost: " + qq.getBoost() + ")");
		} else if (q instanceof WildcardQuery) {
			WildcardQuery qq = (WildcardQuery) q;
			System.out.println(indent + "Wildcard: " + qq.getTerm() + " (Boost: " + qq.getBoost() + ")");
		} else if (q instanceof TermQuery) {
			TermQuery qq = (TermQuery) q;
			System.out.println(indent + "Term: " + qq.getTerm() + " (Boost: " + qq.getBoost() + ")");
		} else {
			System.out.println(indent + q.getClass());
		}
	}

	private <T> T[] arr(T... items) {
		return items;
	}

	private Object[] arro(Object... items) {
		return items;
	}

	private void addDoc(IndexWriter w, String id, String name, String[] groups, String title, String drescription, Object[][] components) throws IOException {
		Document doc = new Document();
		doc.add(new StoredField("_id", id));
		doc.add(new StoredField("name", name));
		for (String group : groups) {
			doc.add(new StringField("group", group, Field.Store.YES));
		}

		doc.add(new TextField("title", title, Field.Store.YES));
		doc.add(new TextField("description", drescription, Field.Store.YES));

		for (Object[] component : components) {
			TextField fld = new TextField("content", (String) component[0], Field.Store.YES);
			fld.setBoost((Float) component[1]);
			doc.add(fld);
		}

		w.addDocument(doc);
	}

}
