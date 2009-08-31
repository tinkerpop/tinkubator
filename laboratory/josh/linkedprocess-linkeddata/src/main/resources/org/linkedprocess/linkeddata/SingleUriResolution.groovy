import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedList;

import net.fortytwo.linkeddata.sail.LinkedDataSail;
import net.fortytwo.linkeddata.WebClosure;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.URIMap;
import net.fortytwo.ripple.rdf.RDFNullSink;

public class SingleUriResolution {

    private static List<URI> uris = new ArrayList<URI>();

    static {
        uris.add(new URIImpl("http://www.w3.org/People/Berners-Lee/card#i"));
        uris.add(new URIImpl("http://dig.csail.mit.edu/2007/wiki/people/RobertHoffmann#RMH"));
        uris.add(new URIImpl("http://www.w3.org/People/djweitzner/foaf#DJW"));
        uris.add(new URIImpl("http://www.cs.umd.edu/~hendler/2003/foaf.rdf#jhendler"));
        uris.add(new URIImpl("http://www.kjetil.kjernsmo.net/foaf#me"));
        uris.add(new URIImpl("http://dig.csail.mit.edu/People/yosi#YES"));
        uris.add(new URIImpl("http://swordfish.rdfweb.org/people/libby/rdfweb/webwho.xrdf#me"));
        uris.add(new URIImpl("http://people.csail.mit.edu/psz/foaf.rdf#me"));
        uris.add(new URIImpl("http://www.w3.org/People/Berners-Lee/card#amy"));
        uris.add(new URIImpl("http://hometown.aol.com/chbussler/foaf/chbussler.foaf#me"));
        uris.add(new URIImpl("http://www.w3.org/People/Berners-Lee/card#cm"));
        uris.add(new URIImpl("http://dig.csail.mit.edu/2007/wiki/people/JoeLambda#JL"));
        uris.add(new URIImpl("http://id.ecs.soton.ac.uk/person/1269"));
        uris.add(new URIImpl("http://heddley.com/edd/foaf.rdf#edd"));
        uris.add(new URIImpl("http://my.opera.com/howcome/xml/foaf#howcome"));
        uris.add(new URIImpl("http://id.ecs.soton.ac.uk/person/2686"));
        uris.add(new URIImpl("http://qdos.com/people/tom.xrdf#me"));
        uris.add(new URIImpl("http://dig.csail.mit.edu/People/RRS"));
        uris.add(new URIImpl("http://people.w3.org/simon/foaf#i"));
        uris.add(new URIImpl("http://id.ecs.soton.ac.uk/person/60"));
        uris.add(new URIImpl("http://bblfish.net/people/henry/card#me"));
        uris.add(new URIImpl("http://norman.walsh.name/knows/who#norman-walsh"));
        uris.add(new URIImpl("http://dbpedia.org/resource/Tim_Bray"));
        uris.add(new URIImpl("http://teole.jfouffa.org/People/Teole/card.rdf#me"));
        uris.add(new URIImpl("http://www.w3.org/People/Jacobs/contact.rdf#IanJacobs"));
        uris.add(new URIImpl("http://dbpedia.org/resource/James_Martin_%28author%29"));
        uris.add(new URIImpl("http://www.mindswap.org/2004/owl/mindswappers#Bijan.Parsia"));
        uris.add(new URIImpl("http://people.apache.org/~oshani/foaf.rdf#me"));
        uris.add(new URIImpl("http://www.dajobe.org/foaf.rdf#i"));
        uris.add(new URIImpl("http://www.cambridgesemantics.com/people/lee"));
        uris.add(new URIImpl("http://www.w3.org/People/Connolly/#me"));
        uris.add(new URIImpl("http://users.ecs.soton.ac.uk/mc/mcfoaf.rdf#me"));
        uris.add(new URIImpl("http://www.w3.org/People/EM/contact#me"));
        uris.add(new URIImpl("http://www.w3.org/People/karl/karl-foaf.xrdf#me"));
        uris.add(new URIImpl("http://people.csail.mit.edu/lkagal/foaf.rdf#me"));
        uris.add(new URIImpl("http://people.csail.mit.edu/crowell/foaf.rdf#crowell"));
        uris.add(new URIImpl("http://www.mindswap.org/2004/owl/mindswappers#Jennifer.Golbeck"));
        uris.add(new URIImpl("http://www.isi.edu/~gil/foaf.rdf#me"));
        uris.add(new URIImpl("http://www.aaronsw.com/about.xrdf#aaronsw"));
        uris.add(new URIImpl("http://people.csail.mit.edu/lkagal/foaf#me"));
        uris.add(new URIImpl("http://www.w3.org/People/Berners-Lee/card#dj"));
        uris.add(new URIImpl("http://rit.mellon.org/Members/ihf/foaf.rdf#me"));
        uris.add(new URIImpl("http://people.csail.mit.edu/ryanlee/about#ryanlee"));
        uris.add(new URIImpl("http://www.lassila.org/ora.rdf#me"));
        uris.add(new URIImpl("http://dbpedia.org/resource/John_Klensin"));
        uris.add(new URIImpl("http://dbpedia.org/resource/John_Gage"));
        uris.add(new URIImpl("http://myopenlink.net/dataspace/person/kidehen#this"));
    }

    public static long resolveSingleUri(URI uri) throws Exception {
        Ripple.initialize();
        Sail memoryStore = new MemoryStore();
        memoryStore.initialize();
        LinkedDataSail sail = new LinkedDataSail(memoryStore, new URIMap());
        sail.initialize();
        WebClosure wc = sail.getWebClosure();

        long startTime = System.currentTimeMillis();
        wc.extend(uri, new RDFNullSink<RippleException>());
        return System.currentTimeMillis() - startTime;
    }

    public static double averageTime(List<Long> times) {
        double averageTime = 0.0d;
        for(Long t : times) {
            averageTime = averageTime + t;
        }
        return averageTime / (double)times.size();
    }

    public static List<Long> doBuckshotExperiment(int numberOfRequests) throws Exception {
        Random random = new Random();
        List<Long> times = new LinkedList();
        for (int i = 0; i < numberOfRequests; i++) {
            URI u = SingleUriResolution.uris.get(random.nextInt(SingleUriResolution.uris.size()));
            Long t = SingleUriResolution.resolveSingleUri(u);
            System.out.println(u.toString() + ":" + t);
            times.add(t);
        }
        return times;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("loaded.");
    }
}