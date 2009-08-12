package org.linkedprocess.linkeddata;

import info.aduna.iteration.CloseableIteration;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.URIMap;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GroovyLinkedDataDemo {
    public static final URI FOAF_KNOWS = new URIImpl("http://xmlns.com/foaf/0.1/knows");
    public static final URI TIMBL = new URIImpl("http://www.w3.org/People/Berners-Lee/card#i");

    private Random random = new Random();

    public Set<Resource> getKnown(final Sail sail,
                               final Resource subject) throws SailException {
        Set<Resource> result = new HashSet<Resource>();
        SailConnection c = sail.getConnection();
        try {
            CloseableIteration<? extends Statement, SailException> iter = c.getStatements(subject, FOAF_KNOWS, null, false);
            try {
                while (iter.hasNext()) {
                    Value obj = iter.next().getObject();
                    if (obj instanceof Resource)
                    result.add((Resource) obj);
                }
            } finally {
                iter.close();
            }
            iter = c.getStatements(null, FOAF_KNOWS, subject, false);
            try {
                while (iter.hasNext()) {
                    result.add(iter.next().getSubject());
                }
            } finally {
                iter.close();
            }
        } finally {
            c.close();
        }

        return result;
    }

    public Map<Resource, Long> foafWalk(final Sail sail,
                                     final int walkers,
                                     final int steps) throws SailException {
        Map<Resource, Long> vector = new HashMap<Resource, Long>();
        
        for (int i = 0; i < walkers; i++) {
            Resource cur = TIMBL;
            for (int j = 0; j < steps; j++) {
                Long weight = vector.get(cur);
                weight = null == weight ? 1l : 1l + weight;
                vector.put(cur, weight);

                Set<Resource> candidates = getKnown(sail, cur);
                if (0 == candidates.size()) {
                    System.err.println("this shouldn't happen");
                    break;
                } else {
                    cur = (Resource) candidates.toArray()[random.nextInt(candidates.size())];
                }
            }
        }

        return vector;
    }
    
    public void simpleDemo() throws Exception {
        Ripple.initialize();

        Sail baseSail = new MemoryStore();
        baseSail.initialize();
        URIMap uriMap = new URIMap();
        Sail sail = new LinkedDataSail(baseSail, uriMap);
        sail.initialize();

        Map<Resource, Long> results = foafWalk(sail, 100, 2);
        for (Resource r : results.keySet()) {
            System.out.println("" + results.get(r) + " -- " + r);
        }

        //Set<Resource> known = getKnown(sail, TIMBL);
        //System.out.println("results:");
        //for (Value v : known) {
        //    System.out.println("    " + v);
        //}

        sail.shutDown();
    }

    public static void main(final String[] args) throws Exception {
        new GroovyLinkedDataDemo().simpleDemo();
    }
}
