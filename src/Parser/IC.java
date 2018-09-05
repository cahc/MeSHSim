package Parser;

import MeSH.MeSHParser;
import MeSH.MeshDescriptorCustom;
import MeSH.TreeNodeMeSH;
import com.fasterxml.jackson.core.TreeNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by crco0001 on 9/3/2018.
 */
public class IC {

    //information value per mesh descriptor based on occurences for 2009

    public static void main(String[] arg) throws IOException, XMLStreamException {

        Persist persist = new Persist("E:\\RESEARCH2018\\PUBMED\\pubmed2009.db");

        Object2IntOpenHashMap<String> freqName = new Object2IntOpenHashMap<String>();
        Object2IntOpenHashMap<String> freqUI = new Object2IntOpenHashMap<String>();


        System.out.println("Records in db:" + persist.dbSize() );



        //calculate frequencies of meshTerms..

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

        //frequency done
        persist.close();

        int totalNumberOfTerms = 0;

        for(Object2IntMap.Entry<String> entry : freqUI.object2IntEntrySet()) {

            totalNumberOfTerms = totalNumberOfTerms + entry.getIntValue();

        }


        System.out.println("N: " + totalNumberOfTerms);
        int totalNumberOfTermsCanityCheck = 0;
        for(Object2IntMap.Entry<String> entry : freqName.object2IntEntrySet()) {

            totalNumberOfTermsCanityCheck = totalNumberOfTermsCanityCheck + entry.getIntValue();

        }

        System.out.println("# names: " + freqName.size());
        System.out.println("# ui: " + freqName.size());

        System.out.println("N: " + totalNumberOfTerms);
        System.out.println("N sanity check: " + totalNumberOfTermsCanityCheck);

       // BufferedWriter writer = new BufferedWriter( new FileWriter( new File("DescriptorFreqIn2009.txt")));
      //  BufferedWriter writer2 = new BufferedWriter( new FileWriter( new File("UIFreqIn2009.txt")));

        for(Object2IntMap.Entry<String> entry : freqName.object2IntEntrySet()) {

          // writer.write( entry.getKey() + "\t" + entry.getIntValue() );
       //    writer.newLine();
        }


        for(Object2IntMap.Entry<String> entry : freqUI.object2IntEntrySet()) {

         //   writer2.write( entry.getKey() + "\t" + entry.getIntValue() );
           // writer2.newLine();

        }


     //   writer.flush();
     //   writer.close();
     //   writer2.flush();
     //   writer2.close();



        Map<String, MeshDescriptorCustom> descriptorsMap;

        MeSHParser meSHParser = new MeSHParser("E:\\RESEARCH2018\\PUBMED\\desc2018.xml");

        descriptorsMap = meSHParser.parse();

        System.out.println("Number of descriptor loaded from desc2018.xml " + descriptorsMap.size());

        ////////////////////////////////////SETUP DESCENDAT CALC////////////////////////////////////////////////////////////





        System.out.println("Creating tree structure..");

        Map<String,TreeNodeMeSH> treeNodesMap = new HashMap<>();
        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            for(String treeString : meshDescriptorCustomEntry.getValue().getTreeNumberStringSet()) treeNodesMap.put(treeString, new TreeNodeMeSH(treeString) );
        }

        /*

        Set parents for each TreeNode

         */

        for(Map.Entry<String,TreeNodeMeSH> treeNodeMeSHEntry : treeNodesMap.entrySet()) {

            TreeNodeMeSH  treeNodeMeSH = treeNodeMeSHEntry.getValue();
            String parentString = treeNodeMeSH.getParentTreeString();

            if(parentString != null) {

                TreeNodeMeSH parentTreeNode = treeNodesMap.get(parentString);
                treeNodeMeSH.setParent(parentTreeNode);

                parentTreeNode.addChild(treeNodeMeSH);
            }


        }

        /*

        populate MesHDescriptorCustom with MeSH.TreeNodeMeSH objects

         */

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            Set<String> stringSet =  meshDescriptorCustomEntry.getValue().getTreeNumberStringSet();

            for(String s : stringSet) {

                TreeNodeMeSH treeNodeMeSH = treeNodesMap.get(s);
                if(treeNodeMeSH == null) {System.out.println("MASSIVE FAILURE"); System.exit(0); }

                meshDescriptorCustomEntry.getValue().addTreeNodeMeSH(treeNodeMeSH);

            }

        }



        /*
        mapping between treeNumber and meshTerm (several unique tree numbers can point to the same meshTerm)
         */

        HashMap<TreeNodeMeSH,MeshDescriptorCustom> treeNodeToUniqueMeSHID = new HashMap<>();

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            MeshDescriptorCustom meshDescriptorCustom = meshDescriptorCustomEntry.getValue();


            for (TreeNodeMeSH treeNod : meshDescriptorCustom.getTreeNodeSet()) {

                treeNodeToUniqueMeSHID.put(treeNod, meshDescriptorCustom);

            }

        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////






        //Keratoconjunctivitis [C11.187.183.394]  || Keratoconjunctivitis [C11.204.564.585] // D015186

        String targetID = "D028081";
        MeshDescriptorCustom descriptor = descriptorsMap.get(targetID);


        System.out.println( descriptor );

        //(1) get numer of occurences for D007637 in db

        int freq = freqUI.getInt(targetID);

        System.out.println("freq: " + freqUI.getInt(targetID));

        Set<TreeNodeMeSH> treeNumbers = descriptor.getTreeNodeSet();

        Set<TreeNodeMeSH> decendants = new HashSet<>();
        for(TreeNodeMeSH treeNodeMeSH : treeNumbers) {

            System.out.println("TREE NUMBERS: " + treeNodeMeSH);

            decendants.addAll(treeNodeMeSH.getAllDescendents());
        }

        System.out.println("decendants:");
        System.out.println(decendants);

        System.out.println("uniqe meshid on lower level");

        Set<String> UIs = new HashSet<>();

        for(TreeNodeMeSH treeNodeMeSH : decendants) {

            System.out.println( treeNodeToUniqueMeSHID.get(treeNodeMeSH) );
            UIs.add( treeNodeToUniqueMeSHID.get(treeNodeMeSH).getDescriptorUI()  );

        }


        System.out.println("freq for descentants:");

        int freqDecendants = 0;
        for(String id : UIs) {

            System.out.println(id +" " + freqUI.getInt(id) );
            freqDecendants = freqDecendants + freqUI.getInt(id);

        }

        double P = (freq+freqDecendants)/(double)totalNumberOfTerms;

        //double res = log10(n)/log10(2);
        double normFactorToGetLog2 = Math.log10(2);

        double ic = -(Math.log10(P)/normFactorToGetLog2);

        System.out.println("P: " + P + " IC: " + ic);


    }

}
