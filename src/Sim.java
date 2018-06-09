import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_BestMatchAverage;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 * Created by crco0001 on 6/4/2018.
 */
public class Sim {

    public static void removeMeshCycles(G meshGraph) throws SLIB_Ex_Critic {
        URIFactory factory = URIFactoryMemory.getSingleton();

        // We remove the edges creating cycles
        URI ethicsURI = factory.getURI("http://www.nlm.nih.gov/mesh/D004989");
        URI moralsURI = factory.getURI("http://www.nlm.nih.gov/mesh/D009014");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> moralsEdges = meshGraph.getE(RDFS.SUBCLASSOF, moralsURI, Direction.OUT);
        for (E e : moralsEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(ethicsURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }

        ValidatorDAG validatorDAG = new ValidatorDAG();
        boolean isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

        System.out.println("MeSH Graph is a DAG: " + isDAG);

        // We remove the edges creating cycles
        // see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh

        URI hydroxybutyratesURI = factory.getURI("http://www.nlm.nih.gov/mesh/D006885");
        URI hydroxybutyricAcidURI = factory.getURI("http://www.nlm.nih.gov/mesh/D020155");

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> hydroxybutyricAcidEdges = meshGraph.getE(RDFS.SUBCLASSOF, hydroxybutyricAcidURI, Direction.OUT);
        for (E e : hydroxybutyricAcidEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(hydroxybutyratesURI)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);
            }
        }

    }


    public static void main(String[] arg) throws SLIB_Exception {

        try {

            Timer t = new Timer();
            t.start();

            URIFactory factory = URIFactoryMemory.getSingleton();
            URI meshURI = factory.getURI("http://www.nlm.nih.gov/mesh/");

            G meshGraph = new GraphMemory(meshURI);

            GDataConf dataMeshXML = new GDataConf(GFormat.MESH_XML, "F:\\mesh\\desc2018.xml"); // the DTD must be located in the same directory
            GraphLoaderGeneric.populate(dataMeshXML, meshGraph);

            System.out.println(meshGraph);

            /*
             * We remove the cycles of the graph in order to obtain
             * a rooted directed acyclic graph (DAG) and therefore be able to
             * use most of semantic similarity measures.
             * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh
             */

            // We check the graph is a DAG: answer NO
            ValidatorDAG validatorDAG = new ValidatorDAG();
            boolean isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

            System.out.println("MeSH Graph is a DAG: " + isDAG);

            // We remove the cycles
            Sim.removeMeshCycles(meshGraph);

            isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

            // We check the graph is a DAG: answer Yes
            System.out.println("MeSH Graph is a DAG: " + isDAG);

            /*
             * Now we can compute Semantic Similarities between pairs vertices
             */

            // we first configure a pairwise measure
            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011);

            SMconf measureConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);

            SMconf groupConf = new SMconf(SMConstants.FLAG_SIM_GROUPWISE_BMA);

            Sim_groupwise_BestMatchAverage bma =  new slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_BestMatchAverage();


            // We define the semantic measure engine to use
            SM_Engine engine = new SM_Engine(meshGraph);





            // We compute semantic similarities between concepts
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)

            URI c1 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D010259"); // Paranoid Disorders //
            URI c2 = factory.getURI("http://www.nlm.nih.gov/mesh/", "D012563"); // Schizophrenia, Paranoid


            // We compute the similarity
            double sim = engine.compare(measureConf, c1, c2);
            System.out.println("Sim " + c1 + "\t" + c2 + "\t" + sim);



            Set<URI> set1 = new HashSet<URI>();
            set1.add(c1);

            Set<URI> set2 = new HashSet<URI>();
            set2.add(c2);
            set2.add(c1);

            System.out.println("BMA: " +  engine.compare(groupConf,measureConf,set1,set2) );



            System.out.println(meshGraph.toString());

            System.exit(0);


            /*
             * The computation of the first similarity is not very fast because
             * the engine compute extra informations which are cached for next computations.
             * Lets compute 10 000 000 random pairwise similarities
             */
            int totalComparison = 10000000;
            List<URI> concepts = new ArrayList<URI>(meshGraph.getV());
            int id1, id2;
            String idC1, idC2;
            Random r = new Random();

            for (int i = 0; i < totalComparison; i++) {
                id1 = r.nextInt(concepts.size());
                id2 = r.nextInt(concepts.size());

                c1 = concepts.get(id1);
                c2 = concepts.get(id2);

                sim = engine.compare(measureConf, c1, c2);

                if ((i + 1) % 50000 == 0) {
                    idC1 = c1.getLocalName();
                    idC2 = c2.getLocalName();

                    System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
                }
            }

            t.stop();
            t.elapsedTime();


        } catch (SLIB_Exception ex) {
            Logger.getLogger(Sim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}






