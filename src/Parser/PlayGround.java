package Parser;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by crco0001 on 9/27/2018.
 */
public class PlayGround {


    public static void main(String[] arg) throws IOException {

        String bibcapAuthor = "UNKNOWN colliander, b";


        StandardAnalyzer standardAnalyzer = new StandardAnalyzer();

        TokenStream stream = LuceneSearcher.standardAnalyzer.tokenStream(null, new StringReader(bibcapAuthor));

        CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
        stream.reset(); //??

       // BooleanQuery.Builder bq = new BooleanQuery.Builder();


        while (stream.incrementToken()) {

            String term = cattr.toString();

           if(term.length() > 1) System.out.println(term);

         //   bq.add(new TermQuery(new Term("title", term)), BooleanClause.Occur.SHOULD);


        }

        stream.end();
        stream.close();



    }
}
