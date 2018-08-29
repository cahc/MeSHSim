package Parser;

public class ParsedMeSHQualifier {

        /*

       <MeshHeading>
          <DescriptorName UI="D011471" MajorTopicYN="N">Prostatic Neoplasms</DescriptorName>
          <QualifierName UI="Q000000981" MajorTopicYN="N">diagnostic imaging</QualifierName>
          <QualifierName UI="Q000453" MajorTopicYN="N">epidemiology</QualifierName>
          <QualifierName UI="Q000473" MajorTopicYN="Y">pathology</QualifierName>
        </MeshHeading>

     */

    String QualifierName;
    String UI;
    boolean isMajor;


    public String getQualifierName() {
        return QualifierName;
    }

    public void setQualifierName(String qualifierName) {
        QualifierName = qualifierName;
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


    @Override
    public String toString() {
        return "QualifierName='" + QualifierName + '\'' +
                ", UI='" + UI + '\'' +
                ", isMajor=" + isMajor +
                '}';
    }
}
