package Parser;

import java.util.ArrayList;
import java.util.List;

public class ParsedMeSHDescriptor {


    /*

       <MeshHeading>
          <DescriptorName UI="D011471" MajorTopicYN="N">Prostatic Neoplasms</DescriptorName>
          <QualifierName UI="Q000000981" MajorTopicYN="N">diagnostic imaging</QualifierName>
          <QualifierName UI="Q000453" MajorTopicYN="N">epidemiology</QualifierName>
          <QualifierName UI="Q000473" MajorTopicYN="Y">pathology</QualifierName>
        </MeshHeading>

     */

    String descriptorName;
    String UI;
    boolean isMajor;
    List<ParsedMeSHQualifier> qualifiers = new ArrayList<>(2);


    public String getDescriptorName() {
        return descriptorName;
    }

    public void setDescriptorName(String descriptorName) {
        this.descriptorName = descriptorName;
    }

    public String getUI() {
        return UI;
    }

    public void setUI(String UI) {
        this.UI = UI;
    }

    public boolean isMajor() {
        return isMajor;
    }

    public void setMajor(boolean major) {
        isMajor = major;
    }

    public List<ParsedMeSHQualifier> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(List<ParsedMeSHQualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public void addQualifier(ParsedMeSHQualifier parsedMeSHQualifier) {

        this.qualifiers.add(parsedMeSHQualifier);
    }

    @Override
    public String toString() {
        return "ParsedMeSHDescriptor{" +
                "descriptorName='" + descriptorName + '\'' +
                ", UI='" + UI + '\'' +
                ", isMajor=" + isMajor +
                ", qualifiers=" + qualifiers +
                '}';
    }
}
