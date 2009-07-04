package gov.lanl.cnls.linkedprocess.xmpp;

import gov.lanl.cnls.linkedprocess.Connection;
import gov.lanl.cnls.linkedprocess.LinkedProcess;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;


public abstract class XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppClient.class);
    protected Connection connection;
    protected Roster roster;
    protected boolean shutdownRequested = false;
    protected ServiceDiscoveryManager discoManager;

    private String username;
    private String password;
    private String server;
    private int port;

    protected void initiateFeatures() {
        XMPPConnection delegate = connection.getDelegate();
        LOGGER.fine(delegate.toString());
		discoManager = ServiceDiscoveryManager.getInstanceFor(delegate);
        Iterator<String> features = discoManager.getFeatures();
        while (features.hasNext()) {
            String feature = features.next();
            discoManager.removeFeature(feature);
        }
        discoManager.addFeature(LinkedProcess.DISCO_INFO_NAMESPACE);
    }

    protected void logon(String server, int port, String username, String password, String resource) throws XMPPException {

        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;

        // if connection is still active, disconnect it.
        if (null != connection && connection.isConnected()) {
            this.logout();
        }

        // logging into an XMPP server requires a username and password
        ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port);
        this.connection = new XMPPConnectionWrapper(connConfig);
        this.connection.connect();

        LOGGER.info("Connected to " + connection.getHost());
        connection.login(username, password, resource + LinkedProcess.FORWARD_SLASH + XmppClient.generateRandomID());
        LOGGER.info("Logged in as " + connection.getUser());
  
        Thread shutdownHook = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!shutdownRequested) {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LOGGER.info("Shutting down");
                //we should log out here but then the calling method would not block
                //logout();
            }
        }, "Shutdown hook");
        shutdownHook.start();

        //this.roster = this.connection.getRoster();
    }

    public void logout() {
        LOGGER.info("Disconnecting from " + connection.getHost());
        connection.disconnect();
    }

    public void printClientStatistics() {
        // print a collection of statistics about the connection
        LOGGER.info("Anonymous: " + connection.isAnonymous());
        LOGGER.info("Authenticated: " + connection.isAuthenticated());
        LOGGER.info("Connected: " + connection.isConnected());
        LOGGER.info("Secure: " + connection.isSecureConnection());
        LOGGER.info("Compression: " + connection.isUsingCompression());
        LOGGER.info("Transport Layer Security: " + connection.isUsingTLS());
    }

    public String getFullJid() {
        return this.connection.getUser();
    }

    public String getBareJid() {
        String fullJid = this.getFullJid();
        return XmppClient.generateBareJid(fullJid);
    }

    public static String generateBareJid(String fullJid) {
         return fullJid.substring(0,fullJid.indexOf("/"));
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void shutDown() {
        LOGGER.info("Requesting shutdown");
        shutdownRequested = true;
        //this is in order to wait until we are logged out
        logout();
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public int getPort() {
        return this.port;
    }

    public String getServer() {
        return this.server;
    }

    public Roster getRoster() {
        return this.roster;
    }

    public static String generateRandomID() {
        // e.g. from gtalk 6D56433B
        Random random = new Random();
        StringBuilder b = new StringBuilder();
        for(int i=0; i<8; i++) {
            int x = random.nextInt(36);
            if(x < 10)
                b.append(x);
            else
                b.append(((char)(x+55)));

        }
        return b.toString();
    }

    public static String generateRandomPassword() {
        return XmppClient.generateRandomID();
    }
}
