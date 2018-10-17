package misc;

import it.unimi.dsi.fastutil.ints.*;
import jsat.linear.IndexValue;
import jsat.linear.SparseMatrix;
import jsat.linear.SparseVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class QualityFunction {

    /*

    input: a partition
    input: a sparse matrix with top k similarity values, not (necessarily symmetric)

     output: quality function eq 4
     */


    private IntArrayList partition = new IntArrayList(1000000);
    private Int2ObjectOpenHashMap<IntOpenHashSet> clusterToPublicationIndices = new Int2ObjectOpenHashMap<>(1000); //cluster starts at 0
    private SparseMatrix topKsimilarity; // zero-based indices

    private int numberOfPublications;
    private int numberOfClusters;

    public QualityFunction(String partitionFile, String topKsimilarityFile) throws IOException {


       this.topKsimilarity = Helpers.clutoToSparseMatrix(topKsimilarityFile);

        BufferedReader partitionReader = new BufferedReader( new FileReader(new File(partitionFile)));
        String line;
        while ( (line = partitionReader.readLine()) != null ) {

            partition.add( Integer.parseInt(line) );

        }

       partitionReader.close();


       int countMax = 0;
       for(int i=0; i<partition.size(); i++) { int cluster = partition.getInt(i); if(cluster>countMax) countMax=cluster; }


       numberOfPublications = partition.size();
       numberOfClusters = (countMax+1); //assuming cluster ID starts at 0

       if(numberOfPublications != topKsimilarity.rows()) throw new IOException("partition - matrix mismatch");

       System.out.println("# publications: " + numberOfPublications);
       System.out.println("# clusters: " + numberOfClusters);

    }


    public int getClusterForPublication(int i) {

       return partition.getInt(i); // i is zero based

    }


    public void createClusterPublicationMapping(boolean print) {

        for(int i=0; i<partition.size(); i++) {


            int cluster = getClusterForPublication(i);

            IntOpenHashSet clusterMapping = clusterToPublicationIndices.get(cluster);

            if(clusterMapping == null) {

                clusterMapping = new IntOpenHashSet(100);
                clusterMapping.add(i);

                clusterToPublicationIndices.put(cluster,clusterMapping);

            } else {

                clusterMapping.add(i);
            }


        }

        if(print) {


          for( Int2ObjectMap.Entry<IntOpenHashSet> entry :   clusterToPublicationIndices.int2ObjectEntrySet() ) {

              System.out.println("cluster: " + entry.getIntKey() + " size: " + entry.getValue().size() );

          }

        }

    }


    public double calculateQualityFunction() {

        long numberOfPairsConsidered = 0;
        double Q = 0;

        for(int i =0; i<numberOfPublications; i++) {

            Iterator<IndexValue> nnz_in_row_i = topKsimilarity.getRowView(i).getNonZeroIterator();
            int cluster_for_i = getClusterForPublication(i);


            while(nnz_in_row_i.hasNext()) {

                IndexValue indexValue = nnz_in_row_i.next();

                int j = indexValue.getIndex();
                int cluster_for_j = getClusterForPublication(j);

                if(cluster_for_i == cluster_for_j)  { Q = Q + indexValue.getValue(); numberOfPairsConsidered++; } //the similarity between i and j

            }


        }

        System.out.println("Number of (ordered) pairs considered: " + numberOfPairsConsidered);
        return Q;
    }

    public static void main(String[] arg) throws IOException {


      QualityFunction qualityFunction = new QualityFunction("massiveClustering.txt","TopK25Similarity.clu");


      //we don't need this
      //  System.out.println("building cluster to publication mappings..");
        // qualityFunction.createClusterPublicationMapping(true);

        System.out.println("Calculating Quality Function..");
       double Q = qualityFunction.calculateQualityFunction();

       System.out.println("Q value: " + Q);
       System.out.println("Q value long format: " + Double.valueOf(Q).longValue());
    }

}
