package MeSH;

import java.io.*;
import java.util.*;

import IndexingStructures.InvertedIndex;
import IndexingStructures.TopNCollector;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

/**
 * Created by crco0001 on 6/4/2018.
 */
public class Sim {

    public static void main(String[] arg) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {


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

        //for(Map.Entry<String,MeSH.MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {

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



    /*

    ICindexer calculations

      pobability for the ID + the probability for all id:s with a lower treenumber
     */

        System.out.println("calculating information content");

        for(Map.Entry<String,MeshDescriptorCustom> meshDescriptorCustomEntry : descriptorsMap.entrySet()) {


            Set<TreeNodeMeSH> NodSet = meshDescriptorCustomEntry.getValue().getTreeNodeSet();

            List<TreeNodeMeSH> descendants = new ArrayList<>();
            for(TreeNodeMeSH treeNodeMeSH1 : NodSet ) {

               descendants.addAll( treeNodeMeSH1.getAllDescendents() );

            }

            Set<MeshDescriptorCustom> setOfMeshTermDescendants = new HashSet<>();

            for(TreeNodeMeSH treeNodeMeSH1 : descendants) setOfMeshTermDescendants.add( treeNodeToUniqueMeSHID.get(treeNodeMeSH1) );


            Iterator<MeshDescriptorCustom> iter = setOfMeshTermDescendants.iterator();
            double summedProbabilities = meshDescriptorCustomEntry.getValue().probability;

            while(iter.hasNext()) {

                summedProbabilities+= iter.next().probability;

            }

            meshDescriptorCustomEntry.getValue().setInformationContent(  (float)(-Math.log(summedProbabilities)) );
        }


        /*

       Setup indexing structures

         */

        TreeNodeMeshIndex treeNodeMeshIndex = new TreeNodeMeshIndex(treeNodesMap);
        treeNodeMeshIndex.setTreeNodeToUniqueMeSHTermMap( treeNodeToUniqueMeSHID ); //to access when calculating sim(v,v) and MeSH.Sim(M,M)

        /*

        Setup complete, testing follows

         */


       // System.out.println("Running tests:");

        //MeSH.MeshDescriptorCustom meshDescriptorCustom1 = descriptorsMap.get("D011148"); //Resistance Training

        //MeSH.MeshDescriptorCustom meshDescriptorCustom2 = descriptorsMap.get("D011323"); //Cool-Down Exercise (higher up)

        /*

        the lowest common ancestor (LCA) of two nodes v and w in a tree  is the lowest (i.e. deepest) node that has both v and w as descendants.
        We define each node to be a descendant of itself (so if v has a direct connection from w, w is the lowest common ancestor).

        */


        System.out.println("Paring data from KI");

        ReaderKI readerKI = new ReaderKI(new File("/Users/Cristian/Desktop/MesH/mesh_by_ut.csv"),true,true);
        HashMap<String,List<String>> utToMeSHList = readerKI.parse();
        System.out.println("docs in KI-data: " + utToMeSHList.size());
        Map<String, GenericPubMedDoc> genericMeSHDocumentMap = new HashMap<>();
        Iterator<Map.Entry<String,List<String>>> iterator =  utToMeSHList.entrySet().iterator();
        while((iterator.hasNext())) {

         Map.Entry<String,List<String>> entry = iterator.next();

         GenericPubMedDoc genericPubMedDoc = new GenericPubMedDoc(entry.getKey());

         Set<MeshDescriptorCustom> uniqueMeshTerms = new HashSet<>();
         for(String s : entry.getValue()) {

            MeshDescriptorCustom meshDescriptorCustom =  descriptorsMap.get(s);
            if(meshDescriptorCustom == null) {System.out.println("not found in map: " + s); continue; }
            uniqueMeshTerms.add(meshDescriptorCustom);


         }

            genericPubMedDoc.addMeshDescriptorCustom( uniqueMeshTerms );

            genericMeSHDocumentMap.put(genericPubMedDoc.getID(), genericPubMedDoc);
        }

        System.out.println("Generic documents created: " + genericMeSHDocumentMap.size());


       GenericPubMedDoc hej = genericMeSHDocumentMap.get("000262559200001");
        System.out.println(hej.getMeshDescriptorCustomList());
        System.out.println("sorted");
        hej.sortMeSHdescriptorsByIC();
        System.out.println(hej.getMeshDescriptorCustomList());
        //System.exit(0);

        //List<MeSH.MeshDescriptorCustom> d1 = new ArrayList<>(); d1.add(descriptorsMap.get("D011148"));
        //List<MeSH.MeshDescriptorCustom> d2 = new ArrayList<>(); d2.add(descriptorsMap.get("D013020")); d2.add( descriptorsMap.get("D011323") );


        System.out.println("Building inverted index");

        InvertedIndex invertedIndex = new InvertedIndex();

        for(Map.Entry<String, GenericPubMedDoc> genericMeSHDocumentEntry : genericMeSHDocumentMap.entrySet()) {

           GenericPubMedDoc genericPubMedDoc = genericMeSHDocumentEntry.getValue();

            for(MeshDescriptorCustom descriptor : genericPubMedDoc.getMeshDescriptorCustomList() ) {

                invertedIndex.addDescriptorDocumentPair(descriptor, genericPubMedDoc);
                //break; //randomly add one..
            }

        }

        System.out.println("index size: " + invertedIndex.getIndexSize());

        long start1 =  System.currentTimeMillis();

        //todo this should be a parallel implementation
        int compleated = 0;
        for(Map.Entry<String,GenericPubMedDoc> entry : genericMeSHDocumentMap.entrySet()) {

            GenericPubMedDoc genericPubMedDoc = entry.getValue();

            TopNCollector topNCollector = new TopNCollector(20, genericPubMedDoc.getID() );

            Set<GenericPubMedDoc> potentialBestMatches = invertedIndex.getAllMatchingDocuemnts(genericPubMedDoc);

            for(GenericPubMedDoc potential : potentialBestMatches) {

                float simDoc = treeNodeMeshIndex.getSimilarityBetweenToDocuments(genericPubMedDoc.getMeshDescriptorCustomList(), potential.getMeshDescriptorCustomList() ) ;

                if(simDoc > 0) topNCollector.offer(potential, simDoc );



            }

            genericPubMedDoc.addTopNCollector(topNCollector);
            compleated++;

            if(compleated % 5000 == 0) System.out.println("compleated: " + compleated);
        }

        long stop1 = System.currentTimeMillis();

        System.out.println("Initial Neighbourhoods calculation took: " + (stop1-start1)/1000.0 );


        //Exact matching
        /*


        GenericPubMedDoc targetDocument = genericMeSHDocumentMap.get("000272141600011");

        TopNCollector topNCollector = new TopNCollector(20, targetDocument.getID() );

       long start1 =  System.currentTimeMillis();
        for(Map.Entry<String,GenericPubMedDoc> entry : genericMeSHDocumentMap.entrySet()) {


           //if(i == targetIndice) continue;
           GenericPubMedDoc potential = entry.getValue();

           float simDoc = treeNodeMeshIndex.getSimilarityBetweenToDocuments(targetDocument.getMeshDescriptorCustomList(), potential.getMeshDescriptorCustomList() ) ;

           if(simDoc > 0) topNCollector.offer(potential, simDoc );


          // System.out.println("simDoc: " + simDoc);
       }

       long stop1 = System.currentTimeMillis();

        topNCollector.sort();
       System.out.println("Exact Neighbourhood for : " + topNCollector.getID() + "took: " + (stop1-start1)/1000.0 );
       System.out.println(topNCollector);




       System.out.println("potential matches for target using inverted index: " + invertedIndex.getAllMatchingDocuemnts(targetDocument).size() ) ;

        TopNCollector topNCollectorApproximate = new TopNCollector(20, targetDocument.getID() );
        Set<GenericPubMedDoc> potentialBestMatches = invertedIndex.getAllMatchingDocuemnts(targetDocument);

        long star2 = System.currentTimeMillis();
        for(GenericPubMedDoc potential : potentialBestMatches) {

            //if(i == targetIndice) continue;

            float simDoc = treeNodeMeshIndex.getSimilarityBetweenToDocuments(targetDocument.getMeshDescriptorCustomList(), potential.getMeshDescriptorCustomList() ) ;

            if(simDoc > 0) topNCollectorApproximate.offer(potential, simDoc );


            // System.out.println("simDoc: " + simDoc);
        }

        long stop2 = System.currentTimeMillis();

        topNCollectorApproximate.sort();
        System.out.println("Approximate Neighbourhood for : " + topNCollectorApproximate.getID() + " took: " + (stop2-star2)/1000.0 );
        System.out.println(topNCollectorApproximate);

        System.out.println("local search heuristic:");

*/


        //      System.out.println(meshDescriptorCustom1);
//        System.out.println(meshDescriptorCustom1.getInformationContent());
   //     System.out.println(meshDescriptorCustom2);
    //    System.out.println(meshDescriptorCustom2.getInformationContent());



/*

        System.out.println("test 1:");
        MeSH.TreeNodeMeSH treeNodeMeSH = treeNodesMap.get("A01.378.610.250.300.792.380");
        System.out.println(treeNodeMeSH);
        System.out.println("parent:");
        System.out.println(treeNodeMeSH.getParent());
        System.out.println("children:");
        System.out.println(treeNodeMeSH.getChildren());

        System.out.println("test 2:");

        MeSH.MeshDescriptorCustom meshDescriptorCustom = descriptorsMap.get("D001829"); //Body Regions

        System.out.println(meshDescriptorCustom.getDescriptorUI() + " " + meshDescriptorCustom.getInformationContent());


        meshDescriptorCustom = descriptorsMap.get("D017773"); //Pelvic Floor

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());

        meshDescriptorCustom = descriptorsMap.get("D006801"); //Humans

        MeSH.TreeNodeMeSH treeNodeMeSH1 = meshDescriptorCustom.getTreeNodeSet().iterator().next();

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());



        meshDescriptorCustom = descriptorsMap.get("D015186"); //gorillaz

        MeSH.TreeNodeMeSH treeNodeMeSH2 = meshDescriptorCustom.getTreeNodeSet().iterator().next();

        System.out.println(meshDescriptorCustom.getDescriptorUI() +" " + meshDescriptorCustom.getInformationContent());





        /*


         */






        ////



    }
}

            /*
            Set<String> test = meshIDtoFreqReader.getAllMeshIds();
            Set<String> mesh = new HashSet<>();
            BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File("/Users/Cristian/Desktop/MesH/AllMeshCustom.txt")));
           for(MeSH.MeshDescriptorCustom meshDescriptorCustom : descriptors) {

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
            List<MeSH.TreeNodeMeSH> treeNodeMeSHList = new ArrayList<>();
            for (MeSH.MeshDescriptorCustom descriptor : descriptors) {

                treeNumbers.addAll(descriptor.treeNumberSet);


                // if(descriptor.annotation != null && descriptor.annotation.contains("check tag")) {writer.write(descriptor.descriptorName + "\t" + descriptor.descriptorUI +"\t" + descriptor.annotation.replaceAll("[\\r\\n]+", "") +"\t" +descriptor.treeNumberSet); writer.newLine(); }
            }

            List<MeSH.TreeNodeMeSH> treeNodeMeSHES = new ArrayList<>();
            List<String> treeNumbers2 = new ArrayList<>(treeNumbers);

            Collections.sort(treeNumbers2);
            for (String s : treeNumbers2) treeNodeMeSHList.add(new MeSH.TreeNodeMeSH(s));
            MeSH.TreeNodeMeshIndex treeNodeMeshIndex = new MeSH.TreeNodeMeshIndex(treeNodeMeSHList);

            System.out.println(treeNodeMeSHList.get(20) + " " + treeNodeMeSHList.get(20) + " common: " + treeNodeMeshIndex.getCommonAncestor(treeNodeMeSHList.get(20), treeNodeMeSHList.get(20)));

            System.out.println(treeNodeMeSHList.get(25) + " " + treeNodeMeSHList.get(60) + " common: " + treeNodeMeshIndex.getCommonAncestor(treeNodeMeSHList.get(25), treeNodeMeSHList.get(60)));

            MeSH.TreeNodeMeSH a = new MeSH.TreeNodeMeSH("E04.100.814.868.937.830");
            MeSH.TreeNodeMeSH b = new MeSH.TreeNodeMeSH("A01");
            //MeSH.TreeNodeMeSH b =  new MeSH.TreeNodeMeSH("E04.100.814.529.968.060");

            System.out.println(a + " " + b +  " common " + treeNodeMeshIndex.getCommonAncestor(a,b) );

            System.exit(0);
        }
    }


  /*




            Collections.sort(treeNumbers2);
            for(String s : treeNumbers2) { writer.write(s); writer.newLine(); treeNodeMeSHES.add( new MeSH.TreeNodeMeSH(s) ); }
            writer.flush();
            writer.close();

            for(MeSH.TreeNodeMeSH treeNodeMeSH : treeNodeMeSHES) System.out.println(treeNodeMeSH);
            // We compute semantic similarities between concepts
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)

            URI c1 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D013812"); // female
            URI c2 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D041883"); // male


           // System.out.println("ICindexer D013812:" + engine.getIC(icConf,c1));
            //System.out.println("ICindexer D041883: " + engine.getIC(icConf,c2));

            // We compute the similarity
            //double sim = engine.compare(measureConf, c1, c2);
            //System.out.println("MeSH.Sim " + c1 + "\t" + c2 + "\t" + sim);



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

                    System.out.println("MeSH.Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
                }
            }

            t.stop();
            t.elapsedTime();


        } catch (SLIB_Exception ex) {
            Logger.getLogger(MeSH.Sim.class.getName()).log(Level.SEVERE, null, ex);

        }


    }
}

*/







