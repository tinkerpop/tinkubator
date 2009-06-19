package gov.lanl.cnls.linkedprocess;

import org.jivesoftware.smack.packet.PacketExtension;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFFormat;

import java.io.ByteArrayOutputStream;

/**
 * A PacketExtension to embed arbitrary RDF/XML data in an XMPP stanza.
 * 
 * Author: josh
 * Date: Jun 19, 2009
 * Time: 2:33:42 PM
 */
class RDFXMLPacketExtension implements PacketExtension {
    private final String serialized;

    /**
     * Creates an extension by serializing all statements in the given Repository.
     * 
     * @param repo the repository to serialize
     * @throws RDFHandlerException
     * @throws RepositoryException
     */
    public RDFXMLPacketExtension(final Repository repo) throws RDFHandlerException, RepositoryException {
        serialized = serializedRDFXMLFromRepository(repo);
    }

    public String getElementName() {
        return "RDF";
    }

    public String getNamespace() {
        return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    }

    public String toXML() {
        return serialized;
    }

    // Note: assumes a particular style of serialization by Rio which is not strictly defined.
    //       Specifically, it assumes that the first line contains an XML declaration (which is discarded),
    //       and that there are no comments or entity declarations (which are not allowed in an XMPP stream).
    private String serializedRDFXMLFromRepository(final Repository repo) throws RDFHandlerException, RepositoryException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        RDFWriter w = Rio.createWriter(RDFFormat.RDFXML, bos);

        RepositoryConnection rc = repo.getConnection();
        try {
            rc.export(w);
        } finally {
            rc.close();
        }

        // Chop off the XML declaration.
        String s = new String(bos.toByteArray());
        int i = s.indexOf('\n');
        return s.substring(i + 1);
    }
}
