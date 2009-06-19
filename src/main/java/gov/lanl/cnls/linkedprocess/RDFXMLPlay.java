package gov.lanl.cnls.linkedprocess;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.model.vocabulary.RDF;

public class RDFXMLPlay {

    // this is a development and testing account. please don't use for any other purposes.
    public static final String
            USERNAME_1 = "linked.process.1@gmail.com",
            PASSWORD_1 = "linked12",
            USERNAME_2 = "linked.process.2@gmail.com",
            PASSWORD_2 = "linked23",
            USERNAME_JOSH = "parcour@gmail.com",
            RESOURCE = "lop/",
            SERVER = "talk1.l.google.com";

    public static void main(final String[] args) {
        try {
            new RDFXMLPlay().rdfPlay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rdfPlay() throws XMPPException, RDFHandlerException, RepositoryException {
        // Create a c to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(RDFXMLPlay.SERVER, 5222);

        XMPPConnection c = new XMPPConnection(config);
        c.connect();

        c.login(RDFXMLPlay.USERNAME_1, RDFXMLPlay.PASSWORD_1);

        Presence p = new Presence(Presence.Type.available);
        c.sendPacket(p);

        Repository repo = createSampleRepository();

        MessageListener l = new MyListener();
        Chat ch = c.getChatManager().createChat(RDFXMLPlay.USERNAME_2, l);
        Message m = new Message();
        m.setBody("Perfunctory message body.");
        m.addExtension(new RDFXMLPacketExtension(repo));
        m.setLanguage("en");
        m.setSubject("let's send some more *real* RDF/XML *with statements*, and chop off the XML declaration");
        m.setType(Message.Type.normal);
        System.out.println("message: " + m.toXML());
        ch.sendMessage(m);
    }

    private Repository createSampleRepository() throws RepositoryException {
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();

        RepositoryConnection rc = repo.getConnection();
        try {
            rc.add(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
        } finally {
            rc.close();
        }

        return repo;
    }

    private class MyListener implements MessageListener {

        public void processMessage(final Chat chat,
                                   final Message message) {
            System.out.println("received message from chat " + chat + ": " + message);
        }
    }
}