package org.linkedprocess.xmpp;

import org.linkedprocess.Connection;
import org.linkedprocess.LinkedProcess;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;


public abstract class XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppClient.class);
    protected Connection connection;
    protected Roster roster;
    protected boolean shutdownRequested = false;
    protected ServiceDiscoveryManager discoManager;

    protected long startTime;
    private String username;
    private String password;
    private String server;
    private int port;

    protected void logon(final String server, final int port, final String username, final String password, String resource) throws XMPPException {

        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.startTime = System.currentTimeMillis();

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

        this.roster = this.connection.getRoster();
    }

    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        this.connection.addPacketListener(listener, filter);
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
        return LinkedProcess.generateBareJid(fullJid);
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

    public long getRunningTime() {
        return System.currentTimeMillis() - this.startTime;
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

    public ServiceDiscoveryManager getDiscoManager() {
        return this.discoManager;
    }

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

    public void requestSubscription(String jid) {
        Presence subscribe = new Presence(Presence.Type.subscribe);
        subscribe.setTo(jid);
        this.connection.sendPacket(subscribe);
    }

    public void requestUnsubscription(String jid, boolean removeFromRoster) {
        Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
        unsubscribe.setTo(jid);
        this.connection.sendPacket(unsubscribe);
        if(removeFromRoster) {
            try {
                this.roster.removeEntry(this.roster.getEntry(jid));
            } catch(XMPPException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
        unsubscribed.setTo(jid);
        this.connection.sendPacket(unsubscribed);
    }

    public void probeJid(String jid) {
        ProbePresence probe = new ProbePresence();
        probe.setTo(jid);
        this.connection.sendPacket(probe);
    }
}
