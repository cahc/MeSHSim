import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class MeshIDtoFreq {


    private HashMap<String,Integer> idToFrequence = new HashMap<>();
    private BufferedReader reader;
    private int totalOccurrence;

    public MeshIDtoFreq(File file) throws FileNotFoundException {

        reader = new BufferedReader( new FileReader( file));

    }




    public void parse() throws IOException {

        String line;
        boolean firstRow = true;
        while( (line = reader.readLine()) != null) {

            if(firstRow) {firstRow = false; continue;}

            String[] parts = line.split("\\|");
            int occurrences = Integer.valueOf(parts[2]);
            idToFrequence.put(parts[0],occurrences );

            totalOccurrence += occurrences;

        }
        reader.close();

    }

    public int getTotalOccurrences() {

        return totalOccurrence;
    }

    public Set getAllMeshIds() {

       return Collections.unmodifiableSet(this.idToFrequence.keySet());

    }

    public void add(String meshId, Integer freq) {

        this.idToFrequence.put(meshId,freq);

    }
    public Integer getFrequency(String meshID) {

        Integer freq = this.idToFrequence.get(meshID);
        return (freq != null) ? freq : 0;

    }

    public int size() {

        return this.idToFrequence.size();
    }

}
