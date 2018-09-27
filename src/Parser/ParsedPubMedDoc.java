package Parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParsedPubMedDoc implements Serializable {

    private static final long serialVersionUID = 201800001L;

    int internalID;

    String pmid;
    String doi;

    String title;
    int pubyear;
    String journal;
    StringBuilder abstractText = new StringBuilder(200);

    List<ParsedMeSHDescriptor> mesh = new ArrayList<>();

    List<String> publicationTypes = new ArrayList<>(2);
    List<String> authorLastNames = new ArrayList<>(4);

    public void addAuthorLastName(String lastName) {


        authorLastNames.add(lastName);
    }

    public List<String> getAuthorLastNames() {

        return this.authorLastNames;
    }

    public StringBuilder getAbstractText() {
        return abstractText;
    }

    public void addAbstractText(String abstractText) {
        this.abstractText.append(abstractText);
    }

    public void setMesh(List<ParsedMeSHDescriptor> mesh) {
        this.mesh = mesh;
    }

    public void setPublicationTypes(List<String> publicationTypes) {
        this.publicationTypes = publicationTypes;
    }

    public void addPublicationtype(String pubtype) {

        publicationTypes.add(pubtype);
    }

    public void addMeSHDescriptor(ParsedMeSHDescriptor descriptor) {

        mesh.add(descriptor);
    }

    public List<ParsedMeSHDescriptor> getMesh() {

        return this.mesh;
    }

    public List<String> getPublicationTypes() {

        return this.publicationTypes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;


    }

    public int getPubyear() {
        return pubyear;
    }

    public void setPubyear(int pubyear) {
        this.pubyear = pubyear;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }


    public int getInternalID() {
        return internalID;
    }

    public void setInternalID(int internalID) {
        this.internalID = internalID;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }


    @Override
    public String toString() {
        return internalID + "\t" + pmid  +"\t" + doi + "\t" + title + "\t" + pubyear + "\t" + journal + "\t" + mesh + "\t" + publicationTypes + "\t" + abstractText + "\t" + authorLastNames;

    }
}

