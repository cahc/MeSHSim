import java.io.*;
import java.util.*;
import org.xml.sax.SAXException;
import slib.utils.ex.SLIB_Exception;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

/**
 * Created by crco0001 on 6/4/2018.
 */
public class Sim {

    public static void main(String[] arg) throws SLIB_Exception, ParserConfigurationException, SAXException, IOException, XMLStreamException {


        /*

        Read in descriptors and their treeNumbers

         */
        Map<String,MeshDescriptorCustom> descriptorsMap;

        MeSHParser meSHParser = new MeSHParser("/Users/Cristian/Desktop/MesH/desc2018.xml");

        descriptorsMap = meSHParser.parse();

        System.out.println("Number of descriptor loaded from desc2018.xml " + descriptorsMap.size());


        /*

        Read on occurrences

         */

        MeshIDtoFreq uniqueMeshIdToFrequency = new MeshIDtoFreq(new File("/Users/Cristian/Desktop/MesH/MH_freq_counts_2018.txt"));
        uniqueMeshIdToFrequency.parse();
        System.out.println("# mappings between descriptors and frequencies from HM_freq_count_2018.txt: " + uniqueMeshIdToFrequency.size());


        /*

        identify missing id --> freq, add 0 for those

         */

        Set<String> ids = uniqueMeshIdToFrequency.getAllMeshIds();

        int max = 0;
        //int missing =0;
        MeshDescriptorCustom maxObject = null;
        for (Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            if (!ids.contains(meshDescriptorCustomEntry.getValue().getDescriptorUI())) {

                //System.out.println("missing: " + meshDescriptorCustom.getDescriptorUI());
                uniqueMeshIdToFrequency.add(meshDescriptorCustomEntry.getValue().getDescriptorUI(), 0);
              //  missing++;
            } else {

                Integer freq = uniqueMeshIdToFrequency.getFrequency(meshDescriptorCustomEntry.getValue().getDescriptorUI());
                if (freq.compareTo(max) > 0) {
                    max = freq;
                  //  maxObject = meshDescriptorCustomEntry.getValue();
                }
            }

        }
        //System.out.println("MAX: " + maxObject + " " + max);
        //System.out.println("# missing: " + missing);

        System.out.println("Added freq=0, for non-occurring descriptors. New # mappings between descriptors and frequencies: " + uniqueMeshIdToFrequency.size());
        System.out.println("N (sum-total occurrences):" + uniqueMeshIdToFrequency.getTotalOccurrences());



        /*

        set probability for a unique mesh ID, i.e., occurrences / total number of occurrences

         */

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            int freq = uniqueMeshIdToFrequency.getFrequency( meshDescriptorCustomEntry.getValue().getDescriptorUI() );
            meshDescriptorCustomEntry.getValue().setProbability( (double)freq / uniqueMeshIdToFrequency.getTotalOccurrences());

        }


       // BufferedWriter writerProb = new BufferedWriter(new FileWriter(new File("/Users/Cristian/Desktop/MesH/probability.txt")));

        //for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

        //  writerProb.write(meshDescriptorCustomEntry.getValue().getDescriptorUI() + "\t" + meshDescriptorCustomEntry.getValue().getProbability() );
        //  writerProb.newLine();
        // }

        // writerProb.flush();
        // writerProb.close();


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

        populate MesHDescriptorCustom with TreeNodeMeSH objects

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

        Setup complet, testing follows
         */

        System.out.println("test 1:");
        TreeNodeMeSH treeNodeMeSH = treeNodesMap.get("A01.378.610.250.300.792.380");
        System.out.println(treeNodeMeSH);
        System.out.println("parent:");
        System.out.println(treeNodeMeSH.getParent());
        System.out.println("children:");
        System.out.println(treeNodeMeSH.getChildren());

        System.out.println("test 2:");


        //mapping between treeNumber and meshTerm (several uniqe tree numbers can point to the same meshTerm)

        HashMap<TreeNodeMeSH,MeshDescriptorCustom> treeNodeToUniqueMeSHID = new HashMap<>();

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

