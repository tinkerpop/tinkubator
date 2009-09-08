package org.linkedprocess.linkeddata;

import net.fortytwo.linkeddata.WebClosure;
import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.URIMap;
import net.fortytwo.ripple.rdf.RDFNullSink;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @version LoPSideD 0.1
 */
public class SingleUriResolution {

    private static List<URI> uris = new ArrayList<URI>();


    static {
        uris.add(new URIImpl("http://lanl.linkedprocess.org:8182/ns/dbpedia.org/resource/Michael_Jackson"));
        uris.add(new URIImpl("http://lanl.linkedprocess.org:8182/ns/dbpedia.org/resource/Grateful_Dead"));
        uris.add(new URIImpl("http://lanl.linkedprocess.org:8182/ns/dbpedia.org/resource/The_Beatles"));
        uris.add(new URIImpl("http://lanl.linkedprocess.org:8182/ns/dbpedia.org/resource/Led_Zeppelin"));
    }

    public static void doBuckshotExperiment(int numberOfRequests) throws Exception {
        Ripple.initialize();
        Sail memoryStore = new MemoryStore();
        memoryStore.initialize();
        LinkedDataSail sail = new LinkedDataSail(memoryStore, new URIMap());
        sail.initialize();
        WebClosure wc = sail.getWebClosure();
        sail.shutDown();
        memoryStore.shutDown();

        Random random = new Random();
        System.out.println("doing buckshot experiment for " + numberOfRequests + " uris...");
        for (int i = 0; i < numberOfRequests; i++) {
            wc.extend(SingleUriResolution.uris.get(random.nextInt(uris.size())), new RDFNullSink<RippleException>());
            wc.getMemos().clear();
        }
    }

    public static double average(List<Long> times) {
        double totalTime = 0.0d;
        for(long time : times) {
            totalTime = totalTime + time;
        }
        return totalTime / (double)times.size();
    }

    public static List<Long> resolveUri() throws Exception {
        Ripple.initialize();
        Sail memoryStore = new MemoryStore();
        memoryStore.initialize();
        LinkedDataSail sail = new LinkedDataSail(memoryStore, new URIMap());
        sail.initialize();
        WebClosure wc = sail.getWebClosure();
        sail.shutDown();
        memoryStore.shutDown();
        List<Long> times = new ArrayList<Long>();
        for (URI uri : uris) {
            long startTime = System.currentTimeMillis();
            wc.extend(uri, new RDFNullSink<RippleException>());
            times.add(System.currentTimeMillis() - startTime);
            wc.getMemos().clear(); 
        }
        return times;
    }

    public static double doOneshotExperiment() throws Exception {
        return average(resolveUri());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(average(resolveUri()));
    }
}
