package misc;

import jsat.linear.IndexValue;
import jsat.linear.SparseMatrix;
import jsat.linear.SparseVector;

import java.io.*;
import java.util.Iterator;

/**
 * Created by crco0001 on 9/7/2018.
 */
public class Helpers {

    public static String printSparseVector(SparseVector vec) {

        StringBuilder stringbuilder = new StringBuilder();

        Iterator<IndexValue> iter = vec.getNonZeroIterator();
        stringbuilder.append("[");
        while(iter.hasNext()) {

            IndexValue indexValue = iter.next();
            stringbuilder.append(indexValue.getIndex()).append(",");
            stringbuilder.append(indexValue.getValue()).append(" ");

        }
        stringbuilder.append("]");
        return stringbuilder.toString();
    }


    public static String printSparseVectorOneBasedToCluto(SparseVector vec) {


        StringBuilder stringbuilder = new StringBuilder();
        Iterator<IndexValue> iter = vec.getNonZeroIterator();

        boolean firstPair = true;

        while (iter.hasNext()) {

            IndexValue indexValue = iter.next();

            if (firstPair) {
                stringbuilder.append(indexValue.getIndex() + 1).append(" ");
                stringbuilder.append(indexValue.getValue());
                firstPair = false;
            } else {

                stringbuilder.append(" ").append(indexValue.getIndex() + 1).append(" ");
                stringbuilder.append(indexValue.getValue());

            }

        }

        return stringbuilder.toString();
    }



    public static SparseMatrix clutoKnngToSymetricUpperRight(String fileName) throws IOException {


        BufferedReader reader = new BufferedReader( new FileReader(new File(fileName)));
        String[] headers = reader.readLine().trim().split(" ");
        if(headers.length != 3) {System.out.println("wrong header in Cluto-file"); System.exit(1); }

        System.out.println("Cluto meta data. rows: " + headers[0] + " cols: " + headers[1] + " nnz: " + headers[2]);

        SparseMatrix sparseMatrix = new SparseMatrix(Integer.valueOf(headers[0]),Integer.valueOf(headers[1]),25);

        String line;
        int zeroBasedRow=0;

        while( (line = reader.readLine()) != null ) {

          String[] clutoLine = line.trim().split(" ");

          for(int i=0; i<clutoLine.length-1; i++) {

              int zeroBasedCol = Integer.valueOf( clutoLine[i] )-1;
              double value = Double.valueOf(clutoLine[i+1]);
              i++;

              //(1)
              //only save i < j , i.e., upper right triangle ( zeroBasedRow < zeroBasedCol )

              //(2)
              // if j > i then flip j and i, thus we make symemetric with add-operation


              if(zeroBasedRow < zeroBasedCol) {

                  sparseMatrix.set(zeroBasedRow,zeroBasedCol,value); // set!!
              } else {

                  sparseMatrix.increment(zeroBasedCol,zeroBasedRow,value); //increment!!

              }


          } //row parsed

            zeroBasedRow++;

        } //cluto file complete

        reader.close();

       System.out.println(  "new nnz in symmetric upper right: " + sparseMatrix.nnz() ) ;

       return sparseMatrix;
    }


    public static void SparseMatrixToCluto(SparseMatrix matrix, String filename) throws IOException {


        int rows = matrix.rows();
        int cols = matrix.cols();
        long nnz = matrix.nnz();

        BufferedWriter writer = new BufferedWriter(new FileWriter( new File(filename)));

        //cluto files as one based, but sparseMatrix is zero-based!

        writer.write(rows + " " +cols + " " +nnz);
        writer.newLine();

        for(int row=0; row<rows; row++) {

           Iterator<IndexValue> iterator =  matrix.getRowView(row).getNonZeroIterator();

           boolean first = true;
           while(iterator.hasNext()) {

               if(first) {

                   IndexValue column = iterator.next();
                   writer.write( String.valueOf(column.getIndex()+1));
                   writer.write(" ");
                   writer.write( String.valueOf( column.getValue() ) );
                   first = false;

               } else {
                   writer.write(" ");
                   IndexValue column = iterator.next();
                   writer.write( String.valueOf(column.getIndex()+1));
                   writer.write(" ");
                   writer.write( String.valueOf( column.getValue() ) );
               }


           }

            writer.newLine();

        }


        writer.flush();
        writer.close();
    }



    public static void SparseMatrixToIJV(SparseMatrix matrix, String filename,String sep) throws IOException {

        //zero-based!!

        int rows = matrix.rows();
        int cols = matrix.cols();
        long nnz = matrix.nnz();

        BufferedWriter writer = new BufferedWriter(new FileWriter( new File(filename)));

        //cluto files as one based, but sparseMatrix is zero-based!
        //writer.write(rows + " " +cols + " " +nnz);
        //writer.newLine();

        for(int row=0; row<rows; row++) {

            Iterator<IndexValue> iterator =  matrix.getRowView(row).getNonZeroIterator();


            while(iterator.hasNext()) {

                    IndexValue column = iterator.next();
                    writer.write(String.valueOf(row));
                    writer.write(sep);
                    writer.write( String.valueOf(column.getIndex()));
                    writer.write(sep);
                    writer.write( String.valueOf( column.getValue() ) );
                    writer.newLine();

            }


        }


        writer.flush();
        writer.close();
    }




    public static void main(String[] arg ) throws IOException {

        SparseMatrix sparseMatrix = clutoKnngToSymetricUpperRight("/Users/Cristian/Desktop/NEW_DATA_BIBCAP_2017/PubMed/MeSHSimvectors/MeSHSimVectorsk25NOTSCALED.clu");



      SparseMatrixToCluto(sparseMatrix,"/Users/Cristian/Desktop/NEW_DATA_BIBCAP_2017/PubMed/MeSHSimvectors/MeSHSimVectorsk25NOTSCALEDSymetricUpperRight.clu");
      SparseMatrixToIJV(sparseMatrix,"/Users/Cristian/Desktop/NEW_DATA_BIBCAP_2017/PubMed/MeSHSimvectors/MeSHSimVectorsk25NOTSCALEDSymetricUpperRight.ijv","\t");


    }


    }




