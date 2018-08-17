package MeSH;

import IndexingStructures.TopNCollector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenericPubMedDoc {


    private String ID;
    private List<MeshDescriptorCustom> meshDescriptorCustomList = new ArrayList<>();
    private TopNCollector topNCollector;

    public GenericPubMedDoc(String ID) { this.ID = ID;}

    public void addTopNCollector(TopNCollector topNCollector) {

        this.topNCollector =topNCollector;

    }

    public TopNCollector getTopNCollector() {

        return this.topNCollector;
    }

    public void sortMeSHdescriptorsByIC() {

        Collections.sort(this.meshDescriptorCustomList);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public List<MeshDescriptorCustom> getMeshDescriptorCustomList() {
        return meshDescriptorCustomList;
    }

    public void addMeshDescriptorCustom(MeshDescriptorCustom meshDescriptorCustom) {

       if(meshDescriptorCustom == null) {System.out.println("Warning! Trying to add null in addMeshDescriptorCustom()"); return;}
        this.meshDescriptorCustomList.add(meshDescriptorCustom);
    }

    public void addMeshDescriptorCustom(Set<MeshDescriptorCustom> meshDescriptorCustom) {

        if(meshDescriptorCustom == null) {System.out.println("Warning! Trying to add null in addMeshDescriptorCustom()"); return;}
        this.meshDescriptorCustomList.addAll(meshDescriptorCustom);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericPubMedDoc that = (GenericPubMedDoc) o;

        return ID.equals(that.ID);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
