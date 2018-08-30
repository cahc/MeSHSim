package Parser;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class LuceneIndexer {


    public static void main(String[] arg) throws IOException {

        StandardAnalyzer analyzer = new StandardAnalyzer(); //TODO customize

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //config.setSimilarity( new ClassicSimilarity()); //TODO OBS!!

        Directory luceneIndex = FSDirectory.open(Paths.get("luceneIndex" ) );

        IndexWriter writer = new IndexWriter(luceneIndex,config);

        Persist persist = new Persist("pubmed2009.db");
        System.out.println("Records in db:" + persist.dbSize() );


        for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet()) {

           ParsedPubMedDoc parsedPubMedDoc = persist.bytesToRecord( entry.getValue() );

            Document doc = new Document();


            doc.add(new StoredField("internalID", parsedPubMedDoc.getInternalID()  ) ); //stored not analysed
            doc.add(new TextField("title", parsedPubMedDoc.getTitle() , Field.Store.YES ));
            doc.add(new TextField("abstract", parsedPubMedDoc.getAbstractText().toString(), Field.Store.NO ));
            doc.add(new TextField("journal", parsedPubMedDoc.getJournal(), Field.Store.NO));


            writer.addDocument(doc);

        }




        persist.close();
        writer.flush();
        writer.commit();
        System.out.println("number of docs in lucene index: " + writer.numDocs() );
        writer.close();


    }
}
