package org.linkedprocess.linkeddata.tools;

import org.linkedprocess.LinkedProcess;
import org.linkedprocess.linkeddata.nquads.NQuadsParser;
import org.linkedprocess.linkeddata.server.DemoServer;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.model.Statement;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Aug 3, 2009
 * Time: 3:17:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateDemoTripleStore {
    private static final Logger LOGGER = LinkedProcess.getLogger(CreateDemoTripleStore.class);

    public static void main(final String[] args) {
        try {
            new CreateDemoTripleStore().addData();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void addData() throws Exception {
        File btcSmallDataSet = new File("/opt/linkedprocess/linkeddatademo/btc-2009-small.nq");
        String baseURI = "http://example.org/bogusBaseURI#";

        Sail sail = new NativeStore(DemoServer.DEMO_STORE_DIRECTORY);
        sail.initialize();

        try {
            SailConnection c = sail.getConnection();
            try {
                RDFParser parser = new NQuadsParser();

                // Log but tolerate parse errors, of which there are many (all due to bad xsd:dateTime values?)
                parser.setStopAtFirstError(false);
                parser.setParseErrorListener(new OurParseErrorListener());

                parser.setRDFHandler(new SailAdder(c));
                InputStream is = new FileInputStream(btcSmallDataSet);
                parser.parse(is, baseURI);
                c.commit();
            } finally {
                c.close();
            }
        } finally {
            sail.shutDown();
        }
    }

    private class SailAdder implements RDFHandler {
        private final SailConnection connection;

        public SailAdder(final SailConnection connection) {
            this.connection = connection;
        }

        public void startRDF() throws RDFHandlerException {
            // Do nothing.
        }

        public void endRDF() throws RDFHandlerException {
            // Do nothing.
        }

        public void handleNamespace(final String prefix,
                                    final String name) throws RDFHandlerException {
            try {
                connection.setNamespace(prefix, name);
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleStatement(final Statement statement) throws RDFHandlerException {
            try {
                connection.addStatement(statement.getSubject(),
                        statement.getPredicate(),
                        statement.getObject(),
                        statement.getContext());
            } catch (SailException e) {
                throw new RDFHandlerException(e);
            }
        }

        public void handleComment(String s) throws RDFHandlerException {
            // Do nothing.
        }
    }

    private class OurParseErrorListener implements ParseErrorListener {

        public void warning(String s, int i, int i1) {
            System.err.println("parse warning (line " + i + ", column " + i1 + "): " + s);
        }

        public void error(String s, int i, int i1) {
            System.err.println("parse error (line " + i + ", column " + i1 + "): " + s);
        }

        public void fatalError(String s, int i, int i1) {
            System.err.println("fatal parse error (line " + i + ", column " + i1 + "): " + s);
        }
    }
}