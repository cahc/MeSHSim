package Parser;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import jsat.linear.SparseVector;
import misc.Helpers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by crco0001 on 9/6/2018.
 */
public class GenerateMeshVectors {


    /*

    In 2009 there are in total 25597 descriptors used and 79 subheadings

    //from IC indexer we have:

    *ICperDescriptor.txt
    *
    * DescriptorUItoIndex.txt (one-based)
    *
    * QualifiersUItoIndex.txt (one-based)


     */

    public static int mapDescriptorIndexToVectorIndex(int descriptorIndex) {



        return (80*descriptorIndex - 79);  //assuming 79 subheadings and assuming indices are ONE-based!!

    }

    public static int mapQualifierIndexToVectorIndex(int descriptorIndex, int qualifierIndex) {


        return (80*descriptorIndex - 79)+qualifierIndex;  //assuming 79 subheadings and assuming indices are ONE-based!!

    }


    public static void main(String[] arg) throws IOException {


        Object2DoubleMap<String> descriptorToIC = new Object2DoubleOpenHashMap<>();
        Object2IntMap<String> descriptorToIndex = new Object2IntOpenHashMap<>();
        Object2IntMap<String> qualifyerToIndex = new Object2IntOpenHashMap<>();


        try (BufferedReader in = new BufferedReader(new FileReader("E:\\RESEARCH2018\\PUBMED\\ICperDescriptor.txt")) ) {
            String line;

            while ((line = in.readLine()) != null) {

                String[] parts = line.split("\t");
                if(parts.length != 5) throw new IOException("IC file not separated correctly");
                descriptorToIC.put(parts[0], (double)Double.valueOf(parts[4]));

            }
        } catch (IOException e) {

            System.out.println("error in reading IC file");
            System.exit(1);
        }


        System.out.println("ic mappings: " + descriptorToIC.size());


        try (BufferedReader in = new BufferedReader(new FileReader("E:\\RESEARCH2018\\PUBMED\\DescriptorUItoIndex.txt")) ) {
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


        System.out.println("descriptor index mapper: " + descriptorToIndex.size());



        try (BufferedReader in = new BufferedReader(new FileReader("E:\\RESEARCH2018\\PUBMED\\QualifiersUItoIndex.txt")) ) {
            String line;

            while ((line = in.readLine()) != null) {

                String[] parts = line.split("\t");
                if(parts.length != 2) throw new IOException("qualifyer index file not separated correctly");
                qualifyerToIndex.put(parts[0], (int)Integer.valueOf(parts[1]));

            }
        } catch (IOException e) {

            System.out.println("error in reading qualifyer index file");
            System.exit(1);
        }


        System.out.println("descriptor index mapper: " + qualifyerToIndex.size());


        //total vector length #descriptors + ( #descriptors * #qualifiers)
        int totalVectorLength = descriptorToIC.size() + (descriptorToIC.size()*qualifyerToIndex.size());
        System.out.println("Sparse vector length: " + totalVectorLength ) ;





        BufferedReader bibMetMesh = new BufferedReader( new FileReader("E:\\RESEARCH2018\\PUBMED\\UT_TO_MESH_CC_VERSION.txt"));

        String header = bibMetMesh.readLine();
        List<SparseVector> sparseVectorList = new ArrayList<>();
        String line;
        String currentUT = "none";
        SparseVector sparseVector = new SparseVector(totalVectorLength,15);
        IntSet seenIndicesForDescriptors = new IntOpenHashSet();

        bibMetMesh.mark(500);
        boolean fistRow = true;

        while( (line = bibMetMesh.readLine()) != null) {

            String[] parts = line.split("\t");
            String newUT = parts[0];
            if(newUT.equals(currentUT)) {

                String descriptorID = parts[4];
                String qualifyerID = parts[5];

               boolean isMajor = (parts[6].equals("true") ||  parts[7].equals("true") ); //descriptor or qualifyer, dosent matter

               int descriptorIndex = descriptorToIndex.getInt(descriptorID);
               if(descriptorIndex == 0) {System.out.println("Catastrophic error!, descriptor to index == 0"); System.exit(0); }

               int qualiferIndex = -1;
               if(qualifyerID.length() > 3) {

                   qualiferIndex = qualifyerToIndex.getInt(qualifyerID);
                   if(qualiferIndex == 0) {System.out.println("Catastrophic error!, qualifyer to index == 0"); System.exit(0); }
               }

               //keep track of repeating descriptors,

               boolean seenBefore = seenIndicesForDescriptors.contains(descriptorIndex);

               if(!seenBefore) { //not seen before

                   seenIndicesForDescriptors.add(descriptorIndex);

                   //get the true index, ZERO BASED
                   int newIndex = mapDescriptorIndexToVectorIndex(descriptorIndex)-1;

                   sparseVector.set(newIndex,10); // temp weight of 10

                   //also check if there is an qualifier

                   if(qualiferIndex != -1) {

                       //get the true index, ZERO BASED
                       int newIndex2 = mapQualifierIndexToVectorIndex(descriptorIndex,qualiferIndex)-1;

                       sparseVector.set(newIndex2,1); //temp weight of 1
                   }




               } else { //seen descriptor before

                   //there might be a qualifier to add however

                   if(qualiferIndex != -1) {

                       //get the true index, ZERO BASED
                      int newIndex2 = mapQualifierIndexToVectorIndex(descriptorIndex,qualiferIndex)-1;

                      sparseVector.set(newIndex2,1); //temp weight of 1
                   }


               }



                //System.out.println("same UT: " + currentUT);

            } else {
             //System.out.println("new UT: " + newUT);
                currentUT = newUT;
                bibMetMesh.reset();

                //clean up and finalize
                if(!fistRow) {


                    sparseVectorList.add(sparseVector); // add to list
                }

                //new
                sparseVector = new SparseVector(totalVectorLength,15); //pre allocate only 15 elements //zero-based indices // create a new vector for this
                seenIndicesForDescriptors.clear();

                fistRow =false;

                continue;
            }

            bibMetMesh.mark(500);

        }


        sparseVectorList.add(sparseVector); // ad the last one

        System.out.println(sparseVectorList.size());

        System.out.println("nnz (vektor1)" + sparseVectorList.get(0).nnz());
        System.out.println("nnz (vectorN)" + sparseVectorList.get(455756).nnz());

        System.out.println( Helpers.printSparseVector(sparseVectorList.get(0)) );

        System.out.println();

        System.out.println( Helpers.printSparseVector(sparseVectorList.get(455756)) );

    }

}
