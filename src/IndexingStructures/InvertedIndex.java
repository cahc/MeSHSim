package IndexingStructures;

import MeSH.GenericPubMedDoc;
import MeSH.MeshDescriptorCustom;

import java.util.*;

public class InvertedIndex {

    HashMap<MeshDescriptorCustom, List<GenericPubMedDoc>> invertedIndex = new HashMap<>(3500);

    //TODO auto index decendants

    public void addDescriptorDocumentPair(MeshDescriptorCustom descriptorCustom, GenericPubMedDoc genericPubMedDoc) {

        List<GenericPubMedDoc> genericPubMedDocList = invertedIndex.get(descriptorCustom);

        if (genericPubMedDocList == null) {

            genericPubMedDocList = new ArrayList<GenericPubMedDoc>(15);
            genericPubMedDocList.add(genericPubMedDoc);
            invertedIndex.put(descriptorCustom, genericPubMedDocList);

        } else {

            genericPubMedDocList.add(genericPubMedDoc);
        }

    }


    public List<GenericPubMedDoc> getPosingList(MeshDescriptorCustom meshDescriptorCustom) {

        List<GenericPubMedDoc> list = invertedIndex.get(meshDescriptorCustom);

        if (list == null) return Collections.emptyList();

        return list;
    }

    public int getIndexSize() {

        return this.invertedIndex.size();
    }


    public Set<GenericPubMedDoc> getAllMatchingDocuemnts(GenericPubMedDoc genericPubMedDoc) {

        HashSet<GenericPubMedDoc> setOfMatchingDocs = new HashSet<>(30);
        for (MeshDescriptorCustom term : genericPubMedDoc.getMeshDescriptorCustomList()) {


            setOfMatchingDocs.addAll(getPosingList(term));

        }


        return setOfMatchingDocs;
    }

}