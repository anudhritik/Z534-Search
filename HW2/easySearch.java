import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class easySearch {
	public static void main(String[] args) throws Exception {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("C:/Users/AnudhritiReddy/Desktop/Z 534/indexgenerated")));
		IndexSearcher searcher=new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		//Get the query terms from the query string
		String queryString="New York";
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		Map<Integer,Float> map1=new HashMap<Integer, Float>();
		DefaultSimilarity dSimi=new DefaultSimilarity();
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		
		for (int i = 0; i < leafContexts.size(); i++) {
			LeafReaderContext leafContext=leafContexts.get(i);
			int startDocNo=leafContext.docBase;
			int numberOfDoc=leafContext.reader().maxDoc();
		
		for(Term t : queryTerms){
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
					"TEXT", 
					new BytesRef(t.text()));
			int doc;
			while(de!= null &&(doc=de.nextDoc())!=PostingsEnum.NO_MORE_DOCS){
				int docNumber=de.docID()+startDocNo;
				float normLength=dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(doc));
				float TF=(de.freq()/normLength);
				int df=reader.docFreq(new Term("TEXT",t.text()));
				float IDF=(float)Math.log10(1+(reader.maxDoc()/df));
				float relevanceScore=TF*IDF;
				System.out.println("term " + t.text() + "occurs " +de.freq() +"time(s) in doc " + docNumber );
				if(map1.containsKey(docNumber))
				{map1.put(docNumber,map1.get(docNumber)+relevanceScore);}
				else{map1.put(docNumber, relevanceScore);}
				
			}
			
				
		}
	}
		
		for(Map.Entry<Integer, Float> entry: map1.entrySet()){
			System.out.println("Doc id :" +entry.getKey()+ "and value is" + entry.getValue());
		}
		
	}
}
