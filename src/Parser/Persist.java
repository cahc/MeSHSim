package Parser;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.nustaq.serialization.FSTConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class Persist {

    private static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    private final MVStore store;
    private MVMap<Integer,byte[]> map;



    public Persist(String databaseFile) {

        this.store = new MVStore.Builder().cacheSize(256).autoCommitBufferSize(2048).fileName(databaseFile).open();
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

        Persist persist = new Persist("pubmed2009.db");

        System.out.println("Records in db:" + persist.dbSize() );

        BufferedWriter writer = new BufferedWriter(new FileWriter("pubmed2009Text.txt"));

       for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet()) {

          // System.out.println(persist.bytesToRecord( entry.getValue() ).getInternalID() );

           writer.write(persist.bytesToRecord( entry.getValue() ).toString());
           writer.newLine();
       }

       writer.flush();
       writer.close();
        persist.close();

    }
}
