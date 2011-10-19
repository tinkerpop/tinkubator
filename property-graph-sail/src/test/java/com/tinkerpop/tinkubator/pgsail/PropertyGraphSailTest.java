package com.tinkerpop.tinkubator.pgsail;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReader;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class PropertyGraphSailTest {
    @Test
    public void testRDFDump() throws Exception {
        Graph g = new TinkerGraph();
        GraphMLReader r = new GraphMLReader(g);
        r.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));

        PropertyGraphSail sail = new PropertyGraphSail(g);
        sail.initialize();

        Repository repo = new SailRepository(sail);
        RepositoryConnection rc = repo.getConnection();
        try {
            RDFWriter w = Rio.createWriter(RDFFormat.TURTLE, System.out);
            rc.export(w);
        } finally {
            rc.close();
        }
    }
}
