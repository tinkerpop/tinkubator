package org.linkedprocess.linkeddata.server;

import net.fortytwo.rdfwiki.RDFWiki;
import net.fortytwo.rdfwiki.SailSelector;
import net.fortytwo.ripple.Ripple;
import org.linkedprocess.LinkedProcess;
import org.linkedprocess.linkeddata.server.rewriter.RewriterSail;
import org.linkedprocess.linkeddata.server.rewriter.RewritingSchema;
import org.linkedprocess.linkeddata.server.rewriter.URIRewriter;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;
import org.restlet.data.Request;

import java.io.File;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 4, 2009
 * Time: 3:27:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class DemoServer {
    public static final File DEMO_STORE_DIRECTORY
            = new File("/opt/linkedprocess/linkeddatademo/btc-2009-small-nativestore");
    //= new File("/opt/nativestore/linkedData");

    private static final String PREFIX = "http://lanl.linkedprocess.org:8182/ns/";
    //private static final String PREFIX = "http://localhost:8182/ns/";

    private static final Logger LOGGER = LinkedProcess.getLogger(DemoServer.class);

    public static void main(final String[] args) throws Exception {
        Ripple.initialize();

        Sail baseSail = new NativeStore(DEMO_STORE_DIRECTORY);
        baseSail.initialize();
        final Sail sail = createRewriterSail(baseSail);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    sail.shutDown();
                } catch (SailException e) {
                    LOGGER.severe("failed to shut down Sail");
                }
            }
        });

        SailSelector selector = new SailSelector() {
            public Sail selectSail(final Request request) throws Exception {
                return sail;
            }
        };

        RDFWiki w = new RDFWiki(selector);

        Object monitor = "";
        synchronized (monitor) {
            monitor.wait();
        }
    }

    private static Sail createRewriterSail(final Sail baseSail) {
        RewritingSchema s = new RewritingSchema();
        final ValueFactory valueFactory = baseSail.getValueFactory();

        URIRewriter fromStoreRewriter = new URIRewriter() {
            public URI rewrite(final URI original) {
                String s = original.toString();
                if (s.startsWith("http://")) {
                    s = s.replaceFirst("http://", PREFIX);
                    return valueFactory.createURI(s);
                } else {
                    return original;
                }
            }
        };

        URIRewriter toStoreRewriter = new URIRewriter() {
            public URI rewrite(final URI original) {
                System.out.println("rewriting: " + original);

                String s = original.toString();
                if (s.startsWith(PREFIX)) {
                    s = s.replaceFirst(PREFIX, "http://");
                    return valueFactory.createURI(s);
                } else {
                    return original;
                }
            }
        };

        s.setRewriter(RewritingSchema.PartOfSpeech.SUBJECT, RewritingSchema.Action.FROM_STORE, fromStoreRewriter);
        s.setRewriter(RewritingSchema.PartOfSpeech.OBJECT, RewritingSchema.Action.FROM_STORE, fromStoreRewriter);
        s.setRewriter(RewritingSchema.PartOfSpeech.SUBJECT, RewritingSchema.Action.TO_STORE, toStoreRewriter);
        s.setRewriter(RewritingSchema.PartOfSpeech.OBJECT, RewritingSchema.Action.TO_STORE, toStoreRewriter);

        return new RewriterSail(baseSail, s);
    }

    /*
    private void doit() throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8182.
        component.getServers().add(Protocol.HTTP, 8182);

        // Attach the sample application.
        component.getDefaultHost().attach(new DemoApplication());

        // Start the component.
        component.start();
    }

    private class DemoApplication extends Application {
        @Override
        public Restlet createRoot() {
            // Create a router Restlet that routes each call to a
            // new instance of HelloWorldResource.
            Router router = new Router(getContext());

            // Defines only one route
            router.attachDefault(HelloWorldResource.class);

            return router;
        }
    }

    private class HelloWorldResource extends Resource {

        public HelloWorldResource(Context context, Request request,
                                  Response response) {
            super(context, request, response);

            // This representation has only one type of representation.
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation represent(Variant variant) throws ResourceException {
            return new StringRepresentation(
                    "hello, world", MediaType.TEXT_PLAIN);
        }
    }
    */
}
