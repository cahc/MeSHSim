import java.util.HashSet;

public class CheckTags {
    /*
Young Adult	D055815	age 19-24; IM only as social, political, psychological entity; NIM as check tag
Rats	D051381	check tag: NIM no qualifiers for RATS, the genus RATTUS unspecified, or any RATTUS species
Rabbits	D011817	check tag: NIM no qualifiers; do not confuse entry term BELGIAN HARE with HARES
Pregnancy	D011247	IM for articles on normal pregnancy, NIM as check tag; in children or adolescents, see note under PREGNANCY IN ADOLESCENCE; surrogate pregnancy = SURROGATE MOTHERS; for "pregnancy" in non-placental organisms (fish, reptiles, monotremes for example): use OVIPARITY; OVOVIVIPARITY; or VIVIPARITY, NONMAMMALIAN
Middle Aged	D008875	age 45-64; IM as psychol, sociol entity: Manual 18.5.12; NIM as check tag; Manual 34.10 for indexing examples
Mice	D051379	check tag: NIM no qualifiers for MICE, the genus MUS unspecified, or any MUS species
Male	D008297	check tag only for male organs, diseases, physiol processes, genetics, etc.; do not confuse with MEN as a social, cultural, political, economic force; CATALOGER: Do not use
Infant, Newborn	D007231	usually check tag: NIM no qualifiers; IM when a healthy neonate is the point of the article; see Manual Chapter 9
Infant	D007223	almost always check tag: NIM no qualifiers; see Manual Chapter 9
Humans	D006801	NIM as check tag only; Manual 18.8+; Homo sp. other than Homo sapiens are indexed HOMO see HOMINIDAE
History, Medieval	D049691	IM general only; NIM as check tag
History, Ancient	D049690	before 500 AD; IM general only; NIM as check tag; note ANCIENT LANDS in Category Z
History, 21st Century	D049674	IM general only; NIM as check tag
History, 20th Century	D049673	IM general only; NIM as check tag
History, 19th Century	D049672	IM general only; NIM as check tag
History, 18th Century	D049671	IM general only; NIM as check tag
History, 17th Century	D049670	IM general only; NIM as check tag
History, 16th Century	D049669	IM general only; NIM as check tag
History, 15th Century	D049668	IM general only; NIM as check tag
Guinea Pigs	D006168	check tag: no qualif
Female	D005260	check tag only for female organs, diseases, physiologic processes, genetics, etc.; do not confuse with WOMEN as a social, cultural, political, economic force; CATALOGER: Do not use
Dogs	D004285	check tag: no qualif; when IM, qualif permitted; TN 3: spelling of breeds
Child, Preschool	D002675	almost always check tag: NIM no qualifiers; see Manual Chapter 9
Child	D002648	almost always check tag: NIM no qualifiers; see Manual Chapter 9
Cats	D002415	check tag: no qualifiers; restrict to domestic cat
Animals	D000818	NIM as check tag; Manual 18.7+; relation to /vet: Manual 19.8.81
Aged, 80 and over	D000369	almost always check tag: NIM no qualifiers; see Manual Chapter 9
Aged	D000368	almost always check tag: NIM no qualifiers; see Manual Chapter 9; differentiate from AGING, a physiological process, & AGE FACTORS & AGE DISTRIBUTION, statistical concepts
Adult	D000328	almost always check tag: NIM no qualifiers; see Manual Chapter 9
Adolescent	D000293	almost always check tag: NIM no qualifiers; see Manual Chapter 9
     */


    private final static HashSet<String> checkTags = new HashSet();

    static {

        checkTags.add("D055815"); //Young Adult
        checkTags.add("D051381"); //Rats
        checkTags.add("D011817"); //Rabbits
        checkTags.add("D011247"); //Pregnancy
        checkTags.add("D008875"); //Middle Aged
        checkTags.add("D051379"); //Mice
        checkTags.add("D008297"); //Male
        checkTags.add("D007231"); //Infant, Newborn
        checkTags.add("D007223"); //Infant
        checkTags.add("D006801"); //Humans
        checkTags.add("D049691"); //History, Medieval
        checkTags.add("D049690"); //History, Ancient
        checkTags.add("D049674"); //History, 21st Century
        checkTags.add("D049673"); //History, 20th Century
        checkTags.add("D049672"); //History, 19th Century
        checkTags.add("D049671"); //History, 18th Century
        checkTags.add("D049670"); //History, 17th Century
        checkTags.add("D049669"); //History, 16th Century
        checkTags.add("D049668"); //History, 15th Century
        checkTags.add("D006168"); //Guinea Pigs
        checkTags.add("D005260"); //Female
        checkTags.add("D004285"); //Dogs
        checkTags.add("D002675"); //Child, Preschool
        checkTags.add("D002648"); //Child
        checkTags.add("D002415"); //Cats
        checkTags.add("D000818"); //Animals
        checkTags.add("D000369"); //Aged, 80 and over
        checkTags.add("D000368"); //Aged
        checkTags.add("D000328"); //Adult
        checkTags.add("D000293"); //Adolescent

    }


    public boolean isAcheckTag(String meshID) {

        return checkTags.contains(meshID);
    }


}
