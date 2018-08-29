package Parser;

import java.io.Serializable;

public class ParsedPubMedDoc implements Serializable {

    int internalID;

    String title;
    int pubyear;
    String journal;

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


    @Override
    public String toString() {
        return "ParsedPubMedDoc{" +
                "title='" + title + '\'' +
                ", pubyear=" + pubyear +
                ", journal='" + journal + '\'' +
                '}';
    }
}
