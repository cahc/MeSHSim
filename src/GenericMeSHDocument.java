import java.util.ArrayList;
import java.util.List;

public class GenericMeSHDocument {


    private String ID;
    private List<MeshDescriptorCustom> meshDescriptorCustomList = new ArrayList<>();

    public GenericMeSHDocument(String ID) { this.ID = ID;}

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericMeSHDocument that = (GenericMeSHDocument) o;

        return ID.equals(that.ID);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}
