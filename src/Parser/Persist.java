package Parser;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.nustaq.serialization.FSTConfiguration;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Persist {

    private static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    private final MVStore store;
    private MVMap<Integer,byte[]> map;



    public Persist(String databaseFile) {

        this.store = new MVStore.Builder().cacheSize(256).autoCommitBufferSize(4096).fileName(databaseFile).open();
        this.store.setVersionsToKeep(0);
        this.store.setReuseSpace(true);
        this.map = store.openMap("records");

        this.conf.registerClass(ParsedPubMedDoc.class);

    }

    private byte[] recordToBytes(ParsedPubMedDoc d) {


        return this.conf.asByteArray(d);

    }

    public ParsedPubMedDoc bytesToRecord(byte[] bytes) {

        return (ParsedPubMedDoc)this.conf.asObject(bytes);

    }

    public void saveRecord(Integer id, ParsedPubMedDoc doc) {

       this.map.put(id,  recordToBytes(doc)  );
    }

    public ParsedPubMedDoc retrieveRecord(Integer id) {

       byte[] serializedRecord =  map.get(id);
       if(serializedRecord == null) return null;

       return bytesToRecord(serializedRecord);
    }

    public void removeRecord(Integer id) {

        map.remove(id);
    }

    public void forceCommit() {

        store.commit();
    }

    public void compact() {

        store.compactMoveChunks();
    }

    public void compactFully() {

        store.compactRewriteFully();

    }


    public Set<Map.Entry<Integer,byte[]>> getEntrySet() {

        return map.entrySet();

    }

    public int dbSize() {

        return map.size();
    }

    public void close() {

        store.close();
    }

    public static void main(String arg[]) throws IOException {

       // Persist persist = new Persist("medline2013-2017.db");
        Persist persist = new Persist("pubmed2009v3.db");

        System.out.println("Records in db:" + persist.dbSize() );

        //just to get the pubs that are in both pubmed 2009 and bibcap




        //BufferedWriter writer = new BufferedWriter(new FileWriter("pubmed2009Textv3.txt"));
        //BufferedWriter writer = new BufferedWriter(new FileWriter("RecordIDs.txt"));

        BufferedWriter writer = new BufferedWriter(new FileWriter("pmidToType.txt"));

       for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet()) {

          // System.out.println(persist.bytesToRecord( entry.getValue() ).getInternalID() );

           ParsedPubMedDoc record = persist.bytesToRecord( entry.getValue());


           writer.write(record.pmid +"\t" + record.publicationTypes);
           writer.newLine();

           //boolean hasMeSH = record.getMesh().size() > 0;

           //writer.write( record.getPmid() + "\t" + record.doi +"\t" + record.getJournal() +"\t" + record.pubyear +"\t" + record.publicationTypes +"\t" + hasMeSH );
           //writer.newLine();
           //writer.write(persist.bytesToRecord( entry.getValue() ).toString());
           //writer.newLine();
       }

       writer.flush();
       writer.close();
       persist.close();

    }
}
