import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.commons.lang3.StringUtils;

public class indexComparison {

	public static void main(String[] args) throws IOException,
			NoSuchFileException {

		try {
			indexComparison ic = new indexComparison();
			ic.compareDifferentIndex();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void compareDifferentIndex() throws IOException {
		Analyzer stdAn = new StandardAnalyzer();
		System.out.println("STANDARD ANALYZER");
		indexing(stdAn);

		Analyzer simpleAn = new SimpleAnalyzer();
		System.out.println("SIMPLE ANALYZER");
		indexing(simpleAn);

		Analyzer stopAn = new StopAnalyzer();
		System.out.println("STOP ANALYZER");
		indexing(stopAn);

		Analyzer keyAn = new KeywordAnalyzer();
		System.out.println("KEYWORD ANALYZER");
		indexing(keyAn);
	}

	private void indexing(Analyzer analyzer) throws IOException {

		String docDir = "C:/Users/AnudhritiReddy/Desktop/Z 534/corpus";
		String indexDir = "C:/Users/AnudhritiReddy/Desktop/Z 534/indexgenerated";
		IndexWriterConfig iwConf = new IndexWriterConfig(analyzer);
		iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		Directory fsDir = FSDirectory.open(Paths.get(indexDir));
		IndexWriter indexWriter = new IndexWriter(fsDir, iwConf);
		ArrayList<String> fileNames = new ArrayList<String>();
		File[] files = new File(docDir).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				fileNames.add(file.getAbsolutePath());
			}
		}
		if (fileNames.size() == 0) {
			System.out.println("The directory is empty");
		} else {

			for (String trectTextFile : fileNames) {
				try {
					String parseFileContent = new String(
							Files.readAllBytes(Paths.get(trectTextFile)));
					String[] docs = StringUtils.substringsBetween(
							parseFileContent, "<DOC>", "</DOC>");

					for (String str : docs) {
						Document lucenedoc = new Document();

						String[] tex = StringUtils.substringsBetween(str,
								"<TEXT>", "</TEXT>");
						String text = Arrays.toString(tex);
						lucenedoc.add(new TextField("TEXT", text,
								Field.Store.YES));
						indexWriter.addDocument(lucenedoc);
					}

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Could not add: " + trectTextFile);
				}
			}
		}
		indexWriter.forceMerge(1);
		indexWriter.commit();
		indexWriter.close();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexDir)));
		System.out.println("Total number of documents in the corpus:"
				+ reader.maxDoc());
		System.out
				.println("Number of documents containing the term \"new\" for field \"TEXT\": "
						+ reader.docFreq(new Term("TEXT", "new")));
		System.out
				.println("Number of occurences of \"new\" in the field\"TEXT\": "
						+ reader.totalTermFreq(new Term("TEXT", "new")));
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
		System.out.println("Size of the vocabulary for this field:"
				+ vocabulary.size());
		System.out
				.println("Number of documents that have at least one term for this field: "
						+ vocabulary.getDocCount());
		System.out.println("Number of tokens for this field:"
				+ vocabulary.getSumTotalTermFreq());
		System.out.println("Number of postings for this field:"
				+ vocabulary.getSumDocFreq());
		/*TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		 * System.out.println("\n*******Vocabulary-Start**********"); while
		 * ((byteRef = iterator.next()) != null) { // String term =
		 * byteRef.utf8ToString(); // System.out.print(term+"\t"); }
		 * System.out.println("\n*******Vocabulary-End**********");
		 */
		reader.close();

	}

}
