import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class searchTRECTopics {
	public static void main(String[] args) throws IOException, ParseException {
		Path path = Paths
				.get("C:/Users/AnudhritiReddy/Desktop/Z 534/topics.51-100");
		PrintWriter shortquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/topicShortQuery.txt",
				"ASCII");
		PrintWriter longquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/topicLongQuery.txt",
				"ASCII");
		String fileData = new String(Files.readAllBytes(path));
		String[] topics = StringUtils.substringsBetween(fileData, "<top>",
				"</top>");
		
		System.out.println("Calculating relevance score for short query");
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");

			String title = (StringUtils.substringBetween(data, "Topic:", "<"))
					.trim().replaceAll("[/,?,(,)]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(title, number, map1, shortquery, "run-Title");
		}
		System.out.println("Relevance score for short query completed");
		System.out.println();
		
		System.out.println("Calculating relevance score for long query");
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data,
					"Number:", "<")).trim().replaceFirst("0", "");
			String description = (StringUtils.substringBetween(data,
					"Description:", "<").trim()).replaceAll("[/,?]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(description, number, map1, longquery,
					"run-Description");
		}
		
		System.out.println("Relevance score for long query completed");
		System.out.println("Search finished");
		shortquery.close();
		longquery.close();
	}

	public static void relevanceScore(String docQuery, String num,
			Map<String, Float> map1, PrintWriter in, String run)
			throws IOException, ParseException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("C:/Users/AnudhritiReddy/Desktop/Z 534/indexgenerated")));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(docQuery);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		DefaultSimilarity dSimi = new DefaultSimilarity();
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		float total = reader.maxDoc();
		for (Term t : queryTerms) {

			for (int i = 0; i < leafContexts.size(); i++) {
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = (leafContext.docBase);
				int doc;
				PostingsEnum de = MultiFields.getTermDocsEnum(
						leafContext.reader(), "TEXT", new BytesRef(t.text()));

				while ((de != null && (doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS)) {
					int docNumber = de.docID() + startDocNo;
					float normDocLeng = dSimi.decodeNormValue(leafContext
							.reader().getNormValues("TEXT").get(de.docID()));
					Document indexDoc = searcher.doc(docNumber);
					String newdocno = indexDoc.get("DOCNO");
					float count = de.freq();
					float length = normDocLeng;
					float TF = (count / length);
					float IDF = (float) Math
							.log10((1 + (reader.maxDoc() / reader
									.docFreq(new Term("TEXT", t.text())))));
					String key = ('!'+newdocno+'!').concat('@'+docQuery+'@').concat('#'+num+'#');

					if (map1.containsKey(key)) {
						map1.put(key, map1.get(key) + (TF * IDF));
					} else {
						map1.put(key, ((TF * IDF)));
					}
				}
			}
		}

		Map<String, Float> sorted = mapSortByValue(map1);

		int count = 0;
		for (Entry<String, Float> entry1 : sorted.entrySet()) {
			String docid = StringUtils.substringBetween(entry1.getKey(), "!","!");
			String document = StringUtils.substringBetween(docid, "[", "]").trim();
			String querynumber = StringUtils.substringBetween(entry1.getKey(),
					"#", "#");
			in.println(querynumber + " Q0 " +document + " " + (count++) + " "
					+ entry1.getValue() + " " + run);
			if (count == 1000) {
				break;
			}
		}
		reader.close();
	}

	public static Map<String, Float> mapSortByValue(Map<String, Float> RevDoc) {
		List<Entry<String, Float>> list = new LinkedList<Entry<String, Float>>(
				RevDoc.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Float>>() {
			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map<String, Float> mapValues = new LinkedHashMap<String, Float>();
		for (Entry<String, Float> entry : list) {
			mapValues.put(entry.getKey(), entry.getValue());
		}

		return mapValues;
	}

}