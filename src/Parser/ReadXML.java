package Parser;

import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class ReadXML {

    public static void main(String[] arg) throws IOException, XMLStreamException {

        Pattern pattern = Pattern.compile("\\d{4}");

        /** The XML content of these elements is wrapped with CDATA blocks, to avoid XML parser problems */

      // final String [] CDATA_ELEMENTS = new String[] { "ArticleTitle", "AbstractText" };


        File f = new File("/Users/Cristian/Downloads/baseline"); // current directory

        FilenameFilter textFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".gz") && lowercaseName.startsWith("pubmed")) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] files = f.listFiles(textFilter);
        for (File file : files) {

            // System.out.println(file.getCanonicalPath());
        }

        System.out.println(files.length + " compressed xml-files found. Parsing..");
        int docs = 0;
        int docs2009 = 0;
        int nrfiles = 1;
        int internalID = 1;
        //List<ParsedPubMedDoc> parsedPubMedDocList = new ArrayList<>(10000);

        Persist persist = new Persist("pubmed2009.db");

        List<File> test = new ArrayList<>();

        test.add( files[32] );
        System.out.println( test.get(0) );
        for(File file : files) {

            InputStream xmlin = new GZIPInputStream(new FileInputStream( file ));

           // xmlin = XmlFilterUtils.cdataWrapper ( xmlin, CDATA_ELEMENTS );

            Reader reader = new BufferedReader(new InputStreamReader( xmlin ));

            XMLInputFactory xmlif = XMLInputFactory.newFactory();

            //xmlif.setProperty(XMLInputFactory.IS_COALESCING, true);
            XMLEventReader xmlReader = xmlif.createXMLEventReader(reader);


            wrongYear:
            while (xmlReader.hasNext()) {

                XMLEvent event = xmlReader.nextEvent();


                if (event.getEventType() == XMLEvent.START_ELEMENT && event.asStartElement().getName().getLocalPart().equals("PubmedArticle")) {


                    ParsedPubMedDoc parsedPubMedDoc = new ParsedPubMedDoc();
                    docs++;

                    while (true) {

                        event = xmlReader.nextEvent();

                        boolean isStart = event.isStartElement();
                        boolean isEnd = event.isEndElement();

                        //get the title, unescaped html stuff so we do like this..
                        if (isStart && event.asStartElement().getName().getLocalPart().equals("ArticleTitle")) {
                            StringBuilder stringBuilder = new StringBuilder(30);

                            while (true) {
                                event = xmlReader.nextEvent();

                                if (event.getEventType() == 4) {

                                    stringBuilder.append(event.asCharacters().getData());
                                }


                                if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("ArticleTitle"))
                                    break;

                            }

                            parsedPubMedDoc.setTitle( stringBuilder.toString() );

                            continue;
                        }




                        //get pmid & doi
                        if(isStart && event.asStartElement().getName().getLocalPart().equals("ArticleIdList")) {

                            while(true) {

                                event = xmlReader.nextEvent();
                                isStart = event.isStartElement();
                                isEnd = event.isEndElement();

                                if(isStart && event.asStartElement().getName().getLocalPart().equals("ArticleId")) {

                                   Attribute attribute =  event.asStartElement().getAttributeByName( new QName("IdType"));

                                   if("pubmed".equals(attribute.getValue())) parsedPubMedDoc.setPmid( xmlReader.getElementText() );



                                    if("doi".equals(attribute.getValue())) parsedPubMedDoc.setDoi( xmlReader.getElementText() );


                                }

                                if(isEnd && event.asEndElement().getName().getLocalPart().equals("ArticleIdList")) break;

                            }

                            continue;
                        }


                        if(isStart && event.asStartElement().getName().getLocalPart().equals("MeshHeadingList")) {


                            while(true) {

                                event = xmlReader.nextEvent();
                                isStart = event.isStartElement();
                                isEnd = event.isEndElement();


                                if(isStart && event.asStartElement().getName().getLocalPart().equals("MeshHeading")) {

                                    /*

                                     <DescriptorName UI="D011471" MajorTopicYN="N">Prostatic Neoplasms</DescriptorName>
                                     <QualifierName UI="Q000000981" MajorTopicYN="N">diagnostic imaging</QualifierName>
                                      <QualifierName UI="Q000453" MajorTopicYN="N">epidemiology</QualifierName>
                                       <QualifierName UI="Q000473" MajorTopicYN="Y">pathology</QualifierName>
                                     */
                                             ParsedMeSHDescriptor parsedMeSHDescriptor = new ParsedMeSHDescriptor();

                                             while (true) {
                                            event = xmlReader.nextEvent();
                                            isStart = event.isStartElement();
                                            isEnd = event.isEndElement();


                                            if(isStart) {

                                              StartElement startElement = event.asStartElement();

                                              if(startElement.getName().getLocalPart().equals("DescriptorName")) {

                                                  //do this
                                                  String UI = startElement.getAttributeByName(new QName("UI")).getValue();
                                                  String isMajor =  startElement.getAttributeByName(new QName("MajorTopicYN")).getValue();
                                                  String name = xmlReader.getElementText();

                                                  parsedMeSHDescriptor.setDescriptorName( name );
                                                  parsedMeSHDescriptor.setUI(UI);
                                                  parsedMeSHDescriptor.setMajor( isMajor.equalsIgnoreCase("Y") );

                                              }

                                              if(startElement.getName().getLocalPart().equals("QualifierName")) {

                                                  //do this
                                                  String UI = startElement.getAttributeByName(new QName("UI")).getValue();
                                                  String isMajor =  startElement.getAttributeByName(new QName("MajorTopicYN")).getValue();
                                                  String name = xmlReader.getElementText();

                                                  ParsedMeSHQualifier qualifier = new ParsedMeSHQualifier();
                                                  qualifier.setMajor(isMajor.equalsIgnoreCase("Y"));
                                                  qualifier.setQualifierName(name);
                                                  qualifier.setUI(UI);

                                                  parsedMeSHDescriptor.addQualifier(qualifier);


                                              }



                                            }



                                    if(isEnd && event.asEndElement().getName().getLocalPart().equals("MeshHeading")) {
                                        parsedPubMedDoc.addMeSHDescriptor(parsedMeSHDescriptor);
                                        break;
                                    }

                                }

                                continue;

                                }



                                if(isEnd && event.asEndElement().getName().getLocalPart().equals("MeshHeadingList")) break;

                            }


                            continue;
                        }


                        //get journal and pubyear
                        if (isStart && event.asStartElement().getName().getLocalPart().equals("Journal")) {

                            while (true) {

                                event = xmlReader.nextEvent();
                                isStart = event.isStartElement();
                                isEnd = event.isEndElement();
                                if (isStart && event.asStartElement().getName().getLocalPart().equals("Year")) {

                                    Integer year = Integer.valueOf(xmlReader.getElementText());
                                    if (year != null) {
                                        parsedPubMedDoc.setPubyear(year);

                                      if (parsedPubMedDoc.getPubyear() != 2009) continue wrongYear;
                                     //if(parsedPubMedDoc.getPubyear()==2009) System.out.println(year + " from Year");

                                    }
                                }

                                if (isStart && event.asStartElement().getName().getLocalPart().equals("Title")) {

                                    parsedPubMedDoc.setJournal(xmlReader.getElementText());

                                }


                                if (isStart && event.asStartElement().getName().getLocalPart().equals("MedlineDate")) {

                                    String date = xmlReader.getElementText();

                                    Matcher matcher = pattern.matcher(date);

                                    if (matcher.find()) {

                                        Integer year = Integer.valueOf(matcher.group(0));
                                        parsedPubMedDoc.setPubyear(year);
                                     if (parsedPubMedDoc.getPubyear() != 2009) continue wrongYear;
                                     //  if(parsedPubMedDoc.getPubyear()==2009) System.out.println(year + " from MedLineDate");

                                    }

                                }


                                if (isEnd && event.asEndElement().getName().getLocalPart().equals("Journal")) {

                                    break;
                                }

                            }
                            continue;
                        } //journal ends


                        //get pubtype

                         //<PublicationTypeList>
                        //<PublicationType UI="D016428">Journal Article</PublicationType>
                        //</PublicationTypeList>

                        if (isStart && event.asStartElement().getName().getLocalPart().equals("PublicationTypeList")) {



                           while(true) {
                               event = xmlReader.nextEvent();
                               isStart = event.isStartElement();
                               isEnd = event.isEndElement();

                               if(isStart && event.asStartElement().getName().getLocalPart().equals("PublicationType")) {

                                   parsedPubMedDoc.addPublicationtype( xmlReader.getElementText() );
                               }


                               if(isEnd && event.asEndElement().getName().getLocalPart().equals("PublicationTypeList")) break;
                           }

                           continue;
                        }



                        //<Abstract>
                        //<AbstractText Label="BACKGROUND" NlmCategory="BACKGROUND">Various skin diseases are commonly observed in diabetic patients. Typical biophysical properties of diabetic skin such as lower skin elasticity, decreased water content in stratum corneum, increased itching and sweating disturbances are reported. The aim of the study was to examine the distribution and intensity of skin pigmentation in diabetic patients in correlation with the metabolic control and with presence of microangiopathy.</AbstractText>
                        //<AbstractText Label="MATERIAL AND METHODS" NlmCategory="METHODS">The study was conducted on 105 patients (42 men and 63 women, median age 31), with type 1 diabetes (DM1). The control group of 53 healthy individuals (22 men and 31 women) was age- and sex-matched. Skin pigmentation was measured at 3 different locations of the body (cheek, dorsal surface of a forearm and dorsal surface of a foot) using Mexameter® MX 18. We calculated melanin index (MI) by the meter from the intensities of absorbed and reflected light at 880 nm.</AbstractText>
                        //<AbstractText Label="RESULTS" NlmCategory="RESULTS">Patients with DM1 had lower MI on the foot (173.2 ± 38.8 vs. 193.4 ± 52.7, p=0.016) as compared to controls. In the univariate analysis cheek MI was negatively related to HbA1c level (β=-4.53, p=0.01). Forearm MI was negatively associated with daily insulin dose (β=-0.58, p=0.01), BMI (β=-3.02, p&lt;0.001), waist circumference (β=-0.75, p=0.009), serum TG concentration (β=-18.47, p&lt;0.001) and positively with HDL cholesterol level (β=15.76, p=0.02). Diabetic patients with hypertension had lower foot MI values (β=-18.28, p=0.03). Lower MI was associated with the presence of diabetic neuropathy (β=-18.67, p=0.04) and retinopathy (β=-17.47, p=0.03).</AbstractText>
                        //<AbstractText Label="CONCLUSIONS" NlmCategory="CONCLUSIONS">In conclusion, there seems to be loss of melanocytes in type 1 diabetes. The melanin content is related to glycemic control of diabetes and obesity. The lower melanin content the higher possibility of microangiopathy. This is a first report in the literature devoted to distribution of melanin in the skin of type 1 diabetic patients.</AbstractText>
                        //<CopyrightInformation>© J. A. Barth Verlag in Georg Thieme Verlag KG Stuttgart · New York.</CopyrightInformation>
                        // </Abstract>


                        if(isStart && event.asStartElement().getName().getLocalPart().equals("AbstractText")) {

                            StringBuilder stringBuilder = new StringBuilder(200);




                                while (true) {
                                    event = xmlReader.nextEvent();


                                    if (event.getEventType() == 4) {

                                        stringBuilder.append(event.asCharacters().getData());
                                    }


                                    if (event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("AbstractText")) break;

                                }


                          if(parsedPubMedDoc.getAbstractText().length() == 0)  { parsedPubMedDoc.addAbstractText( stringBuilder.toString() ); } else {

                              parsedPubMedDoc.addAbstractText(" ");
                              parsedPubMedDoc.addAbstractText( stringBuilder.toString() );
                          }



                            continue;

                        }


                        //end of record, tidy upp!
                        if (isEnd && event.asEndElement().getName().getLocalPart().equals("PubmedArticle")) { // end of record


                            if(parsedPubMedDoc.getPubyear() == 2009) {
                                parsedPubMedDoc.setInternalID(internalID);
                                internalID++;
                                persist.saveRecord(parsedPubMedDoc.getInternalID(), parsedPubMedDoc);
                                docs2009++;
                            }

                            break;
                        }


                    } //while(true) loop ends


                }//if start of a record ends


            } //has next()

            xmlReader.close();
            System.out.println("processed files: " + nrfiles);
            System.out.println("number of 2009 records found so far: " + docs2009 + " total seen: " + docs);
            nrfiles++;

        } //loop over files done

        System.out.println("number of records (year=2009) seen: " + docs2009);
        System.out.println("total number of docs:" + docs);

       // System.out.println(parsedPubMedDocList.size());
      // BufferedWriter writer = new BufferedWriter( new FileWriter( new File("/Users/Cristian/test.txt")));
      // Persist persist = new Persist("pubmedTest.db");

        //System.out.println("Writing to database");
       // for(ParsedPubMedDoc parsedPubMedDoc : parsedPubMedDocList) {

        //    persist.saveRecord(parsedPubMedDoc.getInternalID(), parsedPubMedDoc);
       // }


        persist.forceCommit();
        persist.close();

        //writer.flush();
        //writer.close();
    }

}
