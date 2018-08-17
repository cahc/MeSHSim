package MeSH;

import java.util.HashSet;
import java.util.Set;

public class MeshDescriptorCustom implements Comparable<MeshDescriptorCustom> {
    private String descriptorUI;
    private String descriptorName;
    private Set<String> treeNumberStringSet = new HashSet();
    private Set<TreeNodeMeSH> treeNodeSet = new HashSet<>();

    double probability = 0;  // freq/N

    float informationContent = 0;

    public float getInformationContent() {
        return informationContent;
    }

    public void setInformationContent(float informationContent) {
        this.informationContent = informationContent;
    }

    public void setProbability(double prob) {

        this.probability = prob;
    }

    public double getProbability() {

        return this.probability;
    }

    public Set<String> getTreeNumberStringSet() {

        return this.treeNumberStringSet;
    }

    public void addTreeNodeMeSH(TreeNodeMeSH treeNodeMeSH) {

        this.treeNodeSet.add(treeNodeMeSH);

    }

    public Set<TreeNodeMeSH> getTreeNodeSet() {

        return this.treeNodeSet;
    }

    public MeshDescriptorCustom() {
    }

    public String getDescriptorUI() {
        return this.descriptorUI;
    }

    public void setDescriptorUI(String descriptorUI) {
        this.descriptorUI = descriptorUI;
    }

    public String getDescriptorName() {
        return this.descriptorName;
    }

    public void setDescriptorName(String descriptorName) {
        this.descriptorName = descriptorName;
    }

    public void addTreeNumber(String treeNumber) {
        this.treeNumberStringSet.add(treeNumber);
    }

    public String toString() {
        String out = this.descriptorUI + "\n";
        out = out + "\t" + this.descriptorName + "\n";
        out = out + "\t" + this.informationContent +"\n";
        out = out + "\t" + this.treeNumberStringSet + "\n";
        out = out + "\t" + this.treeNodeSet +"\n";
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeshDescriptorCustom that = (MeshDescriptorCustom) o;

        if (!descriptorUI.equals(that.descriptorUI)) return false;
        return descriptorName != null ? descriptorName.equals(that.descriptorName) : that.descriptorName == null;
    }

    @Override
    public int hashCode() {
        int result = descriptorUI.hashCode();
        result = 31 * result + (descriptorName != null ? descriptorName.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(MeshDescriptorCustom other) {

        if(this.informationContent < other.getInformationContent()) return 1;
        if(this.informationContent > other.getInformationContent()) return -1;
        return 0;

    }
}
