package MeSH;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;

public class MeSHParser {

    HashMap<String,MeshDescriptorCustom> meshDescriptorCustomMap;
    Reader reader;

    public MeSHParser(String fileNameMeshXml) throws FileNotFoundException, UnsupportedEncodingException {
        meshDescriptorCustomMap = new HashMap<>();
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameMeshXml), "UTF-8"));

    }


    public Map<String,MeshDescriptorCustom> parse() throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {

        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLEventReader xmler = xmlif.createXMLEventReader(reader);

        int foundTerms = 0;
        int level;

        while (xmler.hasNext()) {
            XMLEvent xmle = xmler.nextEvent();

            if (xmle.getEventType() == XMLEvent.START_ELEMENT && xmle.asStartElement().getName().getLocalPart().equals("DescriptorRecord")) {
                level = 1;

                MeshDescriptorCustom meshDescriptorCustom = new MeshDescriptorCustom();

                while(true) {

                    xmle = xmler.nextEvent();

                    boolean isStart =  xmle.isStartElement();
                    if(isStart) level++;
                    boolean isEnd =    xmle.isEndElement();
                    if(isEnd) level--;


                    if(isStart && level == 2 && xmle.asStartElement().getName().getLocalPart().equals("DescriptorUI")) {

                        String uniqueID = xmler.getElementText();
                        //getElemetText advance to the end-tag
                        level--;
                        meshDescriptorCustom.setDescriptorUI( uniqueID );

                        continue;

                    }

                    if(isStart && xmle.asStartElement().getName().getLocalPart().equals("TreeNumber")) {

                        String treeNumber = xmler.getElementText();
                        //getElemetText advance to the end-tag
                        level--;
                        meshDescriptorCustom.addTreeNumber( treeNumber );
                        continue;
                    }



                    if(isStart && level==2 && xmle.asStartElement().getName().getLocalPart().equals("DescriptorName")) {


                        while(true) {

                            XMLEvent event = xmler.nextEvent();

                            if(event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("String")) {

                                String descriptorName = xmler.getElementText();
                                meshDescriptorCustom.setDescriptorName( descriptorName );
                                break;
                            }


                        }


                    }


                    if (isEnd && xmle.asEndElement().getName().getLocalPart().equals("DescriptorRecord")) {
                        meshDescriptorCustomMap.put(meshDescriptorCustom.getDescriptorUI(),meshDescriptorCustom);
                        foundTerms++;
                        break;

                    }

                }



            }



        }

      //  System.out.println("terms parsed: " + foundTerms);
      //  System.out.println("terms in set: " + meshDescriptorCustomMap.size());


        return meshDescriptorCustomMap;

    }
}

