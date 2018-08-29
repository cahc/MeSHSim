package Parser;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.nustaq.serialization.FSTConfiguration;

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

    private ParsedPubMedDoc bytesToRecord(byte[] bytes) {

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

    public static void main(String arg[]) {

        Persist persist = new Persist("pubmedTest.db");

        System.out.println("Records in db:" + persist.dbSize() );


       for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet()) {

           System.out.println(persist.bytesToRecord( entry.getValue() ).getInternalID() );

       }



persist.close();

    }
}
