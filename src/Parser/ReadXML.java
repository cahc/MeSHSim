package Parser;

import org.xml.sax.InputSource;

import javax.xml.stream.*;
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
        final String [] CDATA_ELEMENTS = new String[] { "ArticleTitle", "AbstractText" };


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
        List<ParsedPubMedDoc> parsedPubMedDocList = new ArrayList<>(10000);


        List<File> test = new ArrayList<>();

        test.add( files[32] );
        System.out.println( test.get(0) );
        for(File file : files) {

            InputStream xmlin = new GZIPInputStream(new FileInputStream( file ));

           // xmlin = XmlFilterUtils.cdataWrapper ( xmlin, CDATA_ELEMENTS );

            Reader reader = new BufferedReader(new InputStreamReader( xmlin ));


            XMLInputFactory xmlif = XMLInputFactory.newFactory();

/*
            xmlif.setXMLResolver(new XMLResolver() {
                @Override
                public InputSource resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {

                    System.out.println(systemID);
                    return null;
                }
            });

*/
            //xmlif.setProperty(XMLInputFactory.IS_COALESCING, true); //todo what?
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

/*
                            try {
                                parsedPubMedDoc.setTitle(xmlReader.getElementText());
                            } catch (XMLStreamException e) {

                                System.out.println(e);



                                while(true) {


                                   if( event.getEventType() == 4 ) System.out.println(event.asCharacters().getData());

                                   event = xmlReader.nextEvent();

                                   if(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("ArticleTitle")) break;

                                }


                            }

                            continue;
                        }

*/
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
                        } //journal ends


                        //get mesh
                        if (isStart && event.asStartElement().getName().getLocalPart().equals("MeshHeadingList")) {

                            //parse mesh..
                        }

                        //end of record, tidy upp!
                        if (isEnd && event.asEndElement().getName().getLocalPart().equals("PubmedArticle")) { // end of record


                            if(parsedPubMedDoc.getPubyear() == 2009) {

                                docs2009++;
                            }

                            parsedPubMedDocList.add(parsedPubMedDoc);
                           // System.out.println(parsedPubMedDoc);

                            break;
                        }


                    } //while(true) loop ends


                }//if start of a record ends


            } //has next()

            xmlReader.close();
            System.out.println("processed files: " + nrfiles);
            System.out.println("number of 2009 records found so far: " + docs2009);
            nrfiles++;

        } //loop over files done

        System.out.println("number of records (year=2009) seen: " + docs2009);
        System.out.println("total number of docs:" + docs);

        System.out.println(parsedPubMedDocList.size());
        BufferedWriter writer = new BufferedWriter( new FileWriter( new File("/Users/Cristian/pubmedTitles.txt")));
        for(ParsedPubMedDoc parsedPubMedDoc : parsedPubMedDocList) {
            writer.write(parsedPubMedDoc.getTitle());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

}
