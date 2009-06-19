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

public class SmackPlay {

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
            new SmackPlay().rdfPlay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenerPlay(String[] args) {

        // this will load a Java Swing GUI and allow you to view XMPP XML packets/stanzas
        XMPPConnection.DEBUG_ENABLED = true;

        System.out.println("Starting client");
        ConnectionConfiguration connConfig = new ConnectionConfiguration(SERVER, 5222);
        XMPPConnection connection = new XMPPConnection(connConfig);

        // making a connection to an XMPP server is different than logging into an XMPP server
        try {
            connection.connect();
            System.out.println("Connected to " + connection.getHost());
        } catch (XMPPException ex) {
            System.out.println("Failed to connect to " + connection.getHost());
            System.exit(1);
        }
        // logging into an XMPP server requires a username and password
        try {
            connection.login(USERNAME_1, PASSWORD_1, RESOURCE);
            System.out.println("Logged in as " + connection.getUser());
            // acknowledge to all "buddies" your presence. This presence uses a custom message and a priority value.
            Presence presence = new Presence(Presence.Type.available,
                    "Waiting to process...", 24, Presence.Mode.available);
            connection.sendPacket(presence);

        } catch (XMPPException ex) {
            System.out.println("Failed to log in as " + USERNAME_1);
            System.out.println(ex);
            System.exit(1);
        }

        // print a collection of statistics about the connection
        System.out.println();
        System.out.println("Anonymous: " + connection.isAnonymous());
        System.out.println("Authenticated: " + connection.isAuthenticated());
        System.out.println("Connected: " + connection.isConnected());
        System.out.println("Secure: " + connection.isSecureConnection());
        System.out.println("Compression: " + connection.isUsingCompression());
        System.out.println("Transport Layer Security: " + connection.isUsingTLS());
        System.out.println();

        // Smack using a listener framework to receive packets/stanzas and perform operations on packets.
        // A listener can have a filter to only handle certain types of packets (tags/attributes)
        //PacketFilter filter = new PacketTypeFilter(IQ.class);
        connection.addPacketListener(new GenericPacketListener(), null);

    }

    private void rdfPlay() throws XMPPException, RDFHandlerException, RepositoryException {
        // Create a c to the jabber.org server on a specific port.
        ConnectionConfiguration config = new ConnectionConfiguration(SmackPlay.SERVER, 5222);

        XMPPConnection c = new XMPPConnection(config);
        c.connect();

        c.login(SmackPlay.USERNAME_1, SmackPlay.PASSWORD_1);

        Presence p = new Presence(Presence.Type.available);
        c.sendPacket(p);

        Repository repo = createSampleRepository();

        MessageListener l = new MyListener();
        Chat ch = c.getChatManager().createChat(SmackPlay.USERNAME_2, l);
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

