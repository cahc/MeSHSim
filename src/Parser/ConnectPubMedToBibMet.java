package Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by crco0001 on 8/31/2018.
 */
public class ConnectPubMedToBibMet {

    static final String newLine = "\n";
    static final String sep = "\t";


    private static final HashSet<String> problematicUTs = new HashSet<>(400);
    static {

        problematicUTs.add("000207786200033");
        problematicUTs.add("000207786200034");
        problematicUTs.add("000207862500046");
        problematicUTs.add("000270498100010");
        problematicUTs.add("000207862500076");
        problematicUTs.add("000268739500004");
        problematicUTs.add("000207862500087");
        problematicUTs.add("000266899700015");
        problematicUTs.add("000207862500099");
        problematicUTs.add("000265817100003");
        problematicUTs.add("000207862500132");
        problematicUTs.add("000269573600024");
        problematicUTs.add("000208250600005");
        problematicUTs.add("000208250700007");
        problematicUTs.add("000261851700017");
        problematicUTs.add("000261851700018");
        problematicUTs.add("000261851700029");
        problematicUTs.add("000261851700030");
        problematicUTs.add("000261952400005");
        problematicUTs.add("000270595100008");
        problematicUTs.add("000262388900034");
        problematicUTs.add("000271665700002");
        problematicUTs.add("000261685600008");
        problematicUTs.add("000262349000008");
        problematicUTs.add("000262413300019");
        problematicUTs.add("000263339100022");
        problematicUTs.add("000264306700019");
        problematicUTs.add("000265203800020");
        problematicUTs.add("000266059000022");
        problematicUTs.add("000267496600018");
        problematicUTs.add("000270051800022");
        problematicUTs.add("000270755800025");
        problematicUTs.add("000262778300002");
        problematicUTs.add("000262778300003");
        problematicUTs.add("000262821200003");
        problematicUTs.add("000262821200006");
        problematicUTs.add("000263011100002");
        problematicUTs.add("000263015900001");
        problematicUTs.add("000263502700001");
        problematicUTs.add("000265319500001");
        problematicUTs.add("000263502700002");
        problematicUTs.add("000265319500002");
        problematicUTs.add("000263844400005");
        problematicUTs.add("000268595500003");
        problematicUTs.add("000264046000007");
        problematicUTs.add("000268120000008");
        problematicUTs.add("000264050800001");
        problematicUTs.add("000264050800002");
        problematicUTs.add("000264050800003");
        problematicUTs.add("000264050800008");
        problematicUTs.add("000264050800010");
        problematicUTs.add("000262880500002");
        problematicUTs.add("000265163000001");
        problematicUTs.add("000265160200010");
        problematicUTs.add("000272768600001");
        problematicUTs.add("000265307200002");
        problematicUTs.add("000268647800005");
        problematicUTs.add("000273191100004");
        problematicUTs.add("000265736100005");
        problematicUTs.add("000267480800007");
        problematicUTs.add("000265281700067");
        problematicUTs.add("000265281700078");
        problematicUTs.add("000265582400015");
        problematicUTs.add("000266765500025");
        problematicUTs.add("000265671000005");
        problematicUTs.add("000265671000006");
        problematicUTs.add("000265671000007");
        problematicUTs.add("000207862500032");
        problematicUTs.add("000265817100010");
        problematicUTs.add("000263031600012");
        problematicUTs.add("000266130400005");
        problematicUTs.add("000266533800007");
        problematicUTs.add("000266533800008");
        problematicUTs.add("000266533800009");
        problematicUTs.add("000266533800010");
        problematicUTs.add("000266590900004");
        problematicUTs.add("000269692200001");
        problematicUTs.add("000266850700001");
        problematicUTs.add("000266865100001");
        problematicUTs.add("000268150600003");
        problematicUTs.add("000270124700014");
        problematicUTs.add("000268294100002");
        problematicUTs.add("000272163500002");
        problematicUTs.add("000268294100003");
        problematicUTs.add("000272163500003");
        problematicUTs.add("000268294100004");
        problematicUTs.add("000272163500004");
        problematicUTs.add("000268294100005");
        problematicUTs.add("000272163500005");
        problematicUTs.add("000268294100006");
        problematicUTs.add("000272163500006");
        problematicUTs.add("000268294100007");
        problematicUTs.add("000272163500007");
        problematicUTs.add("000268294100008");
        problematicUTs.add("000272163500008");
        problematicUTs.add("000268294100009");
        problematicUTs.add("000272163500009");
        problematicUTs.add("000268294100010");
        problematicUTs.add("000272163500010");
        problematicUTs.add("000268294100011");
        problematicUTs.add("000272163500011");
        problematicUTs.add("000268707200003");
        problematicUTs.add("000274583900019");
        problematicUTs.add("000262788500001");
        problematicUTs.add("000268577400010");
        problematicUTs.add("000268468100015");
        problematicUTs.add("000271570800020");
        problematicUTs.add("000268654100009");
        problematicUTs.add("000268654100010");
        problematicUTs.add("000268818700015");
        problematicUTs.add("000270200200023");
        problematicUTs.add("000271109900017");
        problematicUTs.add("000268878400007");
        problematicUTs.add("000272454900006");
        problematicUTs.add("000268898500003");
        problematicUTs.add("000268898500004");
        problematicUTs.add("000267766100002");
        problematicUTs.add("000269742500002");
        problematicUTs.add("000267766100003");
        problematicUTs.add("000269742500003");
        problematicUTs.add("000267766100004");
        problematicUTs.add("000269742500004");
        problematicUTs.add("000269853800005");
        problematicUTs.add("000269853800006");
        problematicUTs.add("000270600900025");
        problematicUTs.add("000270600900026");
        problematicUTs.add("000271546600112");
        problematicUTs.add("000271546600131");
        problematicUTs.add("000272512600002");
        problematicUTs.add("000272512600004");
        problematicUTs.add("000272512600009");
        problematicUTs.add("000272512600011");
        problematicUTs.add("000272370100011");
        problematicUTs.add("000272370100012");
        problematicUTs.add("000270325500001");
        problematicUTs.add("000273046300001");
        problematicUTs.add("000272920600011");
        problematicUTs.add("000272920600012");
        problematicUTs.add("000262085400003");
        problematicUTs.add("000273149800016");
        problematicUTs.add("000273149800017");
        problematicUTs.add("000274584000006");
        problematicUTs.add("000274584000009");
        problematicUTs.add("000275474500003");
        problematicUTs.add("000275603000004");
        problematicUTs.add("000275600500001");
        problematicUTs.add("000275601500002");
        problematicUTs.add("000275471600004");
        problematicUTs.add("000275472400007");
        problematicUTs.add("000275646400004");
        problematicUTs.add("000275646700002");
        problematicUTs.add("000282858600013");
        problematicUTs.add("000283289400009");
        problematicUTs.add("000262518100030");
        problematicUTs.add("000265266700026");
        problematicUTs.add("000263787900008");
        problematicUTs.add("000270489500013");
        problematicUTs.add("000265293100002");
        problematicUTs.add("000268210800008");
        problematicUTs.add("000268808100001");
        problematicUTs.add("000272865000007");
        problematicUTs.add("000267743300013");
        problematicUTs.add("000269674300017");
        problematicUTs.add("000262803900017");
        problematicUTs.add("000264975100021");
        problematicUTs.add("000266931200021");
        problematicUTs.add("000265747900036");
        problematicUTs.add("000265747900037");
        problematicUTs.add("000268743500003");
        problematicUTs.add("000270540800005");
        problematicUTs.add("000267010200012");
        problematicUTs.add("000267010200034");
        problematicUTs.add("000267371400007");
        problematicUTs.add("000267371500006");
        problematicUTs.add("000270436900022");
        problematicUTs.add("000270436900026");
        problematicUTs.add("000266946900001");
        problematicUTs.add("000268115200016");
        problematicUTs.add("000207862500041");
        problematicUTs.add("000265817300012");
        problematicUTs.add("000207862500136");
        problematicUTs.add("000269573600001");
        problematicUTs.add("000265847600002");
        problematicUTs.add("000267921700001");
        problematicUTs.add("000262352400037");
        problematicUTs.add("000270060800001");
        problematicUTs.add("000270455600010");
        problematicUTs.add("000278139100012");
        problematicUTs.add("000263719700037");
        problematicUTs.add("000263719700039");
        problematicUTs.add("000207819100003");
        problematicUTs.add("000263429600016");
        problematicUTs.add("000264342600013");
        problematicUTs.add("000264342600014");
        problematicUTs.add("000263699000013");
        problematicUTs.add("000265961700002");
        problematicUTs.add("000267679600007");
        problematicUTs.add("000267679600017");
        problematicUTs.add("000266381700046");
        problematicUTs.add("000266381700047");
        problematicUTs.add("000265679800001");
        problematicUTs.add("000268575500001");
        problematicUTs.add("000267595600001");
        problematicUTs.add("000268448700001");
        problematicUTs.add("000273534600001");
        problematicUTs.add("000282630200001");
        problematicUTs.add("000273838700001");
        problematicUTs.add("000282630200002");
        problematicUTs.add("000273838700002");
        problematicUTs.add("000282630200003");
        problematicUTs.add("000273332600001");
        problematicUTs.add("000273534100002");
        problematicUTs.add("000271857300002");
        problematicUTs.add("000272337800001");
        problematicUTs.add("000273005900001");
        problematicUTs.add("000273122300001");
        problematicUTs.add("000266867900001");
        problematicUTs.add("000268083100001");
        problematicUTs.add("000207819900001");
        problematicUTs.add("000271283100023");
        problematicUTs.add("000207819900002");
        problematicUTs.add("000271283100024");
        problematicUTs.add("000207819900003");
        problematicUTs.add("000271283100025");
        problematicUTs.add("000207819900004");
        problematicUTs.add("000271283100026");
        problematicUTs.add("000207819900005");
        problematicUTs.add("000271283100027");
        problematicUTs.add("000207819900006");
        problematicUTs.add("000271283100028");
        problematicUTs.add("000267073100001");
        problematicUTs.add("000267073200001");
        problematicUTs.add("000264475400003");
        problematicUTs.add("000264475800001");
        problematicUTs.add("000269074700006");
        problematicUTs.add("000272088800001");
        problematicUTs.add("000262532300001");
        problematicUTs.add("000263843300001");
        problematicUTs.add("000268118100009");
        problematicUTs.add("000268118100012");
        problematicUTs.add("000270742200001");
        problematicUTs.add("000272361100016");
        problematicUTs.add("000267431800048");
        problematicUTs.add("000270086000036");
        problematicUTs.add("000274369700008");
        problematicUTs.add("000276692500007");
        problematicUTs.add("000274369700009");
        problematicUTs.add("000276692500008");
        problematicUTs.add("000274369700010");
        problematicUTs.add("000276692500009");
        problematicUTs.add("000274369700011");
        problematicUTs.add("000276692500010");
        problematicUTs.add("000274369700012");
        problematicUTs.add("000276692500011");
        problematicUTs.add("000274369700013");
        problematicUTs.add("000276692500012");
        problematicUTs.add("000263502700003");
        problematicUTs.add("000265319500003");
        problematicUTs.add("000261974400021");
        problematicUTs.add("000268968700031");
        problematicUTs.add("000267437600010");
        problematicUTs.add("000268850800018");



    }


