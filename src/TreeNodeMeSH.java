import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TreeNodeMeSH implements Comparable<TreeNodeMeSH> {

    private String prefix; // e.g., A1, Z1

    private String[] numberParts;

    private String treeString;

    TreeNodeMeSH parent;
    List<TreeNodeMeSH> children;


    //todo rewrite with recursion

    private void recursiveAddChildren(TreeNodeMeSH parent, List<TreeNodeMeSH> children) {
        if(null != parent.getChildren()) {
            for(TreeNodeMeSH child : parent.getChildren() ) {
                children.add(child);
                recursiveAddChildren(child, children);
            }
        }
    }


    public List<TreeNodeMeSH> getAllDescendents() {

        List<TreeNodeMeSH> children = new ArrayList<>();
        recursiveAddChildren(this, children);

        return children;
    }



    public TreeNodeMeSH(String treeString) {

        this.treeString = treeString;
        this.prefix = treeString.substring(0,3);
        String[] parts = treeString.split("\\.");
        if(parts.length == 1) {
            numberParts = new String[0]; // e.g., is a A1, Z1 etc

        } else {

            numberParts = new String[parts.length-1];

            int j=0;
            for(int i=1; i<parts.length; i++) {

                numberParts[j] = ( parts[i]  );
                j++;
            }


        }





    }

    public void setParent(TreeNodeMeSH paren) {

        this.parent = paren;
    }

    public void addChild(TreeNodeMeSH child) {

        if(children == null) {

            children = new ArrayList<>();
            children.add(child);
        } else {

            children.add(child);
        }
    }

    public List<TreeNodeMeSH> getChildren() {

        if(children == null) return Collections.emptyList();

        return this.children;
    }



    public TreeNodeMeSH getParent() {

        return this.parent;
    }

    public String getParentTreeString() {

        if(numberParts.length == 0) return null;

        return treeString.substring(0,treeString.length()-4);
    }

    public String getPrefix() {

        return prefix;
    }

    public int getNrParts() {

        return this.numberParts.length;
    }

    public String[] getNumericalParts() {

        return numberParts;
    }

    public String getTreeString() {

        return treeString;
    }

    @Override
    public String toString() {
        return "TreeNodeMeSH{" +
                "prefix='" + prefix + '\'' +
                ", numberParts=" + Arrays.toString(numberParts) +
                ", treeString='" + treeString + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNodeMeSH that = (TreeNodeMeSH) o;

        return treeString.equals(that.treeString);
    }

    @Override
    public int hashCode() {
        return treeString.hashCode();
    }

    @Override
    public int compareTo(TreeNodeMeSH o) {

        return this.treeString.compareTo(o.getTreeString());
    }

    public static void main(String[] arg) {


    }



}
