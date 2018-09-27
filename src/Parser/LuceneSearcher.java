package Parser;

import BibCap.BibCapRecord;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.ObjectDataType;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

public class LuceneSearcher {


    static final StandardAnalyzer standardAnalyzer = new StandardAnalyzer();


    public static BooleanQuery createQueryFromBibCapRecord(BibCapRecord doc) throws IOException {

        BooleanQuery.setMaxClauseCount(3000);
        String title = doc.getTitle();

        TokenStream stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(title));

        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??

        BooleanQuery.Builder bq = new BooleanQuery.Builder();


        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq.add(new TermQuery(new Term("title", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();

        String abstractText = doc.getAbstractText();

        stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(abstractText));

        cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??



        BooleanQuery.Builder bq2 = new BooleanQuery.Builder();


        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq2.add(new TermQuery(new Term("abstract", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();


        String journal = doc.getSource();

        stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(journal));

        cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??

        BooleanQuery.Builder bq3 = new BooleanQuery.Builder();

        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq3.add(new TermQuery(new Term("journal", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();


        //if author information is known (last name) create such an MUST query, otherwise ignore

        BooleanQuery finalQuery = new BooleanQuery.Builder().add(bq.build(),BooleanClause.Occur.MUST).add(bq2.build(),BooleanClause.Occur.SHOULD).add(bq3.build(),BooleanClause.Occur.MUST).build();


        return finalQuery;

    }

    public static BooleanQuery createQueryFromParsedPubMedDoc(ParsedPubMedDoc doc) throws IOException {

        BooleanQuery.setMaxClauseCount(3000);
        String title = doc.getTitle();

        TokenStream stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(title));

        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??

        BooleanQuery.Builder bq = new BooleanQuery.Builder();


        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq.add(new TermQuery(new Term("title", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();

        String abstractText = doc.getAbstractText().toString();

        stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(abstractText));

        cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??



        BooleanQuery.Builder bq2 = new BooleanQuery.Builder();



        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq2.add(new TermQuery(new Term("abstract", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();


        String journal = doc.getJournal();

        stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(journal));

        cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??

        BooleanQuery.Builder bq3 = new BooleanQuery.Builder();

        while (stream.incrementToken()) {

            String term = cattr.toString();

            bq3.add(new TermQuery(new Term("journal", term)),BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();




       BooleanQuery finalQuery = new BooleanQuery.Builder().add(bq.build(),BooleanClause.Occur.MUST).add(bq2.build(),BooleanClause.Occur.SHOULD).add(bq3.build(),BooleanClause.Occur.MUST).build();


      return finalQuery;


    }

    public static void main(String[] arg) throws IOException, ParseException {

        //add jar referens to BibCapResearch to access BibCapRecord

        BufferedWriter writerMatchingResult = new BufferedWriter( new FileWriter( new File("matchingResult.txt")));
        String mappyFileName = "mappy.db";

        MVStore store = new MVStore.Builder().cacheSize(200). // 200MB read cache
                fileName( mappyFileName ).autoCommitBufferSize(1024). // 1MB write cache
                open(); // autoCommitBufferSize
        store.setVersionsToKeep(0);
        store.setReuseSpace(true);

        MVMap<Integer, BibCapRecord> bibCapRecordMap = store.openMap("mymap", new MVMap.Builder<Integer, BibCapRecord>().keyType(new ObjectDataType()).valueType(new BibCapRecord()));

        System.out.println("Bibcap records" + bibCapRecordMap.size());


        //setup lucene index
        Directory luceneIndex = FSDirectory.open(Paths.get("luceneIndex" ) );
       // StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexReader reader = DirectoryReader.open(luceneIndex);
        IndexSearcher searcher = new IndexSearcher(reader);
       // searcher.setSimilarity( new ClassicSimilarity()); // TODO OBS!!

        System.out.println("Docs in index: " + reader.numDocs());

        //Persist persist = new Persist("pubmedTest.db");

        //SELF SEARCH: Map.Entry<Integer,byte[]> entry : persist.getEntrySet()
        //ParsedPubMedDoc target = persist.bytesToRecord(entry.getValue());
        int matchedSoFar = 0;

        try {
            for (Map.Entry<Integer, BibCapRecord> entry : bibCapRecordMap.entrySet()) {

                BibCapRecord target = entry.getValue();

                BooleanQuery q = createQueryFromBibCapRecord(target);

                // System.out.println(q);


                TopDocs returnedDocs = searcher.search(q, 1); // max 20000 hits

                ScoreDoc[] hits = returnedDocs.scoreDocs;


                //   for (int j = 0; j < hits.length; j++) {
                //      int luceneDocId = hits[j].doc;
                //  System.out.println("score: " + hits[j].score);
                //  Document d = searcher.doc(luceneDocId);
                //  System.out.println(d.get("title"));

                //   }


                if (hits.length == 0) {
                    writerMatchingResult.write(target.getUT() + "\t" + -99);
                    writerMatchingResult.newLine();

                } else {

                    if (hits.length == 2) {

                        double bestScore = hits[0].score;
                        double nextBest = hits[1].score;

                        String searchTitle = misc.OSA.simplifyString(target.getTitle());

                        String bestTitle = misc.OSA.simplifyString(searcher.doc(hits[0].doc).get("title"));

                        String nextBestTitle = misc.OSA.simplifyString(searcher.doc(hits[1].doc).get("title"));

                        double simBestTitle = misc.OSA.DamuLevSim(searchTitle, bestTitle, 0.90);
                        double simNextBestTitle = misc.OSA.DamuLevSim(searchTitle, nextBestTitle, 0.9);

                        System.out.println("best score: " + bestScore + " simTitle: " + simBestTitle + " nextBestScore: " + nextBest + " simtitle: " + simNextBestTitle);

                    } else {

                        double bestScore = hits[0].score;

                        Document luceneDoc = searcher.doc(hits[0].doc);

                        String searchTitle = misc.OSA.simplifyString(target.getTitle());
                        String bestTitle = misc.OSA.simplifyString(luceneDoc.get("title"));
                        double simBestTitle = misc.OSA.DamuLevSim(searchTitle, bestTitle, 0.90);

                        writerMatchingResult.write(target.getUT() + "\t" + bestScore + "\t" + simBestTitle + "\t" + luceneDoc.get("internalID"));
                        writerMatchingResult.newLine();

                    }

                }


                matchedSoFar++;
                if (matchedSoFar % 1000 == 0) System.out.println("processed: " + matchedSoFar);

            }


            //persist.close();
            reader.close();
            store.close();
            luceneIndex.close();

            writerMatchingResult.flush();
            writerMatchingResult.close();

        } catch (Exception e) {

            System.out.println(e);
        } finally {

            store.close();
        }

    }
}
