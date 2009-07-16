package gov.lanl.cnls.linkedprocess.xmpp.tools;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import gov.lanl.cnls.linkedprocess.xmpp.XmppClient;

public class SmackPlay {
    
    // this is a development and testing account. please don't use for any other purposes.
    private static String username = "linked.process.2@xmpp.linkedprocess.org";
    private static String password = "linked23";
    private static String resource = XmppClient.generateRandomID();
        
    public static void main(String[] args) throws Exception {

        // this will load a Java Swing GUI and allow you to view and send XMPP XML packets/stanzas
        XMPPConnection.DEBUG_ENABLED = true;
        ConnectionConfiguration connConfig = new ConnectionConfiguration("xmpp.linkedprocess.org", 5222);
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
            connection.login(username, password, resource);
            System.out.println("Logged in as " + connection.getUser());
            // acknowledge to all "buddies" your presence. This presence uses a custom message and a priority value.
            Presence presence = new Presence(Presence.Type.available,
                    "Xmpp Test Client", 24, Presence.Mode.available);
            connection.sendPacket(presence);
            
        } catch (XMPPException ex) {
            System.out.println("Failed to log in as " + username);
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
           
    }
}

