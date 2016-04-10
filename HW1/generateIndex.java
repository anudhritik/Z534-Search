
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.commons.lang3.StringUtils;

public class generateIndex {
	public static void main(String[] args) throws IOException,NoSuchFileException,ParseException,CorruptIndexException {
		String docDir = "C:/Users/AnudhritiReddy/Desktop/Z 534/corpus";
		String indexDir = "C:/Users/AnudhritiReddy/Desktop/Z 534/indexgenerated";
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = new IndexWriter(dir, iwc);
		//File corpusDirectory = new File("C:/Users/AnudhritiReddy/Desktop/Z 534/corpus");
		List<String> fileNames = new ArrayList<String>();
		File[] files = new File("C:/Users/AnudhritiReddy/Desktop/Z 534/corpus").listFiles();
		for (File file : files) {
			if (file.isFile()) {
				fileNames.add(file.getAbsolutePath());
			}
		}
		if (fileNames.size() == 0) {	
			System.out.println("The directory is empty");
		} else {
			
			System.out.println("Parsing the files. This may take time.");
			
			for (String trectTextFile : fileNames) {
				try {
					String parseFileContent = new String(Files.readAllBytes(Paths.get(trectTextFile)));
					String[] document = StringUtils.substringsBetween(parseFileContent,"<DOC>", "</DOC>");

					for (String str : document) {
						Document lucenedoc = new Document();
						String[] documentNumber = StringUtils.substringsBetween(str,"<DOCNO>", "</DOCNO>");
						String[] text = StringUtils.substringsBetween(str,"<TEXT>", "</TEXT>");
						String[] line = StringUtils.substringsBetween(str,"<BYLINE>", "</BYLINE>");
						String[] date = StringUtils.substringsBetween(str,"<DATELINE>", "</DATELINE>");
						String[] header = StringUtils.substringsBetween(str,"<HEAD>", "</HEAD>");
						String byline = Arrays.toString(line);
						String head = Arrays.toString(header);
						String docno = Arrays.toString(documentNumber);
						String textData = Arrays.toString(text);
						String dateline = Arrays.toString(date);
						lucenedoc.add(new StringField("DOCNO", docno,Field.Store.YES));
						lucenedoc.add(new TextField("TEXT", textData,Field.Store.YES));
						lucenedoc.add(new TextField("BYLINE", byline,Field.Store.YES));
						lucenedoc.add(new TextField("DATELINE", dateline,Field.Store.YES));
						lucenedoc.add(new TextField("HEAD", head,Field.Store.YES));
						writer.addDocument(lucenedoc);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Could not add: " + trectTextFile);
				}
			}
		}
		writer.forceMerge(1);
		writer.commit();
		writer.close();

		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexDir)));
		
		System.out.println("Total number of documents in the corpus: "
				+ reader.maxDoc());
		System.out
		.println("Number of documents containing the term \"new\" for field \"TEXT\": "
				+ reader.docFreq(new Term("TEXT", "new")));
		System.out
		.println("Number of occurrences of \"new\" in the field\"TEXT\": "
				+ reader.totalTermFreq(new Term("TEXT", "new")));

		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");		
		System.out.println("Size of the vocabulary for this field: "
				+ vocabulary.size());

		System.out
				.println("Number of documents that have at least one term for this field: "
						+ vocabulary.getDocCount());

		System.out.println("Number of tokens for this field: "
				+ vocabulary.getSumTotalTermFreq());

		System.out.println("Number of postings for this field: "
				+ vocabulary.getSumDocFreq());

		/*TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		System.out.println("\n*******Vocabulary-Start**********");
		while ((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();
			System.out.println(term);
		}
		System.out.println("\n*******Vocabulary-End**********);*/

		reader.close();
	}

}

