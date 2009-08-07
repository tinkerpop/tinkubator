package org.linkedprocess.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.linkedprocess.Connection;
import org.linkedprocess.LinkedProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;


public abstract class XmppClient {

    public static Logger LOGGER = LinkedProcess.getLogger(XmppClient.class);
    protected Connection connection;
    protected Roster roster;
    protected boolean shutdownRequested = false;

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
            this.logout(null);
        }

        ConnectionConfiguration connConfig = new ConnectionConfiguration(server, port);
        connConfig.setRosterLoadedAtLogin(false);
        connConfig.setSendPresence(false);
        //connConfig.setSASLAuthenticationEnabled(false);

        this.connection = new XMPPConnectionWrapper(connConfig);
        this.connection.connect();

        LOGGER.info("Connected to " + connection.getHost());
        connection.login(username, password, resource + LinkedProcess.FORWARD_SLASH + XmppClient.generateRandomResourceId());
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

    protected void logout(Presence logoutPresence) {
        LOGGER.info("Disconnecting from " + connection.getHost());
        if (logoutPresence == null)
            connection.disconnect();
        else
            connection.disconnect(logoutPresence);
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
        return LinkedProcess.generateBareJid(this.getFullJid());
    }

    public String getResource() {
        return getFullJid().replace(this.getBareJid() + "/", "");
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void shutDown(Presence logoutPresence) {
        LOGGER.info("Requesting shutdown");
        shutdownRequested = true;
        //this is in order to wait until we are logged out
        logout(logoutPresence);
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

    public float getRunningTimeInSecods() {
        return this.getRunningTime() / 1000.0f;
    }

    public float getRunningTimeInMinutes() {
        return this.getRunningTime() / 6000.0f;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public java.util.Date getStartTimeAsDate() {
        return new java.util.Date(this.startTime);
    }

    public static String generateRandomResourceId() {
        // e.g. from gtalk 6D56433B
        Random random = new Random();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int x = random.nextInt(36);
            if (x < 10)
                b.append(x);
            else
                b.append(((char) (x + 55)));

        }
        return b.toString();
    }

    public static String generateRandomPassword() {
        return XmppClient.generateRandomResourceId();
    }

    public static String generateRandomJobId() {
        return Packet.nextID();
    }

    public ServiceDiscoveryManager getDiscoManager() {
        return ServiceDiscoveryManager.getInstanceFor(this.connection.getDelegate());
    }

    protected void initiateFeatures() {
        Iterator<String> features = this.getDiscoManager().getFeatures();
        while (features.hasNext()) {
            String feature = features.next();
            this.getDiscoManager().removeFeature(feature);
        }
        this.getDiscoManager().addFeature(LinkedProcess.DISCO_INFO_NAMESPACE);
    }

    public void requestSubscription(String jid) {
        if (jid.equals(this.getFullJid()) || jid.equals(this.getBareJid()))
            return;

        Presence subscribe = new Presence(Presence.Type.subscribe);
        subscribe.setTo(jid);
        subscribe.setFrom(this.getFullJid());
        this.connection.sendPacket(subscribe);
    }

    public void requestUnsubscription(String jid, boolean removeFromRoster) {
        if (jid.equals(this.getFullJid()) || jid.equals(this.getBareJid()))
            return;

        Presence unsubscribe = new Presence(Presence.Type.unsubscribe);
        unsubscribe.setTo(jid);
        unsubscribe.setFrom(this.getFullJid());
        this.connection.sendPacket(unsubscribe);
        if (removeFromRoster) {
            try {
                this.roster.removeEntry(this.roster.getEntry(jid));
            } catch (XMPPException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
        unsubscribed.setTo(jid);
        unsubscribed.setFrom(this.getFullJid());
        this.connection.sendPacket(unsubscribed);
    }

    public void probeJid(String jid) {
        ProbePresence probe = new ProbePresence();
        probe.setFrom(this.getFullJid());
        probe.setTo(jid);
        this.connection.sendPacket(probe);
    }

    public static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }
}