            MeshDescriptorCustom meshDescriptorCustom = meshDescriptorCustomEntry.getValue();


            for (TreeNodeMeSH treeNod : meshDescriptorCustom.getTreeNodeSet()) {

                treeNodeToUniqueMeSHID.put(treeNod, meshDescriptorCustom);

            }

        }


       ///////////////////////////////////////////////////////////////////

        System.out.println("calculating information content");

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {


            Set<TreeNodeMeSH> NodSet = meshDescriptorCustomEntry.getValue().getTreeNodeSet();

            List<TreeNodeMeSH> descendents = new ArrayList<>();
            for(TreeNodeMeSH treeNodeMeSH1 : NodSet ) {

               descendents.addAll( treeNodeMeSH1.getAllDescendents() );

            }

            Set<MeshDescriptorCustom> setOfMeshTermDescentants = new HashSet<>();

            for(TreeNodeMeSH treeNodeMeSH1 : descendents) setOfMeshTermDescentants.add( treeNodeToUniqueMeSHID.get(treeNodeMeSH1) );


            Iterator<MeshDescriptorCustom> iter = setOfMeshTermDescentants.iterator();
            double summedProbs = meshDescriptorCustomEntry.getValue().probability;

            while(iter.hasNext()) {

                summedProbs+= iter.next().probability;

            }

            meshDescriptorCustomEntry.getValue().setInformationContent(  -Math.log(summedProbs) );
        }




        MeshDescriptorCustom meshDescriptorCustom = descriptorsMap.get("D001829"); //Body Regions

        System.out.println(meshDescriptorCustom.getDescriptorUI() + " " + meshDescriptorCustom.getInformationContent());


        meshDescriptorCustom = descriptorsMap.get("D017773"); //Pelvic Floor

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());

        meshDescriptorCustom = descriptorsMap.get("D006801"); //Humans

        TreeNodeMeSH treeNodeMeSH1 = meshDescriptorCustom.getTreeNodeSet().iterator().next();

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());



        meshDescriptorCustom = descriptorsMap.get("D006071"); //gorillaz

        TreeNodeMeSH treeNodeMeSH2 = meshDescriptorCustom.getTreeNodeSet().iterator().next();

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());


            /*

        the lowest common ancestor (LCA) of two nodes v and w in a tree  is the lowest (i.e. deepest) node that has both v and w as descendants.
        We define each node to be a descendant of itself (so if v has a direct connection from w, w is the lowest common ancestor).

         */

            //TODO descendant to itself!

        TreeNodeMeshIndex treeNodeMeshIndex = new TreeNodeMeshIndex(treeNodesMap);

        TreeNodeMeSH treeNodeMeSH3 = treeNodeMeshIndex.getCommonAnsestor(treeNodeMeSH1,treeNodeMeSH2);

        System.out.println(treeNodeMeSH3);



        /*


         */






        ////



    }
}

            /*
            Set<String> test = meshIDtoFreqReader.getAllMeshIds();
            Set<String> mesh = new HashSet<>();
            BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File("/Users/Cristian/Desktop/MesH/AllMeshCustom.txt")));
           for(MeshDescriptorCustom meshDescriptorCustom : descriptors) {

            writer3.write(meshDescriptorCustom.toString());
            writer3.newLine();
            mesh.add(meshDescriptorCustom.descriptorUI);
        }

        writer3.flush();
        writer3.close();

        for(String s : test) {

            if(!mesh.contains(s)) System.out.println("MISSING 2  :" + s);
        }


            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/Users/Cristian/Desktop/MesH/treeNumbers.txt")));

            // BufferedWriter writer = new BufferedWriter( new FileWriter( new File("/Users/Cristian/Desktop/MesH/checktags.txt")));

            Set<String> treeNumbers = new HashSet<>();
            List<TreeNodeMeSH> treeNodeMeSHList = new ArrayList<>();
            for (MeshDescriptorCustom descriptor : descriptors) {

                treeNumbers.addAll(descriptor.treeNumberSet);


                // if(descriptor.annotation != null && descriptor.annotation.contains("check tag")) {writer.write(descriptor.descriptorName + "\t" + descriptor.descriptorUI +"\t" + descriptor.annotation.replaceAll("[\\r\\n]+", "") +"\t" +descriptor.treeNumberSet); writer.newLine(); }
            }

            List<TreeNodeMeSH> treeNodeMeSHES = new ArrayList<>();
            List<String> treeNumbers2 = new ArrayList<>(treeNumbers);

            Collections.sort(treeNumbers2);
            for (String s : treeNumbers2) treeNodeMeSHList.add(new TreeNodeMeSH(s));
            TreeNodeMeshIndex treeNodeMeshIndex = new TreeNodeMeshIndex(treeNodeMeSHList);

            System.out.println(treeNodeMeSHList.get(20) + " " + treeNodeMeSHList.get(20) + " common: " + treeNodeMeshIndex.getCommonAnsestor(treeNodeMeSHList.get(20), treeNodeMeSHList.get(20)));

            System.out.println(treeNodeMeSHList.get(25) + " " + treeNodeMeSHList.get(60) + " common: " + treeNodeMeshIndex.getCommonAnsestor(treeNodeMeSHList.get(25), treeNodeMeSHList.get(60)));

            TreeNodeMeSH a = new TreeNodeMeSH("E04.100.814.868.937.830");
            TreeNodeMeSH b = new TreeNodeMeSH("A01");
            //TreeNodeMeSH b =  new TreeNodeMeSH("E04.100.814.529.968.060");

            System.out.println(a + " " + b +  " common " + treeNodeMeshIndex.getCommonAnsestor(a,b) );

            System.exit(0);
        }
    }


  /*




            Collections.sort(treeNumbers2);
            for(String s : treeNumbers2) { writer.write(s); writer.newLine(); treeNodeMeSHES.add( new TreeNodeMeSH(s) ); }
            writer.flush();
            writer.close();

            for(TreeNodeMeSH treeNodeMeSH : treeNodeMeSHES) System.out.println(treeNodeMeSH);
            // We compute semantic similarities between concepts
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)

            URI c1 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D013812"); // female
            URI c2 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D041883"); // male


           // System.out.println("IC D013812:" + engine.getIC(icConf,c1));
            //System.out.println("IC D041883: " + engine.getIC(icConf,c2));

            // We compute the similarity
            //double sim = engine.compare(measureConf, c1, c2);
            //System.out.println("Sim " + c1 + "\t" + c2 + "\t" + sim);



            Set<URI> set1 = new HashSet<URI>();
            set1.add(c1);

            Set<URI> set2 = new HashSet<URI>();
            set2.add(c2);
            set2.add(c1);

           // System.out.println("BMA: " +  engine.compare(groupConf,measureConf,set1,set2) );



            System.out.println(meshGraph.toString() );

            List<URI> concepts2 = new ArrayList<URI>(meshGraph.getV());


           // System.out.println("Lowest common ansestor: " + engine.getLCAs(c1,c2) );

            System.exit(0);



            // * The computation of the first similarity is not very fast because
            // * the engine compute extra informations which are cached for next computations.
            // * Lets compute 10 000 000 random pairwise similarities

            int totalComparison = 10000000;
            List<URI> concepts = new ArrayList<URI>(meshGraph.getV());
            int id1, id2;
            String idC1, idC2;
            Random r = new Random();

            for (int i = 0; i < totalComparison; i++) {
                id1 = r.nextInt(concepts.size());
                id2 = r.nextInt(concepts.size());

                c1 = concepts.get(id1);
                c2 = concepts.get(id2);

                double sim = engine.compare(measureConf, c1, c2);

                if ((i + 1) % 50000 == 0) {
                    idC1 = c1.getLocalName();
                    idC2 = c2.getLocalName();

                    System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
                }
            }

            t.stop();
            t.elapsedTime();


        } catch (SLIB_Exception ex) {
            Logger.getLogger(Sim.class.getName()).log(Level.SEVERE, null, ex);

        }


    }
}

*/







