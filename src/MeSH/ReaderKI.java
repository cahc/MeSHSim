package MeSH;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.*;
import java.util.*;

public class ReaderKI {


    HashMap<String,List<String>> articleIdToMeshIds = new HashMap<>();
    CheckTags checkTags = new CheckTags();
    BufferedReader reader;
    boolean ignoreCHeckTags;
    boolean onlyMajor;
    public ReaderKI(File file, boolean removeCheckTags, boolean onlyMajor) throws FileNotFoundException {

        //only major considers mesh terms as major *also* if only the mesh terms qualifier is tagged as major. Otherwise some documents might
        //not have any major terms
        this.reader = new BufferedReader( new FileReader(file) );
        this.ignoreCHeckTags = removeCheckTags;
        this.onlyMajor = onlyMajor;
    }


    public HashMap<String,List<String>> parse() throws IOException {
        String line;
        boolean start = true;

        while( (line =reader.readLine()) != null) {

            if(start) {

               boolean check = line.startsWith("ut|pmid|mesh_heading|mesh_qualifier|mesh_id|qualifier_id|ismajor_d|ismajor_q");

               if(!check) {System.out.println("Wrong input!"); System.exit(0);}

               start = false;
               continue;
            }


            String[] splitted = line.split("\\|");
            String ut = splitted[0];
            String meshID = splitted[4];

            if(this.ignoreCHeckTags) {

                if( this.checkTags.isAcheckTag(meshID) ) continue;


            }

            if(this.onlyMajor) {

                boolean majorTemr = splitted[6].equals("no");
                boolean majorQualifyer = splitted[7].equals("no");

                if(majorTemr && majorQualifyer) continue;


            }



            List<String> meshIDs = articleIdToMeshIds.get(ut);
            if(meshIDs == null) {

                meshIDs = new ArrayList<>();
                meshIDs.add(meshID.trim());
                articleIdToMeshIds.put(ut,meshIDs);

            } else {

                meshIDs.add(meshID);
            }



        }


        return articleIdToMeshIds;

    }


    public static void main(String[] arg) throws IOException {

        ReaderKI readerKI = new ReaderKI(new File("/Users/Cristian/Desktop/MesH/mesh_by_ut.csv"),true,false);
        HashMap utToMeSHList = readerKI.parse();
        System.out.println("mappings " + utToMeSHList.size());

        Object2IntOpenHashMap<String> wordCount =  new Object2IntOpenHashMap();

        Iterator<Map.Entry<String,List<String>>> entries = utToMeSHList.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<String, List<String>> pair = entries.next();

            String key = pair.getKey();
            List<String> value = pair.getValue();

            for(String s : value) wordCount.addTo(s,1);

        }


        Iterator<Object2IntMap.Entry<String>> freq = wordCount.object2IntEntrySet().fastIterator();
        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("/Users/Cristian/Desktop/MesH/freq.txt")));
        while(freq.hasNext()) {

            Object2IntMap.Entry<String> f = freq.next();

            writer.write(f.getKey() +" " +f.getIntValue());
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }

}