    public static String printThis(ParsedPubMedDoc doc, double titleSim,String UT) {
        //repeat for each descriptor - qualifier combination
        // pmid|mesh_heading|mesh_qualifier|mesh_id|qualifier_id|ismajor_d|ismajor_q
        StringBuilder stringBuilder = new StringBuilder(50);
        String PMID = doc.getPmid();
        List<ParsedMeSHDescriptor> descriptorList = doc.getMesh();


        for(int i=0; i<descriptorList.size(); i++) {

            ParsedMeSHDescriptor descriptor = descriptorList.get(i);

            int nQualifiers = descriptor.getQualifiers().size();

            if(nQualifiers == 0) {
                stringBuilder.append(UT);
                stringBuilder.append(sep);
                stringBuilder.append(PMID);
                stringBuilder.append(sep);
                stringBuilder.append(descriptor.descriptorName);
                stringBuilder.append(sep);
                stringBuilder.append(""); // no qualier
                stringBuilder.append(sep);
                stringBuilder.append(descriptor.getUI()); //meshID
                stringBuilder.append(sep);
                stringBuilder.append(""); //no qualifierID
                stringBuilder.append(sep);
                stringBuilder.append(descriptor.isMajor); //meshTerm major?
                stringBuilder.append(sep);
                stringBuilder.append(false); // no info on descriptor
                stringBuilder.append(sep);
                stringBuilder.append(doc.publicationTypes);
                stringBuilder.append(sep);
                if(doc.doi != null) {

                    String newDoi = doc.doi.trim();
                    stringBuilder.append(newDoi);
                } else {

                    stringBuilder.append(doc.doi);
                }

                stringBuilder.append(sep);
                stringBuilder.append(doc.title);
                stringBuilder.append(sep);
                stringBuilder.append(titleSim);
                stringBuilder.append(newLine);


            } else {

               List<ParsedMeSHQualifier> qualifierList = descriptor.getQualifiers();

                for(int j=0; j<nQualifiers; j++) {
                    stringBuilder.append(UT);
                    stringBuilder.append(sep);
                    stringBuilder.append(PMID);
                    stringBuilder.append(sep);
                    stringBuilder.append(descriptor.descriptorName);
                    stringBuilder.append(sep);
                    stringBuilder.append(qualifierList.get(j).QualifierName);
                    stringBuilder.append(sep);
                    stringBuilder.append(descriptor.getUI()); //meshID
                    stringBuilder.append(sep);
                    stringBuilder.append(qualifierList.get(j).getUI()); //no qualifierID
                    stringBuilder.append(sep);
                    stringBuilder.append(descriptor.isMajor); //meshTerm major?
                    stringBuilder.append(sep);
                    stringBuilder.append(qualifierList.get(j).isMajor()); // no info on descriptor
                    stringBuilder.append(sep);
                    stringBuilder.append(doc.publicationTypes);
                    stringBuilder.append(sep);

                    if(doc.doi != null) {

                        String newDoi = doc.doi.trim();
                        stringBuilder.append(newDoi);
                    } else {

                        stringBuilder.append(doc.doi);
                    }

                    stringBuilder.append(sep);
                    stringBuilder.append(doc.title);
                    stringBuilder.append(sep);
                    stringBuilder.append(titleSim);
                    stringBuilder.append(newLine);





                }


            }




        }

        return stringBuilder.toString();

    }
    public static void main(String arg[]) throws IOException {



        Persist persist = new Persist("pubmed2009v3.db");
        BufferedReader reader = new BufferedReader( new FileReader( new File("matchingResult.txt") ));



       // BufferedWriter writer = new BufferedWriter( new FileWriter(new File("E:\\RESEARCH2018\\PUBMED\\UT_TO_MESH_CC_VERSION.txt")));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("UT_TO_MESH_CC_VERSIONv3.txt"), StandardCharsets.UTF_8));

        System.out.println("Records in db:" + persist.dbSize() );

StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UT");
        stringBuilder.append(sep);
        stringBuilder.append("PMID");
        stringBuilder.append(sep);
        stringBuilder.append("MESH_HEADING");
        stringBuilder.append(sep);
        stringBuilder.append("MESH_QUALIFIER");
        stringBuilder.append(sep);
        stringBuilder.append("MESH_ID"); //meshID
        stringBuilder.append(sep);
        stringBuilder.append("QUALIFIER_ID"); //no qualifierID
        stringBuilder.append(sep);
        stringBuilder.append("HEADNING_IS_MAJOR"); //meshTerm major?
        stringBuilder.append(sep);
        stringBuilder.append("MESH_QUALIFIER_IS_MAJOR"); // no info on descriptor
        stringBuilder.append(sep);
        stringBuilder.append("PUBTYPE_IN_PUBMED");
        stringBuilder.append(sep);
        stringBuilder.append("DOI_IN_PUBMED");
        stringBuilder.append(sep);
        stringBuilder.append("TITLE_IN_PUBMED");
        stringBuilder.append(sep);
        stringBuilder.append("TITLE_SIM_PUBMED_WOS");
        stringBuilder.append(newLine);

writer.write(stringBuilder.toString());

        String line;

        int uniqueDocs = 0;
        while ( (line = reader.readLine() ) != null) {

            String parts[] = line.split("\t");

            if(parts.length < 4) continue;

            Double simValue = Double.valueOf(parts[2]);
            if( simValue < 0.9 ) continue;


            Integer idInpubmedDb = Integer.valueOf(parts[3]);
            String UT = parts[0];


          //  if(problematicUTs.contains(UT)) continue; // duplicates and stuff

            ParsedPubMedDoc doc = persist.retrieveRecord(idInpubmedDb);

            String info = printThis(doc,simValue,UT);

            if(info.length() == 0) continue; // the pubmed record dont have any meshTerms

            writer.write(info);
            uniqueDocs++;

        }

        writer.flush();
        writer.close();
        reader.close();
        System.out.println("uniqe ut matched: " + uniqueDocs);

        persist.close();

    }

}
