import java.util.*;

public class TreeNodeMeshIndex {

    private HashMap<String,TreeNodeMeSH> index = new HashMap<>();


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

    public TreeNodeMeSH getCommonAnsestor(TreeNodeMeSH node1, TreeNodeMeSH node2) {

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

            System.out.println("getting: " + stringBuilder.toString());
            return index.get(stringBuilder.toString());
        }


    }
