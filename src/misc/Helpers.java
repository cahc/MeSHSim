package misc;

import jsat.linear.IndexValue;
import jsat.linear.SparseVector;

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




    }


