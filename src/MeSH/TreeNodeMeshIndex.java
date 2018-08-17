package MeSH;

import java.util.*;

public class TreeNodeMeshIndex {

    HashMap<TreeNodeMeSH,MeshDescriptorCustom> treeNodeToUniqueMeSHTermMap;


    public static final class MeshTermICComparator implements Comparator<MeshDescriptorCustom>
    {
        @Override
        public int compare(MeshDescriptorCustom x, MeshDescriptorCustom y)
        {
            // Assume neither string is null. Real code should
            // probably be more robust
            // You could also just return x.length() - y.length(),
            // which would be more efficient.
            if (x.getInformationContent() < y.getInformationContent() )
            {
                return 1;
            }
            if (x.getInformationContent() > y.getInformationContent() )
            {
                return -1;
            }
            return 0;
        }
    }



    private HashMap<String,TreeNodeMeSH> index = new HashMap<>();

    public void setTreeNodeToUniqueMeSHTermMap(HashMap<TreeNodeMeSH, MeshDescriptorCustom> treeNodeToUniqueMeSHTermMap) {
        this.treeNodeToUniqueMeSHTermMap = treeNodeToUniqueMeSHTermMap;
    }

    public TreeNodeMeshIndex(Collection<TreeNodeMeSH> treeNodeMeSHList) {

        for(TreeNodeMeSH treeNodeMeSH : treeNodeMeSHList) {

            index.put(treeNodeMeSH.getTreeString(),treeNodeMeSH);

        }


    }

    public TreeNodeMeshIndex(Map<String,TreeNodeMeSH> map) {

        this.index = (HashMap)map;
    }

    public int size() {return index.size(); }
    public TreeNodeMeSH getTreeMeshNode(String string) {

        return this.index.get(string);
    }

    public TreeNodeMeSH getCommonAncestor(TreeNodeMeSH node1, TreeNodeMeSH node2) {

        //a node is an ancestor to itself

        if(!node1.getPrefix().equals(node2.getPrefix())) { return null; } // in different branches

        int mostGeneral = Math.min(node1.getNrParts(),node2.getNrParts());

        if(mostGeneral == 0) return index.get(node1.getPrefix());

        String[] node1parts = node1.getNumericalParts();
        String[] node2parts = node2.getNumericalParts();

        int j=-1;

        for(int i=0; i<mostGeneral; i++) {

            if (node1parts[i].equals(node2parts[i])) {

                j = i;
            } else {

                break;
            }
        }


        if(j==-1) return index.get(node1.getPrefix());

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(node1.getPrefix());

            for(int i=0; i<=j; i++) {

                stringBuilder.append(".").append(node1parts[i]);

            }

           // System.out.println("getting: " + stringBuilder.toString());
            return index.get(stringBuilder.toString());
        }


    public float getSimilarityBetweenTwoNodes(MeshDescriptorCustom mesh1 , MeshDescriptorCustom mesh2) {

        PriorityQueue<MeshDescriptorCustom> queue = new PriorityQueue<MeshDescriptorCustom>(10, new MeshTermICComparator());

        Set<TreeNodeMeSH> set1 = mesh1.getTreeNodeSet();

        Set<TreeNodeMeSH> set2 = mesh2.getTreeNodeSet();


        for(TreeNodeMeSH treeNodeMeSHFromSet1 : set1) {

           // System.out.println("From set1: " + treeNodeMeSHFromSet1.getTreeString() + " " + treeNodeToUniqueMeSHTermMap.get(treeNodeMeSHFromSet1 ).getDescriptorUI() );

            for(TreeNodeMeSH treeNodeMeSHFromSet2 : set2) {

               // System.out.println("comparing with set2: " + treeNodeMeSHFromSet2.getTreeString() + " " + treeNodeToUniqueMeSHTermMap.get(treeNodeMeSHFromSet2 ).getDescriptorUI() );

                TreeNodeMeSH commonAncestor =  getCommonAncestor(treeNodeMeSHFromSet1,treeNodeMeSHFromSet2 );

                if(commonAncestor == null) continue; //different branches

                 MeshDescriptorCustom meshTerm = treeNodeToUniqueMeSHTermMap.get( commonAncestor );

                queue.add(meshTerm);

               // System.out.println("common ancestor: " + meshTerm.getDescriptorName() + " " + meshTerm.getInformationContent());


            }

        }

        if(queue.size() == 0) return 0;

       // System.out.println("# candidates: " + queue.size());
       // System.out.println("best:");
        MeshDescriptorCustom max = queue.peek(); //max with respect to common anscestors IC
       // System.out.println(max.getDescriptorUI() + " " + max.getDescriptorName() + " " + max.getInformationContent());
        return max.getInformationContent();
    }


    public float getSimilarityBetweenToDocuments(List<MeshDescriptorCustom> d1, List<MeshDescriptorCustom> d2) {

        float similarity = 0;
        int n1 = d1.size();
        int n2 = d2.size();

        for(MeshDescriptorCustom meshDescriptorCustomD1 : d1) {

            double localMax = 0;

            for(MeshDescriptorCustom meshDescriptorCustomD2 : d2) {

              double sim1  = getSimilarityBetweenTwoNodes(meshDescriptorCustomD1,meshDescriptorCustomD2);

              if(sim1>localMax) localMax = sim1;
            }

            similarity+=localMax;
        }

        for(MeshDescriptorCustom meshDescriptorCustomD2 : d2) {

            double localMax = 0;

            for(MeshDescriptorCustom meshDescriptorCustomD1 : d1) {

                double sim1  = getSimilarityBetweenTwoNodes(meshDescriptorCustomD2,meshDescriptorCustomD1);

                if(sim1>localMax) localMax = sim1;

            }

            similarity+= localMax;
        }



        return similarity/(n1 + n2);
    }

}

