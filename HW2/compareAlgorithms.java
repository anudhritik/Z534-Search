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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {

	public static void main(String[] args) throws ParseException, IOException {
		Path path = Paths
				.get("C:/Users/AnudhritiReddy/Desktop/Z 534/topics.51-100");
		PrintWriter shortquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/defaultShortQuery.txt",
				"ASCII");
		PrintWriter longquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/defaultLongQuery.txt",
				"ASCII");
		PrintWriter BM25shortquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/BM25ShortQuery.txt",
				"ASCII");
		PrintWriter BM25longquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/BM25LongQuery.txt",
				"ASCII");
		PrintWriter dirishortquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/diriShortQuery.txt",
				"ASCII");
		PrintWriter dirilongquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/diriLongQuery.txt",
				"ASCII");
		PrintWriter dirilambdashortquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/dirilambdaShortQuery.txt",
				"ASCII");
		PrintWriter dirilambdalongquery = new PrintWriter(
				"C:/Users/AnudhritiReddy/Desktop/Z 534/dirilambdaLongQuery.txt",
				"ASCII");
		String fileData = new String(Files.readAllBytes(path));
		String[] topics = StringUtils.substringsBetween(fileData, "<top>",
				"</top>");

		Similarity defaultSimilarity = new DefaultSimilarity();
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");

			String title = (StringUtils.substringBetween(data, "Topic:", "<"))
					.trim().replaceAll("[/,?,(,)]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(title, number, map1, shortquery, "run-Title",
					defaultSimilarity);
		}

		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");
			String description = (StringUtils.substringBetween(data,
					"Description:", "<").trim()).replaceAll("[/,?]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(description, number, map1, longquery,
					"run-Description", defaultSimilarity);
		}
		Similarity BM25 = new BM25Similarity();
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");

			String title = (StringUtils.substringBetween(data, "Topic:", "<"))
					.trim().replaceAll("[/,?,(,)]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(title, number, map1, BM25shortquery, "BM25-Title",
					BM25);
		}
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");
			String description = (StringUtils.substringBetween(data,
					"Description:", "<").trim()).replaceAll("[/,?]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(description, number, map1, BM25longquery,
					"BM25-Description", BM25);
		}
		Similarity dirichiletSimilarity = new LMDirichletSimilarity();
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");

			String title = (StringUtils.substringBetween(data, "Topic:", "<"))
					.trim().replaceAll("[/,?,(,)]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(title, number, map1, dirishortquery,
					"dirichilet-Title", dirichiletSimilarity);
		}

		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");
			String description = (StringUtils.substringBetween(data,
					"Description:", "<").trim()).replaceAll("[/,?]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(description, number, map1, dirilongquery,
					"dirichilet-Description", dirichiletSimilarity);
		}
		Similarity dirichiletlambdaSimilarity = new LMDirichletSimilarity(
				(float) 0.7);
		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");

			String title = (StringUtils.substringBetween(data, "Topic:", "<"))
					.trim().replaceAll("[/,?,(,)]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(title, number, map1, dirilambdashortquery,
					"dirichiletlambda-Title", dirichiletlambdaSimilarity);
		}

		for (String data : topics) {
			String number = (StringUtils.substringBetween(data, "Number:", "<"))
					.trim().replaceFirst("0", "");
			String description = (StringUtils.substringBetween(data,
					"Description:", "<").trim()).replaceAll("[/,?]", " ");
			Map<String, Float> map1 = new HashMap<String, Float>();
			relevanceScore(description, number, map1, dirilambdalongquery,
					"dirichiletlambda-Description", dirichiletlambdaSimilarity);
		}

		shortquery.close();
		longquery.close();
		BM25shortquery.close();
		BM25longquery.close();
		dirishortquery.close();
		dirilongquery.close();
		dirilambdashortquery.close();
		dirilambdalongquery.close();

	}

	public static void relevanceScore(String docQuery, String num,
			Map<String, Float> map1, PrintWriter in, String run,
			Similarity similarity) throws IOException, ParseException {

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("C:/Users/AnudhritiReddy/Desktop/Z 534/indexgenerated")));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(similarity);
		Analyzer analyzer = new StandardAnalyzer();

		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(docQuery);
		TopDocs results = searcher.search(query, 1000);
		int numTotalHits = results.totalHits;
		ScoreDoc[] hits = results.scoreDocs;
		for (int j = 0; j < hits.length; j++) {
			Document doc = searcher.doc(hits[j].doc);
			String key = doc.get("DOCNO");
			if (map1.containsKey(key)) {
				map1.put(key, map1.get(key) + hits[j].score);

			} else {
				map1.put(key, hits[j].score);
			}
		}
		List<Entry<String, Float>> sortedMap = entriesSortedByValues(map1); 
		for (int k = 0, count = 1; k < sortedMap.size() && count <= 1000; k++, count++) { 

			in.println(Integer.valueOf(num.trim()) + " \t\t 0 \t "
					+ StringUtils.substringBetween(sortedMap.get(k).getKey(),"[","]") + " \t " + count + " \t "
					+ sortedMap.get(k).getValue() + " \t " + run);
		}

		reader.close();
	}

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(
				map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	
}
