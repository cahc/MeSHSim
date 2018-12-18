package Parser;

import MeSH.CheckTags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jsat.linear.SparseVector;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class NeesSubset {

    int numberOfDescriptors;
    int numberOfQualifiers;
    public int totalVectorLength;


    private Object2DoubleMap<String> descriptorToIC = new Object2DoubleOpenHashMap<>();
    private Object2IntMap<String> descriptorToIndex = new Object2IntOpenHashMap<>();
    private Object2IntMap<String> qualifierToIndex = new Object2IntOpenHashMap<>();

    public int getDescriptorIndex(String UI) {

        return this.descriptorToIndex.getInt(UI);
    }


    public int getQualifierIndex(String UI) {


        return this.qualifierToIndex.getInt(UI);


    }

    public double getICforDescriptor(String UI) {

        return this.descriptorToIC.getDouble(UI);
    }



    public int mapDescriptorIndexToVectorIndex(int descriptorIndex) {



        return ((this.numberOfQualifiers+1)*descriptorIndex - numberOfQualifiers);  // assuming indices are ONE-based!!

    }

    public int mapQualifierIndexToVectorIndex(int descriptorIndex, int qualifierIndex) {


        return ( (this.numberOfQualifiers+1)*descriptorIndex - numberOfQualifiers)+qualifierIndex;  //assuming indices are ONE-based!!

    }


    public NeesSubset(File ICperDescriptor, File descriptorUItoIndex, File qualifierUItoIndex) {

        if( !ICperDescriptor.exists() || !descriptorUItoIndex.exists() || !qualifierUItoIndex.exists() ) {

            System.out.println("Missing input files"); System.exit(0);
        }



        try (BufferedReader in = new BufferedReader(new FileReader(ICperDescriptor)) ) {
            String line;

            while ((line = in.readLine()) != null) {

                String[] parts = line.split("\t");
                if(parts.length != 5) throw new IOException("IC file not separated correctly");
                this.descriptorToIC.put(parts[0], (double)Double.valueOf(parts[4]));

            }
        } catch (IOException e) {

            System.out.println("error in reading IC file");
            System.exit(1);
        }


        System.out.println("Descriptor to IC mappings: " + this.descriptorToIC.size());



        try (BufferedReader in = new BufferedReader(new FileReader(descriptorUItoIndex)) ) {
            String line;

            while ((line = in.readLine()) != null) {

                String[] parts = line.split("\t");
                if(parts.length != 2) throw new IOException("descriptor index file not separated correctly");
                descriptorToIndex.put(parts[0], (int)Integer.valueOf(parts[1]));

            }
        } catch (IOException e) {

            System.out.println("error in reading descriptor index file");
            System.exit(1);
        }


        System.out.println("Descriptor to index mappings: " + descriptorToIndex.size());


        try (BufferedReader in = new BufferedReader(new FileReader(qualifierUItoIndex)) ) {
            String line;

            while ((line = in.readLine()) != null) {

                String[] parts = line.split("\t");
                if(parts.length != 2) throw new IOException("qualifyer index file not separated correctly");
                this.qualifierToIndex.put(parts[0], (int)Integer.valueOf(parts[1]));

            }
        } catch (IOException e) {

            System.out.println("error in reading qualifyer index file");
            System.exit(1);
        }


        System.out.println("Qualifier to index mappings: " + this.qualifierToIndex.size());

        //total vector length #descriptors + ( #descriptors * #qualifiers)
        this.totalVectorLength = descriptorToIC.size() + (descriptorToIC.size()*qualifierToIndex.size());
        System.out.println("Sparse vector length: " + totalVectorLength ) ;


        this.numberOfDescriptors = descriptorToIC.size();
        this.numberOfQualifiers = this.qualifierToIndex.size();

    }






    public static void main(String[] arg) throws IOException {


        NeesSubset neesSubset = new NeesSubset(new File("ICperDescriptor.txt"), new File("DescriptorUItoIndex.txt"), new File("QualifiersUItoIndex.txt"));

        BufferedReader neesReader = new BufferedReader( new FileReader( "pub_from_Nees.txt" ));

        String line;

        int lineCount = 0;
        int docCount = 0;

        LinkedHashSet<Integer> PMIDs = new LinkedHashSet<>(); //keep insertion order

        while(  (line = neesReader.readLine()) != null) {

            if(lineCount < 2) {lineCount++; continue;}

            String[] parts = line.split("\t");
            docCount++;

            PMIDs.add( Integer.valueOf( parts[2] ) );

        }

        System.out.println("# : " + docCount);
        System.out.println("# : " + PMIDs.size());

        neesReader.close();


        /*TEMPORARY

        Persist persist = new Persist("medline2013-2017.db");

        Persist persistSubset = new Persist("medline2013-2017_NeesSubset.db");

        for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet() ) {

            ParsedPubMedDoc record = persist.bytesToRecord(entry.getValue());

            if( PMIDs.contains(Integer.valueOf(record.pmid ) )) {

                persistSubset.saveRecord(Integer.valueOf(record.pmid ), record  );
            }

        }

        persist.close();
        persistSubset.forceCommit();

        System.out.println("# saved to new db: " + persistSubset.dbSize());

        persistSubset.close();
        */

        Persist persistSubset = new Persist("medline2013-2017_NeesSubset.db");
        CheckTags checkTags = new CheckTags();
        List<SparseVector> sparseVectorList = new ArrayList<>();
        //insertion order so we match Nees pub_ib
        for(Integer pmid : PMIDs) {

        ParsedPubMedDoc record = persistSubset.retrieveRecord(pmid);

            if( record.getMesh().size() == 0) System.out.println("Warning " + record.pmid + " is not indexed with MeSH!!");

            List<ParsedMeSHDescriptor> meSHDescriptorList = record.getMesh();
            SparseVector sparseVector = new SparseVector(neesSubset.totalVectorLength,20);



            for(ParsedMeSHDescriptor parsedMeSHDescriptor : meSHDescriptorList) {

                if( checkTags.isAcheckTag(parsedMeSHDescriptor.getUI() ) ) continue; //ignore check tags

                int descriptorIndexOneBased = neesSubset.getDescriptorIndex( parsedMeSHDescriptor.getUI() );
                if(descriptorIndexOneBased == 0) {System.out.println("Catastrophic error!, descriptor index == 0 should not happen"); persistSubset.close(); System.exit(0); }
                boolean isMajor = parsedMeSHDescriptor.isMajor(); // check later if any of the qualifiers are major, the discriptor inherent the major categorization if so
                double IC = neesSubset.getICforDescriptor(parsedMeSHDescriptor.getUI());
                if(IC == 0.0)   {System.out.println("Catastrophic error!, IC value == 0 should not happen"); persistSubset.close(); System.exit(0); }

                IntList qualifierIndices = new IntArrayList(5);
                for(ParsedMeSHQualifier qualifier : parsedMeSHDescriptor.getQualifiers()) {

                    int qualifierIndexOneBased = neesSubset.getQualifierIndex( qualifier.getUI() );
                    if(qualifierIndexOneBased == 0) {System.out.println("Catastrophic error!, qualifier index == 0 should not happen"); persistSubset.close(); System.exit(0); }
                    if(qualifier.isMajor()) isMajor = true;
                    qualifierIndices.add(qualifierIndexOneBased);

                }

                if(isMajor) IC = IC*2; //BOOST

                int descriptorVectorIndexZeroBased = neesSubset.mapDescriptorIndexToVectorIndex( descriptorIndexOneBased )-1;
                sparseVector.set(descriptorVectorIndexZeroBased,IC);

                for(int qualifierIndexOneBased : qualifierIndices) {

                    int qualifierVectorIndexZeroBased = neesSubset.mapQualifierIndexToVectorIndex(descriptorIndexOneBased,qualifierIndexOneBased)-1;
                    sparseVector.set(qualifierVectorIndexZeroBased,1.0);
                }



            }


            sparseVectorList.add(sparseVector);



        }

        persistSubset.close();
       System.out.println("Vectors created: " + sparseVectorList.size());

    }







}
