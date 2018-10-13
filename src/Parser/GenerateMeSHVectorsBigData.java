package Parser;

import MeSH.CheckTags;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jsat.linear.SparseVector;
import misc.Helpers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 10/11/2018.
 */
public class GenerateMeSHVectorsBigData {



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


    public GenerateMeSHVectorsBigData(File ICperDescriptor, File descriptorUItoIndex, File qualifierUItoIndex) {

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

        if(arg.length != 1) {System.out.println("supply pubmedDB"); System.exit(0); }

        CheckTags checkTags = new CheckTags();

        GenerateMeSHVectorsBigData generateMeSHVectorsBigData = new GenerateMeSHVectorsBigData(new File("ICperDescriptor.txt"), new File("DescriptorUItoIndex.txt"), new File("QualifiersUItoIndex.txt"));

        Persist persist = new Persist(arg[0]);

        System.out.println("Records in DB: " + persist.dbSize() );
        List<SparseVector> sparseVectorList = new ArrayList<>( persist.dbSize() );


        int notIndexedWithMeSH = 0;
        int counter = 1;
        BufferedWriter missingMeSH = new BufferedWriter( new FileWriter( new File("pmidNoMeSH.txt")));
        BufferedWriter pmidInOrder = new BufferedWriter(new FileWriter(new File("pmidsInOrder.txt")));

        System.out.println("Creating vectors..");
        for(Map.Entry<Integer,byte[]> entry : persist.getEntrySet() ) {

            ParsedPubMedDoc record = persist.bytesToRecord(entry.getValue());

            //ignore records without MeSH
           if( record.getMesh().size() == 0) { notIndexedWithMeSH++; missingMeSH.write(record.getPmid() + " " + record.getPubyear() + " " + record.getPublicationTypes()); missingMeSH.newLine(); continue;}

           List<ParsedMeSHDescriptor> meSHDescriptorList = record.getMesh();
           SparseVector sparseVector = new SparseVector(generateMeSHVectorsBigData.totalVectorLength,20);

           for(ParsedMeSHDescriptor parsedMeSHDescriptor : meSHDescriptorList) {

               if( checkTags.isAcheckTag(parsedMeSHDescriptor.getUI() ) ) continue; //ignore check tags

               int descriptorIndexOneBased = generateMeSHVectorsBigData.getDescriptorIndex( parsedMeSHDescriptor.getUI() );
               if(descriptorIndexOneBased == 0) {System.out.println("Catastrophic error!, descriptor index == 0 should not happen"); persist.close(); System.exit(0); }
               boolean isMajor = parsedMeSHDescriptor.isMajor(); // check later if any of the qualifiers are major, the discriptor inherent the major categorization if so
               double IC = generateMeSHVectorsBigData.getICforDescriptor(parsedMeSHDescriptor.getUI());
               if(IC == 0.0)   {System.out.println("Catastrophic error!, IC value == 0 should not happen"); persist.close(); System.exit(0); }

               IntList qualifierIndices = new IntArrayList(5);
               for(ParsedMeSHQualifier qualifier : parsedMeSHDescriptor.getQualifiers()) {

                   int qualifierIndexOneBased = generateMeSHVectorsBigData.getQualifierIndex( qualifier.getUI() );
                   if(qualifierIndexOneBased == 0) {System.out.println("Catastrophic error!, qualifier index == 0 should not happen"); persist.close(); System.exit(0); }
                   if(qualifier.isMajor()) isMajor = true;
                   qualifierIndices.add(qualifierIndexOneBased);

               }

                if(isMajor) IC = IC*2; //BOOST

               int descriptorVectorIndexZeroBased = generateMeSHVectorsBigData.mapDescriptorIndexToVectorIndex( descriptorIndexOneBased )-1;
               sparseVector.set(descriptorVectorIndexZeroBased,IC);

               for(int qualifierIndexOneBased : qualifierIndices) {

                   int qualifierVectorIndexZeroBased = generateMeSHVectorsBigData.mapQualifierIndexToVectorIndex(descriptorIndexOneBased,qualifierIndexOneBased)-1;
                   sparseVector.set(qualifierVectorIndexZeroBased,1.0);
               }



           } //for each descriptor in record

            pmidInOrder.write(record.getPmid() + " " + counter);
            counter++;
            pmidInOrder.newLine();

           sparseVectorList.add(sparseVector);
       }

        persist.close();
        missingMeSH.flush();
        missingMeSH.close();
        pmidInOrder.flush();
        pmidInOrder.close();
        System.out.println("Records without MeSH descriptors: " + notIndexedWithMeSH);
        System.out.println("Vectors created: " + sparseVectorList.size());

        System.out.println("example:");
        System.out.println( Helpers.printSparseVector(sparseVectorList.get(0)) );

        System.out.println("Saving to cluto file");

        int nnz=0;
        for(SparseVector d : sparseVectorList) nnz += d.nnz();

        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("MeSHVectors.clu")));

        writer.write(sparseVectorList.size() +" " + generateMeSHVectorsBigData.totalVectorLength + " " + nnz);
        writer.newLine();
        for(SparseVector d : sparseVectorList) {

            writer.write(Helpers.printSparseVectorOneBasedToCluto(d));
            writer.newLine();
        }

        writer.flush();
        writer.close();

    }



}
