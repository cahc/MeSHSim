package Parser;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 9/3/2018.
 */
public class IC {

    //information value per mesh descriptor based on occurences for 2009

    public static void main(String[] arg) throws IOException {

        Persist persist = new Persist("pubmed2009.db");

        Object2IntOpenHashMap<String> freqName = new Object2IntOpenHashMap<String>();
        Object2IntOpenHashMap<String> freqUI = new Object2IntOpenHashMap<String>();


        System.out.println("Records in db:" + persist.dbSize() );


        for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet()) {

           ParsedPubMedDoc doc =  persist.bytesToRecord(entry.getValue());

           List<ParsedMeSHDescriptor> parsedMeSHDescriptorList = doc.getMesh();

           HashSet<String> uniqueNames = new HashSet<>();
           HashSet<String> uniqueUI = new HashSet<>();
           for(ParsedMeSHDescriptor descriptor : parsedMeSHDescriptorList) {


              String UI = descriptor.getUI();
              String name = descriptor.getDescriptorName();

              uniqueNames.add(name);
              uniqueUI.add(UI);

           }

           for(String name : uniqueNames) freqName.addTo(name,1);
           for(String ui : uniqueUI) freqUI.addTo(ui, 1);

        }

        persist.close();


        System.out.println("# names: " + freqName.size());
        System.out.println("# ui: " + freqName.size());

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("DescriptorFreqIn2009.txt")));
        BufferedWriter writer2 = new BufferedWriter( new FileWriter( new File("UIFreqIn2009.txt")));

        for(Object2IntMap.Entry<String> entry : freqName.object2IntEntrySet()) {

           writer.write( entry.getKey() + "\t" + entry.getIntValue() );
           writer.newLine();
        }


        for(Object2IntMap.Entry<String> entry : freqUI.object2IntEntrySet()) {

            writer2.write( entry.getKey() + "\t" + entry.getIntValue() );
            writer2.newLine();

        }


        writer.flush();
        writer.close();
        writer2.flush();
        writer2.close();
    }

}
